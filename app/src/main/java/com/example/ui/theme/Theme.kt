package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WhatsAppDarkPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF005C4B),
    onPrimaryContainer = Color(0xFFD8FDD2),
    secondary = WhatsAppTeal,
    onSecondary = Color.White,
    background = WhatsAppDarkBg,
    onBackground = DarkTextPrimary,
    surface = WhatsAppDarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = Color(0xFF222E35),
    onSurfaceVariant = DarkTextSecondary
)

private val HighContrastColorScheme = lightColorScheme(
    primary = WhatsAppGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8FDD2),
    onPrimaryContainer = Color(0xFF00382B),
    secondary = WhatsAppTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCEEAD6),
    onSecondaryContainer = Color(0xFF0B3829),
    tertiary = WhatsAppLightGreen,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = Color(0xFF000000),      // Pure solid black
    surface = SurfaceLight,
    onSurface = Color(0xFF000000),         // Pure solid black for all card text
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = Color(0xFF111827)    // Dark charcoal for secondary text
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Always enforce crisp, high-contrast, perfectly visible text
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HighContrastColorScheme,
        typography = Typography,
        content = content
    )
}
