package com.example.util

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.log10
import kotlin.math.sqrt

class MandarinSpeechManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var speechRecognizer: SpeechRecognizer? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.SIMPLIFIED_CHINESE)
                    isTtsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                }
            }
        } catch (e: Exception) {
            Log.w("MandarinTTS", "TTS init failed: ${e.message}")
        }
    }

    fun speak(text: String, slow: Boolean = false) {
        if (text.isBlank()) return
        triggerHaptic()
        try {
            if (isTtsReady) {
                tts?.setSpeechRate(if (slow) 0.62f else 0.98f)
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "hanzi_utt_${System.currentTimeMillis()}")
            }
        } catch (e: Exception) {
            Log.w("MandarinTTS", "Speak failed: ${e.message}")
        }
    }

    fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(18L, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(VibrationEffect.createOneShot(18L, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Uses Android SpeechRecognizer (when available on device) to transcribe spoken Mandarin (zh-CN).
     */
    fun listenForMandarinSpeech(
        onResult: (transcript: String) -> Unit,
        onError: (reason: String) -> Unit
    ) {
        try {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                onError("On-device SpeechRecognizer service is unavailable; use Record Voice (Acoustic Mic) or type your Mandarin transcript below.")
                return
            }
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    onError("Speech recognition finished (code $error). You can also record acoustic pitch directly.")
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val best = matches?.firstOrNull().orEmpty()
                    if (best.isNotBlank()) {
                        onResult(best)
                    } else {
                        onError("No clear Mandarin speech detected. Please speak closer to the microphone.")
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!partial.isNullOrBlank()) {
                        onResult(partial)
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            recognizer.startListening(intent)
        } catch (e: Exception) {
            onError("Speech recognizer error: ${e.message ?: "unavailable"}")
        }
    }

    /**
     * Captures real microphone PCM audio samples when RECORD_AUDIO permission is granted,
     * extracting normalized waveform envelope and zero-crossing / autocorrelation pitch contour
     * estimates for visual comparison in the Speaking Center.
     */
    suspend fun recordAcousticProfile(
        onLiveFrame: (waveform: List<Float>, pitchContour: List<Float>) -> Unit
    ): Pair<List<Float>, List<Float>> = withContext(Dispatchers.IO) {
        val sampleRate = 16000
        val bufferSize = maxOf(
            AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ),
            2048
        )
        val waveformPoints = mutableListOf<Float>()
        val pitchPoints = mutableListOf<Float>()

        var audioRecord: AudioRecord? = null
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord.startRecording()
                val shortBuffer = ShortArray(1024)
                val startMs = System.currentTimeMillis()
                while (isActive && System.currentTimeMillis() - startMs < 3000L) {
                    val readCount = audioRecord.read(shortBuffer, 0, shortBuffer.size)
                    if (readCount > 0) {
                        var sumSquares = 0.0
                        var zeroCrossings = 0
                        for (i in 0 until readCount) {
                            val s = shortBuffer[i].toDouble()
                            sumSquares += s * s
                            if (i > 0 && ((shortBuffer[i - 1] >= 0 && shortBuffer[i] < 0) ||
                                    (shortBuffer[i - 1] < 0 && shortBuffer[i] >= 0))
                            ) {
                                zeroCrossings++
                            }
                        }
                        val rms = sqrt(sumSquares / readCount)
                        val normAmp = (log10(1.0 + rms) / log10(32768.0)).toFloat().coerceIn(0.08f, 0.96f)
                        val zcrHz = (zeroCrossings * sampleRate) / (2.0 * readCount)
                        val normPitch = ((zcrHz - 85.0) / 340.0).toFloat().coerceIn(0.18f, 0.92f)

                        waveformPoints.add(normAmp)
                        pitchPoints.add(normPitch)
                        onLiveFrame(waveformPoints.takeLast(24), pitchPoints.takeLast(24))
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.w("MandarinSpeech", "RECORD_AUDIO permission missing: ${e.message}")
        } catch (e: Exception) {
            Log.w("MandarinSpeech", "Audio capture finished: ${e.message}")
        } finally {
            try {
                audioRecord?.stop()
                audioRecord?.release()
            } catch (_: Exception) {
            }
        }
        Pair(waveformPoints, pitchPoints)
    }

    fun shutdown() {
        try {
            speechRecognizer?.destroy()
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
    }
}
