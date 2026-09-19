package com.example

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.MainApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MercuryViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: MercuryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val appLanguage by viewModel.appLanguage.collectAsState()
            val systemInDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> systemInDark
            }

            val isArabic = remember(appLanguage) {
                if (appLanguage.equals("SYSTEM", ignoreCase = true)) {
                    val defaultLang = Locale.getDefault().language
                    defaultLang.startsWith("ar", ignoreCase = true)
                } else {
                    appLanguage.startsWith("ar", ignoreCase = true)
                }
            }
            val targetLocale = remember(isArabic) {
                if (isArabic) Locale("ar") else Locale("en")
            }

            Locale.setDefault(targetLocale)

            val baseContext = LocalContext.current
            val localizedContext = remember(baseContext, targetLocale) {
                val config = Configuration(baseContext.resources.configuration)
                config.setLocale(targetLocale)
                config.setLayoutDirection(targetLocale)
                @Suppress("DEPRECATION")
                baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
                baseContext.createConfigurationContext(config)
            }

            val currentConfig = LocalConfiguration.current
            val localizedConfig = remember(currentConfig, targetLocale) {
                Configuration(currentConfig).apply {
                    setLocale(targetLocale)
                    setLayoutDirection(targetLocale)
                }
            }

            val layoutDirection = if (isArabic) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfig,
                LocalContext provides localizedContext,
                LocalLayoutDirection provides layoutDirection
            ) {
                MyApplicationTheme(darkTheme = isDarkTheme) {
                    MainApp(viewModel = viewModel)
                }
            }
        }
    }
}
