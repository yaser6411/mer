package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.LocalePreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocalePreferencesTest {

    private lateinit var preferencesManager: LocalePreferencesManager

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        preferencesManager = LocalePreferencesManager(context)
        runBlocking {
            preferencesManager.setAppLanguage(LocalePreferencesManager.LANGUAGE_SYSTEM)
        }
    }

    @Test
    fun testDefaultLanguageIsSystem() = runBlocking {
        val initialLanguage = preferencesManager.appLanguage.first()
        // Default when not set should be SYSTEM
        assertEquals(LocalePreferencesManager.LANGUAGE_SYSTEM, initialLanguage)
    }

    @Test
    fun testPersistArabicLanguage() = runBlocking {
        preferencesManager.setAppLanguage(LocalePreferencesManager.LANGUAGE_ARABIC)
        val persistedLanguage = preferencesManager.appLanguage.first()
        assertEquals(LocalePreferencesManager.LANGUAGE_ARABIC, persistedLanguage)
    }

    @Test
    fun testPersistEnglishLanguage() = runBlocking {
        preferencesManager.setAppLanguage(LocalePreferencesManager.LANGUAGE_ENGLISH)
        val persistedLanguage = preferencesManager.appLanguage.first()
        assertEquals(LocalePreferencesManager.LANGUAGE_ENGLISH, persistedLanguage)
    }
}
