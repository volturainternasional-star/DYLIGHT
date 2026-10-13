package com.example.market

import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Data structure representing a single candlestick (OHLCV).
 */
data class CandlePoint(
  val index: Int,
  val dateLabel: String,
  val open: Double,
  val high: Double,
  val low: Double,
  val close: Double,
  val volume: Double,
  val ma5: Double? = null,
  val ma20: Double? = null,
  val volMa20: Double? = null,
  val rsi14: Double? = null,
  val stochK: Double? = null,
  val stochD: Double? = null,
  val macdLine: Double? = null,
  val macdSignal: Double? = null,
  val macdHistogram: Double? = null
) {
  val isGreen: Boolean get() = close >= open
  val changePercent: Double get() = if (open > 0.0) ((close - open) / open) * 100.0 else 0.0
}

/**
 * Engine to calculate technical indicators:
 * - MA 5 & MA 20
 * - VOL 20 (Volume Moving Average)
 * - RSI 14 (14, 14, 70, 30)
 * - Stochastic (14, 3, 3, 80, 20)
 * - MACD (12, 26, 9)
 * Matching the exact specification from https://purbayasadewa.id/idx/saham/IHSG
 */
object PurbayaIndicatorCalculator {

  fun enrichCandles(rawCandles: List<RawCandle>): List<CandlePoint> {
    if (rawCandles.isEmpty()) return emptyList()

    val closes = rawCandles.map { it.close }
    val highs = rawCandles.map { it.high }
    val lows = rawCandles.map { it.low }
    val volumes = rawCandles.map { it.volume }

    // 1. MA 5 & MA 20
    val ma5List = calculateSMA(closes, 5)
    val ma20List = calculateSMA(closes, 20)
    val volMa20List = calculateSMA(volumes, 20)

    // 2. RSI 14 (Wilder's Smoothing / Standard RSI)
    val rsi14List = calculateRSI(closes, 14)

    // 3. Stochastic Oscillator (14, 3, 3)
    val (stochKList, stochDList) = calculateStochastic(highs, lows, closes, 14, 3, 3)

    // 4. MACD (12, 26, 9)
    val (macdLineList, macdSignalList, macdHistList) = calculateMACD(closes, 12, 26, 9)

    return rawCandles.mapIndexed { i, c ->
      CandlePoint(
        index = i,
        dateLabel = c.dateLabel,
        open = c.open,
        high = c.high,
        low = c.low,
        close = c.close,
        volume = c.volume,
        ma5 = ma5List.getOrNull(i),
        ma20 = ma20List.getOrNull(i),
        volMa20 = volMa20List.getOrNull(i),
        rsi14 = rsi14List.getOrNull(i),
        stochK = stochKList.getOrNull(i),
        stochD = stochDList.getOrNull(i),
        macdLine = macdLineList.getOrNull(i),
        macdSignal = macdSignalList.getOrNull(i),
        macdHistogram = macdHistList.getOrNull(i)
      )
    }
  }

  private fun calculateSMA(values: List<Double>, period: Int): List<Double?> {
    val result = ArrayList<Double?>(values.size)
    var sum = 0.0
    for (i in values.indices) {
      sum += values[i]
      if (i >= period) {
        sum -= values[i - period]
      }
      if (i >= period - 1) {
        result.add(sum / period)
      } else {
        result.add(null)
      }
    }
    return result
  }

  private fun calculateRSI(prices: List<Double>, period: Int): List<Double?> {
    val result = ArrayList<Double?>(prices.size)
    if (prices.size <= period) {
      return List(prices.size) { null }
    }

    var avgGain = 0.0
    var avgLoss = 0.0

    // Initial period
    for (i in 1..period) {
      val change = prices[i] - prices[i - 1]
      if (change > 0) avgGain += change else avgLoss += -change
    }
    avgGain /= period
    avgLoss /= period

    for (i in 0 until period) {
      result.add(null)
    }

    var rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
    var rsi = 100.0 - (100.0 / (1.0 + rs))
    result.add(rsi)

    // Smoothed subsequent periods
    for (i in (period + 1) until prices.size) {
      val change = prices[i] - prices[i - 1]
      val gain = if (change > 0) change else 0.0
      val loss = if (change < 0) -change else 0.0

      avgGain = ((avgGain * (period - 1)) + gain) / period
      avgLoss = ((avgLoss * (period - 1)) + loss) / period

      rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
      rsi = 100.0 - (100.0 / (1.0 + rs))
      result.add(rsi.coerceIn(0.0, 100.0))
    }
    return result
  }

  private fun calculateStochastic(
    highs: List<Double>,
    lows: List<Double>,
    closes: List<Double>,
    kPeriod: Int,
    kSlowing: Int,
    dPeriod: Int
  ): Pair<List<Double?>, List<Double?>> {
    val rawK = ArrayList<Double?>()

    for (i in closes.indices) {
      if (i < kPeriod - 1) {
        rawK.add(null)
      } else {
        var highest = Double.MIN_VALUE
        var lowest = Double.MAX_VALUE
        for (j in (i - kPeriod + 1)..i) {
          if (highs[j] > highest) highest = highs[j]
          if (lows[j] < lowest) lowest = lows[j]
        }
        val currentClose = closes[i]
        val range = highest - lowest
        val kVal = if (range > 0.0) ((currentClose - lowest) / range) * 100.0 else 50.0
        rawK.add(kVal.coerceIn(0.0, 100.0))
      }
    }

    // Smooth %K by kSlowing
    val stochK = ArrayList<Double?>()
    for (i in rawK.indices) {
      if (i < (kPeriod - 1 + kSlowing - 1)) {
        stochK.add(null)
      } else {
        val window = rawK.subList(i - kSlowing + 1, i + 1).filterNotNull()
        if (window.size == kSlowing) {
          stochK.add(window.average().coerceIn(0.0, 100.0))
        } else {
          stochK.add(null)
        }
      }
    }

    // %D is SMA of %K over dPeriod
    val stochD = ArrayList<Double?>()
    for (i in stochK.indices) {
      val window = (0 until dPeriod).mapNotNull { offset ->
        val idx = i - offset
        if (idx >= 0) stochK[idx] else null
      }
      if (window.size == dPeriod) {
        stochD.add(window.average().coerceIn(0.0, 100.0))
      } else {
        stochD.add(null)
      }
    }

    return Pair(stochK, stochD)
  }

  private fun calculateMACD(
    prices: List<Double>,
    fastPeriod: Int = 12,
    slowPeriod: Int = 26,
    signalPeriod: Int = 9
  ): Triple<List<Double?>, List<Double?>, List<Double?>> {
    val fastEma = calculateEMA(prices, fastPeriod)
    val slowEma = calculateEMA(prices, slowPeriod)

    val macdLine = ArrayList<Double?>()
    for (i in prices.indices) {
      val f = fastEma[i]
      val s = slowEma[i]
      if (f != null && s != null) {
        macdLine.add(f - s)
      } else {
        macdLine.add(null)
      }
    }

    // Signal is EMA of MACD Line
    val validMacdStart = macdLine.indexOfFirst { it != null }
    val macdSignal = ArrayList<Double?>()
    val macdHist = ArrayList<Double?>()

    if (validMacdStart == -1 || macdLine.size - validMacdStart < signalPeriod) {
      return Triple(macdLine, List(prices.size) { null }, List(prices.size) { null })
    }

    val nonNullMacd = macdLine.subList(validMacdStart, macdLine.size).filterNotNull()
    val signalValues = calculateEMA(nonNullMacd, signalPeriod)

    var signalIdx = 0
    for (i in prices.indices) {
      if (i < validMacdStart) {
        macdSignal.add(null)
        macdHist.add(null)
      } else {
        val sig = signalValues.getOrNull(signalIdx++)
        val m = macdLine[i]
        macdSignal.add(sig)
        if (m != null && sig != null) {
          macdHist.add(m - sig)
        } else {
          macdHist.add(null)
        }
      }
    }

    return Triple(macdLine, macdSignal, macdHist)
  }

  private fun calculateEMA(values: List<Double>, period: Int): List<Double?> {
    val result = ArrayList<Double?>(values.size)
    if (values.size < period) {
      return List(values.size) { null }
    }

    val multiplier = 2.0 / (period + 1.0)
    var initialSma = 0.0
    for (i in 0 until period) {
      initialSma += values[i]
      result.add(null)
    }
    initialSma /= period
    result[period - 1] = initialSma

    var currentEma = initialSma
    for (i in period until values.size) {
      currentEma = ((values[i] - currentEma) * multiplier) + currentEma
      result.add(currentEma)
    }
    return result
  }
}

data class RawCandle(
  val dateLabel: String,
  val open: Double,
  val high: Double,
  val low: Double,
  val close: Double,
  val volume: Double
)
