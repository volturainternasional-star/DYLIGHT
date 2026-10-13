package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.market.CandlePoint
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
import kotlin.math.max
import kotlin.math.min

/**
 * High-performance, lightweight Candlestick & Technical Indicators Chart View.
 * Matches: https://purbayasadewa.id/idx/saham/IHSG?t=ohlc&ind=VOL_20.RSI_14_14_70_30.STOCH_14_3_3_80_20.MACD_12_26_9&ov=MA_5_20
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PurbayaMarketChartView(
  candles: List<CandlePoint>,
  symbol: String,
  title: String,
  currency: String = "IDR",
  modifier: Modifier = Modifier
) {
  if (candles.isEmpty()) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .height(300.dp)
        .background(DarkSurface),
      contentAlignment = Alignment.Center
    ) {
      Text("Memuat data grafik teknikal...", color = TextMuted)
    }
    return
  }

  // Toggles for overlays & indicators
  var showMa5 by remember { mutableStateOf(true) }
  var showMa20 by remember { mutableStateOf(true) }
  var showVolume by remember { mutableStateOf(true) }
  var activeIndicatorTab by remember { mutableStateOf("RSI") } // "RSI", "STOCH", "MACD", "ALL"

  // Selected crosshair index (-1 means default to latest candle)
  var selectedIndex by remember { mutableIntStateOf(-1) }
  var showDownloadDialog by remember { mutableStateOf(false) }

  val activeCandle = remember(selectedIndex, candles) {
    if (selectedIndex in candles.indices) candles[selectedIndex] else candles.last()
  }

  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = BorderStroke(1.dp, DarkCardBorder),
    modifier = modifier
      .fillMaxWidth()
      .testTag("purbaya_market_chart_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // 1. Chart Header with Symbol, Title & Current Values + Download Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Surface(shape = RoundedCornerShape(4.dp), color = AccentGreen.copy(alpha = 0.15f)) {
              Text(
                text = symbol,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = AccentGreen,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            Text(
              text = title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
          }
          Text(
            text = "Data OHLCV & 4 Indikator Standar BEI (Purbaya Sadewa Spec)",
            fontSize = 11.sp,
            color = TextSecondary
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = if (currency == "IDR") "Rp %,.0f".format(Locale.US, activeCandle.close) else "$%,.2f".format(Locale.US, activeCandle.close),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = if (activeCandle.isGreen) StatusSuccess else StatusDanger
            )
            Text(
              text = String.format(Locale.US, "%+.2f%%", activeCandle.changePercent),
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (activeCandle.isGreen) StatusSuccess else StatusDanger
            )
          }

          IconButton(
            onClick = { showDownloadDialog = true },
            modifier = Modifier
              .size(34.dp)
              .background(DarkBackground, RoundedCornerShape(6.dp))
              .border(1.dp, DarkCardBorder, RoundedCornerShape(6.dp))
          ) {
            Icon(
              imageVector = Icons.Default.Download,
              contentDescription = "Download Hasil Analis",
              tint = AccentGreen,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // 2. Interactive Tooltip Bar (Showing exact candle stats under crosshair)
      Surface(
        shape = RoundedCornerShape(6.dp),
        color = DarkBackground,
        border = BorderStroke(0.5.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Waktu: ${activeCandle.dateLabel}",
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = AccentBlue
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "O: ${formatShortNum(activeCandle.open, currency)}",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TextPrimary
              )
              Text(
                text = "H: ${formatShortNum(activeCandle.high, currency)}",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = StatusSuccess
              )
              Text(
                text = "L: ${formatShortNum(activeCandle.low, currency)}",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = StatusDanger
              )
              Text(
                text = "C: ${formatShortNum(activeCandle.close, currency)}",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (activeCandle.isGreen) StatusSuccess else StatusDanger
              )
            }
          }

          // Indicator Values Strip
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              if (showMa5 && activeCandle.ma5 != null) {
                Text(
                  text = "MA5: ${formatShortNum(activeCandle.ma5, currency)}",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 9.5.sp,
                  color = Color(0xFF00E5FF)
                )
              }
              if (showMa20 && activeCandle.ma20 != null) {
                Text(
                  text = "MA20: ${formatShortNum(activeCandle.ma20, currency)}",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 9.5.sp,
                  color = Color(0xFFFFD54F)
                )
              }
              if (activeCandle.volMa20 != null) {
                Text(
                  text = "VMA: ${formatCompactVol(activeCandle.volMa20)}",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 9.5.sp,
                  color = Color(0xFFB388FF)
                )
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              if (activeCandle.rsi14 != null) {
                Text(
                  text = "RSI: %.1f".format(Locale.US, activeCandle.rsi14),
                  fontFamily = FontFamily.Monospace,
                  fontSize = 9.5.sp,
                  color = if (activeCandle.rsi14 >= 70.0) StatusWarning else if (activeCandle.rsi14 <= 30.0) StatusSuccess else AccentBlue
                )
              }
              if (activeCandle.stochK != null && activeCandle.stochD != null) {
                Text(
                  text = "STOCH: %.0f/%.0f".format(Locale.US, activeCandle.stochK, activeCandle.stochD),
                  fontFamily = FontFamily.Monospace,
                  fontSize = 9.5.sp,
                  color = Color(0xFFFF8A80)
                )
              }
              if (activeCandle.macdHistogram != null) {
                Text(
                  text = "MACD: %+.1f".format(Locale.US, activeCandle.macdHistogram),
                  fontFamily = FontFamily.Monospace,
                  fontSize = 9.5.sp,
                  color = if (activeCandle.macdHistogram >= 0) StatusSuccess else StatusDanger
                )
              }
            }
          }
        }
      }

      // 3. Overlay & Indicator Selector Controls
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        FilterChip(
          selected = showMa5,
          onClick = { showMa5 = !showMa5 },
          label = { Text("MA 5", fontSize = 10.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.2f),
            selectedLabelColor = Color(0xFF00E5FF)
          )
        )
        FilterChip(
          selected = showMa20,
          onClick = { showMa20 = !showMa20 },
          label = { Text("MA 20", fontSize = 10.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFFFFD54F).copy(alpha = 0.2f),
            selectedLabelColor = Color(0xFFFFD54F)
          )
        )
        FilterChip(
          selected = showVolume,
          onClick = { showVolume = !showVolume },
          label = { Text("VOL 20", fontSize = 10.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFFB388FF).copy(alpha = 0.2f),
            selectedLabelColor = Color(0xFFB388FF)
          )
        )

        // Sub-chart selector
        listOf("RSI", "STOCH", "MACD", "ALL").forEach { tab ->
          FilterChip(
            selected = activeIndicatorTab == tab,
            onClick = { activeIndicatorTab = tab },
            label = { Text(tab, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AccentGreen,
              selectedLabelColor = DarkBackground
            )
          )
        }
      }

      // 4. MAIN CANDLESTICK CANVAS WITH DRAG & PAN SUPPORT
      val candleCount = candles.size
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp)
          .background(DarkBackground)
          .border(0.5.dp, DarkCardBorder)
          .pointerInput(candleCount) {
            detectTapGestures { offset ->
              val step = size.width / candleCount
              val idx = (offset.x / step).toInt().coerceIn(0, candleCount - 1)
              selectedIndex = idx
            }
          }
          .pointerInput(candleCount) {
            detectDragGestures(
              onDragStart = { offset ->
                val step = size.width / candleCount
                selectedIndex = (offset.x / step).toInt().coerceIn(0, candleCount - 1)
              },
              onDrag = { change, _ ->
                val step = size.width / candleCount
                selectedIndex = (change.position.x / step).toInt().coerceIn(0, candleCount - 1)
              }
            )
          }
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val canvasWidth = size.width
          val canvasHeight = size.height
          val stepX = canvasWidth / candleCount

          val minPrice = candles.minOf { it.low } * 0.995
          val maxPrice = candles.maxOf { it.high } * 1.005
          val priceRange = max(1.0, maxPrice - minPrice)

          fun priceToY(price: Double): Float {
            return (canvasHeight - ((price - minPrice) / priceRange * canvasHeight)).toFloat()
          }

          // Horizontal Grid lines (4 levels)
          val gridSteps = 4
          for (g in 0..gridSteps) {
            val y = canvasHeight * (g.toFloat() / gridSteps)
            drawLine(
              color = DarkCardBorder.copy(alpha = 0.6f),
              start = Offset(0f, y),
              end = Offset(canvasWidth, y),
              strokeWidth = 1f,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
          }

          // Draw Candlesticks
          candles.forEachIndexed { i, c ->
            val centerX = (i * stepX) + (stepX / 2f)
            val wickTop = priceToY(c.high)
            val wickBottom = priceToY(c.low)
            val openY = priceToY(c.open)
            val closeY = priceToY(c.close)

            val color = if (c.isGreen) StatusSuccess else StatusDanger
            val bodyTop = min(openY, closeY)
            val bodyHeight = max(1.5f, kotlin.math.abs(closeY - openY))
            val candleWidth = max(2.5f, stepX * 0.7f)

            // Wick
            drawLine(
              color = color,
              start = Offset(centerX, wickTop),
              end = Offset(centerX, wickBottom),
              strokeWidth = 1.2f
            )

            // Body
            drawRect(
              color = color,
              topLeft = Offset(centerX - (candleWidth / 2f), bodyTop),
              size = Size(candleWidth, bodyHeight)
            )
          }

          // Overlay MA 5 Line (Cyan)
          if (showMa5) {
            val ma5Path = Path()
            var started = false
            candles.forEachIndexed { i, c ->
              val ma = c.ma5
              if (ma != null) {
                val x = (i * stepX) + (stepX / 2f)
                val y = priceToY(ma)
                if (!started) {
                  ma5Path.moveTo(x, y)
                  started = true
                } else {
                  ma5Path.lineTo(x, y)
                }
              }
            }
            if (started) {
              drawPath(ma5Path, color = Color(0xFF00E5FF), style = Stroke(width = 1.8f))
            }
          }

          // Overlay MA 20 Line (Kuning)
          if (showMa20) {
            val ma20Path = Path()
            var started = false
            candles.forEachIndexed { i, c ->
              val ma = c.ma20
              if (ma != null) {
                val x = (i * stepX) + (stepX / 2f)
                val y = priceToY(ma)
                if (!started) {
                  ma20Path.moveTo(x, y)
                  started = true
                } else {
                  ma20Path.lineTo(x, y)
                }
              }
            }
            if (started) {
              drawPath(ma20Path, color = Color(0xFFFFD54F), style = Stroke(width = 1.8f))
            }
          }

          // Vertical Crosshair on selected index
          if (selectedIndex in candles.indices) {
            val crossX = (selectedIndex * stepX) + (stepX / 2f)
            val targetCandle = candles[selectedIndex]
            val crossY = priceToY(targetCandle.close)

            drawLine(
              color = Color.White.copy(alpha = 0.7f),
              start = Offset(crossX, 0f),
              end = Offset(crossX, canvasHeight),
              strokeWidth = 1f,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            )

            drawLine(
              color = Color.White.copy(alpha = 0.7f),
              start = Offset(0f, crossY),
              end = Offset(canvasWidth, crossY),
              strokeWidth = 1f,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            )

            drawCircle(
              color = if (targetCandle.isGreen) StatusSuccess else StatusDanger,
              radius = 4f,
              center = Offset(crossX, crossY)
            )
          }
        }
      }

      // 5. VOLUME BARS WITH VOL 20 MOVING AVERAGE
      if (showVolume) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Volume Transaksi & VOL 20 (Rata-rata 20 Hari)", fontSize = 10.sp, color = Color(0xFFB388FF), fontFamily = FontFamily.Monospace)
            Text(
              text = formatCompactVol(activeCandle.volume),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (activeCandle.isGreen) StatusSuccess else StatusDanger
            )
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(55.dp)
              .background(DarkBackground)
              .border(0.5.dp, DarkCardBorder)
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val w = size.width
              val h = size.height
              val stepX = w / candles.size
              val maxVol = max(1.0, candles.maxOf { it.volume } * 1.05)

              // Draw Volume Bars
              candles.forEachIndexed { i, c ->
                val barW = max(2f, stepX * 0.65f)
                val barX = (i * stepX) + (stepX / 2f) - (barW / 2f)
                val barH = (c.volume / maxVol * h).toFloat().coerceAtLeast(1.5f)
                val color = if (c.isGreen) StatusSuccess.copy(alpha = 0.5f) else StatusDanger.copy(alpha = 0.5f)

                drawRect(
                  color = color,
                  topLeft = Offset(barX, h - barH),
                  size = Size(barW, barH)
                )
              }

              // Volume MA 20 line (Purple)
              val volPath = Path()
              var started = false
              candles.forEachIndexed { i, c ->
                val v = c.volMa20
                if (v != null) {
                  val x = (i * stepX) + (stepX / 2f)
                  val y = (h - (v / maxVol * h)).toFloat()
                  if (!started) {
                    volPath.moveTo(x, y)
                    started = true
                  } else {
                    volPath.lineTo(x, y)
                  }
                }
              }
              if (started) {
                drawPath(volPath, color = Color(0xFFB388FF), style = Stroke(width = 1.5f))
              }
            }
          }
        }
      }

      // 6. TECHNICAL INDICATORS (RSI / STOCH / MACD)
      if (activeIndicatorTab == "RSI" || activeIndicatorTab == "ALL") {
        RsiSubChart(candles = candles, selectedIndex = selectedIndex)
      }

      if (activeIndicatorTab == "STOCH" || activeIndicatorTab == "ALL") {
        StochasticSubChart(candles = candles, selectedIndex = selectedIndex)
      }

      if (activeIndicatorTab == "MACD" || activeIndicatorTab == "ALL") {
        MacdSubChart(candles = candles, selectedIndex = selectedIndex)
      }
    }
  }

  // Dialog Unduh / Export Hasil Analis
  if (showDownloadDialog) {
    val context = LocalContext.current
    val latest = candles.last()
    val rsiVal = latest.rsi14 ?: 50.0
    val stochKVal = latest.stochK ?: 50.0
    val stochDVal = latest.stochD ?: 50.0
    val macdVal = latest.macdHistogram ?: 0.0

    val signalStatus = when {
      rsiVal < 35 && macdVal > 0 -> "BUY AKUMULASI (Oversold & Reversal Bullish)"
      rsiVal > 68 && macdVal < 0 -> "TAKE PROFIT (Overbought & Distribusi)"
      macdVal > 0 -> "BULLISH MOMENTUM (Trend Menguat)"
      else -> "KONSOLIDASI NETRAL (Wait and See)"
    }

    val reportText = buildString {
      appendLine("=== HASIL ANALISIS TEKNIKAL PURBAYA SADEWA ===")
      appendLine("Aset: $symbol - $title")
      appendLine("Waktu/Tanggal: ${latest.dateLabel}")
      appendLine("Harga Terakhir: ${formatShortNum(latest.close, currency)} ($currency)")
      appendLine("Perubahan: %+.2f%%".format(Locale.US, latest.changePercent))
      appendLine("Range OHLC: O: ${formatShortNum(latest.open, currency)} | H: ${formatShortNum(latest.high, currency)} | L: ${formatShortNum(latest.low, currency)} | C: ${formatShortNum(latest.close, currency)}")
      appendLine("MA Overlay: MA5 = ${latest.ma5?.let { formatShortNum(it, currency) } ?: "-"} | MA20 = ${latest.ma20?.let { formatShortNum(it, currency) } ?: "-"}")
      appendLine("Volume & VMA20: Vol = ${formatCompactVol(latest.volume)} | VMA20 = ${latest.volMa20?.let { formatCompactVol(it) } ?: "-"}")
      appendLine("RSI (14): %.1f".format(Locale.US, rsiVal))
      appendLine("Stochastic (14,3,3): %K=%.1f / %D=%.1f".format(Locale.US, stochKVal, stochDVal))
      appendLine("MACD (12,26,9): Histogram = %+.2f".format(Locale.US, macdVal))
      appendLine("Kesimpulan Analis: $signalStatus")
      appendLine("Sumber: Sistem Analisis Kuantitatif & Chart Real-Time")
    }

    Dialog(onDismissRequest = { showDownloadDialog = false }) {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131722)),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp)
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
            Text(
              text = "Download Hasil Analis $symbol",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            IconButton(onClick = { showDownloadDialog = false }) {
              Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextMuted)
            }
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkBackground, RoundedCornerShape(6.dp))
              .border(0.5.dp, DarkCardBorder, RoundedCornerShape(6.dp))
              .padding(10.dp)
          ) {
            Text(
              text = reportText,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              color = TextPrimary,
              lineHeight = 16.sp
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clip = ClipData.newPlainText("Analisis $symbol", reportText)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(context, "Laporan Analisis $symbol Berhasil Disalin!", Toast.LENGTH_SHORT).show()
                showDownloadDialog = false
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(6.dp),
              border = BorderStroke(1.dp, AccentGreen)
            ) {
              Text("Salin Laporan", color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = {
                val sendIntent = Intent().apply {
                  action = Intent.ACTION_SEND
                  putExtra(Intent.EXTRA_TEXT, reportText)
                  type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Ekspor Hasil Analisis $symbol")
                context.startActivity(shareIntent)
                showDownloadDialog = false
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(6.dp),
              colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
            ) {
              Icon(Icons.Default.Download, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Unduh / Share", color = DarkBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RsiSubChart(candles: List<CandlePoint>, selectedIndex: Int) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text("RSI (14) [Overbought: 70 | Oversold: 30]", fontSize = 10.sp, color = AccentBlue, fontFamily = FontFamily.Monospace)
      val latestRsi = candles.lastOrNull()?.rsi14 ?: 50.0
      Text(
        text = "%.1f".format(Locale.US, latestRsi),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = if (latestRsi >= 70) StatusWarning else if (latestRsi <= 30) StatusSuccess else AccentBlue
      )
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(65.dp)
        .background(DarkBackground)
        .border(0.5.dp, DarkCardBorder)
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val stepX = w / candles.size

        // 70 & 30 Reference lines
        val y70 = (h * (1f - 0.7f))
        val y30 = (h * (1f - 0.3f))

        // Neutral channel zone
        drawRect(
          color = AccentBlue.copy(alpha = 0.08f),
          topLeft = Offset(0f, y70),
          size = Size(w, y30 - y70)
        )

        drawLine(
          color = StatusWarning.copy(alpha = 0.6f),
          start = Offset(0f, y70),
          end = Offset(w, y70),
          strokeWidth = 1f,
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
        )

        drawLine(
          color = StatusSuccess.copy(alpha = 0.6f),
          start = Offset(0f, y30),
          end = Offset(w, y30),
          strokeWidth = 1f,
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
        )

        // RSI Line
        val rsiPath = Path()
        var started = false
        candles.forEachIndexed { i, c ->
          val rsi = c.rsi14
          if (rsi != null) {
            val x = (i * stepX) + (stepX / 2f)
            val y = (h * (1f - (rsi / 100f))).toFloat()
            if (!started) {
              rsiPath.moveTo(x, y)
              started = true
            } else {
              rsiPath.lineTo(x, y)
            }
          }
        }
        if (started) {
          drawPath(rsiPath, color = AccentBlue, style = Stroke(width = 1.8f))
        }
      }
    }
  }
}

@Composable
private fun StochasticSubChart(candles: List<CandlePoint>, selectedIndex: Int) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text("Stochastic (14, 3, 3) [%K: Hijau | %D: Merah]", fontSize = 10.sp, color = Color(0xFFFF8A80), fontFamily = FontFamily.Monospace)
      val latest = candles.lastOrNull()
      val k = latest?.stochK ?: 50.0
      val d = latest?.stochD ?: 50.0
      Text(
        text = "K: %.0f / D: %.0f".format(Locale.US, k, d),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(65.dp)
        .background(DarkBackground)
        .border(0.5.dp, DarkCardBorder)
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val stepX = w / candles.size

        // 80 & 20 Level lines
        val y80 = h * 0.2f
        val y20 = h * 0.8f

        drawLine(color = StatusWarning.copy(alpha = 0.5f), start = Offset(0f, y80), end = Offset(w, y80), strokeWidth = 1f)
        drawLine(color = StatusSuccess.copy(alpha = 0.5f), start = Offset(0f, y20), end = Offset(w, y20), strokeWidth = 1f)

        // %K Line (Green)
        val kPath = Path()
        var kStarted = false
        candles.forEachIndexed { i, c ->
          val valK = c.stochK
          if (valK != null) {
            val x = (i * stepX) + (stepX / 2f)
            val y = (h * (1f - (valK / 100f))).toFloat()
            if (!kStarted) { kPath.moveTo(x, y); kStarted = true } else { kPath.lineTo(x, y) }
          }
        }
        if (kStarted) drawPath(kPath, color = StatusSuccess, style = Stroke(width = 1.6f))

        // %D Line (Red)
        val dPath = Path()
        var dStarted = false
        candles.forEachIndexed { i, c ->
          val valD = c.stochD
          if (valD != null) {
            val x = (i * stepX) + (stepX / 2f)
            val y = (h * (1f - (valD / 100f))).toFloat()
            if (!dStarted) { dPath.moveTo(x, y); dStarted = true } else { dPath.lineTo(x, y) }
          }
        }
        if (dStarted) drawPath(dPath, color = StatusDanger, style = Stroke(width = 1.6f))
      }
    }
  }
}

@Composable
private fun MacdSubChart(candles: List<CandlePoint>, selectedIndex: Int) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text("MACD (12, 26, 9) [MACD: Biru | Signal: Kuning]", fontSize = 10.sp, color = Color(0xFF40C4FF), fontFamily = FontFamily.Monospace)
      val latestHist = candles.lastOrNull()?.macdHistogram ?: 0.0
      Text(
        text = "Hist: %+.2f".format(Locale.US, latestHist),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = if (latestHist >= 0) StatusSuccess else StatusDanger
      )
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(70.dp)
        .background(DarkBackground)
        .border(0.5.dp, DarkCardBorder)
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val stepX = w / candles.size
        val centerY = h / 2f

        val nonNullHist = candles.mapNotNull { it.macdHistogram }
        val maxAbs = max(1.0, nonNullHist.maxOfOrNull { kotlin.math.abs(it) } ?: 1.0).toFloat()

        // Zero line
        drawLine(color = Color.Gray.copy(alpha = 0.5f), start = Offset(0f, centerY), end = Offset(w, centerY), strokeWidth = 1f)

        // Histogram bars
        candles.forEachIndexed { i, c ->
          val hist = c.macdHistogram
          if (hist != null) {
            val barW = max(2f, stepX * 0.6f)
            val x = (i * stepX) + (stepX / 2f) - (barW / 2f)
            val barHeight = (kotlin.math.abs(hist) / maxAbs * (h / 2.2f)).toFloat()

            if (hist >= 0) {
              drawRect(
                color = StatusSuccess.copy(alpha = 0.75f),
                topLeft = Offset(x, centerY - barHeight),
                size = Size(barW, barHeight)
              )
            } else {
              drawRect(
                color = StatusDanger.copy(alpha = 0.75f),
                topLeft = Offset(x, centerY),
                size = Size(barW, barHeight)
              )
            }
          }
        }

        // MACD Line (Blue)
        val macdPath = Path()
        var mStarted = false
        candles.forEachIndexed { i, c ->
          val m = c.macdLine
          if (m != null) {
            val x = (i * stepX) + (stepX / 2f)
            val y = centerY - ((m / maxAbs * (h / 2.5f)).toFloat())
            if (!mStarted) { macdPath.moveTo(x, y); mStarted = true } else { macdPath.lineTo(x, y) }
          }
        }
        if (mStarted) drawPath(macdPath, color = Color(0xFF40C4FF), style = Stroke(width = 1.8f))

        // Signal Line (Yellow)
        val sigPath = Path()
        var sStarted = false
        candles.forEachIndexed { i, c ->
          val s = c.macdSignal
          if (s != null) {
            val x = (i * stepX) + (stepX / 2f)
            val y = centerY - ((s / maxAbs * (h / 2.5f)).toFloat())
            if (!sStarted) { sigPath.moveTo(x, y); sStarted = true } else { sigPath.lineTo(x, y) }
          }
        }
        if (sStarted) drawPath(sigPath, color = Color(0xFFFFD740), style = Stroke(width = 1.8f))
      }
    }
  }
}

private fun formatShortNum(value: Double, currency: String): String {
  return if (currency == "IDR") {
    if (value >= 1000) String.format(Locale.US, "%,.0f", value) else String.format(Locale.US, "%.1f", value)
  } else {
    String.format(Locale.US, "%.2f", value)
  }
}

private fun formatCompactVol(volume: Double): String {
  return when {
    volume >= 1_000_000_000.0 -> String.format(Locale.US, "%.1fB", volume / 1_000_000_000.0)
    volume >= 1_000_000.0 -> String.format(Locale.US, "%.1fM", volume / 1_000_000.0)
    volume >= 1_000.0 -> String.format(Locale.US, "%.1fK", volume / 1_000.0)
    else -> String.format(Locale.US, "%.0f", volume)
  }
}
