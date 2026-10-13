package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameSecRepository
import com.example.data.TerminalLog
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalMonoStyle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun TerminalScreen(
  modifier: Modifier = Modifier
) {
  val initialBanner = """
=============================================================
 GAMESEC LAB // SIMULATED SECURITY TERMINAL
 Sandbox v1.4 - Conceptual Simulation Engine
=============================================================
DISCLAIMER:
Terminal ini HANYA simulasi konseptual. Tidak terhubung ke
sistem nyata dan tidak dapat mengakses proses game lain.
Ketik 'help' untuk daftar perintah simulasi yang tersedia.
-------------------------------------------------------------
  """.trimIndent()

  val logs = remember {
    mutableStateListOf(
      TerminalLog(id = 1L, isInput = false, text = initialBanner)
    )
  }

  var inputText by rememberSaveable { mutableStateOf("") }
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  val quickCommands = listOf(
    "help",
    "architecture",
    "validate movement",
    "validate cooldown",
    "behavior score",
    "analyze logs",
    "hash demo",
    "network demo",
    "rate limit demo",
    "security checklist",
    "analyze claim",
    "status",
    "clear"
  )

  fun executeCommand(rawCmd: String) {
    val trimmed = rawCmd.trim()
    if (trimmed.isEmpty()) return

    if (trimmed.equals("clear", ignoreCase = true)) {
      logs.clear()
      inputText = ""
      return
    }

    logs.add(
      TerminalLog(
        id = System.currentTimeMillis(),
        isInput = true,
        text = "sec-lab@sandbox:~$ $trimmed"
      )
    )

    val output = GameSecRepository.executeTerminalCommand(trimmed)
    logs.add(
      TerminalLog(
        id = System.currentTimeMillis() + 1,
        isInput = false,
        text = output,
        isError = output.startsWith("Perintah")
      )
    )

    inputText = ""
    scope.launch {
      listState.animateScrollToItem(logs.size - 1)
    }
  }

  LaunchedEffect(logs.size) {
    if (logs.isNotEmpty()) {
      listState.scrollToItem(logs.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .navigationBarsPadding()
      .imePadding()
      .testTag("terminal_screen")
  ) {
    // Header & Disclaimer bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(
          text = "Terminal Simulasi",
          style = MaterialTheme.typography.titleLarge,
          color = TextPrimary
        )
        Text(
          text = "Simulasi logika validasi dan inspeksi data",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary
        )
      }

      IconButton(
        onClick = {
          logs.clear()
          logs.add(TerminalLog(id = System.currentTimeMillis(), isInput = false, text = initialBanner))
        },
        modifier = Modifier.testTag("terminal_reset_btn")
      ) {
        Icon(
          imageVector = Icons.Default.DeleteSweep,
          contentDescription = "Bersihkan terminal",
          tint = TextMuted
        )
      }
    }

    // Terminal Screen Window (Monospace, dark background, borders)
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = TerminalBg,
      border = BorderStroke(1.dp, DarkCardBorder),
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(horizontal = 16.dp)
        .testTag("terminal_viewport")
    ) {
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxSize()
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(logs, key = { it.id }) { log ->
          if (log.isInput) {
            Text(
              text = log.text,
              fontFamily = FontFamily.Monospace,
              fontSize = 12.5.sp,
              fontWeight = FontWeight.Bold,
              color = AccentBlue,
              lineHeight = 18.sp
            )
          } else {
            Text(
              text = log.text,
              style = TerminalMonoStyle,
              color = if (log.isError) MaterialTheme.colorScheme.error else ColorTextDim,
              lineHeight = 18.sp
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Quick Command Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      quickCommands.forEach { cmd ->
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = DarkSurfaceVariant,
          border = BorderStroke(1.dp, DarkCardBorder),
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { executeCommand(cmd) }
            .testTag("quick_cmd_$cmd")
        ) {
          Text(
            text = cmd,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.5.sp,
            color = AccentBlue,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Terminal Input Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = inputText,
        onValueChange = { inputText = it },
        placeholder = {
          Text(
            text = "Ketik perintah misal: validate movement",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = TextMuted
          )
        },
        leadingIcon = {
          Text(
            text = "$",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = AccentBlue,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 12.dp)
          )
        },
        singleLine = true,
        textStyle = TerminalMonoStyle.copy(color = TextPrimary),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DarkSurface,
          unfocusedContainerColor = DarkSurface,
          focusedBorderColor = AccentBlue,
          unfocusedBorderColor = DarkCardBorder,
          cursorColor = AccentBlue
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = {
          executeCommand(inputText)
        }),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("terminal_input_field")
      )

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = { executeCommand(inputText) },
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(AccentBlue)
          .testTag("terminal_send_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "Jalankan perintah",
          tint = DarkBackground
        )
      }
    }
  }
}

private val ColorTextDim = androidx.compose.ui.graphics.Color(0xFFCBD5E1)
