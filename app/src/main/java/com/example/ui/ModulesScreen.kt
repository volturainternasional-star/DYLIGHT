package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameSecRepository
import com.example.data.LearningModule
import com.example.firebase.ModuleProgressManager
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ModulesScreen(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val progress by ModuleProgressManager.progressState.collectAsState()

  var selectedModuleIndex by rememberSaveable {
    mutableIntStateOf(ModuleProgressManager.targetModuleIndex.intValue)
  }

  LaunchedEffect(ModuleProgressManager.targetModuleIndex.intValue) {
    selectedModuleIndex = ModuleProgressManager.targetModuleIndex.intValue
  }

  val currentModule = GameSecRepository.modules.getOrElse(selectedModuleIndex) { GameSecRepository.modules[0] }
  val isCurrentCompleted = progress.isCompleted(selectedModuleIndex)
  val currentRecord = progress.getRecord(selectedModuleIndex)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("modules_screen")
  ) {
    // 1. Top Bar / Title & Progress Card
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Modul Belajar Keamanan Game",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
          )
          Text(
            text = "10 Materi sistematis arsitektur keamanan, client-server & anti-cheat",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
          )
        }

        IconButton(
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
          modifier = Modifier
            .size(38.dp)
            .background(DarkSurface, CircleShape)
            .testTag("btn_sync_modules")
        ) {
          if (progress.isSyncing) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              color = AccentGreen,
              strokeWidth = 2.dp
            )
          } else {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Sinkronkan Progress Cloud",
              tint = AccentGreen,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      // Progress Tracker Card with Linear Indicator
      Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (progress.completedCount > 0) AccentGreen.copy(alpha = 0.5f) else DarkCardBorder),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("module_progress_overview_card")
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "Progress Belajar: ${progress.completedCount} dari ${progress.totalModulesCount} Modul Selesai",
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = TextPrimary
              )
            }
            Text(
              text = "${progress.progressPercent}%",
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = if (progress.completedCount > 0) AccentGreen else TextMuted
            )
          }

          LinearProgressIndicator(
            progress = { progress.progressFraction },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = AccentGreen,
            trackColor = DarkSurfaceVariant
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = if (progress.isSyncing) Icons.Default.Sync else Icons.Default.CloudDone,
                contentDescription = null,
                tint = if (progress.isSyncing) AccentBlue else AccentGreen,
                modifier = Modifier.size(13.dp)
              )
              Text(
                text = progress.syncStatusMessage,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TextMuted
              )
            }

            Text(
              text = if (progress.completedCount == progress.totalModulesCount) "Semua Modul Tuntas!" else "${progress.totalModulesCount - progress.completedCount} modul tersisa",
              fontSize = 10.5.sp,
              color = if (progress.completedCount == progress.totalModulesCount) AccentGreen else TextSecondary
            )
          }
        }
      }
    }

    // 2. Horizontal Module Selector Chips (With ✓ indicator on completed modules)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      GameSecRepository.modules.forEachIndexed { index, module ->
        val isSelected = index == selectedModuleIndex
        val isCompleted = progress.isCompleted(index)

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = if (isSelected) AccentBlue else DarkSurface,
          border = BorderStroke(
            1.dp,
            if (isSelected) AccentBlue else if (isCompleted) AccentGreen.copy(alpha = 0.6f) else DarkCardBorder
          ),
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable {
              selectedModuleIndex = index
              ModuleProgressManager.targetModuleIndex.intValue = index
            }
            .testTag("module_tab_$index")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            if (isCompleted) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selesai",
                tint = if (isSelected) DarkBackground else AccentGreen,
                modifier = Modifier.size(14.dp)
              )
            }

            Text(
              text = module.numberLabel,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) DarkBackground else if (isCompleted) AccentGreen else TextPrimary
            )

            Text(
              text = module.title,
              style = MaterialTheme.typography.bodyMedium,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
              color = if (isSelected) DarkBackground else TextSecondary
            )
          }
        }
      }
    }

    HorizontalDivider(
      modifier = Modifier.padding(top = 6.dp),
      thickness = 1.dp,
      color = DarkCardBorder
    )

    // 3. Module Content Scroll
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .weight(1f)
        .testTag("module_content_list"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Module Header Card with Completion Toggle
      item {
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isCurrentCompleted) Color(0xFF10241A) else DarkSurface
          ),
          border = BorderStroke(
            1.dp,
            if (isCurrentCompleted) AccentGreen else DarkCardBorder
          ),
          modifier = Modifier.fillMaxWidth().testTag("module_header_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${currentModule.numberLabel} — ${currentModule.subtitle}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.5.sp,
                color = if (isCurrentCompleted) AccentGreen else AccentBlue,
                fontWeight = FontWeight.SemiBold
              )

              // Status Chip
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isCurrentCompleted) AccentGreen.copy(alpha = 0.2f) else DarkSurfaceVariant
              ) {
                Text(
                  text = if (isCurrentCompleted) "✓ SELESAI" else "BELUM SELESAI",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 9.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isCurrentCompleted) AccentGreen else TextMuted,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = currentModule.title,
              style = MaterialTheme.typography.titleLarge,
              color = TextPrimary
            )

            if (isCurrentCompleted && currentRecord != null && currentRecord.completedAt.isNotBlank()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Diselesaikan: ${currentRecord.completedAt} (Tersimpan di Cloud Firestore)",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = AccentGreen
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Completion Action Button
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              if (isCurrentCompleted) {
                OutlinedButton(
                  onClick = {
                    ModuleProgressManager.toggleModuleCompletion(context, selectedModuleIndex, currentModule)
                    Toast.makeText(context, "Modul ditandai belum selesai.", Toast.LENGTH_SHORT).show()
                  },
                  shape = RoundedCornerShape(6.dp),
                  border = BorderStroke(1.dp, DarkCardBorder),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("btn_toggle_module_completed")
                ) {
                  Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Batal Selesai", fontSize = 11.5.sp, color = TextMuted)
                }
              } else {
                Button(
                  onClick = {
                    ModuleProgressManager.toggleModuleCompletion(context, selectedModuleIndex, currentModule)
                    Toast.makeText(context, "Hebat! Modul berhasil diselesaikan & disimpan ke Firestore.", Toast.LENGTH_SHORT).show()
                  },
                  shape = RoundedCornerShape(6.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                  contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("btn_toggle_module_completed")
                ) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = DarkBackground,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Tandai Modul Selesai ✓",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBackground
                  )
                }
              }
            }
          }
        }
      }

      // If Module 1: Responsibilities Comparison Matrix
      if (currentModule.clientResponsibilities.isNotEmpty() && currentModule.serverResponsibilities.isNotEmpty()) {
        item {
          ClientServerMatrixCard(module = currentModule)
        }
      }

      // Optional Steps or Key Points List Card
      if (!currentModule.steps.isNullOrEmpty()) {
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
                text = "Poin & Tahapan Analisis",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
              )

              currentModule.steps.forEachIndexed { stepIdx, stepText ->
                Row(
                  verticalAlignment = Alignment.Top,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Surface(
                    shape = CircleShape,
                    color = AccentBlue.copy(alpha = 0.2f),
                    modifier = Modifier.size(22.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = "${stepIdx + 1}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue
                      )
                    }
                  }
                  Text(
                    text = stepText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                  )
                }
              }
            }
          }
        }
      }

      // Content Sections
      items(currentModule.sections) { section ->
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
            Text(
              text = section.heading,
              style = MaterialTheme.typography.titleMedium,
              color = TextPrimary
            )
            Text(
              text = section.body,
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
              lineHeight = 21.sp
            )
            if (section.bulletPoints.isNotEmpty()) {
              Spacer(modifier = Modifier.height(2.dp))
              section.bulletPoints.forEach { point ->
                Row(
                  modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text("•", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  Text(
                    text = point,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                  )
                }
              }
            }
            if (section.codeExample != null) {
              Spacer(modifier = Modifier.height(4.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(4.dp))
                  .background(DarkBackground)
                  .padding(12.dp)
              ) {
                Text(
                  text = section.codeExample,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.5.sp,
                  color = AccentGreen,
                  lineHeight = 16.sp
                )
              }
            }
          }
        }
      }

      // Key Takeaway Card
      item {
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
          border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Text(
              text = "KESIMPULAN INTI:",
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = AccentBlue,
              modifier = Modifier.padding(top = 2.dp)
            )
            Text(
              text = currentModule.keyTakeaway,
              style = MaterialTheme.typography.bodyMedium,
              color = TextPrimary,
              fontWeight = FontWeight.Medium,
              lineHeight = 20.sp,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      // Bottom Completion Action Card
      item {
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isCurrentCompleted) Color(0xFF10241A) else DarkSurface
          ),
          border = BorderStroke(
            1.dp,
            if (isCurrentCompleted) AccentGreen else AccentBlue.copy(alpha = 0.5f)
          ),
          modifier = Modifier.fillMaxWidth().testTag("module_bottom_completion_card")
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = if (isCurrentCompleted) Icons.Default.CheckCircle else Icons.Default.School,
                contentDescription = null,
                tint = if (isCurrentCompleted) AccentGreen else AccentBlue,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = if (isCurrentCompleted) "Modul Ini Selesai Dipelajari" else "Sudah Selesai Membaca Modul Ini?",
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = TextPrimary
              )
            }

            Text(
              text = if (isCurrentCompleted) {
                "Status tersimpan di Cloud Firestore. Anda dapat melanjutkan ke modul berikutnya."
              } else {
                "Tandai selesai untuk menyimpan progres ke Firestore dan memperbarui statistik belajar."
              },
              fontSize = 11.5.sp,
              color = TextSecondary
            )

            Button(
              onClick = {
                ModuleProgressManager.toggleModuleCompletion(context, selectedModuleIndex, currentModule)
                val msg = if (isCurrentCompleted) "Status modul dibatalkan." else "Modul tersimpan ke Firestore!"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(6.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isCurrentCompleted) DarkSurfaceVariant else AccentGreen
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = if (isCurrentCompleted) Icons.Default.CheckCircle else Icons.Default.Check,
                contentDescription = null,
                tint = if (isCurrentCompleted) AccentGreen else DarkBackground,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isCurrentCompleted) "✓ Modul Telah Selesai (Klik Batal)" else "Selesaikan Modul & Simpan ke Firestore",
                color = if (isCurrentCompleted) TextPrimary else DarkBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }
        }
      }

      // Navigation Buttons Between Modules
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (selectedModuleIndex > 0) {
            OutlinedButton(
              onClick = {
                selectedModuleIndex--
                ModuleProgressManager.targetModuleIndex.intValue = selectedModuleIndex
              },
              shape = RoundedCornerShape(6.dp),
              border = BorderStroke(1.dp, DarkCardBorder),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
              modifier = Modifier.testTag("btn_prev_module")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.padding(end = 6.dp)
              )
              Text("Modul ${selectedModuleIndex}")
            }
          } else {
            Spacer(modifier = Modifier.width(1.dp))
          }

          if (selectedModuleIndex < GameSecRepository.modules.lastIndex) {
            OutlinedButton(
              onClick = {
                selectedModuleIndex++
                ModuleProgressManager.targetModuleIndex.intValue = selectedModuleIndex
              },
              shape = RoundedCornerShape(6.dp),
              border = BorderStroke(1.dp, AccentBlue),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue),
              modifier = Modifier.testTag("btn_next_module")
            ) {
              Text("Modul ${selectedModuleIndex + 2}")
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.padding(start = 6.dp)
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun ClientServerMatrixCard(module: LearningModule) {
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
        text = "Matriks Tanggung Jawab: Client vs Server",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Client Side
        Column(
          modifier = Modifier
            .weight(1f)
            .background(DarkBackground, RoundedCornerShape(6.dp))
            .padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = "CLIENT SIDE",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentBlue
          )
          module.clientResponsibilities.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Text("•", color = AccentBlue, fontSize = 11.sp)
              Text(
                text = item,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 16.sp
              )
            }
          }
        }

        // Server Side
        Column(
          modifier = Modifier
            .weight(1f)
            .background(DarkBackground, RoundedCornerShape(6.dp))
            .padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = "SERVER SIDE",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGreen
          )
          module.serverResponsibilities.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Text("•", color = AccentGreen, fontSize = 11.sp)
              Text(
                text = item,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 16.sp
              )
            }
          }
        }
      }
    }
  }
}
