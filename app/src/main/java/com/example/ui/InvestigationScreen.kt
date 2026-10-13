package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClaimItem
import com.example.data.ClaimVerdict
import com.example.data.GameSecRepository
import com.example.data.InvestigationCase
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class InvestigationViewMode {
  CASES,
  CLAIM_CHECKER,
  LOG_ANALYSIS
}

@Composable
fun InvestigationScreen(
  modifier: Modifier = Modifier
) {
  var viewMode by rememberSaveable { mutableStateOf(InvestigationViewMode.CASES) }
  var selectedCaseIndex by rememberSaveable { mutableIntStateOf(0) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("investigation_screen")
  ) {
    // Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Text(
        text = "Investigasi Digital & Claim Checker",
        style = MaterialTheme.typography.titleLarge,
        color = TextPrimary
      )
      Text(
        text = "Rekonstruksi kasus forensik, verifikasi bukti independen, dan evaluasi klaim",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
      )
    }

    // View Mode Switcher
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = viewMode == InvestigationViewMode.CASES,
        onClick = { viewMode = InvestigationViewMode.CASES },
        label = { Text("Kasus Latihan (5)", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp) },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = AccentGreen,
          selectedLabelColor = DarkBackground,
          containerColor = DarkSurface,
          labelColor = TextPrimary
        ),
        border = BorderStroke(1.dp, if (viewMode == InvestigationViewMode.CASES) AccentGreen else DarkCardBorder),
        modifier = Modifier.testTag("chip_tab_cases")
      )

      FilterChip(
        selected = viewMode == InvestigationViewMode.CLAIM_CHECKER,
        onClick = { viewMode = InvestigationViewMode.CLAIM_CHECKER },
        label = { Text("Claim Checker", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp) },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = AccentGreen,
          selectedLabelColor = DarkBackground,
          containerColor = DarkSurface,
          labelColor = TextPrimary
        ),
        border = BorderStroke(1.dp, if (viewMode == InvestigationViewMode.CLAIM_CHECKER) AccentGreen else DarkCardBorder),
        modifier = Modifier.testTag("chip_tab_claim_checker")
      )

      FilterChip(
        selected = viewMode == InvestigationViewMode.LOG_ANALYSIS,
        onClick = { viewMode = InvestigationViewMode.LOG_ANALYSIS },
        label = { Text("Dataset Log", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp) },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = AccentGreen,
          selectedLabelColor = DarkBackground,
          containerColor = DarkSurface,
          labelColor = TextPrimary
        ),
        border = BorderStroke(1.dp, if (viewMode == InvestigationViewMode.LOG_ANALYSIS) AccentGreen else DarkCardBorder),
        modifier = Modifier.testTag("chip_tab_logs")
      )
    }

    // Main Content
    when (viewMode) {
      InvestigationViewMode.CASES -> {
        CasesView(
          selectedCaseIndex = selectedCaseIndex,
          onSelectCase = { selectedCaseIndex = it }
        )
      }
      InvestigationViewMode.CLAIM_CHECKER -> {
        ClaimCheckerView()
      }
      InvestigationViewMode.LOG_ANALYSIS -> {
        LogAnalysisView()
      }
    }
  }
}

@Composable
private fun CasesView(
  selectedCaseIndex: Int,
  onSelectCase: (Int) -> Unit
) {
  val cases = GameSecRepository.sampleInvestigationCases
  val currentCase = cases[selectedCaseIndex]

  var userHypothesis by rememberSaveable { mutableStateOf("") }
  var userConfidence by rememberSaveable { mutableStateOf("Sedang") }
  var showConclusionReport by rememberSaveable { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(top = 8.dp),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Horizontal Case Selector
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        cases.forEachIndexed { idx, c ->
          val isSelected = idx == selectedCaseIndex
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isSelected) AccentGreen else DarkSurface,
            border = BorderStroke(1.dp, if (isSelected) AccentGreen else DarkCardBorder),
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .clickable {
                onSelectCase(idx)
                showConclusionReport = false
              }
              .testTag("case_chip_$idx")
          ) {
            Text(
              text = "Kasus ${idx + 1}",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) DarkBackground else TextPrimary,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }
      }
    }

    // Case Details Card
    item {
      Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = currentCase.title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
          )

          Text(
            text = currentCase.problemDescription,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 22.sp
          )

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(4.dp))
              .background(DarkSurfaceVariant)
              .padding(12.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = "PERTANYAAN INVESTIGASI:",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGreen
              )
              Text(
                text = currentCase.investigationQuestion,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
              )
            }
          }
        }
      }
    }

    // Evidence List (8-Step flow: Step 2)
    item {
      Text(
        text = "Daftar Bukti Digital Terkumpul",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary
      )
    }

    items(currentCase.evidenceList) { ev ->
      Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.Top
        ) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = DarkSurfaceVariant
          ) {
            Text(
              text = ev.id,
              fontFamily = FontFamily.Monospace,
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              color = AccentGreen,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = ev.title, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(text = "Sumber: ${ev.source} • Timestamp: ${ev.timestamp}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextMuted)
            Text(text = ev.detail, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, lineHeight = 19.sp)
          }
        }
      }
    }

    // Hypothesis & Confidence Form (Step 4 to 7)
    item {
      Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Formulir Kesimpulan Analis Forensik",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
          )

          OutlinedTextField(
            value = userHypothesis,
            onValueChange = { userHypothesis = it },
            placeholder = { Text("Tuliskan hipotesis/kesimpulan sementara Anda berdasarkan bukti...") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = DarkSurfaceVariant,
              unfocusedContainerColor = DarkSurfaceVariant,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Tingkat Keyakinan:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf("Rendah", "Sedang", "Tinggi").forEach { level ->
                FilterChip(
                  selected = userConfidence == level,
                  onClick = { userConfidence = level },
                  label = { Text(level, fontSize = 11.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentGreen,
                    selectedLabelColor = DarkBackground
                  )
                )
              }
            }
          }

          Button(
            onClick = { showConclusionReport = true },
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("btn_save_investigation_report")
          ) {
            Text("Simpan Laporan & Buka Fakta Objektif", color = DarkBackground, fontWeight = FontWeight.Bold)
          }

          if (showConclusionReport) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(DarkSurfaceVariant)
                .padding(12.dp)
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "FAKTA FORENSIK SEBENARNYA:",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = StatusSuccess
                )
                Text(
                  text = currentCase.actualFactSummary,
                  style = MaterialTheme.typography.bodyMedium,
                  color = TextPrimary,
                  lineHeight = 20.sp
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ClaimCheckerView() {
  val claims = GameSecRepository.sampleClaims

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(top = 8.dp),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(text = "Metodologi Claim Checker", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
          Text(
            text = "Jangan menyebut sebuah klaim sebagai penipuan hanya karena belum terbukti. Periksa sumber asli, tanggal, konteks, independensi bukti, kemungkinan konflik kepentingan, dan penjelasan alternatif.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 21.sp
          )
        }
      }
    }

    items(claims) { clm ->
      val verdictColor = when (clm.verdict) {
        ClaimVerdict.SUPPORTED -> StatusSuccess
        ClaimVerdict.CONTRADICTED -> StatusDanger
        ClaimVerdict.INSUFFICIENT -> StatusWarning
        ClaimVerdict.UNVERIFIABLE -> TextMuted
      }

      Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = DarkSurfaceVariant
            ) {
              Text(
                text = "${clm.source} • ${clm.date}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = AccentBlue,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }

            Surface(
              shape = RoundedCornerShape(4.dp),
              color = verdictColor.copy(alpha = 0.2f),
              border = BorderStroke(1.dp, verdictColor)
            ) {
              Text(
                text = clm.verdict.label,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = verdictColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Text(
            text = "\"${clm.claimText}\"",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
          )

          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("• Bukti Pendukung: ${clm.supportingEvidence}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Text("• Bukti Berlawanan: ${clm.opposingEvidence}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Text("• Hal Belum Diketahui: ${clm.unknownAspects}", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(4.dp))
              .background(DarkSurfaceVariant)
              .padding(10.dp)
          ) {
            Text(
              text = "Analisis: " + clm.analystNotes,
              style = MaterialTheme.typography.bodyMedium,
              fontSize = 12.sp,
              color = TextPrimary,
              lineHeight = 18.sp
            )
          }
        }
      }
    }
  }
}

@Composable
private fun LogAnalysisView() {
  val logs = GameSecRepository.sampleForensicLogs

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(top = 8.dp),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("Dataset Log Latihan Forensik", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
          Text(
            text = "Dataset memuat timestamp, ID pengguna fiktif, aksi, koordinat, hasil validasi, dan sumber event. Pengguna menandai anomali dan merekonstruksi kronologi.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
          )
        }
      }
    }

    items(logs) { entry ->
      val borderColor = if (entry.isSuspicious) StatusDanger else DarkCardBorder
      val tagColor = if (entry.isSuspicious) StatusDanger else StatusSuccess

      Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "[${entry.id}] ${entry.timestamp}",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.5.sp,
              color = AccentGreen
            )

            Surface(
              shape = RoundedCornerShape(3.dp),
              color = tagColor.copy(alpha = 0.2f),
              border = BorderStroke(1.dp, tagColor)
            ) {
              Text(
                text = if (entry.isSuspicious) "ANOMALI" else "NORMAL",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = tagColor,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
              )
            }
          }

          Text(
            text = "${entry.userId} -> ${entry.action} (${entry.coordinates})",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
          )

          Text(
            text = "Hasil: ${entry.result} • Sumber: ${entry.source}",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TextSecondary
          )

          if (entry.isSuspicious && entry.anomalyReason.isNotBlank()) {
            Text(
              text = "Penjelasan: ${entry.anomalyReason}",
              style = MaterialTheme.typography.bodyMedium,
              fontSize = 11.5.sp,
              color = StatusDanger
            )
          }
        }
      }
    }
  }
}
