package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DetectionStep
import com.example.data.GameSecRepository
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun DetectionScreen(
  modifier: Modifier = Modifier
) {
  // Map of expanded steps (Step 1 expanded by default)
  val expandedMap = remember {
    mutableStateMapOf<Int, Boolean>().apply {
      this[0] = true
    }
  }

  // Interactive Movement Validation parameters
  var deltaTime by remember { mutableFloatStateOf(0.10f) } // 0.05s to 1.0s
  var reportedDistance by remember { mutableFloatStateOf(1.20f) } // 0.5m to 20m
  var maxCharacterSpeed by remember { mutableFloatStateOf(8.0f) } // 5 to 15 m/s
  var tolerancePercent by remember { mutableFloatStateOf(15f) } // 5% to 30%

  // Calculations
  val maxAllowedDistance = (maxCharacterSpeed * deltaTime) * (1.0f + tolerancePercent / 100f)
  val calculatedSpeed = if (deltaTime > 0f) reportedDistance / deltaTime else 0f
  val isAnomaly = reportedDistance > maxAllowedDistance

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("detection_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Logika Deteksi",
          style = MaterialTheme.typography.titleLarge,
          color = TextPrimary
        )
        Text(
          text = "Alur komputasi validasi dan penilaian risiko server secara bertahap",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary
        )
      }
    }

    // Interactive Parameter Simulator Card
    item {
      Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (isAnomaly) StatusDanger.copy(alpha = 0.6f) else DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Simulator Validasi Kinetika",
              style = MaterialTheme.typography.titleMedium,
              color = TextPrimary
            )
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = if (isAnomaly) StatusDanger.copy(alpha = 0.2f) else StatusSuccess.copy(alpha = 0.2f),
              border = BorderStroke(
                1.dp,
                if (isAnomaly) StatusDanger else StatusSuccess
              )
            ) {
              Text(
                text = if (isAnomaly) "ANOMALI: DITOLAK" else "VALID: DITERIMA",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAnomaly) StatusDanger else StatusSuccess,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Text(
            text = "Sesuaikan parameter di bawah untuk melihat bagaimana rumus toleransi server menyaring manipulasi kecepatan (speed / teleport anomaly).",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
          )

          // Parameter Sliders
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Delta Time
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Selisih Waktu (Δt)",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = TextSecondary
              )
              Text(
                text = String.format(Locale.US, "%.3f s (%d ms)", deltaTime, (deltaTime * 1000).toInt()),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = AccentBlue,
                fontWeight = FontWeight.Bold
              )
            }
            Slider(
              value = deltaTime,
              onValueChange = { deltaTime = it },
              valueRange = 0.05f..0.80f,
              colors = SliderDefaults.colors(
                thumbColor = AccentBlue,
                activeTrackColor = AccentBlue,
                inactiveTrackColor = DarkSurfaceVariant
              ),
              modifier = Modifier.testTag("slider_delta_time")
            )

            // Reported Distance
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Jarak Laporan Client (d)",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = TextSecondary
              )
              Text(
                text = String.format(Locale.US, "%.2f meter", reportedDistance),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = AccentBlue,
                fontWeight = FontWeight.Bold
              )
            }
            Slider(
              value = reportedDistance,
              onValueChange = { reportedDistance = it },
              valueRange = 0.2f..15.0f,
              colors = SliderDefaults.colors(
                thumbColor = AccentBlue,
                activeTrackColor = AccentBlue,
                inactiveTrackColor = DarkSurfaceVariant
              ),
              modifier = Modifier.testTag("slider_distance")
            )

            // Max Speed
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Kecepatan Karakter Resmi (v_max)",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = TextSecondary
              )
              Text(
                text = String.format(Locale.US, "%.1f m/s", maxCharacterSpeed),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = AccentBlue,
                fontWeight = FontWeight.Bold
              )
            }
            Slider(
              value = maxCharacterSpeed,
              onValueChange = { maxCharacterSpeed = it },
              valueRange = 4.0f..16.0f,
              colors = SliderDefaults.colors(
                thumbColor = AccentBlue,
                activeTrackColor = AccentBlue,
                inactiveTrackColor = DarkSurfaceVariant
              ),
              modifier = Modifier.testTag("slider_speed")
            )
          }

          // Telemetry Result Box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(DarkSurfaceVariant)
              .padding(12.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = "HASIL PERHITUNGAN SERVER TICK:",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AccentBlue
              )
              Text(
                text = String.format(
                  Locale.US,
                  "• Jarak Maksimum Diizinkan : %.3f m",
                  maxAllowedDistance
                ),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = TextPrimary
              )
              Text(
                text = String.format(
                  Locale.US,
                  "• Kecepatan Laporan Terhitung: %.2f m/s",
                  calculatedSpeed
                ),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = if (isAnomaly) StatusDanger else TextPrimary
              )
              Text(
                text = if (isAnomaly)
                  String.format(
                    Locale.US,
                    "• Keputusan: Anomali terdeteksi (+%.2f m melebihi batas). Status flag SUSPICIOUS_MOVEMENT.",
                    reportedDistance - maxAllowedDistance
                  )
                else
                  "• Keputusan: Dalam ambang toleransi fisika. Paket diterima dan disinkronkan ke peer.",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.5.sp,
                color = if (isAnomaly) StatusDanger else StatusSuccess,
                lineHeight = 16.sp
              )
            }
          }
        }
      }
    }

    // Section title for Step By Step Pipeline
    item {
      Text(
        text = "Alur Deteksi Langkah demi Langkah",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        modifier = Modifier.padding(top = 4.dp)
      )
    }

    // 6 Expandable Steps
    itemsIndexed(GameSecRepository.detectionSteps) { index, step ->
      val isExpanded = expandedMap[index] == true
      DetectionStepExpandableCard(
        step = step,
        isExpanded = isExpanded,
        testTag = "step_card_$index",
        onToggle = {
          expandedMap[index] = !isExpanded
        }
      )
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun DetectionStepExpandableCard(
  step: DetectionStep,
  isExpanded: Boolean,
  testTag: String,
  onToggle: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = BorderStroke(1.dp, if (isExpanded) AccentBlue else DarkCardBorder),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .clickable(onClick = onToggle)
      .testTag(testTag)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = if (isExpanded) AccentBlue else DarkSurfaceVariant,
            modifier = Modifier.padding(top = 1.dp)
          ) {
            Text(
              text = "0${step.stepNumber}",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = if (isExpanded) DarkBackground else AccentBlue,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }

          Text(
            text = step.title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontSize = 14.5.sp,
            lineHeight = 20.sp
          )
        }

        Icon(
          imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
          contentDescription = if (isExpanded) "Tutup" else "Buka",
          tint = if (isExpanded) AccentBlue else TextMuted
        )
      }

      AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(),
        exit = shrinkVertically()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Formula / Pseudo Code
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(4.dp))
              .background(DarkSurfaceVariant)
              .padding(10.dp)
          ) {
            Text(
              text = step.formula,
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp,
              color = AccentBlue,
              lineHeight = 17.sp
            )
          }

          // Explanation
          Text(
            text = step.explanation,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 21.sp
          )

          // Technical Note
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(4.dp))
              .background(DarkBackground)
              .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
          ) {
            Text(
              text = "Catatan:",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = TextMuted
            )
            Text(
              text = step.technicalNote,
              style = MaterialTheme.typography.bodyMedium,
              fontSize = 12.sp,
              color = TextMuted,
              lineHeight = 17.sp,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }
  }
}
