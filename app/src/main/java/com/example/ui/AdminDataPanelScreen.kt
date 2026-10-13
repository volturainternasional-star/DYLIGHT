package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthConfig
import com.example.auth.AuthManager
import com.example.market.PortfolioManager
import com.example.market.PortfolioSummary
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
import java.util.Locale

@Composable
fun AdminDataPanelScreen(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  remember {
    try {
      AuthManager.init(context)
      PortfolioManager.init(context)
    } catch (_: Exception) {
      // Safe fallback
    }
  }

  val session by AuthManager.sessionState.collectAsState()
  val isAdmin = AuthManager.isAdmin() && session.email.trim().equals(AuthConfig.ADMIN_EMAIL, ignoreCase = true)



  // State with dynamic mutation capability for real admin management
  var recordedEmails by remember {
    mutableStateOf(
      try {
        AuthManager.getRecordedEmails()
      } catch (_: Exception) {
        emptyList()
      }
    )
  }

  var adminTrades by remember {
    mutableStateOf(
      try {
        PortfolioManager.getTrades(AuthConfig.ADMIN_EMAIL)
      } catch (_: Exception) {
        emptyList()
      }
    )
  }

  val adminSummary: PortfolioSummary = remember(adminTrades) {
    try {
      PortfolioManager.calculateSummary(adminTrades)
    } catch (_: Exception) {
      PortfolioSummary(
        totalTrades = 0,
        winningTrades = 0,
        losingTrades = 0,
        winRatePercent = 0.0,
        totalProfitLoss = 0.0,
        totalCapitalTurnover = 0.0,
        consistencyScore = 50,
        consistencyLabel = "Belum Ada Transaksi",
        avgReturnPercent = 0.0
      )
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("admin_data_panel_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Admin Header
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(shape = RoundedCornerShape(4.dp), color = AccentGreen) {
            Text(
              text = "ADMIN ACCESS",
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = DarkBackground,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          Text(
            text = "Panel Kontrol Admin",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
          )
        }
        Text(
          text = "Akses terverifikasi untuk administrator resmi: ${AuthConfig.ADMIN_EMAIL}",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary
        )
      }
    }

    // 2. Admin Status Banner
    item {
      Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, AccentGreen),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = AccentGreen.copy(alpha = 0.2f),
            modifier = Modifier.size(42.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier.size(24.dp)
              )
            }
          }

          Column(modifier = Modifier.weight(1f)) {
            Text("Sesi Otoritas Admin Aktif", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(
              text = "Status: Terverifikasi • Akun: ${session.email.ifBlank { AuthConfig.ADMIN_EMAIL }}",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              color = AccentGreen,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (session.lastLoginDate.isNotBlank()) {
              Text(
                text = "Waktu Login: ${session.lastLoginDate}",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TextMuted
              )
            }
          }
        }
      }
    }

    // 3. Ringkasan Metrik Admin Sederhana & Bebas Crash
    item {
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
          Text("Ringkasan Metrik Admin", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Status Akun", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = TextMuted)
              Text("Aktif (Root)", style = MaterialTheme.typography.titleSmall, color = AccentGreen, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Konsistensi", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = TextMuted)
              Text("Terpantau", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
              Text("Portfolio Lokal", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = TextMuted)
              Text("${adminTrades.size} Item", style = MaterialTheme.typography.titleSmall, color = AccentBlue, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 4. Data Portofolio Admin (Menggunakan PortfolioManager.formatRupiah tanpa crash)
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
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Data Portofolio Administrator", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Button(
                onClick = {
                  try {
                    adminTrades = PortfolioManager.resetToInitialSeed(AuthConfig.ADMIN_EMAIL)
                    Toast.makeText(context, "Data portofolio admin diperbarui", Toast.LENGTH_SHORT).show()
                  } catch (e: Exception) {
                    Toast.makeText(context, "Gagal memuat ulang data", Toast.LENGTH_SHORT).show()
                  }
                },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("Refresh", fontSize = 10.sp, color = TextPrimary)
              }
            }
          }

          if (adminTrades.isEmpty()) {
            Text(
              text = "Belum ada data transaksi tercatat untuk akun admin.",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.5.sp,
              color = TextMuted,
              modifier = Modifier.padding(vertical = 8.dp)
            )
          } else {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Total Transaksi", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = TextMuted)
                Text("${adminSummary.totalTrades} Transaksi", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Win Rate Admin", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = TextMuted)
                Text(
                  text = String.format(Locale.US, "%.1f%%", adminSummary.winRatePercent),
                  style = MaterialTheme.typography.bodyLarge,
                  color = StatusSuccess,
                  fontWeight = FontWeight.Bold
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("P/L Akumulatif", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = TextMuted)
                Text(
                  text = PortfolioManager.formatRupiah(adminSummary.totalProfitLoss, withSign = true),
                  style = MaterialTheme.typography.bodyLarge,
                  color = if (adminSummary.totalProfitLoss >= 0) StatusSuccess else StatusDanger,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }

    // 5. Akun-Akun Terdaftar di Perangkat Ini
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
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Email Tercatat di Perangkat Ini", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Surface(shape = RoundedCornerShape(4.dp), color = DarkSurfaceVariant) {
              Text(
                text = "${recordedEmails.size} AKUN",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = AccentBlue,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          if (recordedEmails.isEmpty()) {
            Text(
              text = "Belum ada data email tercatat di perangkat ini.",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.5.sp,
              color = TextMuted,
              modifier = Modifier.padding(vertical = 8.dp)
            )
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              recordedEmails.forEach { email ->
                val isCurrentAdmin = email.equals(AuthConfig.ADMIN_EMAIL, ignoreCase = true)

                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = DarkSurfaceVariant,
                  border = BorderStroke(1.dp, if (isCurrentAdmin) AccentGreen.copy(alpha = 0.5f) else DarkCardBorder),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                      modifier = Modifier.weight(1f)
                    ) {
                      Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = if (isCurrentAdmin) AccentGreen else TextMuted,
                        modifier = Modifier.size(18.dp)
                      )
                      Text(
                        text = email,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }

                    Surface(
                      shape = RoundedCornerShape(3.dp),
                      color = if (isCurrentAdmin) AccentGreen.copy(alpha = 0.2f) else DarkSurface
                    ) {
                      Text(
                        text = if (isCurrentAdmin) "ADMIN ROOT" else "PENGGUNA",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentAdmin) AccentGreen else TextMuted,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
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

    // 6. Kebijakan Keamanan Sandbox
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(6.dp))
          .background(DarkSurfaceVariant)
          .padding(12.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "KEBIJAKAN PRIVASI DATA ADMIN:",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGreen
          )
          Text(
            text = "Semua data disimpan di penyimpanan lokal perangkat yang aman (SharedPreferences sandbox). Tidak ada data transaksi yang bocor ke luar perangkat.",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.5.sp,
            color = TextMuted,
            lineHeight = 15.sp
          )
        }
      }
    }
  }
}
