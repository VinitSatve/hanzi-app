package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.curriculum.HskJsonCurriculumLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HskJsonCurriculumLoaderTest {

    @Test
    fun `hsk 1 through 6 levels each contain units lessons vocabulary and grammar`() {
        val manifest = HskJsonCurriculumLoader.buildDefaultManifest()
        for (hskLevel in 1..6) {
            val levelData = manifest.levels.firstOrNull { it.level == hskLevel }
            requireNotNull(levelData) { "Missing HSK level $hskLevel in manifest" }
            assertTrue("HSK $hskLevel must have units", levelData.units.isNotEmpty())
            assertTrue("HSK $hskLevel must have lessons", levelData.lessons.isNotEmpty())
            assertTrue("HSK $hskLevel must have structured vocabulary", levelData.vocabulary.isNotEmpty())
            assertTrue("HSK $hskLevel must have structured grammar", levelData.grammar.isNotEmpty())
        }
    }

    @Test
    fun `json roundtrip preserves hsk 1 to 6 curriculum structure`() {
        val original = HskJsonCurriculumLoader.buildDefaultManifest()
        val jsonString = HskJsonCurriculumLoader.toJson(original)
        assertTrue(jsonString.contains("\"hskStandardsSupported\""))

        val parsed = HskJsonCurriculumLoader.fromJson(jsonString)
        assertEquals(original.schemaVersion, parsed.schemaVersion)
        assertEquals(original.levels.size, parsed.levels.size)

        for (hskLevel in 1..6) {
            val origLevel = original.levels.first { it.level == hskLevel }
            val parsedLevel = parsed.levels.first { it.level == hskLevel }
            assertEquals(origLevel.title, parsedLevel.title)
            assertEquals(origLevel.units.size, parsedLevel.units.size)
            assertEquals(origLevel.lessons.size, parsedLevel.lessons.size)
            assertEquals(origLevel.vocabulary.size, parsedLevel.vocabulary.size)
            assertEquals(origLevel.grammar.size, parsedLevel.grammar.size)
        }
    }

    @Test
    fun `offline json loader caches and deletes per-level json files on disk`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manifest = HskJsonCurriculumLoader.loadCurriculumOfflineFirst(context)
        assertTrue(manifest.levels.any { it.level == 1 })

        // Cache HSK 1 and HSK 4-6 offline packs as local JSON files
        val writtenHsk1 = HskJsonCurriculumLoader.cachePackToLocalJson(context, "pack_hsk1", manifest)
        val writtenHsk4To6 = HskJsonCurriculumLoader.cachePackToLocalJson(context, "pack_hsk4_6", manifest)
        assertEquals(1, writtenHsk1.size)
        assertEquals(3, writtenHsk4To6.size)
        assertTrue(writtenHsk1.first().exists())

        val statsAfterWrite = HskJsonCurriculumLoader.getOfflineCacheStats(context)
        assertTrue(statsAfterWrite.cachedLevels.containsAll(listOf(1, 4, 5, 6)))
        assertTrue(statsAfterWrite.totalBytesOnDisk > 1000L)

        // Delete HSK 1 pack from local JSON cache
        val deletedCount = HskJsonCurriculumLoader.deletePackLocalJson(context, "pack_hsk1")
        assertEquals(1, deletedCount)
        val statsAfterDelete = HskJsonCurriculumLoader.getOfflineCacheStats(context)
        assertFalse(statsAfterDelete.cachedLevels.contains(1))
    }
}
