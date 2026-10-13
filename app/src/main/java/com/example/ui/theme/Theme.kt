package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GameSecColorScheme = darkColorScheme(
  primary = AccentBlue,
  onPrimary = Color(0xFFFFFFFF),
  primaryContainer = AccentBlueDark,
  onPrimaryContainer = Color(0xFFD6E3F8),

  secondary = AccentBlueLight,
  onSecondary = Color(0xFF0F1115),
  secondaryContainer = DarkSurfaceVariant,
  onSecondaryContainer = TextPrimary,

  background = DarkBackground,
  onBackground = TextPrimary,

  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = TextSecondary,

  outline = DarkCardBorder,
  outlineVariant = Color(0xFF1E232B),

  error = StatusDanger,
  onError = Color(0xFFFFFFFF)
)

@Composable
fun GameSecLabTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = GameSecColorScheme,
    typography = Typography,
    content = content
  )
}
