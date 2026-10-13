package com.example.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameSecRepository
import com.example.data.ToolCategory
import com.example.data.ToolConfig
import com.example.tools.MenuArchitectureInspection
import com.example.tools.RealConnectionResult
import com.example.tools.RealToolConnector
import com.example.tools.repository.SecurityToolQueryType
import com.example.tools.repository.SecurityToolRepository
import com.example.ui.components.CyberMatrixRain
import com.example.ui.components.CyberRadarScanner
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

enum class ToolsHubSubView {
  TOOLS_INTEGRATION,
  BEDAH_MENU
}

@Composable
fun ToolsHubScreen(
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  val toolList = remember { mutableStateListOf<ToolConfig>().apply { addAll(GameSecRepository.initialToolConfigs) } }
  var subView by rememberSaveable { mutableStateOf(ToolsHubSubView.TOOLS_INTEGRATION) }
  var selectedCategoryFilter by rememberSaveable { mutableStateOf<ToolCategory?>(null) }
  var showAddDialog by rememberSaveable { mutableStateOf(false) }

  // Ping test result cache for tools
  val pingResults = remember { mutableStateMapOf<String, RealConnectionResult>() }
  val isPingingMap = remember { mutableStateMapOf<String, Boolean>() }

  // SecurityToolRepository & Retrofit API fetch states
  val securityToolRepository = remember { SecurityToolRepository() }
  var selectedQueryType by rememberSaveable { mutableStateOf(SecurityToolQueryType.ALL) }
  var isFetchingApi by rememberSaveable { mutableStateOf(false) }
  var apiFetchResultSummary by rememberSaveable { mutableStateOf<String?>(null) }
  var apiFetchLatency by rememberSaveable { mutableStateOf<Long?>(null) }
  var apiFetchIsLive by rememberSaveable { mutableStateOf(false) }
  var apiFetchError by rememberSaveable { mutableStateOf<String?>(null) }

  // Form states for new tool
  var newName by remember { mutableStateOf("") }
  var newDescription by remember { mutableStateOf("") }
  var newBaseUrl by remember { mutableStateOf("") }
  var newCategory by remember { mutableStateOf(ToolCategory.SECURITY) }

  val bedahMenuList = remember { RealToolConnector.getBedahMenuDiagnostics() }

  val filteredTools = if (selectedCategoryFilter == null) {
    toolList
  } else {
    toolList.filter { it.category == selectedCategoryFilter }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("tools_hub_screen")
  ) {
    // Cyber Matrix Rain Hero Banner
    Card(
      shape = RoundedCornerShape(0.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      border = BorderStroke(0.dp, Color.Transparent),
      modifier = Modifier
        .fillMaxWidth()
        .height(130.dp)
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        CyberMatrixRain(
          modifier = Modifier.fillMaxSize(),
          streamCount = 24,
          primaryColor = AccentGreen,
          alpha = 0.45f
        )

        Column(
          modifier = Modifier
            .fillMaxSize()
            .background(
              androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(
                  Color.Transparent,
                  DarkSurface.copy(alpha = 0.85f),
                  DarkSurface
                )
              )
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(3.dp),
              color = AccentGreen
            ) {
              Text(
                text = "CYBER TOOLS HUB // LIVE CONNECTOR",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBackground,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              CyberRadarScanner(modifier = Modifier.size(18.dp))
              Text("SSRF SHIELD: ACTIVE", fontFamily = FontFamily.Monospace, fontSize = 9.5.sp, color = AccentGreen)
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            Column {
              Text("Tools Hub & Bedah Menu", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
              Text("Konektivitas live API eksternal & dekonstruksi arsitektur aplikasi", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }

            if (subView == ToolsHubSubView.TOOLS_INTEGRATION) {
              Button(
                onClick = { showAddDialog = !showAddDialog },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                modifier = Modifier.testTag("btn_add_tool")
              ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Tambah", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
              }
            }
          }
        }
      }
    }

    // Sub-view Toggle: "Tools & Integrasi" vs "Bedah Menu Sistem"
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(DarkSurfaceVariant)
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (subView == ToolsHubSubView.TOOLS_INTEGRATION) AccentGreen else Color.Transparent,
        border = BorderStroke(1.dp, if (subView == ToolsHubSubView.TOOLS_INTEGRATION) AccentGreen else DarkCardBorder),
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(4.dp))
          .clickable { subView = ToolsHubSubView.TOOLS_INTEGRATION }
          .testTag("tab_tools_integration")
      ) {
        Row(
          modifier = Modifier.padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Hub,
            contentDescription = null,
            tint = if (subView == ToolsHubSubView.TOOLS_INTEGRATION) DarkBackground else TextMuted,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "Integrasi Tools & API",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (subView == ToolsHubSubView.TOOLS_INTEGRATION) DarkBackground else TextPrimary
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (subView == ToolsHubSubView.BEDAH_MENU) AccentGreen else Color.Transparent,
        border = BorderStroke(1.dp, if (subView == ToolsHubSubView.BEDAH_MENU) AccentGreen else DarkCardBorder),
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(4.dp))
          .clickable { subView = ToolsHubSubView.BEDAH_MENU }
          .testTag("tab_bedah_menu")
      ) {
        Row(
          modifier = Modifier.padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Layers,
            contentDescription = null,
            tint = if (subView == ToolsHubSubView.BEDAH_MENU) DarkBackground else TextMuted,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "Bedah Menu Sistem",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (subView == ToolsHubSubView.BEDAH_MENU) DarkBackground else TextPrimary
          )
        }
      }
    }

    if (subView == ToolsHubSubView.TOOLS_INTEGRATION) {
      // Category Filter Chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        FilterChip(
          selected = selectedCategoryFilter == null,
          onClick = { selectedCategoryFilter = null },
          label = { Text("Semua Kategori", fontSize = 11.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AccentGreen,
            selectedLabelColor = DarkBackground
          )
        )

        ToolCategory.entries.forEach { cat ->
          val isSelected = selectedCategoryFilter == cat
          FilterChip(
            selected = isSelected,
            onClick = { selectedCategoryFilter = cat },
            label = { Text(cat.label, fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AccentGreen,
              selectedLabelColor = DarkBackground
            )
          )
        }
      }

      // Tools List
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("tools_list_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Retrofit Cyber Security Tools API Integration Card
        item {
          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.6f)),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("card_retrofit_cyber_api")
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(16.dp)
                  )
                  Text(
                    text = "RETROFIT CYBER API // REPO SYNC",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen
                  )
                }

                Surface(
                  shape = RoundedCornerShape(3.dp),
                  color = if (apiFetchIsLive) StatusSuccess.copy(alpha = 0.2f) else DarkSurfaceVariant
                ) {
                  Text(
                    text = if (apiFetchIsLive) "LIVE HTTP 200" else "CATALOG READY",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = if (apiFetchIsLive) StatusSuccess else AccentBlue,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                  )
                }
              }

              Text(
                text = "Tarik Metadata Security Tools Real-World",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
              )

              Text(
                text = "Ambil metadata tools keamanan siber nyata (hash checkers, network sniffers, dynamic binary analyzer) via Retrofit service untuk melengkapi katalog Tools Hub.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
              )

              // Query Type Selector Pills
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                SecurityToolQueryType.entries.forEach { queryType ->
                  val isSelected = selectedQueryType == queryType
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isSelected) AccentGreen.copy(alpha = 0.25f) else DarkSurfaceVariant,
                    border = BorderStroke(1.dp, if (isSelected) AccentGreen else DarkCardBorder),
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .clickable { selectedQueryType = queryType }
                  ) {
                    Text(
                      text = queryType.displayName,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 10.sp,
                      color = if (isSelected) AccentGreen else TextMuted,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                  }
                }
              }

              // Fetch Action Button & Stats
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Button(
                  onClick = {
                    scope.launch {
                      isFetchingApi = true
                      val result = securityToolRepository.fetchSecurityToolsFromApi(selectedQueryType)
                      apiFetchResultSummary = result.sourceSummary
                      apiFetchLatency = result.latencyMs
                      apiFetchIsLive = result.isLiveFromNetwork
                      apiFetchError = result.errorMessage

                      // Populate tools into toolList
                      result.tools.forEach { newTool ->
                        val existingIndex = toolList.indexOfFirst { it.id == newTool.id }
                        if (existingIndex != -1) {
                          toolList[existingIndex] = newTool
                        } else {
                          toolList.add(newTool)
                        }
                      }

                      isFetchingApi = false
                    }
                  },
                  enabled = !isFetchingApi,
                  colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                  shape = RoundedCornerShape(4.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("btn_fetch_cyber_api")
                ) {
                  Icon(
                    imageVector = if (isFetchingApi) Icons.Default.Sync else Icons.Default.Hub,
                    contentDescription = null,
                    tint = DarkBackground,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (isFetchingApi) "Memuat API..." else "Tarik Metadata Tools (Retrofit)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBackground
                  )
                }

                Text(
                  text = "${toolList.count { it.id.startsWith("EXT-") }} Tools Eksternal",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.sp,
                  color = AccentBlue
                )
              }

              // Status Summary Banner
              apiFetchResultSummary?.let { summary ->
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = DarkSurfaceVariant,
                  border = BorderStroke(1.dp, if (apiFetchIsLive) StatusSuccess.copy(alpha = 0.5f) else DarkCardBorder),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(
                        text = if (apiFetchIsLive) "RESPON RETROFIT: OK" else "KATALOG SINKRON: OK",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (apiFetchIsLive) StatusSuccess else AccentBlue
                      )
                      apiFetchLatency?.let { lat ->
                        Text(
                          text = "Latensi: $lat ms",
                          fontFamily = FontFamily.Monospace,
                          fontSize = 9.5.sp,
                          color = TextMuted
                        )
                      }
                    }
                    Text(
                      text = summary,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 10.sp,
                      color = TextPrimary
                    )
                  }
                }
              }
            }
          }
        }

        // Add Tool Dialog
        if (showAddDialog) {
          item {
            Card(
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = DarkSurface),
              border = BorderStroke(1.dp, AccentGreen),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Daftarkan Tool / Endpoint Baru", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text("Masukkan URL API sungguhan (misal: https://httpbin.org/get atau endpoint server Anda)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

                OutlinedTextField(
                  value = newName,
                  onValueChange = { newName = it },
                  label = { Text("Nama Tool") },
                  singleLine = true,
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                  ),
                  modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                  value = newBaseUrl,
                  onValueChange = { newBaseUrl = it },
                  label = { Text("Endpoint URL (Contoh: https://api.github.com)") },
                  singleLine = true,
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                  ),
                  modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                  value = newDescription,
                  onValueChange = { newDescription = it },
                  label = { Text("Deskripsi Fungsi") },
                  singleLine = true,
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                  ),
                  modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Button(
                    onClick = {
                      if (newName.isNotBlank() && newBaseUrl.isNotBlank()) {
                        toolList.add(
                          ToolConfig(
                            id = "TOOL-${System.currentTimeMillis() % 10000}",
                            name = newName,
                            description = newDescription.ifBlank { "Tool kustom pengguna" },
                            category = newCategory,
                            baseUrl = newBaseUrl,
                            authType = "API Key / Public",
                            allowedEndpoints = listOf("/api/v1"),
                            enabled = true,
                            lastHealthCheck = "Belum diuji",
                            isOnline = true
                          )
                        )
                        showAddDialog = false
                        newName = ""
                        newBaseUrl = ""
                        newDescription = ""
                      }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                  ) {
                    Text("Simpan Konfigurasi", color = DarkBackground, fontWeight = FontWeight.Bold)
                  }

                  OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Batal", color = TextSecondary)
                  }
                }
              }
            }
          }
        }

        items(filteredTools) { tool ->
          val isPinging = isPingingMap[tool.id] == true
          val pingResult = pingResults[tool.id]

          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, if (tool.enabled) DarkCardBorder else DarkCardBorder.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = DarkSurfaceVariant
                  ) {
                    Text(
                      text = tool.category.label,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 10.5.sp,
                      color = AccentGreen,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                  if (tool.id.startsWith("EXT-")) {
                    Surface(
                      shape = RoundedCornerShape(4.dp),
                      color = AccentBlue.copy(alpha = 0.2f),
                      border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.4f))
                    ) {
                      Text(
                        text = "EXTERNAL API",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                      )
                    }
                  }
                  Text(
                    text = tool.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (tool.enabled) TextPrimary else TextMuted
                  )
                }

                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = if (tool.isOnline && tool.enabled) StatusSuccess.copy(alpha = 0.2f) else StatusDanger.copy(alpha = 0.2f)
                ) {
                  Text(
                    text = if (!tool.enabled) "DISABLED" else if (tool.isOnline) "READY" else "OFFLINE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (tool.enabled && tool.isOnline) StatusSuccess else TextMuted,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              Text(
                text = tool.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
              )

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(4.dp))
                  .background(DarkSurfaceVariant)
                  .padding(8.dp)
              ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                  Text("Endpoint: ${tool.baseUrl}", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = TextSecondary)
                  Text("Auth: ${tool.authType} • Status: ${tool.lastHealthCheck}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextMuted)
                }
              }

              // Live ping result card if available
              pingResult?.let { res ->
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = if (res.isReachable) StatusSuccess.copy(alpha = 0.12f) else StatusDanger.copy(alpha = 0.12f),
                  border = BorderStroke(1.dp, if (res.isReachable) StatusSuccess.copy(alpha = 0.4f) else StatusDanger.copy(alpha = 0.4f)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(
                        text = if (res.isReachable) "KONEKSI BERHASIL (HTTP ${res.statusCode})" else "KONEKSI GAGAL (${res.statusCode})",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (res.isReachable) StatusSuccess else StatusDanger
                      )
                      Text(
                        text = "RTT: ${res.latencyMs} ms",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = AccentBlue
                      )
                    }
                    Text(text = res.message, fontSize = 11.sp, color = TextPrimary)
                    if (res.responsePreview.isNotBlank()) {
                      Text(
                        text = "Preview: ${res.responsePreview}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        color = TextMuted,
                        maxLines = 2
                      )
                    }
                  }
                }
              }

              // Action Buttons: Real Live Test & Toggle
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = {
                    scope.launch {
                      isPingingMap[tool.id] = true
                      val result = RealToolConnector.testLiveConnection(tool.baseUrl)
                      pingResults[tool.id] = result
                      isPingingMap[tool.id] = false

                      // Update item state
                      val idx = toolList.indexOfFirst { it.id == tool.id }
                      if (idx != -1) {
                        toolList[idx] = toolList[idx].copy(
                          isOnline = result.isReachable,
                          lastHealthCheck = "HTTP ${result.statusCode} (${result.latencyMs}ms)"
                        )
                      }
                    }
                  },
                  shape = RoundedCornerShape(4.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                  border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.8f)),
                  enabled = !isPinging
                ) {
                  Icon(
                    imageVector = if (isPinging) Icons.Default.Sync else Icons.Default.ElectricalServices,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (isPinging) "Menguji Live..." else "Uji Koneksi Sungguhan",
                    fontSize = 11.sp,
                    color = AccentGreen,
                    fontWeight = FontWeight.Bold
                  )
                }

                OutlinedButton(
                  onClick = {
                    val idx = toolList.indexOfFirst { it.id == tool.id }
                    if (idx != -1) {
                      toolList[idx] = toolList[idx].copy(enabled = !tool.enabled)
                    }
                  },
                  shape = RoundedCornerShape(4.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                  border = BorderStroke(1.dp, DarkCardBorder)
                ) {
                  Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = null,
                    tint = if (tool.enabled) TextMuted else AccentGreen,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(if (tool.enabled) "Nonaktifkan" else "Aktifkan", fontSize = 11.sp, color = TextSecondary)
                }
              }
            }
          }
        }
      }
    } else {
      // BEDAH MENU SISTEM VIEW
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("bedah_menu_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        item {
          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(imageVector = Icons.Default.Layers, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                Text("Bedah Arsitektur & Perimeter Menu", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
              }
              Text(
                text = "Audit sistem menyeluruh membongkar fungsi, perimeter keamanan, dan tumpukan teknologi dari setiap menu di GameSec Lab.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
              )
            }
          }
        }

        items(bedahMenuList) { item ->
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
                Text(text = item.menuName, style = MaterialTheme.typography.titleMedium, color = AccentGreen)
                Surface(
                  shape = RoundedCornerShape(3.dp),
                  color = StatusSuccess.copy(alpha = 0.2f)
                ) {
                  Text(
                    text = item.activeStatus,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusSuccess,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              Text(text = item.functionalPurpose, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)

              HorizontalDivider(thickness = 0.5.dp, color = DarkCardBorder)

              Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text("Perimeter Keamanan: ", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = TextMuted)
                  Text(item.securityBoundary, fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = AccentBlue)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text("Stack Teknis: ", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = TextMuted)
                  Text(item.technicalStack, fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text("Route ID: ", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = TextMuted)
                  Text(item.routeId, fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = AccentGreen)
                }
              }
            }
          }
        }
      }
    }
  }
}
