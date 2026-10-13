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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.auth.AuthManager
import com.example.market.PortfolioManager
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
fun PortfolioScreen(
  modifier: Modifier = Modifier,
  onNavigateToProfile: () -> Unit = {}
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
  var trades by remember(session.email) {
    mutableStateOf(
      try {
        PortfolioManager.getTrades(session.email)
      } catch (_: Exception) {
        emptyList()
      }
    )
  }

  var selectedFilter by rememberSaveable { mutableStateOf("ALL") }
  var showAddDialog by rememberSaveable { mutableStateOf(false) }

  val summary = remember(trades) {
    try {
      PortfolioManager.calculateSummary(trades)
    } catch (_: Exception) {
      PortfolioManager.calculateSummary(emptyList())
    }
  }

  val filteredTrades = remember(trades, selectedFilter) {
    when (selectedFilter) {
      "WIN" -> trades.filter { it.isWin }
      "LOSS" -> trades.filter { !it.isWin }
      "CRYPTO" -> trades.filter { it.assetType.equals("Crypto", ignoreCase = true) }
      "SAHAM" -> trades.filter { it.assetType.equals("Saham", ignoreCase = true) }
      else -> trades
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("portfolio_screen")
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Header Section
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Portofolio & Riwayat Transaksi",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
          )
          Text(
            text = "Pencatatan hasil investasi/trading riil, evaluasi konsistensi, dan rasio risiko.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
          )
        }
      }

      // 2. Google Account Sync Card / Guest Reminder
      item {
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          border = BorderStroke(1.dp, if (session.isLoggedIn) AccentGreen.copy(alpha = 0.5f) else DarkCardBorder),
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
              shape = RoundedCornerShape(6.dp),
              color = if (session.isLoggedIn) AccentGreen.copy(alpha = 0.15f) else DarkSurfaceVariant,
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = if (session.isLoggedIn) Icons.Default.CheckCircle else Icons.Default.Lock,
                  contentDescription = null,
                  tint = if (session.isLoggedIn) AccentGreen else TextMuted,
                  modifier = Modifier.size(20.dp)
                )
              }
            }

            Column(modifier = Modifier.weight(1f)) {
              if (session.isLoggedIn) {
                Text(
                  text = "Akun Terhubung: ${session.email}",
                  style = MaterialTheme.typography.titleSmall,
                  color = AccentGreen,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "Data tersimpan di penyimpanan lokal akun ini.",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  color = TextMuted
                )
              } else {
                Text(
                  text = "Penyimpanan Tamu (Lokal Perangkat)",
                  style = MaterialTheme.typography.titleSmall,
                  color = TextPrimary
                )
                Text(
                  text = "Login Google di Profil untuk mengaitkan portofolio ke email Anda.",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  color = TextSecondary
                )
              }
            }

            if (!session.isLoggedIn) {
              Button(
                onClick = onNavigateToProfile,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "Login",
                  color = DarkBackground,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }
            }
          }
        }
      }

      // 3. Ringkasan Kinerja (Total P/L, Win Rate, Konsistensi) - Bebas Crash
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
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Ringkasan Kinerja Trading", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (summary.totalProfitLoss >= 0) StatusSuccess.copy(alpha = 0.15f) else StatusDanger.copy(alpha = 0.15f)
              ) {
                Text(
                  text = if (summary.totalProfitLoss >= 0) "PROFIT NET" else "LOSS NET",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (summary.totalProfitLoss >= 0) StatusSuccess else StatusDanger,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            // Big Metric Total Profit / Loss Bersih (Menggunakan formatRupiah yang 100% aman)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "TOTAL PROFIT / LOSS BERSIH",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TextMuted
              )
              Text(
                text = PortfolioManager.formatRupiah(summary.totalProfitLoss, withSign = true),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (summary.totalProfitLoss >= 0) StatusSuccess else StatusDanger
              )
            }

            // Metrics Grid
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Total Transaksi", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = TextMuted)
                Text("${summary.totalTrades} Trade", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Win Rate", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = TextMuted)
                Text(
                  text = String.format(Locale.US, "%.1f%% (%d/%d)", summary.winRatePercent, summary.winningTrades, summary.totalTrades),
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Bold,
                  color = if (summary.winRatePercent >= 50.0) StatusSuccess else StatusWarning
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Skor Konsistensi", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = TextMuted)
                Text(
                  text = "${summary.consistencyScore} / 100",
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Bold,
                  color = AccentBlue
                )
              }
            }

            // Banner Analisis Konsistensi
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = DarkSurfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ShowChart,
                  contentDescription = null,
                  tint = AccentGreen,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "ANALISIS: ${summary.consistencyLabel}",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = AccentGreen
                )
              }
            }
          }
        }
      }

      // 4. Filter Chips & Tambah Trade Action
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
          ) {
            listOf("ALL" to "Semua", "WIN" to "Profit", "LOSS" to "Loss", "CRYPTO" to "Crypto", "SAHAM" to "Saham").forEach { (key, label) ->
              val isSelected = selectedFilter == key
              FilterChip(
                selected = isSelected,
                onClick = { selectedFilter = key },
                label = {
                  Text(
                    text = label,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = AccentGreen,
                  selectedLabelColor = DarkBackground,
                  containerColor = DarkSurface,
                  labelColor = TextPrimary
                ),
                border = BorderStroke(1.dp, if (isSelected) AccentGreen else DarkCardBorder)
              )
            }
          }

          Spacer(modifier = Modifier.width(6.dp))

          Button(
            onClick = { showAddDialog = true },
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("btn_add_trade")
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Catat Trade", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }

      // 5. Empty State / Trade Records List
      if (filteredTrades.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Text("Belum Ada Transaksi", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
              Text(
                text = "Gunakan tombol 'Catat Trade' di atas atau kirim kalkulasi dari tab Market untuk menyimpan transaksi riil Anda.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.5.sp,
                lineHeight = 16.sp
              )
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = { showAddDialog = true },
                  shape = RoundedCornerShape(4.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                  Text("+ Catat Transaksi Baru", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                OutlinedButton(
                  onClick = {
                    trades = PortfolioManager.resetToInitialSeed(session.email)
                    Toast.makeText(context, "Contoh data transaksi riil dimuat", Toast.LENGTH_SHORT).show()
                  },
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text("Muat Contoh Riil", color = TextPrimary, fontSize = 12.sp)
                }
              }
            }
          }
        }
      } else {
        items(filteredTrades, key = { it.id }) { trade ->
          val isGain = trade.isWin

          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, if (isGain) StatusSuccess.copy(alpha = 0.3f) else StatusDanger.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Top line: Symbol, Type, Date, Delete button
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Surface(shape = RoundedCornerShape(4.dp), color = DarkSurfaceVariant) {
                    Text(
                      text = trade.symbol,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = AccentBlue,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                  }
                  Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = if (trade.assetType.equals("Crypto", ignoreCase = true)) AccentGreen.copy(alpha = 0.2f) else AccentBlue.copy(alpha = 0.2f)
                  ) {
                    Text(
                      text = trade.assetType.uppercase(),
                      fontFamily = FontFamily.Monospace,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (trade.assetType.equals("Crypto", ignoreCase = true)) AccentGreen else AccentBlue,
                      modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                  }
                  Text(
                    text = trade.date,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextMuted
                  )
                }

                IconButton(
                  onClick = {
                    trades = PortfolioManager.deleteTrade(session.email, trade.id)
                    Toast.makeText(context, "Transaksi dihapus", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }

              // Trade Details & Profit/Loss Numbers (Menggunakan formatRupiah aman)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "Entry: ${PortfolioManager.formatRupiah(trade.entryPrice)} • Exit: ${PortfolioManager.formatRupiah(trade.exitPrice)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp,
                    color = TextPrimary
                  )
                  Text(
                    text = "${trade.units} unit • Modal: ${PortfolioManager.formatRupiah(trade.totalCost)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp,
                    color = TextMuted
                  )
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = PortfolioManager.formatRupiah(trade.netProfitLoss, withSign = true),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isGain) StatusSuccess else StatusDanger
                  )
                  Text(
                    text = String.format(Locale.US, "%+.2f%%", trade.roiPercent),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isGain) StatusSuccess else StatusDanger
                  )
                }
              }

              if (trade.notes.isNotEmpty()) {
                Text(
                  text = "Catatan: ${trade.notes}",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextSecondary,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Strategi: ${trade.strategy}",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.sp,
                  color = TextMuted
                )
                Text(
                  text = if (isGain) "WIN TRADE" else "LOSS TRADE",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isGain) StatusSuccess else StatusDanger
                )
              }
            }
          }
        }
      }
    }
  }

  // Dialog Catat Trade Baru
  if (showAddDialog) {
    AddTradeDialog(
      onDismiss = { showAddDialog = false },
      onSave = {
        trades = PortfolioManager.addTrade(session.email, it)
        showAddDialog = false
        Toast.makeText(context, "Transaksi berhasil disimpan ke portofolio", Toast.LENGTH_SHORT).show()
      }
    )
  }
}

@Composable
private fun AddTradeDialog(
  onDismiss: () -> Unit,
  onSave: (TradeRecord) -> Unit
) {
  var symbol by remember { mutableStateOf("BTC/USDT") }
  var assetType by remember { mutableStateOf("Crypto") }
  var entryPriceText by remember { mutableStateOf("62500") }
  var exitPriceText by remember { mutableStateOf("65800") }
  var unitsText by remember { mutableStateOf("0.2") }
  var notes by remember { mutableStateOf("") }
  var strategy by remember { mutableStateOf("Breakout / Disiplin RR") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Catat Transaksi Investasi / Trading", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Quick Presets
        Text("Pilihan Aset Cepat:", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextMuted)
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          listOf(
            Triple("BTC/USDT", "Crypto", "62500"),
            Triple("ETH/USDT", "Crypto", "2450"),
            Triple("BBCA.JK", "Saham", "9850"),
            Triple("TLKM.JK", "Saham", "3120")
          ).forEach { (sym, type, defaultPrice) ->
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = DarkSurfaceVariant,
              modifier = Modifier.weight(1f)
            ) {
              Button(
                onClick = {
                  symbol = sym
                  assetType = type
                  entryPriceText = defaultPrice
                  exitPriceText = (defaultPrice.toDoubleOrNull()?.let { (it * 1.05).toInt().toString() }) ?: defaultPrice
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(sym.substringBefore("/").substringBefore("."), fontSize = 10.sp, color = AccentBlue)
              }
            }
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = symbol,
            onValueChange = { symbol = it },
            label = { Text("Simbol / Aset", fontSize = 11.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = DarkSurfaceVariant,
              unfocusedContainerColor = DarkSurfaceVariant,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.weight(1.2f)
          )

          OutlinedTextField(
            value = assetType,
            onValueChange = { assetType = it },
            label = { Text("Tipe", fontSize = 11.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = DarkSurfaceVariant,
              unfocusedContainerColor = DarkSurfaceVariant,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.weight(0.8f)
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = entryPriceText,
            onValueChange = { entryPriceText = it.filter { ch -> ch.isDigit() || ch == '.' } },
            label = { Text("Harga Entry", fontSize = 11.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = DarkSurfaceVariant,
              unfocusedContainerColor = DarkSurfaceVariant,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.weight(1f)
          )

          OutlinedTextField(
            value = exitPriceText,
            onValueChange = { exitPriceText = it.filter { ch -> ch.isDigit() || ch == '.' } },
            label = { Text("Harga Exit", fontSize = 11.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = DarkSurfaceVariant,
              unfocusedContainerColor = DarkSurfaceVariant,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.weight(1f)
          )
        }

        OutlinedTextField(
          value = unitsText,
          onValueChange = { unitsText = it.filter { ch -> ch.isDigit() || ch == '.' } },
          label = { Text("Jumlah Unit / Lot", fontSize = 11.sp) },
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
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Catatan / Alasan Eksekusi", fontSize = 11.sp) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkSurfaceVariant,
            unfocusedContainerColor = DarkSurfaceVariant,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val entry = entryPriceText.toDoubleOrNull() ?: 1.0
          val exit = exitPriceText.toDoubleOrNull() ?: 1.0
          val units = unitsText.toDoubleOrNull() ?: 1.0
          val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date())

          val trade = TradeRecord(
            id = "TR-${System.currentTimeMillis()}",
            symbol = symbol.trim().uppercase(),
            assetType = assetType.trim(),
            entryPrice = entry,
            exitPrice = exit,
            units = units,
            date = dateStr,
            notes = notes.trim(),
            strategy = strategy
          )
          onSave(trade)
        },
        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
      ) {
        Text("Simpan", color = DarkBackground, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Batal", color = TextPrimary)
      }
    },
    containerColor = DarkSurface
  )
}
