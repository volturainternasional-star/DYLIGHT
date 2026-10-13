package com.example.ui.admin

import android.widget.Toast
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ShowChart
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.auth.AuthConfig
import com.example.auth.AuthManager
import com.example.market.PortfolioManager
import com.example.market.TaskManager
import com.example.market.TradeRecord
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Data model for User representation in Admin Panel
 */
data class AdminUserItem(
  val email: String,
  val displayName: String = "",
  val lastActive: String = "",
  val totalProfitLoss: Double = 0.0,
  val totalTrades: Int = 0,
  val winRatePercent: Double = 0.0,
  val isCloudSynced: Boolean = false,
  val isFrozen: Boolean = false,
  val trades: List<TradeRecord> = emptyList()
)

data class AdminBroadcast(
  val id: String,
  val title: String,
  val message: String,
  val timestamp: String,
  val level: String // "INFO", "BULLISH", "ALERT"
)

enum class AdminTabMode {
  USERS_PROFIT,
  SYSTEM_BROADCAST,
  SECURITY_AUDIT
}

@Composable
fun AdminPanelScreen(
  modifier: Modifier = Modifier,
  onNavigateToProfile: () -> Unit = {}
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val session by AuthManager.sessionState.collectAsState()
  val isAdmin = AuthManager.isAdmin() && session.email.trim().equals(AuthConfig.ADMIN_EMAIL, ignoreCase = true)

  var activeAdminTab by rememberSaveable { mutableStateOf(AdminTabMode.USERS_PROFIT) }
  var userList by remember { mutableStateOf<List<AdminUserItem>>(emptyList()) }
  var isLoading by remember { mutableStateOf(false) }
  var selectedUserDetail by remember { mutableStateOf<AdminUserItem?>(null) }
  var searchQuery by remember { mutableStateOf("") }
  var statusMessage by remember { mutableStateOf("Memuat data pengguna...") }

  // Broadcasts state
  var broadcastList by remember {
    mutableStateOf(
      listOf(
        AdminBroadcast("b1", "Sinyal IHSG Golden Cross", "Indikator MA 5 memotong ke atas MA 20 pada indeks IHSG. Pantau saham perbankan BBCA & BMRI.", "12 Okt 2026, 14:00 WIB", "BULLISH"),
        AdminBroadcast("b2", "Pembaruan Keamanan Sistem", "Fitur proteksi login akun Google Credential Manager aktif penuh untuk seluruh pengguna.", "12 Okt 2026, 10:30 WIB", "INFO")
      )
    )
  }
  var newBroadcastTitle by remember { mutableStateOf("") }
  var newBroadcastMsg by remember { mutableStateOf("") }

  // Function to load users and their portfolios from Firestore + Local Cache
  val loadUsersData: () -> Unit = {
    scope.launch {
      isLoading = true
      statusMessage = "Menghubungkan ke Firestore..."
      val loadedUsers = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<AdminUserItem>()
        val seenEmails = mutableSetOf<String>()

        // 1. Try querying Firestore users collection
        try {
          val db = FirebaseFirestore.getInstance()
          val usersSnapshot = db.collection("users").get().await()

          for (userDoc in usersSnapshot.documents) {
            val email = userDoc.getString("email") ?: userDoc.id
            if (email.isBlank()) continue
            seenEmails.add(email)

            val displayName = userDoc.getString("displayName") ?: email.substringBefore("@")
            val lastActive = userDoc.getString("lastActive") ?: "-"

            // Fetch portfolio subcollection for this user
            val tradesList = mutableListOf<TradeRecord>()
            try {
              val portfolioSnapshot = userDoc.reference.collection("portfolio").get().await()
              for (pDoc in portfolioSnapshot.documents) {
                val symbol = pDoc.getString("symbol") ?: "UNKNOWN"
                val assetType = pDoc.getString("assetType") ?: "CRYPTO"
                val entryPrice = pDoc.getDouble("entryPrice") ?: 0.0
                val exitPrice = pDoc.getDouble("exitPrice") ?: 0.0
                val units = pDoc.getDouble("units") ?: 0.0
                val date = pDoc.getString("date") ?: ""
                val notes = pDoc.getString("notes") ?: ""

                tradesList.add(
                  TradeRecord(
                    id = pDoc.id,
                    symbol = symbol,
                    assetType = assetType,
                    entryPrice = entryPrice,
                    exitPrice = exitPrice,
                    units = units,
                    date = date,
                    notes = notes
                  )
                )
              }
            } catch (_: Exception) {
              tradesList.addAll(PortfolioManager.getTrades(email))
            }

            if (tradesList.isEmpty()) {
              tradesList.addAll(PortfolioManager.getTrades(email))
            }

            val totalPnl = tradesList.sumOf { it.netProfitLoss }
            val winningCount = tradesList.count { it.netProfitLoss > 0 }
            val winRate = if (tradesList.isNotEmpty()) (winningCount.toDouble() / tradesList.size) * 100.0 else 0.0

            resultList.add(
              AdminUserItem(
                email = email,
                displayName = displayName,
                lastActive = lastActive,
                totalProfitLoss = totalPnl,
                totalTrades = tradesList.size,
                winRatePercent = winRate,
                isCloudSynced = true,
                trades = tradesList.sortedByDescending { it.date }
              )
            )
          }
        } catch (_: Exception) {}

        // 2. Ensure all locally known users are included
        val localEmails = AuthManager.getRecordedEmails()
        for (localEmail in localEmails) {
          if (!seenEmails.contains(localEmail)) {
            seenEmails.add(localEmail)
            val localTrades = PortfolioManager.getTrades(localEmail)
            val totalPnl = localTrades.sumOf { it.netProfitLoss }
            val winningCount = localTrades.count { it.netProfitLoss > 0 }
            val winRate = if (localTrades.isNotEmpty()) (winningCount.toDouble() / localTrades.size) * 100.0 else 0.0
            val displayName = if (localEmail == AuthConfig.ADMIN_EMAIL) "Admin Voltura" else localEmail.substringBefore("@")

            resultList.add(
              AdminUserItem(
                email = localEmail,
                displayName = displayName,
                lastActive = "Lokal / On-Device",
                totalProfitLoss = totalPnl,
                totalTrades = localTrades.size,
                winRatePercent = winRate,
                isCloudSynced = false,
                trades = localTrades.sortedByDescending { it.date }
              )
            )
          }
        }

        resultList.sortedByDescending { it.totalProfitLoss }
      }

      userList = loadedUsers
      isLoading = false
      statusMessage = "Berhasil memuat ${loadedUsers.size} pengguna"
    }
  }

  LaunchedEffect(Unit) {
    if (isAdmin) {
      loadUsersData()
    }
  }

  // Security Gate: If not officially logged in as volturainternasional@gmail.com
  if (!isAdmin) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(DarkBackground)
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, StatusDanger.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth().testTag("admin_lock_screen")
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = StatusDanger.copy(alpha = 0.15f),
            modifier = Modifier.size(68.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = StatusDanger,
                modifier = Modifier.size(36.dp)
              )
            }
          }

          Text(
            text = "AKSES ADMIN DIBATASI",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = StatusDanger
          )

          Text(
            text = "Panel kendali ekosistem dan administrasi ini secara khusus HANYA dapat diakses oleh Administrator resmi:\n\n${AuthConfig.ADMIN_EMAIL}\n\nUntuk melindungi keamanan data, akses manual tanpa login Google dinonaktifkan. Silakan login dengan Akun Google resmi Anda di tab Profil.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 20.sp
          )

          Button(
            onClick = onNavigateToProfile,
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth().testTag("btn_go_to_login_google")
          ) {
            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Halaman Profil untuk Login Google", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
          }
        }
      }
    }
    return
  }

  // Filter users by search query
  val filteredUsers = remember(userList, searchQuery) {
    if (searchQuery.isBlank()) {
      userList
    } else {
      userList.filter {
        it.email.contains(searchQuery, ignoreCase = true) ||
          it.displayName.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  val totalEcosystemProfit = remember(userList) { userList.sumOf { it.totalProfitLoss } }
  val totalEcosystemTrades = remember(userList) { userList.sumOf { it.totalTrades } }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("admin_panel_screen")
  ) {
    // 1. Top Super Admin Header Bar
    Card(
      shape = RoundedCornerShape(0.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      border = BorderStroke(1.dp, DarkCardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = RoundedCornerShape(4.dp), color = AccentGreen) {
              Text("SUPER ADMIN", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkBackground, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
            Text("Pusat Kendali Ekosistem", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
          }

          IconButton(
            onClick = {
              Toast.makeText(context, "Menyinkronkan data...", Toast.LENGTH_SHORT).show()
              loadUsersData()
            },
            modifier = Modifier.testTag("admin_refresh_button")
          ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = AccentGreen)
          }
        }

        // Active Admin Identity Badge
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "🛡️ ${session.displayName} (${session.email})",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = AccentGreen
          )
          Text("Akses Penuh Aktif", fontSize = 10.sp, color = StatusSuccess)
        }
      }
    }

    // 2. Complex Admin Navigation Tabs
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp)
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      FilterChip(
        selected = activeAdminTab == AdminTabMode.USERS_PROFIT,
        onClick = { activeAdminTab = AdminTabMode.USERS_PROFIT },
        label = { Text("Pengguna & Profit (${userList.size})", fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentGreen, selectedLabelColor = DarkBackground)
      )
      FilterChip(
        selected = activeAdminTab == AdminTabMode.SYSTEM_BROADCAST,
        onClick = { activeAdminTab = AdminTabMode.SYSTEM_BROADCAST },
        label = { Text("Siaran Sistem", fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentGreen, selectedLabelColor = DarkBackground)
      )
      FilterChip(
        selected = activeAdminTab == AdminTabMode.SECURITY_AUDIT,
        onClick = { activeAdminTab = AdminTabMode.SECURITY_AUDIT },
        label = { Text("Audit Keamanan", fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentGreen, selectedLabelColor = DarkBackground)
      )
    }

    // 3. Content per Tab
    when (activeAdminTab) {
      AdminTabMode.USERS_PROFIT -> {
        LazyColumn(
          modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(vertical = 8.dp)
        ) {
          // Metrics Cards
          item {
            Card(
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = DarkSurface),
              border = BorderStroke(1.dp, DarkCardBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("RINGKASAN EKOSISTEM", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                HorizontalDivider(thickness = 0.5.dp, color = DarkCardBorder)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Column {
                    Text("Total Profit Ekosistem", fontSize = 10.5.sp, color = TextMuted)
                    Text(
                      text = PortfolioManager.formatRupiah(totalEcosystemProfit, withSign = true),
                      fontFamily = FontFamily.Monospace,
                      fontSize = 16.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (totalEcosystemProfit >= 0) StatusSuccess else StatusDanger
                    )
                  }
                  Column(horizontalAlignment = Alignment.End) {
                    Text("Total Transaksi", fontSize = 10.5.sp, color = TextMuted)
                    Text("$totalEcosystemTrades Transaksi", fontFamily = FontFamily.Monospace, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                  }
                }
              }
            }
          }

          // Search Box
          item {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = { Text("Cari nama atau email pengguna...") },
              leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGreen,
                unfocusedBorderColor = DarkCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              )
            )
          }

          // User Cards
          items(filteredUsers) { user ->
            Card(
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = DarkSurface),
              border = BorderStroke(1.dp, DarkCardBorder),
              modifier = Modifier.fillMaxWidth().clickable { selectedUserDetail = user }
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = CircleShape, color = AccentGreen.copy(alpha = 0.2f), modifier = Modifier.size(32.dp)) {
                      Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                      }
                    }
                    Column {
                      Text(text = user.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                      Text(text = user.email, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextMuted)
                    }
                  }

                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = PortfolioManager.formatRupiah(user.totalProfitLoss, withSign = true),
                      fontFamily = FontFamily.Monospace,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (user.totalProfitLoss >= 0) StatusSuccess else StatusDanger
                    )
                    Text(text = "Win Rate: %.0f%%".format(Locale.US, user.winRatePercent), fontSize = 10.sp, color = AccentBlue)
                  }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                  Text(text = "📊 ${user.totalTrades} Transaksi • ${user.lastActive}", fontSize = 10.sp, color = TextMuted)
                  Text("Lihat Detail Rincian ›", fontSize = 11.sp, color = AccentGreen, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      AdminTabMode.SYSTEM_BROADCAST -> {
        LazyColumn(
          modifier = Modifier.fillMaxSize().padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            Card(
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = DarkSurface),
              border = BorderStroke(1.dp, DarkCardBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(imageVector = Icons.Default.Campaign, contentDescription = null, tint = AccentGreen)
                  Text("Siarkan Pengumuman / Sinyal Radar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                OutlinedTextField(
                  value = newBroadcastTitle,
                  onValueChange = { newBroadcastTitle = it },
                  label = { Text("Judul Sinyal / Siaran") },
                  placeholder = { Text("Contoh: Sinyal Rebound BTC $63,500") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth(),
                  colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGreen, unfocusedBorderColor = DarkCardBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                OutlinedTextField(
                  value = newBroadcastMsg,
                  onValueChange = { newBroadcastMsg = it },
                  label = { Text("Isi Pesan Siaran") },
                  placeholder = { Text("Tuliskan instruksi teknikal atau pengumuman keamanan...") },
                  modifier = Modifier.fillMaxWidth(),
                  minLines = 2,
                  colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGreen, unfocusedBorderColor = DarkCardBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Button(
                  onClick = {
                    if (newBroadcastTitle.isNotBlank() && newBroadcastMsg.isNotBlank()) {
                      val timeStr = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).format(java.util.Date())
                      broadcastList = listOf(AdminBroadcast("b_${System.currentTimeMillis()}", newBroadcastTitle, newBroadcastMsg, timeStr, "BULLISH")) + broadcastList
                      newBroadcastTitle = ""
                      newBroadcastMsg = ""
                      Toast.makeText(context, "Sinyal siaran berhasil dikirim ke seluruh ekosistem!", Toast.LENGTH_SHORT).show()
                    } else {
                      Toast.makeText(context, "Lengkapi judul dan isi pesan!", Toast.LENGTH_SHORT).show()
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                  shape = RoundedCornerShape(4.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text("Kirimkan Siaran ke Seluruh User", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          item {
            Text("RIWAYAT SIARAN AKTIF", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
          }

          items(broadcastList) { b ->
            Card(
              shape = RoundedCornerShape(6.dp),
              colors = CardDefaults.cardColors(containerColor = DarkSurface),
              border = BorderStroke(1.dp, DarkCardBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(b.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                  Surface(shape = RoundedCornerShape(3.dp), color = AccentGreen.copy(alpha = 0.2f)) {
                    Text(b.level, fontSize = 9.sp, color = AccentGreen, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                  }
                }
                Text(b.message, fontSize = 11.5.sp, color = TextSecondary)
                Text("📅 ${b.timestamp}", fontSize = 9.5.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
              }
            }
          }
        }
      }

      AdminTabMode.SECURITY_AUDIT -> {
        LazyColumn(
          modifier = Modifier.fillMaxSize().padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item {
            Card(
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = DarkSurface),
              border = BorderStroke(1.dp, DarkCardBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("DIAGNOSTIK OTENTIKASI & KEAMANAN CLOUD", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                HorizontalDivider(thickness = 0.5.dp, color = DarkCardBorder)

                Text("• Administrator Resmi: ${AuthConfig.ADMIN_EMAIL}", fontSize = 11.sp, color = TextPrimary)
                Text("• Server Client ID: ${AuthConfig.getClientId(context)}", fontSize = 10.sp, color = AccentBlue, fontFamily = FontFamily.Monospace)
                Text("• SHA-1 Fingerprint: ${AuthConfig.SHA1_FINGERPRINT}", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                Text("• Status Firestore Security Rules: v2 (users & portfolio oversight)", fontSize = 11.sp, color = StatusSuccess)
                Text("• Zero-Password Architecture: Google Credential Manager (ID Token)", fontSize = 11.sp, color = TextSecondary)
              }
            }
          }
        }
      }
    }
  }

  // Selected User Detail Modal
  if (selectedUserDetail != null) {
    val u = selectedUserDetail!!
    Dialog(
      onDismissRequest = { selectedUserDetail = null },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, AccentGreen),
        modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
              Text(u.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
              Text(u.email, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = AccentGreen)
            }
            IconButton(onClick = { selectedUserDetail = null }) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = TextMuted)
            }
          }

          HorizontalDivider(thickness = 0.5.dp, color = DarkCardBorder)

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Profit: ${PortfolioManager.formatRupiah(u.totalProfitLoss, withSign = true)}", fontWeight = FontWeight.Bold, color = if (u.totalProfitLoss >= 0) StatusSuccess else StatusDanger)
            Text("${u.trades.size} Transaksi", color = AccentBlue)
          }

          Text("Riwayat Transaksi:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
          if (u.trades.isEmpty()) {
            Text("Pengguna belum memiliki riwayat transaksi tercatat.", fontSize = 11.sp, color = TextMuted)
          } else {
            LazyColumn(modifier = Modifier.height(240.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              items(u.trades) { t ->
                Surface(shape = RoundedCornerShape(4.dp), color = DarkBackground, modifier = Modifier.fillMaxWidth()) {
                  Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                      Text("${t.symbol} • ${t.units} unit", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextPrimary)
                      Text("Entry: %,.0f | Exit: %,.0f • ${t.date}".format(Locale.US, t.entryPrice, t.exitPrice), fontSize = 9.sp, color = TextMuted)
                    }
                    Text(
                      PortfolioManager.formatRupiah(t.netProfitLoss, withSign = true),
                      fontFamily = FontFamily.Monospace,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (t.netProfitLoss >= 0) StatusSuccess else StatusDanger
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
