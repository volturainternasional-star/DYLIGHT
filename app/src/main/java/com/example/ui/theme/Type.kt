package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
  displayMedium = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = (-0.5).sp,
  ),
  titleLarge = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp,
  ),
  titleMedium = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp,
  ),
  bodyLarge = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 23.sp,
    letterSpacing = 0.25.sp,
  ),
  bodyMedium = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 13.5.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp,
  ),
  labelLarge = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.5.sp,
  ),
  labelMedium = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 11.5.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
  ),
)

// Monospace styles for technical terminal, data logs, calculations
val TerminalMonoStyle = TextStyle(
  fontFamily = FontFamily.Monospace,
  fontWeight = FontWeight.Normal,
  fontSize = 13.sp,
  lineHeight = 19.sp,
  letterSpacing = 0.sp,
)

val TerminalHeaderStyle = TextStyle(
  fontFamily = FontFamily.Monospace,
  fontWeight = FontWeight.SemiBold,
  fontSize = 12.sp,
  lineHeight = 16.sp,
  letterSpacing = 0.5.sp,
)
