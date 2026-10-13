package com.example.market

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

data class LiveMarketQuote(
  val symbol: String,
  val name: String,
  val priceUsd: Double,
  val priceIdr: Double,
  val change24h: Double,
  val volumeUsd: String,
  val marketCapUsd: String,
  val isLiveFetched: Boolean,
  val timestamp: String,
  val sourceUrl: String,
  val latencyMs: Long
)

sealed class MarketFeedState {
  object Loading : MarketFeedState()
  data class Success(val quotes: List<LiveMarketQuote>, val latencyMs: Long, val provider: String) : MarketFeedState()
  data class Offline(val message: String, val cachedQuotes: List<LiveMarketQuote>) : MarketFeedState()
}

data class TechnicalIndicatorResult(
  val sma7: Double,
  val sma25: Double,
  val rsi14: Double,
  val rsiSignal: String,
  val upperBollinger: Double,
  val lowerBollinger: Double
)

object RealMarketService {

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(5, TimeUnit.SECONDS)
    .build()

  private const val USD_TO_IDR_ESTIMATE = 15850.0

  /**
   * Fetches real live market data using Binance 24h ticker first (ultra fast, high reliability),
   * falling back to CoinGecko, or returning clean Offline state if network fails.
   */
  suspend fun fetchMarketFeed(): MarketFeedState = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()

    // 1. Try Binance public 24h ticker API
    try {
      val binanceUrl = "https://api.binance.com/api/v3/ticker/24hr?symbols=[%22BTCUSDT%22,%22ETHUSDT%22,%22SOLUSDT%22,%22BNBUSDT%22,%22XRPUSDT%22]"
      val request = Request.Builder()
        .url(binanceUrl)
        .header("User-Agent", "GameSecLab/1.0 (Android)")
        .build()

      val response = httpClient.newCall(request).execute()
      val latency = System.currentTimeMillis() - startTime

      if (response.isSuccessful) {
        val body = response.body?.string() ?: ""
        val jsonArray = JSONArray(body)
        val nameMap = mapOf(
          "BTCUSDT" to Pair("BTC", "Bitcoin"),
          "ETHUSDT" to Pair("ETH", "Ethereum"),
          "SOLUSDT" to Pair("SOL", "Solana"),
          "BNBUSDT" to Pair("BNB", "BNB Chain"),
          "XRPUSDT" to Pair("XRP", "Ripple")
        )

        val timeString = java.text.SimpleDateFormat("HH:mm:ss 'WIB'", Locale.US).format(java.util.Date())
        val quotes = mutableListOf<LiveMarketQuote>()

        for (i in 0 until jsonArray.length()) {
          val item = jsonArray.getJSONObject(i)
          val sym = item.optString("symbol")
          val pair = nameMap[sym] ?: continue
          val lastPrice = item.optDouble("lastPrice", 0.0)
          val priceChangePercent = item.optDouble("priceChangePercent", 0.0)
          val quoteVolume = item.optDouble("quoteVolume", 0.0)

          val estMarketCap = when (pair.first) {
            "BTC" -> lastPrice * 19_750_000.0
            "ETH" -> lastPrice * 120_400_000.0
            "SOL" -> lastPrice * 470_000_000.0
            "BNB" -> lastPrice * 145_000_000.0
            "XRP" -> lastPrice * 56_000_000_000.0
            else -> lastPrice * 10_000_000.0
          }

          quotes.add(
            LiveMarketQuote(
              symbol = pair.first,
              name = pair.second,
              priceUsd = lastPrice,
              priceIdr = lastPrice * USD_TO_IDR_ESTIMATE,
              change24h = priceChangePercent,
              volumeUsd = formatCurrencyShort(quoteVolume),
              marketCapUsd = formatCurrencyShort(estMarketCap),
              isLiveFetched = true,
              timestamp = timeString,
              sourceUrl = "api.binance.com",
              latencyMs = latency
            )
          )
        }

        if (quotes.isNotEmpty()) {
          return@withContext MarketFeedState.Success(quotes, latency, "Binance Public Feed")
        }
      }
    } catch (_: Exception) {
      // Continue to CoinGecko fallback
    }

    // 2. Try CoinGecko Public API
    try {
      val coinGeckoUrl = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,solana,binancecoin,ripple&vs_currencies=usd,idr&include_24hr_change=true&include_24hr_vol=true&include_market_cap=true"
      val request = Request.Builder()
        .url(coinGeckoUrl)
        .header("User-Agent", "GameSecLab/1.0 (Android)")
        .build()

      val response = httpClient.newCall(request).execute()
      val latency = System.currentTimeMillis() - startTime

      if (response.isSuccessful) {
        val body = response.body?.string() ?: ""
        val json = JSONObject(body)
        val coinDefs = listOf(
          Triple("bitcoin", "BTC", "Bitcoin"),
          Triple("ethereum", "ETH", "Ethereum"),
          Triple("solana", "SOL", "Solana"),
          Triple("binancecoin", "BNB", "BNB Chain"),
          Triple("ripple", "XRP", "Ripple")
        )
        val timeString = java.text.SimpleDateFormat("HH:mm:ss 'WIB'", Locale.US).format(java.util.Date())
        val quotes = mutableListOf<LiveMarketQuote>()

        for ((id, sym, name) in coinDefs) {
          if (json.has(id)) {
            val obj = json.getJSONObject(id)
            val usd = obj.optDouble("usd", 0.0)
            val idr = obj.optDouble("idr", usd * USD_TO_IDR_ESTIMATE)
            val change = obj.optDouble("usd_24h_change", 0.0)
            val vol = obj.optDouble("usd_24h_vol", 0.0)
            val mcap = obj.optDouble("usd_market_cap", 0.0)

            quotes.add(
              LiveMarketQuote(
                symbol = sym,
                name = name,
                priceUsd = usd,
                priceIdr = idr,
                change24h = change,
                volumeUsd = formatCurrencyShort(vol),
                marketCapUsd = formatCurrencyShort(mcap),
                isLiveFetched = true,
                timestamp = timeString,
                sourceUrl = "api.coingecko.com",
                latencyMs = latency
              )
            )
          }
        }

        if (quotes.isNotEmpty()) {
          return@withContext MarketFeedState.Success(quotes, latency, "Global Market Liquidity Network")
        }
      }
    } catch (_: Exception) {
      // Offline
    }

    // Return explicit Offline state with cached data
    val fallbackTime = java.text.SimpleDateFormat("HH:mm 'WIB'", Locale.US).format(java.util.Date())
    val cached = listOf(
      LiveMarketQuote("BTC", "Bitcoin", 62450.0, 975000000.0, 2.45, "$28.4B", "$1.23T", false, fallbackTime, "Local Cache", 0L),
      LiveMarketQuote("ETH", "Ethereum", 2450.5, 38200000.0, -1.15, "$14.2B", "$294.8B", false, fallbackTime, "Local Cache", 0L),
      LiveMarketQuote("SOL", "Solana", 144.2, 2250000.0, 4.80, "$3.6B", "$67.3B", false, fallbackTime, "Local Cache", 0L)
    )

    return@withContext MarketFeedState.Offline(
      message = "Data harga tidak tersedia (mode offline)",
      cachedQuotes = cached
    )
  }

  suspend fun fetchLiveCryptoPrices(): List<LiveMarketQuote> {
    return when (val state = fetchMarketFeed()) {
      is MarketFeedState.Success -> state.quotes
      is MarketFeedState.Offline -> state.cachedQuotes
      is MarketFeedState.Loading -> emptyList()
    }
  }

  /**
   * Real calculation of technical indicators on price points.
   */
  fun calculateIndicators(prices: List<Double>): TechnicalIndicatorResult {
    if (prices.isEmpty()) {
      return TechnicalIndicatorResult(0.0, 0.0, 50.0, "NETRAL", 0.0, 0.0)
    }

    val p7 = prices.takeLast(7)
    val sma7 = p7.average()

    val p25 = prices.takeLast(25)
    val sma25 = p25.average()

    var gain = 0.0
    var loss = 0.0
    val rsiWindow = prices.takeLast(15)
    if (rsiWindow.size >= 2) {
      for (i in 1 until rsiWindow.size) {
        val delta = rsiWindow[i] - rsiWindow[i - 1]
        if (delta > 0) gain += delta else loss += -delta
      }
      val avgGain = gain / (rsiWindow.size - 1)
      val avgLoss = loss / (rsiWindow.size - 1)
      val rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
      val rsi = 100.0 - (100.0 / (1.0 + rs))

      val signal = when {
        rsi <= 30.0 -> "OVERSOLD (Peluang Rebound)"
        rsi >= 70.0 -> "OVERBOUGHT (Peluang Koreksi)"
        else -> "NETRAL"
      }

      val mean = sma7
      val variance = p7.map { (it - mean) * (it - mean) }.average()
      val stdDev = sqrt(variance)
      val upper = mean + (2 * stdDev)
      val lower = mean - (2 * stdDev)

      return TechnicalIndicatorResult(
        sma7 = sma7,
        sma25 = sma25,
        rsi14 = rsi,
        rsiSignal = signal,
        upperBollinger = upper,
        lowerBollinger = lower
      )
    }

    return TechnicalIndicatorResult(sma7, sma25, 50.0, "NETRAL", sma7 * 1.05, sma7 * 0.95)
  }

  private fun formatCurrencyShort(amount: Double): String {
    return when {
      amount >= 1_000_000_000_000.0 -> String.format(Locale.US, "$%.2fT", amount / 1_000_000_000_000.0)
      amount >= 1_000_000_000.0 -> String.format(Locale.US, "$%.2fB", amount / 1_000_000_000.0)
      amount >= 1_000_000.0 -> String.format(Locale.US, "$%.2fM", amount / 1_000_000.0)
      else -> String.format(Locale.US, "$%.0f", amount)
    }
  }
}
