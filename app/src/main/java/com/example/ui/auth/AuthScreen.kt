package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.theme.AcidLime
import com.example.ui.theme.AcidLimeSurface
import com.example.ui.theme.AlabasterWhite
import com.example.ui.theme.CyberMint
import com.example.ui.theme.MonoBadge
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorderStructural
import com.example.ui.theme.MonoButton
import com.example.ui.theme.MonoCard
import com.example.ui.theme.MonoObsidian
import com.example.ui.theme.MonoSlateCard
import com.example.ui.theme.MutedZinc
import com.example.ui.theme.NeoBrutalIllustratedEmblem
import com.example.ui.theme.SignalCrimson
import com.example.ui.theme.SignalCrimsonSurface
import com.example.ui.theme.VoltageAmber
import com.example.ui.theme.neoBrutalSurface
import com.example.util.MonoShimmerSkeleton
import com.example.util.rememberMonoHaptics
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val activityContext = context as? Activity ?: context
            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun signOut(
    context: Context,
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = rememberMonoHaptics()
    val credentialManager = remember { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                haptics.success()
                onAuthSuccess()
            },
            onUnauthenticated = {},
            scope = scope
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MonoBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MonoBadge(
                        text = "MONO // FINANCIAL OS v4.2",
                        backgroundColor = AcidLimeSurface,
                        borderColor = AcidLime,
                        textColor = AcidLime
                    )
                    MonoBadge(
                        text = "ZERO-TRUST CLOUD",
                        backgroundColor = MonoSlateCard,
                        borderColor = MonoBorderStructural,
                        textColor = AlabasterWhite
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PRECISION WEALTH.\nACID CLARITY.",
                    style = MaterialTheme.typography.displayMedium,
                    color = AlabasterWhite
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "High-contrast personal ledger engineered with spring-physics motion, tactile haptics, and real-time cloud synchronization.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedZinc
                )

                Spacer(modifier = Modifier.height(16.dp))

                MonoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MonoObsidian,
                    accentHeaderColor = AcidLime,
                    headerTitle = "TERMINAL 01 // TELEMETRY & VAULT ARCHITECTURE",
                    headerRightText = "60-120HZ",
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_mono_hero_banner_1793868051061),
                        contentDescription = "MONO Finance Dark Neo-Brutalist Hero Telemetry",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(185.dp),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MonoBorderStructural)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MonoSlateCard)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LEDGER · BUDGETS · SPLIT · VAULTS · ANALYTICS",
                            style = MaterialTheme.typography.labelSmall,
                            color = AcidLime,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ENCRYPTED",
                            style = MaterialTheme.typography.labelSmall,
                            color = AlabasterWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                MonoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MonoSlateCard,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MonoAuthFeatureRow(
                            glyph = "$",
                            badgeColor = AcidLime,
                            title = "TABULAR FINANCIAL COUNTERS & SPLIT LEDGER",
                            subtitle = "Smooth interpolated balance transitions, itemized bill splitting, and multi-currency formatting."
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MonoBorderStructural)
                        )
                        MonoAuthFeatureRow(
                            glyph = "%",
                            badgeColor = CyberMint,
                            title = "INTERACTIVE TOUCH TELEMETRY & STRIPED BUDGETS",
                            subtitle = "Scrubbable Canvas cash-flow charts with haptic detents and diagonal-stripe burn indicators."
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MonoBorderStructural)
                        )
                        MonoAuthFeatureRow(
                            glyph = "⚡",
                            badgeColor = VoltageAmber,
                            title = "RECURRING SUBSCRIPTIONS & SAVINGS VAULTS",
                            subtitle = "Track monthly bills, milestone savings goals, and portable JSON backup & restore."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .neoBrutalSurface(
                                backgroundColor = SignalCrimsonSurface,
                                borderColor = SignalCrimson,
                                shadowColor = MonoBlack
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "AUTH ALERT // ${errorMessage ?: ""}",
                            color = SignalCrimson,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                if (isLoading) {
                    MonoShimmerSkeleton(
                        height = 54.dp,
                        cornerRadius = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    MonoButton(
                        text = "SIGN IN WITH GOOGLE →",
                        icon = Icons.Default.Lock,
                        backgroundColor = AcidLime,
                        contentColor = MonoBlack,
                        borderColor = AcidLime,
                        height = 54.dp,
                        useMediumHaptic = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_sign_in_button"),
                        onClick = {
                            errorMessage = null
                            isLoading = true
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isLoading = false
                                    haptics.success()
                                    onAuthSuccess()
                                },
                                onAuthError = { msg ->
                                    isLoading = false
                                    haptics.error()
                                    errorMessage = msg
                                },
                                scope = scope,
                                onAuthCancelled = {
                                    isLoading = false
                                }
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MonoAuthFeatureRow(
    glyph: String,
    badgeColor: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        NeoBrutalIllustratedEmblem(
            glyph = glyph,
            backgroundColor = badgeColor,
            size = 42.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = AlabasterWhite,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MutedZinc
            )
        }
    }
}
