package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import java.security.MessageDigest
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

import com.example.security.RealGameSecurityAnalyzer
import com.example.security.FileAnalysisResult
import com.example.ui.components.CyberRadarScanner

enum class LabExperimentType(val label: String) {
  FILE_INSPECTOR("Analisis File & Config"),
  MOVEMENT("Movement Validation"),
  COOLDOWN("Cooldown Validation"),
  INVENTORY("Inventory Validation"),
  RATE_LIMIT("Rate Limiting"),
  HASH("Hash Comparison"),
  NETWORK("Network Demo"),
  BEHAVIOR("Behavior Score")
}

@Composable
fun SecurityLabScreen(
  modifier: Modifier = Modifier
) {
  var selectedTab by rememberSaveable { mutableStateOf(LabExperimentType.FILE_INSPECTOR) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("security_lab_screen")
  ) {
    // Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Text(
        text = "Security Lab",
        style = MaterialTheme.typography.titleLarge,
        color = TextPrimary
      )
      Text(
        text = "Audit file/konfigurasi sungguhan, Shannon entropy, dan validasi pertahanan",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
      )
    }

    // Horizontal Experiment Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      LabExperimentType.entries.forEach { exp ->
        val isSelected = exp == selectedTab
        FilterChip(
          selected = isSelected,
          onClick = { selectedTab = exp },
          label = {
            Text(
              text = exp.label,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.5.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AccentGreen,
            selectedLabelColor = DarkBackground,
            containerColor = DarkSurface,
            labelColor = TextPrimary
          ),
          border = BorderStroke(1.dp, if (isSelected) AccentGreen else DarkCardBorder),
          modifier = Modifier.testTag("chip_lab_${exp.name.lowercase()}")
        )
      }
    }

    // Experiment Body Content
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = 8.dp),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      when (selectedTab) {
        LabExperimentType.FILE_INSPECTOR -> item { FileConfigAnalyzerExperiment() }
        LabExperimentType.MOVEMENT -> item { MovementValidationExperiment() }
        LabExperimentType.COOLDOWN -> item { CooldownValidationExperiment() }
        LabExperimentType.INVENTORY -> item { InventoryValidationExperiment() }
        LabExperimentType.RATE_LIMIT -> item { RateLimitingExperiment() }
        LabExperimentType.HASH -> item { HashComparisonExperiment() }
        LabExperimentType.NETWORK -> item { NetworkDemoExperiment() }
        LabExperimentType.BEHAVIOR -> item { BehaviorScoreExperiment() }
      }
    }
  }
}

@Composable
private fun FileConfigAnalyzerExperiment() {
  var fileName by remember { mutableStateOf("game_remote_config.json") }
  var fileContent by remember {
    mutableStateOf(
      """
{
  "app_version": "1.0.4-prod",
  "allow_insecure_http": true,
  "server_verify_cooldown": false,
  "authoritative_movement": false,
  "debug_mode": true,
  "anti_cheat_hook_watch": ["frida", "gameguardian", "speedhack"],
  "encryption_key_derivation": "SHA-256",
  "target_package": "com.aistudio.gameseclab"
}
      """.trimIndent()
    )
  }

  var analysisResult by remember {
    mutableStateOf<FileAnalysisResult?>(
      RealGameSecurityAnalyzer.analyzeContent(fileName, fileContent)
    )
  }

  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = BorderStroke(1.dp, DarkCardBorder),
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
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          CyberRadarScanner(modifier = Modifier.size(24.dp))
          Text(
            text = "Audit File & Konfigurasi Sungguhan",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
          )
        }

        analysisResult?.let { res ->
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = when {
              res.overallRiskScore >= 60 -> StatusDanger.copy(alpha = 0.2f)
              res.overallRiskScore >= 25 -> StatusWarning.copy(alpha = 0.2f)
              else -> StatusSuccess.copy(alpha = 0.2f)
            }
          ) {
            Text(
              text = "SKOR RISIKO: ${res.overallRiskScore}/100",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = when {
                res.overallRiskScore >= 60 -> StatusDanger
                res.overallRiskScore >= 25 -> StatusWarning
                else -> StatusSuccess
              },
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      Text(
        text = "Menganalisis berkas biner/JSON/XML game sungguhan: menghitung SHA-256, Shannon Entropy, mendeteksi magic bytes, serta memindai pola hooking (Frida/Xposed) & celah konfigurasi.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
      )

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Button(
          onClick = {
            fileName = "game_vulnerable_config.json"
            fileContent = """
{
  "allow_insecure_http": true,
  "server_verify_cooldown": false,
  "authoritative_movement": false,
  "debug_mode": true,
  "godmode_flag": true,
  "agent": "frida-server"
}
            """.trimIndent()
            analysisResult = RealGameSecurityAnalyzer.analyzeContent(fileName, fileContent)
          },
          colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text("Muat Config Rawan", fontSize = 11.sp, color = AccentBlue)
        }

        Button(
          onClick = {
            fileName = "game_hardened_manifest.xml"
            fileContent = """
<?xml version="1.0" encoding="utf-8"?>
<game_config>
  <security authoritative_movement="true" verify_cooldown="true" />
  <network use_https="true" ssl_pinning="true" debug_mode="false" />
  <integrity sha256_checksum="e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855" />
</game_config>
            """.trimIndent()
            analysisResult = RealGameSecurityAnalyzer.analyzeContent(fileName, fileContent)
          },
          colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text("Muat Config Aman", fontSize = 11.sp, color = AccentGreen)
        }
      }

      OutlinedTextField(
        value = fileContent,
        onValueChange = {
          fileContent = it
          analysisResult = RealGameSecurityAnalyzer.analyzeContent(fileName, it)
        },
        label = { Text("Isi File / Konfigurasi / Log Game") },
        textStyle = androidx.compose.ui.text.TextStyle(
          fontFamily = FontFamily.Monospace,
          fontSize = 11.5.sp,
          color = TextPrimary
        ),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DarkSurfaceVariant,
          unfocusedContainerColor = DarkSurfaceVariant,
          focusedBorderColor = AccentGreen,
          unfocusedBorderColor = DarkCardBorder
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
      )

      analysisResult?.let { res ->
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceVariant)
            .padding(12.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("HASIL AUDIT FORENSIK FILE:", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
            Text("• Format Terdeteksi: ${res.detectedFormat}", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, color = TextPrimary)
            Text("• Ukuran File      : ${res.byteSize} bytes", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, color = TextPrimary)
            Text("• SHA-256 Hash     : ${res.sha256Hash.take(28)}...", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = AccentBlue)
            Text("• MD5 Checksum     : ${res.md5Hash}", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = AccentBlue)
            Text("• Shannon Entropy  : ${res.entropyAssessment}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextPrimary)
            Text("• Status Evaluasi  : ${res.riskLevel}", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (res.overallRiskScore >= 60) StatusDanger else StatusSuccess)
          }
        }

        if (res.threatKeywordsFound.isNotEmpty()) {
          Text("Kata Kunci Ancaman Terdeteksi (${res.threatKeywordsFound.size}):", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, color = StatusDanger, fontWeight = FontWeight.Bold)
          res.threatKeywordsFound.forEach { threat ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("• [${threat.severity}] ${threat.keyword}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextPrimary)
              Text(threat.category, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = StatusDanger)
            }
          }
        }

        if (res.configIssues.isNotEmpty()) {
          Text("Celah Konfigurasi Kritis (${res.configIssues.size}):", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, color = StatusWarning, fontWeight = FontWeight.Bold)
          res.configIssues.forEach { issue ->
            Text("• ${issue.description}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextSecondary)
          }
        }
      }
    }
  }
}

// 1. Movement Validation (Spesifikasi Halaman 4)
@Composable
private fun MovementValidationExperiment() {
  var x1 by remember { mutableFloatStateOf(10f) }
  var y1 by remember { mutableFloatStateOf(20f) }
  var x2 by remember { mutableFloatStateOf(25f) }
  var y2 by remember { mutableFloatStateOf(35f) }
  var t1 by remember { mutableFloatStateOf(0.0f) }
  var t2 by remember { mutableFloatStateOf(1.5f) }
  var maxSpeed by remember { mutableFloatStateOf(12.0f) }
  var tolerance by remember { mutableFloatStateOf(2.0f) }

  val deltaTime = t2 - t1
  val isTimeValid = deltaTime > 0f

  val distance2D = if (isTimeValid) sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2)) else 0f
  val maxAllowedDistance = if (isTimeValid) (maxSpeed * deltaTime) + tolerance else 0f
  val calculatedSpeed = if (isTimeValid && deltaTime > 0f) distance2D / deltaTime else 0f

  val status = when {
    !isTimeValid -> "INPUT DITOLAK (Waktu tidak positif)"
    distance2D <= (maxSpeed * deltaTime) -> "VALID"
    distance2D <= maxAllowedDistance -> "PERLU PEMERIKSAAN (Dalam toleransi jitter)"
    else -> "MELAMPAUI BATAS (Teleport / Speed Anomali)"
  }

  val statusColor = when {
    !isTimeValid -> StatusDanger
    status.startsWith("VALID") -> StatusSuccess
    status.startsWith("PERLU") -> StatusWarning
    else -> StatusDanger
  }

  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = BorderStroke(1.dp, DarkCardBorder),
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
          text = "Movement Validation (Kinematika 2D)",
          style = MaterialTheme.typography.titleMedium,
          color = TextPrimary
        )
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = statusColor.copy(alpha = 0.2f),
          border = BorderStroke(1.dp, statusColor)
        ) {
          Text(
            text = status,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = statusColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Text(
        text = "Rumus: Jarak = √((x2−x1)² + (y2−y1)²). Waktu tempuh = t2 − t1. Batas jarak = v_max × Δt + toleransi. Jika waktu tidak positif, input ditolak.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
      )

      // Parameter Sliders
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SliderRow(
          label = "Posisi Akhir X (x2)",
          valueText = String.format(Locale.US, "%.1f m", x2),
          value = x2,
          range = 10f..100f,
          onValueChange = { x2 = it }
        )

        SliderRow(
          label = "Posisi Akhir Y (y2)",
          valueText = String.format(Locale.US, "%.1f m", y2),
          value = y2,
          range = 20f..100f,
          onValueChange = { y2 = it }
        )

        SliderRow(
          label = "Waktu Akhir (t2)",
          valueText = String.format(Locale.US, "%.2f s (Δt = %.2f s)", t2, deltaTime),
          value = t2,
          range = 0.1f..5.0f,
          onValueChange = { t2 = it }
        )

        SliderRow(
          label = "Kecepatan Maksimum Karakter (v_max)",
          valueText = String.format(Locale.US, "%.1f m/s", maxSpeed),
          value = maxSpeed,
          range = 5.0f..25.0f,
          onValueChange = { maxSpeed = it }
        )

        SliderRow(
          label = "Toleransi Buffer Latency",
          valueText = String.format(Locale.US, "%.1f m", tolerance),
          value = tolerance,
          range = 0.5f..8.0f,
          onValueChange = { tolerance = it }
        )
      }

      // Calculation Outputs Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(6.dp))
          .background(DarkSurfaceVariant)
          .padding(12.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "KONTRAK KALKULASI SERVER:",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGreen
          )
          Text(
            text = String.format(
              Locale.US,
              "• Jarak Tempuh Terhitung : %.2f meter",
              distance2D
            ),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = TextPrimary
          )
          Text(
            text = String.format(
              Locale.US,
              "• Batas Maksimum Diizinkan: %.2f meter (v_max*Δt + tol)",
              maxAllowedDistance
            ),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = TextPrimary
          )
          Text(
            text = String.format(
              Locale.US,
              "• Kecepatan Aktual Laporan: %.2f m/s",
              calculatedSpeed
            ),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = if (status.startsWith("MELAMPAUI")) StatusDanger else TextPrimary
          )
          Text(
            text = "• Keputusan: $status",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.5.sp,
            color = statusColor
          )
        }
      }
    }
  }
}

// 2. Cooldown Validation Experiment (Halaman 4)
@Composable
private fun CooldownValidationExperiment() {
  var minCooldown by remember { mutableFloatStateOf(4.0f) }
  var elapsedActionTime by remember { mutableFloatStateOf(2.5f) }

  val isAllowed = elapsedActionTime >= minCooldown

  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = BorderStroke(1.dp, DarkCardBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = "Cooldown Validation",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary
      )

      Text(
        text = "Membandingkan waktu kedatangan aksi dengan cooldown minimum skill. Jika aksi dilaporkan sebelum cooldown selesai, aksi ditolak oleh server.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
      )

      SliderRow(
        label = "Durasi Cooldown Minimum",
        valueText = String.format(Locale.US, "%.1f detik", minCooldown),
        value = minCooldown,
        range = 1.0f..10.0f,
        onValueChange = { minCooldown = it }
      )

      SliderRow(
        label = "Jeda Waktu Aksi Dilaporkan",
        valueText = String.format(Locale.US, "%.1f detik", elapsedActionTime),
        value = elapsedActionTime,
        range = 0.5f..10.0f,
        onValueChange = { elapsedActionTime = it }
      )

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(6.dp))
          .background(DarkSurfaceVariant)
          .padding(12.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = if (isAllowed) "AKSI DIIZINKAN (Cooldown Selesai)" else "DITOLAK (Cooldown Violation)",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isAllowed) StatusSuccess else StatusDanger
          )
          Text(
            text = if (isAllowed)
              "Jeda ${String.format(Locale.US, "%.1f s", elapsedActionTime)} >= batas ${String.format(Locale.US, "%.1f s", minCooldown)}. State diubah dan efek skill disebarkan ke peer."
            else
              "Aksi tiba ${String.format(Locale.US, "%.1f s", minCooldown - elapsedActionTime)} terlalu cepat. Paket di-drop tanpa mengeksekusi damage.",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 12.sp,
            color = TextSecondary
          )
        }
      }
    }
  }
}

// 3. Inventory Validation Experiment (Halaman 4)
@Composable
private fun InventoryValidationExperiment() {
  var userDiamonds by remember { mutableIntStateOf(100) }
  var requestedItemCost by remember { mutableIntStateOf(120) }

  val canPurchase = (userDiamonds - requestedItemCost) >= 0

  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = BorderStroke(1.dp, DarkCardBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = "Inventory & Balance Validation",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary
      )

      Text(
        text = "Server wajib memastikan jumlah item/saldo tidak bernilai negatif, transaksi konsisten, dan item benar-benar dimiliki pengguna.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text("Saldo Akun (Server)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextMuted)
          Text("$userDiamonds Koin", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        }
        Column(modifier = Modifier.weight(1f)) {
          Text("Harga Item Diminta", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextMuted)
          Text("$requestedItemCost Koin", style = MaterialTheme.typography.titleMedium, color = AccentBlue)
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = { requestedItemCost = 50 },
          colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
        ) {
          Text("Uji Biaya 50 Koin", fontSize = 11.sp, color = TextPrimary)
        }
        Button(
          onClick = { requestedItemCost = 150 },
          colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
        ) {
          Text("Uji Biaya 150 Koin", fontSize = 11.sp, color = TextPrimary)
        }
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(6.dp))
          .background(DarkSurfaceVariant)
          .padding(12.dp)
      ) {
        Text(
          text = if (canPurchase)
            "TRANSAKSI BERHASIL: Sisa saldo ${(userDiamonds - requestedItemCost)} koin. Konsistensi ACID terjaga."
          else
            "TRANSAKSI DITOLAK (400 BAD REQUEST): Saldo tidak mencukupi (defisit ${requestedItemCost - userDiamonds}). Mencegah integer underflow.",
          fontFamily = FontFamily.Monospace,
          fontSize = 11.5.sp,
          color = if (canPurchase) StatusSuccess else StatusDanger
        )
      }
    }
  }
}

// 4. Rate Limiting Experiment (Halaman 4)
@Composable
private fun RateLimitingExperiment() {
  val logs = remember { mutableStateListOf<String>() }
  var requestCounter by remember { mutableIntStateOf(0) }

  fun sendRequestBurst() {
    requestCounter++
    val timestamp = System.currentTimeMillis() % 10000
    if (requestCounter <= 5) {
      logs.add("[${timestamp}ms] Request #$requestCounter -> 200 OK (DITERIMA)")
    } else {
      logs.add("[${timestamp}ms] Request #$requestCounter -> 429 TOO MANY REQUESTS (RATE LIMIT DIBLOKIR)")
    }
  }

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
        text = "Rate Limiting Simulation (Sliding Window)",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary
      )

      Text(
        text = "Ambang batas: Maksimum 5 request per detik. Ketuk tombol 'Kirim Request' berulang kali untuk melihat penolakan otomatis.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = { sendRequestBurst() },
          colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
          modifier = Modifier.testTag("btn_send_rate_limit_req")
        ) {
          Text("Kirim Request (#$requestCounter)", color = DarkBackground, fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = {
            requestCounter = 0
            logs.clear()
          },
          colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
        ) {
          Text("Reset Window", color = TextPrimary)
        }
      }

      Surface(
        shape = RoundedCornerShape(6.dp),
        color = DarkBackground,
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          if (logs.isEmpty()) {
            Text("Belum ada request dikirim. Ketuk tombol untuk memulai.", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextMuted)
          } else {
            logs.takeLast(6).forEach { entry ->
              Text(
                text = entry,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = if (entry.contains("429")) StatusDanger else StatusSuccess
              )
            }
          }
        }
      }
    }
  }
}

// 5. Hash Comparison Experiment (Halaman 4)
@Composable
private fun HashComparisonExperiment() {
  var inputA by remember { mutableStateOf("PlayerModel_CharacterMesh_v1") }
  var inputB by remember { mutableStateOf("PlayerModel_CharacterMesh_v2") }

  fun sha256(text: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(text.toByteArray())
    return digest.fold("") { str, it -> str + "%02x".format(it) }
  }

  val hashA = remember(inputA) { sha256(inputA) }
  val hashB = remember(inputB) { sha256(inputB) }
  val isMatch = hashA == hashB

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
        text = "Hash Comparison & File Integrity",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary
      )

      Text(
        text = "Bandingkan hash input latihan. Catatan: Hash membuktikan integritas berkas belum diubah, tetapi bukan jaminan keamanan seluruh aplikasi.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
      )

      OutlinedTextField(
        value = inputA,
        onValueChange = { inputA = it },
        label = { Text("Teks Input A (Original)") },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DarkSurfaceVariant,
          unfocusedContainerColor = DarkSurfaceVariant,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Text("SHA-256 (A): $hashA", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = AccentBlue)

      OutlinedTextField(
        value = inputB,
        onValueChange = { inputB = it },
        label = { Text("Teks Input B (Uji Modifikasi)") },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DarkSurfaceVariant,
          unfocusedContainerColor = DarkSurfaceVariant,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Text("SHA-256 (B): $hashB", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = AccentBlue)

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(6.dp))
          .background(DarkSurfaceVariant)
          .padding(10.dp)
      ) {
        Text(
          text = if (isMatch)
            "STATUS HASH: 100% IDENTIK. Integritas file terverifikasi."
          else
            "STATUS HASH: BERBEDA (Avalanche Effect). Modifikasi file terdeteksi.",
          fontFamily = FontFamily.Monospace,
          fontSize = 11.5.sp,
          color = if (isMatch) StatusSuccess else StatusDanger
        )
      }
    }
  }
}

// 6. Network Demo (Halaman 4)
@Composable
private fun NetworkDemoExperiment() {
  var clientReportedAmmo by remember { mutableIntStateOf(999) }
  val serverAuthoritativeAmmo = 30

  val isTampered = clientReportedAmmo != serverAuthoritativeAmmo

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
        text = "Network Request/Response Demo",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary
      )

      Text(
        text = "Simulasi pengiriman paket data tembakan fiktif dan validasi server ledger.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = { clientReportedAmmo = 30 },
          colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
        ) {
          Text("Kirim Normal (30 peluru)", fontSize = 11.sp, color = TextPrimary)
        }
        Button(
          onClick = { clientReportedAmmo = 999 },
          colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
        ) {
          Text("Simulasi Tampered (999 peluru)", fontSize = 11.sp, color = AccentBlue)
        }
      }

      Surface(
        shape = RoundedCornerShape(6.dp),
        color = DarkBackground,
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("PAYLOAD JARINGAN:", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = AccentBlue)
          Text("{\"action\": \"SHOOT\", \"ammo_claimed\": $clientReportedAmmo, \"server_expected\": $serverAuthoritativeAmmo}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextPrimary)
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (isTampered)
              "HASIL VERIFIKASI: [REJECTED 400] Nilai amunisi client tidak sesuai ledger server. Server mengoreksi balik amunisi."
            else
              "HASIL VERIFIKASI: [ACCEPTED 200] Nilai amunisi sinkron dengan server. Tembakan dinyatakan sah.",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.5.sp,
            color = if (isTampered) StatusDanger else StatusSuccess
          )
        }
      }
    }
  }
}

// 7. Behavior Score (Halaman 4)
@Composable
private fun BehaviorScoreExperiment() {
  var clickJitterVariance by remember { mutableFloatStateOf(1.2f) } // 0ms to 20ms
  var snapAimAngleSpeed by remember { mutableFloatStateOf(95f) } // deg/s

  // Behavior risk calculation with labeled weights
  val jitterRisk = if (clickJitterVariance < 2.0f) 50 else if (clickJitterVariance < 5.0f) 25 else 0
  val snapRisk = if (snapAimAngleSpeed > 80f) 40 else if (snapAimAngleSpeed > 50f) 20 else 0
  val totalScore = (jitterRisk + snapRisk).coerceIn(0, 100)

  val evaluation = when {
    totalScore >= 70 -> "RESIKO TINGGI (Anomali Otomatisasi Bot)"
    totalScore >= 35 -> "PERLU PENINJAUAN (Deviasi Moderat)"
    else -> "NORMAL (Distribusi Pola Manusia)"
  }

  val evalColor = when {
    totalScore >= 70 -> StatusDanger
    totalScore >= 35 -> StatusWarning
    else -> StatusSuccess
  }

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
        text = "Behavioral Scoring Heuristics",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary
      )

      Text(
        text = "Menghitung skor risiko dari sinyal berlabel. Sinyal dengan variansi mikro di bawah batas motorik manusia meningkatkan bobot risiko.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
      )

      SliderRow(
        label = "Variansi Jeda Klik (Jitter Std-Dev)",
        valueText = String.format(Locale.US, "%.1f ms", clickJitterVariance),
        value = clickJitterVariance,
        range = 0.1f..15.0f,
        onValueChange = { clickJitterVariance = it }
      )

      SliderRow(
        label = "Kecepatan Snap Sudut Aim",
        valueText = String.format(Locale.US, "%.0f deg/frame", snapAimAngleSpeed),
        value = snapAimAngleSpeed,
        range = 10f..120f,
        onValueChange = { snapAimAngleSpeed = it }
      )

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(6.dp))
          .background(DarkSurfaceVariant)
          .padding(12.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "SKOR ANOMALI: $totalScore / 100",
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = evalColor
          )
          Text(
            text = "Status: $evaluation",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = evalColor
          )
          Text(
            text = "Catatan: Skor anomali memperhitungkan bobot sinyal. Jitter sangat rendah (<2ms) dapat terjadi pada mouse gaming ber-debounce rendah (kemungkinan false positive tetap dicatat).",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 11.sp,
            color = TextMuted
          )
        }
      }
    }
  }
}

@Composable
private fun SliderRow(
  label: String,
  valueText: String,
  value: Float,
  range: ClosedFloatingPointRange<Float>,
  onValueChange: (Float) -> Unit
) {
  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(label, fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, color = TextSecondary)
      Text(valueText, fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, color = AccentGreen, fontWeight = FontWeight.Bold)
    }
    Slider(
      value = value,
      onValueChange = onValueChange,
      valueRange = range,
      colors = SliderDefaults.colors(
        thumbColor = AccentGreen,
        activeTrackColor = AccentGreen,
        inactiveTrackColor = DarkSurfaceVariant
      )
    )
  }
}
