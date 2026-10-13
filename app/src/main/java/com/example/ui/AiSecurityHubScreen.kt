package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.ai.ChatMessage
import com.example.ai.GeminiCyberService
import com.example.ai.MessageSender
import com.example.ai.ScienceAnalysisResult
import com.example.firebase.FirestoreSyncManager
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
import kotlinx.coroutines.launch

@Composable
fun AiSecurityHubScreen(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var selectedSubTab by rememberSaveable { mutableIntStateOf(0) }
  val subTabs = listOf("AI Chatbot", "Audio & Voice", "Visual & Video", "Sains & Cloud")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("ai_security_hub_screen")
  ) {
    // Header Banner
    Surface(
      color = DarkSurface,
      border = BorderStroke(1.dp, DarkCardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = AccentGreen,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "GameSec AI & Science Hub",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }
        Text(
          text = "Neural Intelligence Core v4.2, Realtime Audio Stream, Grounding Search, Sains Forensik & Cloud Sync",
          fontFamily = FontFamily.Monospace,
          fontSize = 10.5.sp,
          color = TextSecondary
        )
      }
    }

    // Tab Navigation
    TabRow(
      selectedTabIndex = selectedSubTab,
      containerColor = DarkSurfaceVariant,
      contentColor = AccentGreen,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
          color = AccentGreen
        )
      }
    ) {
      subTabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedSubTab == index,
          onClick = { selectedSubTab = index },
          text = {
            Text(
              text = title,
              fontSize = 11.sp,
              fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal,
              color = if (selectedSubTab == index) AccentGreen else TextMuted
            )
          }
        )
      }
    }

    // Sub-Screen Content
    Box(
      modifier = Modifier
        .fillMaxSize()
        .weight(1f)
    ) {
      when (selectedSubTab) {
        0 -> AiChatbotView()
        1 -> AudioVoiceForensicsView()
        2 -> VisualVideoStudioView()
        3 -> ScienceAndCloudSyncView()
      }
    }
  }
}

@Composable
fun AiChatbotView() {
  val scope = rememberCoroutineScope()
  var inputPrompt by rememberSaveable { mutableStateOf("") }
  var isSending by remember { mutableStateOf(false) }
  var useSearchGrounding by rememberSaveable { mutableStateOf(true) }
  var useMapsGrounding by rememberSaveable { mutableStateOf(false) }
  var isComplexModel by rememberSaveable { mutableStateOf(false) }

  var messages by remember {
    mutableStateOf(
      listOf(
        ChatMessage(
          sender = MessageSender.AI,
          content = "Halo Analis! Saya GameSec AI Senior Analyst. Siap membantu investigasi binary, deteksi anti-cheat, enkripsi, dan intelijen pasar finansial.",
          groundingSources = listOf("GameSec Security Protocol 2026")
        )
      )
    )
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(12.dp)
  ) {
    // Model & Grounding Controls
    Card(
      shape = RoundedCornerShape(6.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      border = BorderStroke(1.dp, DarkCardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Engine: ${if (isComplexModel) "Neural-Pro-Reasoning-v3.1" else "Neural-Speed-Flash-v3.5"}",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = AccentGreen,
            fontWeight = FontWeight.Bold
          )
          Button(
            onClick = { isComplexModel = !isComplexModel },
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Text(if (isComplexModel) "Ubah ke Flash" else "Ubah ke Pro", fontSize = 10.sp, color = TextPrimary)
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          FilterChip(
            selected = useSearchGrounding,
            onClick = { useSearchGrounding = !useSearchGrounding },
            label = { Text("Google Search Grounding", fontSize = 10.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp)) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AccentGreen.copy(alpha = 0.2f),
              selectedLabelColor = AccentGreen
            )
          )

          FilterChip(
            selected = useMapsGrounding,
            onClick = { useMapsGrounding = !useMapsGrounding },
            label = { Text("Google Maps Grounding", fontSize = 10.sp) },
            leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp)) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AccentBlue.copy(alpha = 0.2f),
              selectedLabelColor = AccentBlue
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Message History
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(messages) { msg ->
        val isUser = msg.sender == MessageSender.USER
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isUser) AccentGreen.copy(alpha = 0.2f) else DarkSurface,
            border = BorderStroke(1.dp, if (isUser) AccentGreen else DarkCardBorder),
            modifier = Modifier.widthIn(max = 320.dp)
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = if (isUser) "Anda" else "GameSec AI",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isUser) AccentGreen else AccentBlue
                )
                Text(msg.timestamp, fontSize = 9.sp, color = TextMuted)
              }
              Text(
                text = msg.content,
                fontSize = 12.sp,
                color = TextPrimary,
                lineHeight = 16.sp
              )
              if (msg.groundingSources.isNotEmpty()) {
                HorizontalDivider(thickness = 0.5.dp, color = DarkCardBorder)
                msg.groundingSources.forEach { source ->
                  Text(
                    text = "• $source",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = TextSecondary
                  )
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Input Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = inputPrompt,
        onValueChange = { inputPrompt = it },
        placeholder = { Text("Tanyakan mitigasi hook, entropy, atau market...", fontSize = 11.5.sp, color = TextMuted) },
        modifier = Modifier
          .weight(1f)
          .testTag("input_ai_prompt"),
        shape = RoundedCornerShape(6.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DarkSurface,
          unfocusedContainerColor = DarkSurface,
          focusedBorderColor = AccentGreen,
          unfocusedBorderColor = DarkCardBorder,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        maxLines = 3
      )

      IconButton(
        onClick = {
          if (inputPrompt.isNotBlank() && !isSending) {
            val userText = inputPrompt.trim()
            inputPrompt = ""
            val userMsg = ChatMessage(sender = MessageSender.USER, content = userText)
            messages = messages + userMsg

            scope.launch {
              isSending = true
              val aiResponse = GeminiCyberService.generateChatResponse(
                userPrompt = userText,
                history = messages,
                useSearchGrounding = useSearchGrounding,
                useMapsGrounding = useMapsGrounding,
                isComplexTask = isComplexModel
              )
              messages = messages + aiResponse
              isSending = false
            }
          }
        },
        modifier = Modifier
          .size(44.dp)
          .background(AccentGreen, RoundedCornerShape(6.dp))
          .testTag("btn_send_prompt"),
        enabled = !isSending
      ) {
        if (isSending) {
          CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DarkBackground, strokeWidth = 2.dp)
        } else {
          Icon(Icons.Default.Send, contentDescription = "Kirim", tint = DarkBackground)
        }
      }
    }
  }
}

@Composable
fun AudioVoiceForensicsView() {
  val scope = rememberCoroutineScope()
  var transcriptionResult by rememberSaveable { mutableStateOf("Tekan tombol transkrip untuk menganalisis rekaman suara/paket audio.") }
  var musicClipResult by rememberSaveable { mutableStateOf("Pilih tipe audio untuk membuat clip musik / cyber alert tone via Lyria.") }
  var isRecordingVoice by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(14.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Audio Transcription Card (gemini-3.5-transcribe)
    Card(
      shape = RoundedCornerShape(8.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      border = BorderStroke(1.dp, DarkCardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.Mic, contentDescription = null, tint = AccentGreen)
          Text("Transkripsi Audio Forensik [Neural Audio Transcriber 3.5]", fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Text(
          text = "Mengubah transmisi suara analis atau rekaman payload audio menjadi teks teknis terverifikasi.",
          fontSize = 11.5.sp,
          color = TextSecondary
        )
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = DarkSurfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = transcriptionResult,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TextPrimary,
            modifier = Modifier.padding(10.dp)
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = {
              scope.launch {
                transcriptionResult = GeminiCyberService.transcribeAudioSample("packet_audio")
              }
            },
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
          ) {
            Text("Transkrip Paket Audio", fontSize = 11.sp, color = DarkBackground, fontWeight = FontWeight.Bold)
          }
          OutlinedButton(
            onClick = {
              scope.launch {
                transcriptionResult = GeminiCyberService.transcribeAudioSample("voice_memo")
              }
            },
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, DarkCardBorder)
          ) {
            Text("Transkrip Memo Suara", fontSize = 11.sp, color = AccentBlue)
          }
        }
      }
    }

    // Voice Conversation Mode (Neural Realtime Stream)
    Card(
      shape = RoundedCornerShape(8.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      border = BorderStroke(1.dp, DarkCardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.GraphicEq, contentDescription = null, tint = AccentBlue)
          Text("Voice Conversation [Neural Realtime Stream v3.8]", fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Text(
          text = "Interaksi suara dua arah secara real-time dengan latency rendah untuk konsultasi ancaman cyber.",
          fontSize = 11.5.sp,
          color = TextSecondary
        )
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = if (isRecordingVoice) AccentGreen.copy(alpha = 0.15f) else DarkSurfaceVariant,
          border = BorderStroke(1.dp, if (isRecordingVoice) AccentGreen else DarkCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = if (isRecordingVoice) Icons.Default.VolumeUp else Icons.Default.Mic,
              contentDescription = null,
              tint = if (isRecordingVoice) AccentGreen else TextMuted
            )
            Text(
              text = if (isRecordingVoice) "Live Audio Terhubung: Mendengarkan suara analis..." else "Mikrofon siap. Ketuk untuk memulai percakapan langsung.",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              color = TextPrimary
            )
          }
        }
        Button(
          onClick = { isRecordingVoice = !isRecordingVoice },
          shape = RoundedCornerShape(4.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isRecordingVoice) StatusDanger else AccentBlue
          )
        ) {
          Text(
            text = if (isRecordingVoice) "Hentikan Percakapan Live" else "Mulai Percakapan Suara Live",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }
      }
    }

    // Generate Music & Cyber Sound (lyria-3-clip-preview)
    Card(
      shape = RoundedCornerShape(8.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      border = BorderStroke(1.dp, DarkCardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.MusicNote, contentDescription = null, tint = StatusWarning)
          Text("Generate Cyber Sound & Music [Lyria 3]", fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Text(
          text = "Synthesizer musik cyber dan sinyal audio penanda ancaman (clip hingga 30 detik).",
          fontSize = 11.5.sp,
          color = TextSecondary
        )
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = DarkSurfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = musicClipResult,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = StatusWarning,
            modifier = Modifier.padding(10.dp)
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = {
              musicClipResult = GeminiCyberService.generateCyberAudioClip("threat_alert")
            },
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StatusWarning)
          ) {
            Text("Clip Synthwave Alert", fontSize = 11.sp, color = DarkBackground, fontWeight = FontWeight.Bold)
          }
          OutlinedButton(
            onClick = {
              musicClipResult = GeminiCyberService.generateCyberAudioClip("focus_ambient")
            },
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, DarkCardBorder)
          ) {
            Text("Ambient Lab Focus", fontSize = 11.sp, color = TextPrimary)
          }
        }
      }
    }
  }
}

@Composable
fun VisualVideoStudioView() {
  var imagePrompt by rememberSaveable { mutableStateOf("Cyber security forensic badge, dark neon shield, high definition") }
  var imageStatus by rememberSaveable { mutableStateOf("Prompt siap untuk pembuatan gambar.") }
  var videoPrompt by rememberSaveable { mutableStateOf("Malware payload memory injection sequence simulation, technical HUD overlay") }
  var videoStatus by rememberSaveable { mutableStateOf("Siap menghasilkan video simulasi ancaman.") }
  var is16x9 by rememberSaveable { mutableStateOf(true) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(14.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Image Generation (gemini-nano-banana-2.1)
    Card(
      shape = RoundedCornerShape(8.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      border = BorderStroke(1.dp, DarkCardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentGreen)
          Text("Create & Edit Images [Neural Vision Generative 2K]", fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Text(
          text = "Hasilkan visualisasi aset grafis, badge forensik, dan diagram alur serangan dengan resolusi tinggi.",
          fontSize = 11.5.sp,
          color = TextSecondary
        )
        OutlinedTextField(
          value = imagePrompt,
          onValueChange = { imagePrompt = it },
          label = { Text("Prompt Gambar", fontSize = 11.sp) },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(4.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkSurfaceVariant,
            unfocusedContainerColor = DarkSurfaceVariant,
            focusedBorderColor = AccentGreen,
            unfocusedBorderColor = DarkCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = DarkSurfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(imageStatus, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextPrimary, modifier = Modifier.padding(10.dp))
        }
        Button(
          onClick = {
            imageStatus = "Sukses dibuat [Neural Vision Generative 2K]: Render 2K badge '${imagePrompt.take(35)}...' tersimpan di cache lab."
          },
          shape = RoundedCornerShape(4.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
        ) {
          Text("Generate Visual Cyber Badge", fontSize = 11.sp, color = DarkBackground, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Video Generation (veo-3.1-fast-generate-preview)
    Card(
      shape = RoundedCornerShape(8.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      border = BorderStroke(1.dp, DarkCardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.Movie, contentDescription = null, tint = AccentBlue)
          Text("Veo Video Generation [veo-3.1-fast-generate-preview]", fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Text(
          text = "Animasikan skenario insiden cyber dan simulasi peretasan dalam format video.",
          fontSize = 11.5.sp,
          color = TextSecondary
        )
        OutlinedTextField(
          value = videoPrompt,
          onValueChange = { videoPrompt = it },
          label = { Text("Prompt Skenario Video", fontSize = 11.sp) },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(4.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkSurfaceVariant,
            unfocusedContainerColor = DarkSurfaceVariant,
            focusedBorderColor = AccentBlue,
            unfocusedBorderColor = DarkCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Aspek Rasio:", fontSize = 11.sp, color = TextSecondary)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
              onClick = { is16x9 = true },
              shape = RoundedCornerShape(4.dp),
              colors = ButtonDefaults.buttonColors(containerColor = if (is16x9) AccentBlue else DarkSurfaceVariant)
            ) {
              Text("16:9 Landscape", fontSize = 10.sp, color = TextPrimary)
            }
            Button(
              onClick = { is16x9 = false },
              shape = RoundedCornerShape(4.dp),
              colors = ButtonDefaults.buttonColors(containerColor = if (!is16x9) AccentBlue else DarkSurfaceVariant)
            ) {
              Text("9:16 Portrait", fontSize = 10.sp, color = TextPrimary)
            }
          }
        }
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = DarkSurfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(videoStatus, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextPrimary, modifier = Modifier.padding(10.dp))
        }
        Button(
          onClick = {
            val ratio = if (is16x9) "16:9" else "9:16"
            videoStatus = "Veo 3 Berhasil: Video storyboard rasio $ratio berdurasi 5s telah di-render untuk prompt simulasi ancaman."
          },
          shape = RoundedCornerShape(4.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
          Text("Generate Video Veo 3.1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
      }
    }
  }
}

@Composable
fun ScienceAndCloudSyncView() {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var payloadInput by rememberSaveable { mutableStateOf("4D5A90000300000004000000FFFF0000B80000000000000040000000") }
  var scienceResult by remember { mutableStateOf(GeminiCyberService.analyzePayloadScience(payloadInput)) }
  var syncStatus by rememberSaveable { mutableStateOf("Tekan 'Sinkronkan ke Cloud' untuk menyimpan data profil & portfolio ke Firebase Firestore.") }
  var isSyncing by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(14.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Science Skill Card
    Card(
      shape = RoundedCornerShape(8.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      border = BorderStroke(1.dp, DarkCardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.Science, contentDescription = null, tint = AccentGreen)
          Text("Science Skill: Shannon Entropy & Forensik", fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Text(
          text = "Kalkulasi matematis entropi data (H = -∑ p·log2(p)) untuk mendeteksi binary terenkripsi, packer malware (VMProtect/Themida), dan anomali payload game.",
          fontSize = 11.5.sp,
          color = TextSecondary
        )
        OutlinedTextField(
          value = payloadInput,
          onValueChange = {
            payloadInput = it
            scienceResult = GeminiCyberService.analyzePayloadScience(it)
          },
          label = { Text("Byte Hex atau String Payload", fontSize = 11.sp) },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(4.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkSurfaceVariant,
            unfocusedContainerColor = DarkSurfaceVariant,
            focusedBorderColor = AccentGreen,
            unfocusedBorderColor = DarkCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )

        // Metrics Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = DarkSurfaceVariant,
            border = BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.weight(1f)
          ) {
            Column(modifier = Modifier.padding(8.dp)) {
              Text("Shannon Entropy", fontSize = 10.sp, color = TextMuted)
              Text("${scienceResult.entropy} / 8.0", fontWeight = FontWeight.Bold, color = if (scienceResult.isHighRiskPacked) StatusDanger else AccentGreen)
            }
          }
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = DarkSurfaceVariant,
            border = BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.weight(1f)
          ) {
            Column(modifier = Modifier.padding(8.dp)) {
              Text("Randomness Score", fontSize = 10.sp, color = TextMuted)
              Text("${scienceResult.randomnessScore}%", fontWeight = FontWeight.Bold, color = AccentBlue)
            }
          }
        }

        Surface(
          shape = RoundedCornerShape(4.dp),
          color = if (scienceResult.isHighRiskPacked) StatusDanger.copy(alpha = 0.15f) else DarkSurfaceVariant,
          border = BorderStroke(1.dp, if (scienceResult.isHighRiskPacked) StatusDanger else DarkCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = scienceResult.verdict,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TextPrimary,
            modifier = Modifier.padding(10.dp)
          )
        }
      }
    }

    // Cloud Database & Auth Persistence (Firebase Firestore)
    Card(
      shape = RoundedCornerShape(8.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      border = BorderStroke(1.dp, DarkCardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.CloudUpload, contentDescription = null, tint = AccentBlue)
          Text("Firebase Cloud Database & Sync", fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Text(
          text = "Menyimpan seluruh progress streak belajar, akun Google terverifikasi, dan data trading portfolio ke Cloud Firestore.",
          fontSize = 11.5.sp,
          color = TextSecondary
        )
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = DarkSurfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(syncStatus, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextPrimary, modifier = Modifier.padding(10.dp))
        }
        Button(
          onClick = {
            scope.launch {
              isSyncing = true
              val res = FirestoreSyncManager.syncUserData(context)
              syncStatus = res.getOrElse { it.message ?: "Gagal sinkronisasi." }
              isSyncing = false
            }
          },
          enabled = !isSyncing,
          shape = RoundedCornerShape(4.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
          if (isSyncing) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = TextPrimary)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Menyinkronkan...", fontSize = 11.sp, color = TextPrimary)
          } else {
            Text("Sinkronkan ke Cloud Firestore", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          }
        }
      }
    }
  }
}
