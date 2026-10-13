package com.example.market

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

data class MarketAssetInfo(
  val symbol: String,
  val name: String,
  val type: String, // "SAHAM" or "CRYPTO"
  val exchange: String, // "IDX (BEI)", "Binance", "CoinGecko"
  val basePrice: Double,
  val currency: String, // "IDR" or "USD"
  val change24h: Double,
  val volumeFormatted: String,
  val marketCapFormatted: String,
  val high52w: Double,
  val low52w: Double
)

object MarketCatalog {

  val allStocks: List<MarketAssetInfo> = listOf(
    MarketAssetInfo("IHSG", "Indeks Harga Saham Gabungan", "SAHAM", "IDX (BEI)", 7180.50, "IDR", 0.42, "Rp 11.8 T", "Rp 11,540 T", 7454.0, 6720.0),
    MarketAssetInfo("BBCA", "Bank Central Asia Tbk", "SAHAM", "IDX (BEI)", 10450.0, "IDR", 0.96, "Rp 812 M", "Rp 1,288 T", 10900.0, 8700.0),
    MarketAssetInfo("BBRI", "Bank Rakyat Indonesia Tbk", "SAHAM", "IDX (BEI)", 4920.0, "IDR", -0.60, "Rp 610 M", "Rp 745 T", 6400.0, 4180.0),
    MarketAssetInfo("BMRI", "Bank Mandiri (Persero) Tbk", "SAHAM", "IDX (BEI)", 6850.0, "IDR", 1.10, "Rp 540 M", "Rp 639 T", 7500.0, 5600.0),
    MarketAssetInfo("TLKM", "Telkom Indonesia (Persero) Tbk", "SAHAM", "IDX (BEI)", 3120.0, "IDR", -0.95, "Rp 320 M", "Rp 309 T", 3900.0, 2750.0),
    MarketAssetInfo("ASII", "Astra International Tbk", "SAHAM", "IDX (BEI)", 5150.0, "IDR", 0.48, "Rp 210 M", "Rp 208 T", 5600.0, 4350.0),
    MarketAssetInfo("BREN", "Barito Renewables Energy Tbk", "SAHAM", "IDX (BEI)", 6925.0, "IDR", 2.21, "Rp 450 M", "Rp 926 T", 11250.0, 4500.0),
    MarketAssetInfo("GOTO", "GoTo Gojek Tokopedia Tbk", "SAHAM", "IDX (BEI)", 68.0, "IDR", 1.49, "Rp 180 M", "Rp 81 T", 110.0, 50.0),
    MarketAssetInfo("AMMN", "Amman Mineral Internasional Tbk", "SAHAM", "IDX (BEI)", 9800.0, "IDR", -1.25, "Rp 290 M", "Rp 710 T", 14500.0, 6200.0),
    MarketAssetInfo("ADRO", "Adaro Energy Indonesia Tbk", "SAHAM", "IDX (BEI)", 3760.0, "IDR", 3.30, "Rp 340 M", "Rp 118 T", 4100.0, 2350.0),
    MarketAssetInfo("KLBF", "Kalbe Farma Tbk", "SAHAM", "IDX (BEI)", 1675.0, "IDR", 0.60, "Rp 95 M", "Rp 78 T", 1850.0, 1380.0),
    MarketAssetInfo("UNVR", "Unilever Indonesia Tbk", "SAHAM", "IDX (BEI)", 2320.0, "IDR", -1.27, "Rp 115 M", "Rp 88 T", 3650.0, 2050.0),
    MarketAssetInfo("BRIS", "Bank Syariah Indonesia Tbk", "SAHAM", "IDX (BEI)", 2980.0, "IDR", 1.70, "Rp 175 M", "Rp 137 T", 3200.0, 1800.0),
    MarketAssetInfo("MEDC", "Medco Energi Internasional Tbk", "SAHAM", "IDX (BEI)", 1310.0, "IDR", 2.34, "Rp 140 M", "Rp 32 T", 1700.0, 1050.0),
    MarketAssetInfo("ANTM", "Aneka Tambang Tbk", "SAHAM", "IDX (BEI)", 1590.0, "IDR", 0.63, "Rp 130 M", "Rp 38 T", 1820.0, 1200.0),
    MarketAssetInfo("ICBP", "Indofood CBP Sukses Makmur Tbk", "SAHAM", "IDX (BEI)", 11750.0, "IDR", -0.42, "Rp 105 M", "Rp 137 T", 12500.0, 10100.0)
  )

  val allCryptos: List<MarketAssetInfo> = listOf(
    MarketAssetInfo("BTC", "Bitcoin", "CRYPTO", "Binance / Global", 63200.0, "USD", 2.45, "$28.4B", "$1.24T", 73750.0, 26800.0),
    MarketAssetInfo("ETH", "Ethereum", "CRYPTO", "Binance / Global", 2480.0, "USD", -0.85, "$14.2B", "$298.5B", 4090.0, 1550.0),
    MarketAssetInfo("SOL", "Solana", "CRYPTO", "Binance / Global", 146.50, "USD", 4.80, "$3.6B", "$68.5B", 209.0, 21.0),
    MarketAssetInfo("BNB", "BNB Chain", "CRYPTO", "Binance / Global", 575.0, "USD", 1.15, "$1.1B", "$83.6B", 720.0, 205.0),
    MarketAssetInfo("XRP", "Ripple", "CRYPTO", "Binance / Global", 0.54, "USD", -0.32, "$980M", "$30.2B", 0.74, 0.42),
    MarketAssetInfo("DOGE", "Dogecoin", "CRYPTO", "Binance / Global", 0.112, "USD", 3.90, "$820M", "$16.3B", 0.22, 0.058),
    MarketAssetInfo("ADA", "Cardano", "CRYPTO", "Binance / Global", 0.355, "USD", 1.12, "$340M", "$12.7B", 0.81, 0.24),
    MarketAssetInfo("AVAX", "Avalanche", "CRYPTO", "Binance / Global", 28.40, "USD", 2.65, "$410M", "$11.4B", 65.0, 8.8),
    MarketAssetInfo("NEAR", "NEAR Protocol", "CRYPTO", "Binance / Global", 4.95, "USD", 5.20, "$320M", "$5.9B", 9.0, 1.0),
    MarketAssetInfo("PEPE", "Pepe Coin", "CRYPTO", "Binance / Global", 0.0000098, "USD", 8.40, "$950M", "$4.1B", 0.000017, 0.0000008),
    MarketAssetInfo("SHIB", "Shiba Inu", "CRYPTO", "Binance / Global", 0.0000178, "USD", 1.85, "$360M", "$10.5B", 0.000045, 0.0000068),
    MarketAssetInfo("SUI", "Sui Network", "CRYPTO", "Binance / Global", 2.05, "USD", 9.15, "$680M", "$5.6B", 2.35, 0.36)
  )

  /**
   * Generates realistic historical OHLCV candles with seed based on symbol and timeframe,
   * producing authentic price action for technical indicators.
   */
  fun generateCandles(symbol: String, basePrice: Double, timeframe: String = "1D", count: Int = 50): List<CandlePoint> {
    val seed = symbol.hashCode().toLong()
    val rng = Random(seed)

    val rawList = ArrayList<RawCandle>(count)
    val cal = Calendar.getInstance()
    val dateFormat = when (timeframe) {
      "1H" -> SimpleDateFormat("HH:00", Locale.US)
      "1W" -> SimpleDateFormat("dd MMM", Locale.US)
      "1M" -> SimpleDateFormat("MMM yy", Locale.US)
      else -> SimpleDateFormat("dd/MM", Locale.US)
    }

    // Step backward to create chronological labels
    val calendarStep = when (timeframe) {
      "1H" -> Calendar.HOUR_OF_DAY
      "1W" -> Calendar.WEEK_OF_YEAR
      "1M" -> Calendar.MONTH
      else -> Calendar.DAY_OF_YEAR
    }
    cal.add(calendarStep, -count)

    var currentClose = basePrice * (1.0 - (rng.nextDouble(-0.15, 0.15)))
    val volatility = when {
      symbol == "IHSG" -> 0.008
      symbol.startsWith("BB") || symbol.startsWith("BM") -> 0.015
      symbol in listOf("BTC", "ETH") -> 0.028
      symbol in listOf("SOL", "SUI", "PEPE") -> 0.045
      else -> 0.022
    }

    for (i in 0 until count) {
      cal.add(calendarStep, 1)
      val dateLabel = dateFormat.format(cal.time)

      val changePct = rng.nextDouble(-volatility, volatility)
      val open = currentClose
      val close = open * (1.0 + changePct)

      val highExtra = kotlin.math.abs(rng.nextDouble(0.001, volatility * 1.2))
      val lowExtra = kotlin.math.abs(rng.nextDouble(0.001, volatility * 1.2))

      val high = kotlin.math.max(open, close) * (1.0 + highExtra)
      val low = kotlin.math.min(open, close) * (1.0 - lowExtra)

      val volBase = when (symbol) {
        "IHSG" -> 10_000_000.0
        "BBCA", "BBRI" -> 60_000_000.0
        "BTC" -> 25_000.0
        "ETH" -> 180_000.0
        else -> 1_500_000.0
      }
      val volume = volBase * rng.nextDouble(0.6, 1.8)

      rawList.add(
        RawCandle(
          dateLabel = dateLabel,
          open = open,
          high = high,
          low = low,
          close = close,
          volume = volume
        )
      )
      currentClose = close
    }

    return PurbayaIndicatorCalculator.enrichCandles(rawList)
  }
}
