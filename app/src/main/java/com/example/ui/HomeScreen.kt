package com.example.ui

import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameSecRepository
import com.example.firebase.ModuleProgressManager
import com.example.ui.components.CyberMatrixRain
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
  onNavigateToTab: (GameSecNavTab) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val progress by ModuleProgressManager.progressState.collectAsState()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("home_screen_column"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Cyber Animated Hero Banner with Matrix Rain
    item {
      Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.6f)),
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          // Falling Matrix Code Canvas in background
          CyberMatrixRain(
            modifier = Modifier.fillMaxSize(),
            streamCount = 20,
            primaryColor = AccentGreen,
            alpha = 0.40f
          )

          // Foreground overlay with gradient shadow
          Column(
            modifier = Modifier
              .fillMaxSize()
              .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                  colors = listOf(
                    Color.Transparent,
                    DarkSurface.copy(alpha = 0.85f),
                    DarkSurface
                  )
                )
              )
              .padding(14.dp),
            verticalArrangement = Arrangement.Bottom
          ) {
            Surface(
              shape = RoundedCornerShape(3.dp),
              color = AccentGreen
            ) {
              Text(
                text = "CYBER CORE ACTIVE // LIVE TELEMETRY",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = DarkSurface,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "GAMESEC LAB",
              fontFamily = FontFamily.Monospace,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "Platform Keamanan Game, Forensik Log & Analisis Pasar",
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
              fontSize = 12.sp
            )
          }
        }
      }
    }

    // Header Title Text
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 2.dp, bottom = 2.dp)
      ) {
        Text(
          text = "Pusat Operasi Forensik",
          style = MaterialTheme.typography.titleLarge,
          color = TextPrimary
        )
        Text(
          text = "Memahami sistem, menguji logika, dan memverifikasi bukti",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary
        )
      }
    }

    // Activity & Streak Banner Card with Dynamic Module Progress
    item {
      Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {
          StatColumn(label = "Daily Streak", value = "3 Hari", isHighlighted = true)
          StatColumn(
            label = "Modul Selesai",
            value = "${progress.completedCount}/${progress.totalModulesCount}",
            isHighlighted = progress.completedCount > 0
          )
          StatColumn(label = "Security Lab", value = "7 Lab", isHighlighted = false)
          StatColumn(label = "Kasus Forensik", value = "5 Kasus", isHighlighted = false)
        }
      }
    }

    // PROMINENT STUDENT MODULE PROGRESS TRACKING CARD (CLOUD FIRESTORE SYNCED)
    item {
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(
          1.dp,
          if (progress.completedCount > 0) AccentGreen.copy(alpha = 0.6f) else DarkCardBorder
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("home_module_progress_card")
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Card Header: Title, Cloud Badge & Percentage
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(
                shape = CircleShape,
                color = AccentGreen.copy(alpha = 0.15f),
                modifier = Modifier.size(34.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
              Column {
                Text(
                  text = "Progress Modul Keamanan",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = if (progress.isSyncing) Icons.Default.Sync else Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = if (progress.isSyncing) AccentBlue else AccentGreen,
                    modifier = Modifier.size(11.dp)
                  )
                  Text(
                    text = progress.syncStatusMessage,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = if (progress.isSyncing) AccentBlue else AccentGreen
                  )
                }
              }
            }

            Surface(
              shape = RoundedCornerShape(4.dp),
              color = if (progress.completedCount == 10) AccentGreen else AccentBlue.copy(alpha = 0.2f)
            ) {
              Text(
                text = if (progress.completedCount == 10) "TUNTAS 100%" else "${progress.progressPercent}% SELESAI",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (progress.completedCount == 10) DarkBackground else AccentBlue,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
              )
            }
          }

          // Progress Bar with Counter
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${progress.completedCount} dari 10 Modul Selesai Dipelajari",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
              )
              Text(
                text = "${progress.completedCount}/10",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGreen
              )
            }

            LinearProgressIndicator(
              progress = { progress.progressFraction },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = AccentGreen,
              trackColor = DarkSurfaceVariant
            )
          }

          // 10 Interactive Module Status Badges (Rows of 5)
          Text(
            text = "STATUS 10 MODUL SISWA (KLIK UNTUK BUKA MODUL):",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.5.sp,
            color = TextMuted
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            (0..4).forEach { idx ->
              val isDone = progress.isCompleted(idx)
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isDone) AccentGreen.copy(alpha = 0.2f) else DarkBackground,
                border = BorderStroke(1.dp, if (isDone) AccentGreen else DarkCardBorder),
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(4.dp))
                  .clickable {
                    ModuleProgressManager.targetModuleIndex.intValue = idx
                    onNavigateToTab(GameSecNavTab.MODUL)
                  }
                  .testTag("home_module_chip_$idx")
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = "M${idx + 1}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) AccentGreen else TextMuted
                  )
                  Text(
                    text = if (isDone) "✓" else "—",
                    fontSize = 10.sp,
                    color = if (isDone) AccentGreen else TextMuted
                  )
                }
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            (5..9).forEach { idx ->
              val isDone = progress.isCompleted(idx)
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isDone) AccentGreen.copy(alpha = 0.2f) else DarkBackground,
                border = BorderStroke(1.dp, if (isDone) AccentGreen else DarkCardBorder),
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(4.dp))
                  .clickable {
                    ModuleProgressManager.targetModuleIndex.intValue = idx
                    onNavigateToTab(GameSecNavTab.MODUL)
                  }
                  .testTag("home_module_chip_$idx")
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = "M${idx + 1}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) AccentGreen else TextMuted
                  )
                  Text(
                    text = if (isDone) "✓" else "—",
                    fontSize = 10.sp,
                    color = if (isDone) AccentGreen else TextMuted
                  )
                }
              }
            }
          }

          // Action Buttons: Continue learning & Sync
          val nextUncompleted = (0..9).firstOrNull { !progress.isCompleted(it) } ?: 0
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = {
                ModuleProgressManager.targetModuleIndex.intValue = nextUncompleted
                onNavigateToTab(GameSecNavTab.MODUL)
              },
              shape = RoundedCornerShape(6.dp),
              colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
              modifier = Modifier
                .weight(1f)
                .testTag("btn_continue_learning_module")
            ) {
              Text(
                text = if (progress.completedCount == 10) "Tinjau Semua Modul" else "Lanjutkan Modul ${nextUncompleted + 1}",
                color = DarkBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = DarkBackground,
                modifier = Modifier.size(16.dp)
              )
            }

            OutlinedButton(
              onClick = {
                scope.launch {
                  val res = ModuleProgressManager.manualSync(context)
                  if (res.isSuccess) {
                    Toast.makeText(context, res.getOrNull() ?: "Tersinkron ke Firestore!", Toast.LENGTH_SHORT).show()
                  } else {
                    Toast.makeText(context, "Sinkronisasi offline: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                  }
                }
              },
              shape = RoundedCornerShape(6.dp),
              border = BorderStroke(1.dp, DarkCardBorder),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier.testTag("btn_home_sync_firestore")
            ) {
              if (progress.isSyncing) {
                CircularProgressIndicator(
                  modifier = Modifier.size(16.dp),
                  color = AccentGreen,
                  strokeWidth = 2.dp
                )
              } else {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Sinkronkan Firestore",
                  tint = AccentGreen,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }
    }

    // Teks Pembuka Card (Halaman 3)
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
            text = "Prinsip Utama & Metodologi",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
          )

          Text(
            text = GameSecRepository.introText,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 21.sp
          )

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(4.dp))
              .background(DarkSurfaceVariant)
              .padding(12.dp)
          ) {
            Text(
              text = "STATUS SISTEM: Tidak ada alat memodifikasi game pihak ketiga. Semua simulasi dijalankan dalam sandbox komputasi transparan.",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.5.sp,
              color = TextMuted,
              lineHeight = 16.sp
            )
          }
        }
      }
    }

    // Ringkasan Navigasi Cepat (Pages 3-11)
    item {
      Text(
        text = "Pusat Laboratorium & Fitur",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        modifier = Modifier.padding(top = 4.dp)
      )
    }

    item {
      SectionNavCard(
        title = "Buku Tugas & PR Sekolah",
        subtitle = "Foto lembar soal, lampirkan foto jawaban, tanggal pemotretan, timer pengerjaan & alarm pengingat bunyi",
        tagLabel = "PR & Belajar",
        icon = Icons.Default.MenuBook,
        testTag = "nav_card_tugas_sekolah",
        onClick = { onNavigateToTab(GameSecNavTab.TUGAS_SEKOLAH) }
      )
    }

    item {
      SectionNavCard(
        title = "Market & Chart Teknikal Pro",
        subtitle = "Candlestick OHLCV interaktif ala Purbaya Sadewa (MA 5/20, VOL 20, RSI, Stoch, MACD) & Download Analisis",
        tagLabel = "Chart Pro",
        icon = Icons.Default.ShowChart,
        testTag = "nav_card_market_pro",
        onClick = { onNavigateToTab(GameSecNavTab.MARKET) }
      )
    }

    item {
      SectionNavCard(
        title = "AI & Sains Hub",
        subtitle = "Neural Multi-modal Core, Voice Live, Audio & Video Gen, Entropi Sains & Cloud Sync",
        tagLabel = "Neural AI",
        icon = Icons.Default.AutoAwesome,
        testTag = "nav_card_ai_hub",
        onClick = { onNavigateToTab(GameSecNavTab.AI_HUB) }
      )
    }

    item {
      SectionNavCard(
        title = "Modul Belajar Keamanan Game",
        subtitle = "10 modul inti dari client-server, memori, jaringan, hingga berpikir kritis",
        tagLabel = "${progress.completedCount}/10 Selesai",
        icon = Icons.Default.MenuBook,
        testTag = "nav_card_modules",
        onClick = { onNavigateToTab(GameSecNavTab.MODUL) }
      )
    }

    item {
      SectionNavCard(
        title = "Security Lab",
        subtitle = "Simulasi validasi gerakan, cooldown, inventory, rate limit, hash, dan network",
        tagLabel = "7 Lab",
        icon = Icons.Default.Science,
        testTag = "nav_card_security_lab",
        onClick = { onNavigateToTab(GameSecNavTab.SECURITY_LAB) }
      )
    }

    item {
      SectionNavCard(
        title = "Terminal Simulasi",
        subtitle = "CLI lokal dengan parser perintah kalkulasi nyata dan dataset analisis log",
        tagLabel = "CLI Sim",
        icon = Icons.Default.Terminal,
        testTag = "nav_card_terminal",
        onClick = { onNavigateToTab(GameSecNavTab.TERMINAL) }
      )
    }

    item {
      SectionNavCard(
        title = "Investigasi & Claim Checker",
        subtitle = "5 kasus latihan forensik, timeline waktu, dan verifikasi klaim bukti independen",
        tagLabel = "Forensik",
        icon = Icons.Default.FactCheck,
        testTag = "nav_card_investigasi",
        onClick = { onNavigateToTab(GameSecNavTab.INVESTIGASI) }
      )
    }

    item {
      SectionNavCard(
        title = "Coding Academy",
        subtitle = "Jalur pembelajaran 6 level (Dasar, HTML/CSS, JS, Python, Backend, Lanjutan)",
        tagLabel = "Playground",
        icon = Icons.Default.Code,
        testTag = "nav_card_coding",
        onClick = { onNavigateToTab(GameSecNavTab.CODING) }
      )
    }

    item {
      SectionNavCard(
        title = "Market Intelligence & Tools",
        subtitle = "Data pasar crypto/saham terverifikasi, kalkulator PnL, DCA, position sizing, portfolio",
        tagLabel = "Market",
        icon = Icons.Default.ShowChart,
        testTag = "nav_card_market",
        onClick = { onNavigateToTab(GameSecNavTab.MARKET) }
      )
    }

    item {
      SectionNavCard(
        title = "Deteksi Anti-Cheat",
        subtitle = "Analisis pola perilaku, signature memory scanning, heuristik, dan integritas",
        tagLabel = "Deteksi",
        icon = Icons.Default.Build,
        testTag = "nav_card_deteksi",
        onClick = { onNavigateToTab(GameSecNavTab.DETEKSI) }
      )
    }

    item {
      SectionNavCard(
        title = "Sertifikasi Kuis",
        subtitle = "Evaluasi pemahaman konsep arsitektur dan metodologi validasi keamanan",
        tagLabel = "Sertifikasi",
        icon = Icons.Default.Whatshot,
        testTag = "nav_card_kuis",
        onClick = { onNavigateToTab(GameSecNavTab.KUIS) }
      )
    }

    item {
      SectionNavCard(
        title = "Profil & Verifikasi Akun",
        subtitle = "ID Token, status sertifikasi, audit trail, dan keamanan akun",
        tagLabel = "Akun",
        icon = Icons.Default.Person,
        testTag = "nav_card_profile",
        onClick = { onNavigateToTab(GameSecNavTab.PROFIL) }
      )
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun StatColumn(
  label: String,
  value: String,
  isHighlighted: Boolean
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Text(
      text = value,
      fontFamily = FontFamily.Monospace,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold,
      color = if (isHighlighted) AccentGreen else TextPrimary
    )
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = TextMuted,
      fontSize = 11.sp
    )
  }
}

@Composable
private fun SectionNavCard(
  title: String,
  subtitle: String,
  tagLabel: String,
  icon: ImageVector,
  testTag: String,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = BorderStroke(1.dp, DarkCardBorder),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Surface(
        shape = RoundedCornerShape(6.dp),
        color = DarkSurfaceVariant,
        modifier = Modifier.size(42.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentGreen,
            modifier = Modifier.size(22.dp)
          )
        }
      }

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
          )
          Surface(
            shape = RoundedCornerShape(3.dp),
            color = AccentBlue.copy(alpha = 0.15f)
          ) {
            Text(
              text = tagLabel,
              fontFamily = FontFamily.Monospace,
              fontSize = 9.5.sp,
              fontWeight = FontWeight.Bold,
              color = AccentBlue,
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
          }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
          fontSize = 12.sp,
          lineHeight = 17.sp
        )
      }

      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = "Buka",
        tint = TextMuted,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}
