package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import kotlin.random.Random

/**
 * Animated Cyber Matrix Digital Rain Effect.
 * Drops falling binary and cyber characters vertically with glowing heads and fading trails.
 */
@Composable
fun CyberMatrixRain(
  modifier: Modifier = Modifier,
  streamCount: Int = 22,
  primaryColor: Color = Color(0xFF00FF66),
  trailColor: Color = Color(0xFF008833),
  alpha: Float = 0.55f
) {
  val infiniteTransition = rememberInfiniteTransition(label = "matrix_rain")
  val progress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "matrix_flow"
  )

  // Seeded random streams so they remain stable across recompositions
  val streams = remember(streamCount) {
    List(streamCount) { index ->
      val chars = "010101XYZ9876543210ABCDEF<>#*!@%{}[]:;$/\\GAMESEC".toCharArray()
      val speed = Random(index * 71).nextFloat() * 0.8f + 0.6f
      val offset = Random(index * 37).nextFloat()
      val length = Random(index * 19).nextInt(8, 20)
      MatrixStream(chars = chars, speed = speed, initialOffset = offset, length = length)
    }
  }

  Canvas(modifier = modifier) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val colWidth = canvasWidth / streamCount.coerceAtLeast(1)
    val charHeight = 22f

    val paintLead = android.graphics.Paint().apply {
      color = android.graphics.Color.WHITE
      textSize = 20f
      isAntiAlias = true
      typeface = android.graphics.Typeface.MONOSPACE
      setShadowLayer(8f, 0f, 0f, android.graphics.Color.GREEN)
    }

    val paintBody = android.graphics.Paint().apply {
      color = android.graphics.Color.argb((alpha * 255).toInt(), 0, 230, 90)
      textSize = 18f
      isAntiAlias = true
      typeface = android.graphics.Typeface.MONOSPACE
    }

    streams.forEachIndexed { i, stream ->
      val x = i * colWidth + colWidth / 4f
      val totalDistance = canvasHeight + (stream.length * charHeight)
      val currentY = ((progress * stream.speed + stream.initialOffset) % 1f) * totalDistance - (stream.length * charHeight)

      for (j in 0 until stream.length) {
        val y = currentY + (j * charHeight)
        if (y in -20f..canvasHeight + 20f) {
          val charIndex = (i + j + (progress * 50).toInt()) % stream.chars.size
          val char = stream.chars[charIndex].toString()

          val charPaint = if (j == stream.length - 1) paintLead else paintBody
          drawContext.canvas.nativeCanvas.drawText(char, x, y, charPaint)
        }
      }
    }
  }
}

private data class MatrixStream(
  val chars: CharArray,
  val speed: Float,
  val initialOffset: Float,
  val length: Int
)

/**
 * Animated Cyber Radar Scanner graphic.
 */
@Composable
fun CyberRadarScanner(
  modifier: Modifier = Modifier,
  scannerColor: Color = AccentGreen
) {
  val infiniteTransition = rememberInfiniteTransition(label = "radar")
  val angle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "radar_angle"
  )

  Canvas(modifier = modifier) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.minDimension / 2f - 4f

    // Concentric circles
    drawCircle(
      color = scannerColor.copy(alpha = 0.2f),
      radius = radius,
      center = center,
      style = Stroke(width = 1.5.dp.toPx())
    )
    drawCircle(
      color = scannerColor.copy(alpha = 0.15f),
      radius = radius * 0.65f,
      center = center,
      style = Stroke(width = 1.dp.toPx())
    )
    drawCircle(
      color = scannerColor.copy(alpha = 0.1f),
      radius = radius * 0.35f,
      center = center,
      style = Stroke(width = 1.dp.toPx())
    )

    // Crosshairs
    drawLine(
      color = scannerColor.copy(alpha = 0.2f),
      start = Offset(center.x, center.y - radius),
      end = Offset(center.x, center.y + radius),
      strokeWidth = 1.dp.toPx()
    )
    drawLine(
      color = scannerColor.copy(alpha = 0.2f),
      start = Offset(center.x - radius, center.y),
      end = Offset(center.x + radius, center.y),
      strokeWidth = 1.dp.toPx()
    )

    // Rotating Sweep Ray
    val radians = Math.toRadians(angle.toDouble())
    val sweepEnd = Offset(
      x = (center.x + radius * Math.cos(radians)).toFloat(),
      y = (center.y + radius * Math.sin(radians)).toFloat()
    )
    drawLine(
      color = scannerColor,
      start = center,
      end = sweepEnd,
      strokeWidth = 2.dp.toPx()
    )

    // Center Blip
    drawCircle(
      color = scannerColor,
      radius = 3.dp.toPx(),
      center = center
    )
  }
}

/**
 * Cyber Grid Background Canvas with subtle high-tech scanning line.
 */
@Composable
fun CyberGridBackground(
  modifier: Modifier = Modifier,
  lineColor: Color = Color(0xFF14241B)
) {
  Canvas(modifier = modifier.fillMaxSize()) {
    val step = 32.dp.toPx()
    var x = 0f
    while (x < size.width) {
      drawLine(
        color = lineColor,
        start = Offset(x, 0f),
        end = Offset(x, size.height),
        strokeWidth = 1f
      )
      x += step
    }
    var y = 0f
    while (y < size.height) {
      drawLine(
        color = lineColor,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = 1f
      )
      y += step
    }
  }
}
