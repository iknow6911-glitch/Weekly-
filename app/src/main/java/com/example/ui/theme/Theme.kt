package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = EmeraldContainer,
    onPrimary = EmeraldOnContainer,
    primaryContainer = EmeraldPrimary,
    secondary = MintContainer,
    onSecondary = MintOnContainer,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    error = CoralContainer,
    onError = CoralOnContainer
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldOnContainer,
    secondary = MintSecondary,
    onSecondary = EmeraldOnPrimary,
    secondaryContainer = MintContainer,
    onSecondaryContainer = MintOnContainer,
    background = SlateBackground,
    surface = SlateSurface,
    onBackground = SlateOnSurface,
    onSurface = SlateOnSurface,
    error = CoralDeduction,
    onError = EmeraldOnPrimary,
    errorContainer = CoralContainer,
    onErrorContainer = CoralOnContainer
  )

@Composable
fun WeeklyBudgetTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set false to ensure cohesive emerald financial theme
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
