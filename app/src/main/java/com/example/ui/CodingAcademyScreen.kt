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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CodingExercise
import com.example.data.GameSecRepository
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalMonoStyle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import com.example.coding.RealCodeRunner
import com.example.coding.ExecutionResult
import com.example.ui.components.CyberRadarScanner
import androidx.compose.foundation.layout.size

@Composable
fun CodingAcademyScreen(
  modifier: Modifier = Modifier
) {
  val exercises = GameSecRepository.codingExercises
  var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
  val currentExercise = exercises[selectedIndex]

  var userCode by rememberSaveable(selectedIndex) { mutableStateOf(currentExercise.initialCode) }
  var consoleOutput by rememberSaveable(selectedIndex) { mutableStateOf("") }
  var isTestPassed by rememberSaveable(selectedIndex) { mutableStateOf<Boolean?>(null) }
  var showHint by rememberSaveable(selectedIndex) { mutableStateOf(false) }
  var lastExecMs by rememberSaveable(selectedIndex) { mutableStateOf<Long?>(null) }

  val clipboardManager = LocalClipboardManager.current

  // Real Executable JS & Python Runner
  fun executeCode() {
    val lang = if (currentExercise.levelNumber == 4) "python" else "javascript"
    val res = RealCodeRunner.runCode(userCode, lang)
    lastExecMs = res.executionTimeMs

    if (res.isSuccess) {
      val out = if (res.stdout.isNotBlank()) res.stdout else "[Program dieksekusi sukses tanpa output stdout. Waktu: ${res.executionTimeMs}ms]"
      consoleOutput = out
      val expected = currentExercise.expectedOutput.trim()
      isTestPassed = out.trim() == expected || out.trim().contains(expected)
    } else {
      consoleOutput = res.error ?: "Runtime execution error."
      isTestPassed = false
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("coding_academy_screen")
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Coding Academy",
          style = MaterialTheme.typography.titleLarge,
          color = TextPrimary
        )
        Text(
          text = "Latihan koding terisolasi, sandboxed parser, dan tes otomatis",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary
        )
      }
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        CyberRadarScanner(modifier = Modifier.size(20.dp))
        Surface(
          shape = RoundedCornerShape(3.dp),
          color = AccentGreen
        ) {
          Text(
            text = "EVAL ACTIVE",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBackground,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
          )
        }
      }
    }

    // Level selector tabs (6 Levels)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      exercises.forEachIndexed { idx, ex ->
        val isSelected = idx == selectedIndex
        FilterChip(
          selected = isSelected,
          onClick = { selectedIndex = idx },
          label = {
            Text(
              text = "L${ex.levelNumber}: ${ex.levelName}",
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
          modifier = Modifier.testTag("chip_level_${ex.levelNumber}")
        )
      }
    }

    // Scrollable Main Section
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = 8.dp),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Exercise Description Card
      item {
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
              Text(
                text = currentExercise.title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
              )
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = DarkSurfaceVariant
              ) {
                Text(
                  text = "LEVEL ${currentExercise.levelNumber}",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.5.sp,
                  color = AccentGreen,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Text(
              text = currentExercise.description,
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
              lineHeight = 21.sp
            )

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "Output yang Diharapkan:",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TextMuted
              )
              Surface(
                shape = RoundedCornerShape(3.dp),
                color = DarkBackground
              ) {
                Text(
                  text = currentExercise.expectedOutput,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }

      // Code Editor Card with Line Numbers & Monospace
      item {
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = TerminalBg),
          border = BorderStroke(1.dp, DarkCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            // Editor Toolbar
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "EDITOR KODE // SANDBOX",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGreen
              )

              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                  onClick = { userCode = currentExercise.initialCode },
                  shape = RoundedCornerShape(4.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                  border = BorderStroke(1.dp, DarkCardBorder)
                ) {
                  Icon(imageVector = Icons.Default.RestartAlt, contentDescription = "Reset", modifier = Modifier.height(14.dp))
                  Text("Reset", fontSize = 11.sp, color = TextSecondary)
                }

                OutlinedButton(
                  onClick = { clipboardManager.setText(AnnotatedString(userCode)) },
                  shape = RoundedCornerShape(4.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                  border = BorderStroke(1.dp, DarkCardBorder)
                ) {
                  Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.height(14.dp))
                  Text("Salin", fontSize = 11.sp, color = TextSecondary)
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Code Text Field
            OutlinedTextField(
              value = userCode,
              onValueChange = { userCode = it },
              textStyle = TerminalMonoStyle.copy(color = TextPrimary, fontSize = 12.5.sp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkBackground,
                unfocusedContainerColor = DarkBackground,
                focusedBorderColor = AccentGreen,
                unfocusedBorderColor = DarkCardBorder,
                cursorColor = AccentGreen
              ),
              shape = RoundedCornerShape(4.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("code_editor_field")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Run & Hint Actions Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                  onClick = { showHint = !showHint },
                  shape = RoundedCornerShape(4.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  border = BorderStroke(1.dp, DarkCardBorder)
                ) {
                  Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "Hint", tint = AccentBlue, modifier = Modifier.height(16.dp))
                  Text("Petunjuk", fontSize = 11.5.sp, color = AccentBlue)
                }

                OutlinedButton(
                  onClick = { userCode = currentExercise.solutionCode },
                  shape = RoundedCornerShape(4.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  border = BorderStroke(1.dp, DarkCardBorder)
                ) {
                  Icon(imageVector = Icons.Default.Visibility, contentDescription = "Solution", tint = TextMuted, modifier = Modifier.height(16.dp))
                  Text("Solusi", fontSize = 11.5.sp, color = TextMuted)
                }
              }

              Button(
                onClick = { executeCode() },
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                modifier = Modifier.testTag("btn_run_code")
              ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", tint = DarkBackground, modifier = Modifier.height(18.dp))
                Text("Jalankan (Run)", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
              }
            }

            if (showHint) {
              Spacer(modifier = Modifier.height(8.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(4.dp))
                  .background(DarkSurfaceVariant)
                  .padding(10.dp)
              ) {
                Text(
                  text = "PETUNJUK: " + currentExercise.hint,
                  style = MaterialTheme.typography.bodyMedium,
                  fontSize = 11.5.sp,
                  color = AccentBlue
                )
              }
            }
          }
        }
      }

      // Output Console Window
      item {
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          border = BorderStroke(1.dp, DarkCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Console Output & Tes Otomatis",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
              )

              if (isTestPassed != null) {
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = if (isTestPassed == true) StatusSuccess.copy(alpha = 0.2f) else StatusDanger.copy(alpha = 0.2f),
                  border = BorderStroke(1.dp, if (isTestPassed == true) StatusSuccess else StatusDanger)
                ) {
                  Text(
                    text = if (isTestPassed == true) "TES LULUS (PASSED)" else "TES GAGAL (FAILED)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTestPassed == true) StatusSuccess else StatusDanger,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(TerminalBg)
                .padding(12.dp)
            ) {
              Text(
                text = if (consoleOutput.isBlank())
                  "> Program belum dijalankan. Klik 'Jalankan (Run)' untuk melihat output."
                else
                  "> $consoleOutput",
                style = TerminalMonoStyle,
                color = if (isTestPassed == false) StatusDanger else TextPrimary
              )
            }
          }
        }
      }
    }
  }
}
