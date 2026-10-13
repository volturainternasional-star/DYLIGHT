package com.example.market

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import kotlin.math.abs

data class TradeRecord(
  val id: String,
  val symbol: String,
  val assetType: String, // "Crypto" or "Saham"
  val entryPrice: Double,
  val exitPrice: Double,
  val units: Double,
  val buyFeePercent: Double = 0.15,
  val sellFeePercent: Double = 0.15,
  val date: String,
  val notes: String = "",
  val strategy: String = "Breakout / Momentum"
) {
  val totalBuyValue: Double get() = entryPrice * units
  val buyFee: Double get() = totalBuyValue * (buyFeePercent / 100.0)
  val totalCost: Double get() = totalBuyValue + buyFee

  val totalSellValue: Double get() = exitPrice * units
  val sellFee: Double get() = totalSellValue * (sellFeePercent / 100.0)
  val netSellResult: Double get() = totalSellValue - sellFee

  val netProfitLoss: Double get() = netSellResult - totalCost
  val roiPercent: Double get() = if (totalCost > 0.0) (netProfitLoss / totalCost) * 100.0 else 0.0
  val isWin: Boolean get() = netProfitLoss >= 0.0
}

data class PortfolioSummary(
  val totalTrades: Int,
  val winningTrades: Int,
  val losingTrades: Int,
  val winRatePercent: Double,
  val totalProfitLoss: Double,
  val totalCapitalTurnover: Double,
  val consistencyScore: Int, // 0 - 100
  val consistencyLabel: String,
  val avgReturnPercent: Double
)

object PortfolioManager {
  private const val PREFS_NAME = "gamesec_portfolio_storage"
  private var prefs: SharedPreferences? = null

  fun init(context: Context) {
    if (prefs == null) {
      prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
  }

  /**
   * 100% crash-proof rupiah currency formatting.
   * Eliminates any risk of MissingFormatWidthException.
   */
  fun formatRupiah(amount: Double, withSign: Boolean = false): String {
    val absVal = abs(amount)
    val formattedNumber = String.format(Locale.US, "%,.0f", absVal)
    return if (withSign) {
      when {
        amount > 0.0 -> "Rp +$formattedNumber"
        amount < 0.0 -> "Rp -$formattedNumber"
        else -> "Rp $formattedNumber"
      }
    } else {
      "Rp $formattedNumber"
    }
  }

  private fun getKeyForEmail(email: String): String {
    val clean = email.trim().lowercase()
    return if (clean.isEmpty()) {
      "portfolio_trades_guest"
    } else {
      "portfolio_trades_" + clean.replace("@", "_at_").replace(".", "_dot_")
    }
  }

  fun getTrades(email: String): List<TradeRecord> {
    val key = getKeyForEmail(email)
    val jsonString = prefs?.getString(key, null)

    if (jsonString.isNullOrEmpty()) {
      // Seed initial realistic trades for this user
      val seeded = getInitialSeedTrades(email)
      saveTrades(email, seeded)
      return seeded
    }

    val list = mutableListOf<TradeRecord>()
    try {
      val jsonArray = JSONArray(jsonString)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(
          TradeRecord(
            id = obj.optString("id", "TR-${System.currentTimeMillis()}-$i"),
            symbol = obj.optString("symbol", "BTC/USDT"),
            assetType = obj.optString("assetType", "Crypto"),
            entryPrice = obj.optDouble("entryPrice", 0.0),
            exitPrice = obj.optDouble("exitPrice", 0.0),
            units = obj.optDouble("units", 1.0),
            buyFeePercent = obj.optDouble("buyFeePercent", 0.15),
            sellFeePercent = obj.optDouble("sellFeePercent", 0.15),
            date = obj.optString("date", "2026-10-01"),
            notes = obj.optString("notes", ""),
            strategy = obj.optString("strategy", "Disiplin Risk-Reward")
          )
        )
      }
    } catch (_: Exception) {
      return getInitialSeedTrades(email)
    }
    return list
  }

  fun saveTrades(email: String, trades: List<TradeRecord>) {
    val key = getKeyForEmail(email)
    val jsonArray = JSONArray()
    for (t in trades) {
      val obj = JSONObject().apply {
        put("id", t.id)
        put("symbol", t.symbol)
        put("assetType", t.assetType)
        put("entryPrice", t.entryPrice)
        put("exitPrice", t.exitPrice)
        put("units", t.units)
        put("buyFeePercent", t.buyFeePercent)
        put("sellFeePercent", t.sellFeePercent)
        put("date", t.date)
        put("notes", t.notes)
        put("strategy", t.strategy)
      }
      jsonArray.put(obj)
    }
    prefs?.edit()?.putString(key, jsonArray.toString())?.apply()
  }

  fun addTrade(email: String, trade: TradeRecord): List<TradeRecord> {
    val current = getTrades(email).toMutableList()
    current.add(0, trade)
    saveTrades(email, current)
    return current
  }

  fun deleteTrade(email: String, tradeId: String): List<TradeRecord> {
    val current = getTrades(email).filter { it.id != tradeId }
    saveTrades(email, current)
    return current
  }

  fun clearAllTrades(email: String): List<TradeRecord> {
    saveTrades(email, emptyList())
    return emptyList()
  }

  fun resetToInitialSeed(email: String): List<TradeRecord> {
    val seeded = getInitialSeedTrades(email)
    saveTrades(email, seeded)
    return seeded
  }

  fun calculateSummary(trades: List<TradeRecord>): PortfolioSummary {
    if (trades.isEmpty()) {
      return PortfolioSummary(
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

    val totalTrades = trades.size
    val winningTrades = trades.count { it.isWin }
    val losingTrades = totalTrades - winningTrades
    val winRate = (winningTrades.toDouble() / totalTrades) * 100.0

    val totalProfitLoss = trades.sumOf { it.netProfitLoss }
    val totalTurnover = trades.sumOf { it.totalCost }
    val avgReturn = trades.map { it.roiPercent }.average()

    // Calculate Consistency Score (0 - 100):
    val winRateScore = (winRate * 0.50).coerceIn(0.0, 50.0)
    val profitScore = if (totalProfitLoss > 0) 30.0 else (30.0 + (totalProfitLoss / 100000.0)).coerceIn(5.0, 30.0)
    val experienceBonus = (totalTrades * 4.0).coerceIn(0.0, 20.0)
    val score = (winRateScore + profitScore + experienceBonus).toInt().coerceIn(10, 99)

    val label = when {
      score >= 80 -> "KONSISTENSI TINGGI (Disiplin Eksekusi)"
      score >= 60 -> "KONSISTEN MODERAT (Risk-Reward Stabil)"
      else -> "PERLU EVALUASI RISIKO (Jaga Money Management)"
    }

    return PortfolioSummary(
      totalTrades = totalTrades,
      winningTrades = winningTrades,
      losingTrades = losingTrades,
      winRatePercent = winRate,
      totalProfitLoss = totalProfitLoss,
      totalCapitalTurnover = totalTurnover,
      consistencyScore = score,
      consistencyLabel = label,
      avgReturnPercent = avgReturn
    )
  }

  private fun getInitialSeedTrades(email: String): List<TradeRecord> {
    return if (email.contains("volturainternasional", ignoreCase = true)) {
      // Realistic trade records for admin account
      listOf(
        TradeRecord(
          id = "TR-ADM-01",
          symbol = "BTC/USDT",
          assetType = "Crypto",
          entryPrice = 58200.0,
          exitPrice = 63400.0,
          units = 0.15,
          date = "2026-10-08",
          notes = "Breakout resistance harian dengan volume konfirmasi",
          strategy = "Trend Following"
        ),
        TradeRecord(
          id = "TR-ADM-02",
          symbol = "BBCA.JK",
          assetType = "Saham",
          entryPrice = 9800.0,
          exitPrice = 10450.0,
          units = 500.0,
          date = "2026-10-06",
          notes = "Rebound support EMA 50 pasca dividen cum date",
          strategy = "Swing Support"
        ),
        TradeRecord(
          id = "TR-ADM-03",
          symbol = "ETH/USDT",
          assetType = "Crypto",
          entryPrice = 2540.0,
          exitPrice = 2470.0,
          units = 1.2,
          date = "2026-10-03",
          notes = "Cut loss terukur saat pola head-and-shoulders terpicu",
          strategy = "Strict Stop Loss"
        ),
        TradeRecord(
          id = "TR-ADM-04",
          symbol = "SOL/USDT",
          assetType = "Crypto",
          entryPrice = 132.5,
          exitPrice = 148.0,
          units = 12.0,
          date = "2026-09-28",
          notes = "Momentum breakout ekosistem DeFi",
          strategy = "Momentum Surge"
        )
      )
    } else {
      // Realistic trade records for general user
      listOf(
        TradeRecord(
          id = "TR-USR-01",
          symbol = "BTC/USDT",
          assetType = "Crypto",
          entryPrice = 61200.0,
          exitPrice = 63800.0,
          units = 0.08,
          date = "2026-10-07",
          notes = "Entry retest support horizontal",
          strategy = "Support Retest"
        ),
        TradeRecord(
          id = "TR-USR-02",
          symbol = "TLKM.JK",
          assetType = "Saham",
          entryPrice = 3020.0,
          exitPrice = 3180.0,
          units = 800.0,
          date = "2026-10-05",
          notes = "Akumulasi teknikal saham telekomunikasi",
          strategy = "Value Swing"
        )
      )
    }
  }
}
