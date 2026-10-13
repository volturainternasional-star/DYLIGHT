package com.example.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthManager
import com.example.market.MarketAssetInfo
import com.example.market.MarketCatalog
import com.example.market.MarketFeedState
import com.example.market.PortfolioManager
import com.example.market.RealMarketService
import com.example.market.TaskManager
import com.example.market.TradeRecord
import com.example.ui.components.CyberRadarScanner
import com.example.ui.components.PurbayaMarketChartView
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
import java.util.Locale

enum class MarketTabMode {
  CRYPTO,
  STOCKS,
  CHART_PRO,
  CALCULATORS,
  PORTFOLIO
}

@Composable
fun MarketScreen(
  modifier: Modifier = Modifier,
  onNavigateToPortfolio: () -> Unit = {}
) {
  var activeTab by rememberSaveable { mutableStateOf(MarketTabMode.CRYPTO) }

  // Shared prefilled calculator state
  var prefilledBuyPrice by rememberSaveable { mutableStateOf("10000") }
  var prefilledSellPrice by rememberSaveable { mutableStateOf("12500") }
  var prefilledUnits by rememberSaveable { mutableStateOf("10") }
  var prefilledSymbol by rememberSaveable { mutableStateOf("ASET") }

  // Chart Pro state
  var selectedChartSymbol by rememberSaveable { mutableStateOf("IHSG") }
  var selectedChartTitle by rememberSaveable { mutableStateOf("Indeks Harga Saham Gabungan") }
  var selectedChartCurrency by rememberSaveable { mutableStateOf("IDR") }
  var selectedChartTimeframe by rememberSaveable { mutableStateOf("1D") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("market_screen")
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
          text = "Pasar & Analisis Teknikal",
          style = MaterialTheme.typography.titleLarge,
          color = TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = "Chart OHLCV Candlestick ala Purbaya Sadewa (MA, RSI, STOCH, MACD) & Feed Pasar",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
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
            text = "PRO CHART",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBackground,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
          )
        }
      }
    }

    // Tab Switcher Chips (Scrollable row)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      MarketTabMode.entries.forEach { mode ->
        val isSelected = mode == activeTab
        FilterChip(
          selected = isSelected,
          onClick = { activeTab = mode },
          label = {
            Text(
              text = when (mode) {
                MarketTabMode.CRYPTO -> "Crypto (${MarketCatalog.allCryptos.size})"
                MarketTabMode.STOCKS -> "Saham (${MarketCatalog.allStocks.size})"
                MarketTabMode.CHART_PRO -> "Chart Pro"
                MarketTabMode.CALCULATORS -> "Kalkulator"
                MarketTabMode.PORTFOLIO -> "Portofolio"
              },
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
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
          modifier = Modifier.testTag("chip_market_${mode.name.lowercase()}")
        )
      }
    }

    // Body by Active Tab
    when (activeTab) {
      MarketTabMode.CRYPTO -> CryptoMarketView(
        onOpenChart = { sym, name, curr ->
          selectedChartSymbol = sym
          selectedChartTitle = name
          selectedChartCurrency = curr
          activeTab = MarketTabMode.CHART_PRO
        },
        onSendToCalculator = { sym, priceIdr ->
          prefilledSymbol = sym
          prefilledBuyPrice = String.format(Locale.US, "%.0f", priceIdr)
          prefilledSellPrice = String.format(Locale.US, "%.0f", priceIdr * 1.05)
          prefilledUnits = if (priceIdr > 10_000_000) "0.05" else "5"
          activeTab = MarketTabMode.CALCULATORS
        }
      )

      MarketTabMode.STOCKS -> StockMarketView(
        onOpenChart = { sym, name, curr ->
          selectedChartSymbol = sym
          selectedChartTitle = name
          selectedChartCurrency = curr
          activeTab = MarketTabMode.CHART_PRO
        },
        onSendToCalculator = { sym, price ->
          prefilledSymbol = sym
          prefilledBuyPrice = String.format(Locale.US, "%.0f", price)
          prefilledSellPrice = String.format(Locale.US, "%.0f", price * 1.05)
          prefilledUnits = "100"
          activeTab = MarketTabMode.CALCULATORS
        }
      )

      MarketTabMode.CHART_PRO -> ChartProView(
        symbol = selectedChartSymbol,
        title = selectedChartTitle,
        currency = selectedChartCurrency,
        timeframe = selectedChartTimeframe,
        onTimeframeChange = { selectedChartTimeframe = it },
        onSelectAsset = { sym, name, curr ->
          selectedChartSymbol = sym
          selectedChartTitle = name
          selectedChartCurrency = curr
        }
      )

      MarketTabMode.CALCULATORS -> MarketCalculatorsView(
        initialBuyPrice = prefilledBuyPrice,
        initialSellPrice = prefilledSellPrice,
        initialUnits = prefilledUnits,
        initialSymbol = prefilledSymbol
      )

      MarketTabMode.PORTFOLIO -> PortfolioScreen()
    }
  }
}

@Composable
private fun ChartProView(
  symbol: String,
  title: String,
  currency: String,
  timeframe: String,
  onTimeframeChange: (String) -> Unit,
  onSelectAsset: (symbol: String, name: String, currency: String) -> Unit
) {
  val basePrice = remember(symbol) {
    val stock = MarketCatalog.allStocks.find { it.symbol == symbol }
    val crypto = MarketCatalog.allCryptos.find { it.symbol == symbol }
    stock?.basePrice ?: crypto?.basePrice ?: 10000.0
  }

  val candles = remember(symbol, timeframe, basePrice) {
    MarketCatalog.generateCandles(symbol, basePrice, timeframe = timeframe, count = 45)
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 6.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 1. Asset Quick Chips (IHSG, BBCA, BBRI, BMRI, BTC, SOL, dll.)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("PILIH ASET SAHAM & KRIPTO:", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextMuted)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val quickAssets = listOf(
            Triple("IHSG", "Indeks IHSG", "IDR"),
            Triple("BBCA", "Bank BCA", "IDR"),
            Triple("BBRI", "Bank BRI", "IDR"),
            Triple("BMRI", "Bank Mandiri", "IDR"),
            Triple("TLKM", "Telkom", "IDR"),
            Triple("BREN", "Barito Renewables", "IDR"),
            Triple("GOTO", "GoTo", "IDR"),
            Triple("BTC", "Bitcoin", "USD"),
            Triple("ETH", "Ethereum", "USD"),
            Triple("SOL", "Solana", "USD"),
            Triple("BNB", "BNB Chain", "USD"),
            Triple("XRP", "Ripple", "USD"),
            Triple("SUI", "Sui Network", "USD")
          )
          quickAssets.forEach { (sym, name, curr) ->
            FilterChip(
              selected = symbol == sym,
              onClick = { onSelectAsset(sym, name, curr) },
              label = { Text(sym, fontSize = 11.sp, fontWeight = if (symbol == sym) FontWeight.Bold else FontWeight.Normal) },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentGreen, selectedLabelColor = DarkBackground)
            )
          }
        }
      }
    }

    // 2. Timeframe Selector (1D, 1W, 1M, 1H)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("1D", "1W", "1M", "1H").forEach { tf ->
            FilterChip(
              selected = timeframe == tf,
              onClick = { onTimeframeChange(tf) },
              label = { Text(tf, fontSize = 10.5.sp, fontWeight = FontWeight.Bold) },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentBlue, selectedLabelColor = DarkBackground)
            )
          }
        }
      }
    }

    // 3. Purbaya Sadewa Candlestick Chart View
    item {
      PurbayaMarketChartView(
        candles = candles,
        symbol = symbol,
        title = title,
        currency = currency
      )
    }

    // 4. Formula & Legend Guide
    item {
      Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("SPESIFIKASI INDIKATOR TEKNIKAL PURBAYA SADEWA:", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
          Text("• Overlay: Moving Average MA 5 (Cyan) & MA 20 (Kuning)", fontSize = 11.sp, color = TextPrimary)
          Text("• Volume: Batang volume harian dengan garis rata-rata 20 periode (VOL 20)", fontSize = 11.sp, color = TextPrimary)
          Text("• RSI 14: Saluran netral 30 - 70. Batas atas 70 (Overbought), Batas bawah 30 (Oversold)", fontSize = 11.sp, color = TextPrimary)
          Text("• Stochastic: Garis %K (14) dan %D (3) dengan level 80 / 20", fontSize = 11.sp, color = TextPrimary)
          Text("• MACD: Fast EMA 12, Slow EMA 26, Signal 9 dengan histogram hijau/merah", fontSize = 11.sp, color = TextPrimary)
        }
      }
    }
  }
}

@Composable
private fun CryptoMarketView(
  onOpenChart: (symbol: String, name: String, currency: String) -> Unit,
  onSendToCalculator: (symbol: String, priceIdr: Double) -> Unit
) {
  val cryptos = MarketCatalog.allCryptos

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(top = 4.dp),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    items(cryptos) { coin ->
      val isGain = coin.change24h >= 0
      val estIdrPrice = coin.basePrice * 15850.0

      Card(
        shape = RoundedCornerShape(8.dp),
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
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(shape = RoundedCornerShape(4.dp), color = DarkSurfaceVariant) {
                Text(
                  text = coin.symbol,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = AccentGreen,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
              Column {
                Text(text = coin.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(text = "${coin.exchange} • Cap ${coin.marketCapFormatted}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextMuted)
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "$%,.2f".format(Locale.US, coin.basePrice),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = String.format(Locale.US, "%+.2f%%", coin.change24h),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGain) StatusSuccess else StatusDanger
              )
            }
          }

          // Action Buttons: Chart Pro & Hitung Kalkulator
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { onOpenChart(coin.symbol, coin.name, "USD") },
              shape = RoundedCornerShape(4.dp),
              border = BorderStroke(1.dp, AccentGreen),
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
            ) {
              Icon(imageVector = Icons.Default.ShowChart, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Chart Pro", fontSize = 11.sp, color = AccentGreen, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = { onSendToCalculator(coin.symbol, estIdrPrice) },
              shape = RoundedCornerShape(4.dp),
              border = BorderStroke(1.dp, DarkCardBorder),
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
            ) {
              Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Kalkulator", fontSize = 11.sp, color = TextSecondary)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StockMarketView(
  onOpenChart: (symbol: String, name: String, currency: String) -> Unit,
  onSendToCalculator: (symbol: String, price: Double) -> Unit
) {
  val stocks = MarketCatalog.allStocks

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(top = 4.dp),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    items(stocks) { stock ->
      val isGain = stock.change24h >= 0

      Card(
        shape = RoundedCornerShape(8.dp),
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
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(shape = RoundedCornerShape(4.dp), color = DarkSurfaceVariant) {
                Text(
                  text = stock.symbol,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = AccentBlue,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
              Column {
                Text(text = stock.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(text = "${stock.exchange} • Vol ${stock.volumeFormatted}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextMuted)
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "Rp %,.0f".format(Locale.US, stock.basePrice),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = String.format(Locale.US, "%+.2f%%", stock.change24h),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGain) StatusSuccess else StatusDanger
              )
            }
          }

          // Action Buttons: Chart Pro & Hitung Kalkulator
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { onOpenChart(stock.symbol, stock.name, stock.currency) },
              shape = RoundedCornerShape(4.dp),
              border = BorderStroke(1.dp, AccentGreen),
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
            ) {
              Icon(imageVector = Icons.Default.ShowChart, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Chart Pro", fontSize = 11.sp, color = AccentGreen, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = { onSendToCalculator(stock.symbol, stock.basePrice) },
              shape = RoundedCornerShape(4.dp),
              border = BorderStroke(1.dp, DarkCardBorder),
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
            ) {
              Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Kalkulator", fontSize = 11.sp, color = TextSecondary)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MarketCalculatorsView(
  initialBuyPrice: String = "10000",
  initialSellPrice: String = "12500",
  initialUnits: String = "10",
  initialSymbol: String = "ASET"
) {
  val context = LocalContext.current
  val session by AuthManager.sessionState.collectAsState()

  var symbolInput by rememberSaveable { mutableStateOf(initialSymbol) }
  var buyPriceInput by rememberSaveable { mutableStateOf(initialBuyPrice) }
  var sellPriceInput by rememberSaveable { mutableStateOf(initialSellPrice) }
  var unitsInput by rememberSaveable { mutableStateOf(initialUnits) }
  var feePercentInput by rememberSaveable { mutableStateOf("0.15") }

  LaunchedEffect(initialBuyPrice, initialSellPrice, initialUnits, initialSymbol) {
    buyPriceInput = initialBuyPrice
    sellPriceInput = initialSellPrice
    unitsInput = initialUnits
    symbolInput = initialSymbol
  }

  val parsedBuyPrice = buyPriceInput.toDoubleOrNull()
  val parsedSellPrice = sellPriceInput.toDoubleOrNull()
  val parsedUnits = unitsInput.toDoubleOrNull()
  val parsedFee = feePercentInput.toDoubleOrNull() ?: 0.0

  val isPnlInputValid = parsedBuyPrice != null && parsedBuyPrice > 0.0 &&
    parsedSellPrice != null && parsedSellPrice >= 0.0 &&
    parsedUnits != null && parsedUnits > 0.0

  val buyValue = if (isPnlInputValid) (parsedBuyPrice!! * parsedUnits!!) else 0.0
  val buyFee = buyValue * (parsedFee / 100.0)
  val totalCapital = buyValue + buyFee

  val sellValue = if (isPnlInputValid) (parsedSellPrice!! * parsedUnits!!) else 0.0
  val sellFee = sellValue * (parsedFee / 100.0)
  val netSellResult = sellValue - sellFee

  val netProfitLoss = if (isPnlInputValid) (netSellResult - totalCapital) else 0.0
  val returnPercent = if (isPnlInputValid && totalCapital > 0.0) (netProfitLoss / totalCapital) * 100.0 else 0.0

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Kalkulator Profit / Loss Transaksi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

          OutlinedTextField(
            value = symbolInput,
            onValueChange = { symbolInput = it },
            label = { Text("Simbol Aset") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGreen, unfocusedBorderColor = DarkCardBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
          )

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = buyPriceInput,
              onValueChange = { buyPriceInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
              label = { Text("Harga Beli (Rp)") },
              modifier = Modifier.weight(1f),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGreen, unfocusedBorderColor = DarkCardBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )

            OutlinedTextField(
              value = sellPriceInput,
              onValueChange = { sellPriceInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
              label = { Text("Harga Jual (Rp)") },
              modifier = Modifier.weight(1f),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGreen, unfocusedBorderColor = DarkCardBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = unitsInput,
              onValueChange = { unitsInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
              label = { Text("Jumlah Unit / Lembar") },
              modifier = Modifier.weight(1f),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGreen, unfocusedBorderColor = DarkCardBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )

            OutlinedTextField(
              value = feePercentInput,
              onValueChange = { feePercentInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
              label = { Text("Fee (%)") },
              modifier = Modifier.weight(0.6f),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGreen, unfocusedBorderColor = DarkCardBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
          }

          Surface(shape = RoundedCornerShape(6.dp), color = DarkBackground, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text("HASIL PERHITUNGAN BERSIH:", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextMuted)
              Text(
                text = "Net P/L: ${PortfolioManager.formatRupiah(netProfitLoss, withSign = true)}",
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (netProfitLoss >= 0) StatusSuccess else StatusDanger
              )
              Text("Return on Investment (ROI): %+.2f%%".format(Locale.US, returnPercent), fontSize = 11.5.sp, color = AccentBlue)
            }
          }

          Button(
            onClick = {
              if (isPnlInputValid) {
                val record = TradeRecord(
                  id = "calc_${System.currentTimeMillis()}",
                  symbol = symbolInput.uppercase(),
                  assetType = "Saham",
                  entryPrice = parsedBuyPrice!!,
                  exitPrice = parsedSellPrice!!,
                  units = parsedUnits!!,
                  buyFeePercent = parsedFee,
                  sellFeePercent = parsedFee,
                  date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(java.util.Date()),
                  notes = "Disimpan dari Kalkulator Market"
                )
                PortfolioManager.addTrade(session.email, record)
                Toast.makeText(context, "Disimpan ke Portofolio!", Toast.LENGTH_SHORT).show()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Simpan ke Portofolio Saya", color = DarkBackground, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
