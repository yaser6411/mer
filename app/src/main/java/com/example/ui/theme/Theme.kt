package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MercuryPrimaryDark,
    onPrimary = MercuryOnPrimaryDark,
    primaryContainer = MercuryPrimaryContainerDark,
    onPrimaryContainer = MercuryOnPrimaryContainerDark,
    secondary = MercurySecondaryDark,
    onSecondary = MercuryOnSecondaryDark,
    secondaryContainer = MercurySecondaryContainerDark,
    onSecondaryContainer = MercuryOnSecondaryContainerDark,
    tertiary = MercuryTertiaryDark,
    onTertiary = MercuryOnTertiaryDark,
    tertiaryContainer = MercuryTertiaryContainerDark,
    onTertiaryContainer = MercuryOnTertiaryContainerDark,
    background = MercuryBackgroundDark,
    onBackground = MercuryOnBackgroundDark,
    surface = MercurySurfaceDark,
    onSurface = MercuryOnSurfaceDark,
    surfaceVariant = MercurySurfaceVariantDark,
    onSurfaceVariant = MercuryOnSurfaceVariantDark,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

private val LightColorScheme = lightColorScheme(
    primary = MercuryPrimaryLight,
    onPrimary = MercuryOnPrimaryLight,
    primaryContainer = MercuryPrimaryContainerLight,
    onPrimaryContainer = MercuryOnPrimaryContainerLight,
    secondary = MercurySecondaryLight,
    onSecondary = MercuryOnSecondaryLight,
    secondaryContainer = MercurySecondaryContainerLight,
    onSecondaryContainer = MercuryOnSecondaryContainerLight,
    tertiary = MercuryTertiaryLight,
    onTertiary = MercuryOnTertiaryLight,
    tertiaryContainer = MercuryTertiaryContainerLight,
    onTertiaryContainer = MercuryOnTertiaryContainerLight,
    background = MercuryBackgroundLight,
    onBackground = MercuryOnBackgroundLight,
    surface = MercurySurfaceLight,
    onSurface = MercuryOnSurfaceLight,
    surfaceVariant = MercurySurfaceVariantLight,
    onSurfaceVariant = MercuryOnSurfaceVariantLight,
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to prioritize MERCURY brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
