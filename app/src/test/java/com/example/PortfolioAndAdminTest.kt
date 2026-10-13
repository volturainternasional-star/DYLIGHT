package com.example

import com.example.auth.AuthConfig
import com.example.market.PortfolioManager
import com.example.market.RealMarketService
import com.example.market.TradeRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PortfolioAndAdminTest {

  @Test
  fun testAdminEmailConfiguration() {
    assertEquals("volturainternasional@gmail.com", AuthConfig.ADMIN_EMAIL)
  }

  @Test
  fun testTradeRecordCalculations() {
    val winTrade = TradeRecord(
      id = "T1",
      symbol = "BTC/USDT",
      assetType = "Crypto",
      entryPrice = 60000.0,
      exitPrice = 66000.0,
      units = 0.1,
      buyFeePercent = 0.1,
      sellFeePercent = 0.1,
      date = "2026-10-09"
    )

    // Buy value = 6000. Fee = 6. Total cost = 6006.
    // Sell value = 6600. Fee = 6.6. Net sell = 6593.4.
    // Net profit = 6593.4 - 6006 = 587.4
    assertTrue(winTrade.isWin)
    assertTrue(winTrade.netProfitLoss > 580.0)
    assertTrue(winTrade.roiPercent > 9.0)

    val lossTrade = TradeRecord(
      id = "T2",
      symbol = "ETH/USDT",
      assetType = "Crypto",
      entryPrice = 2500.0,
      exitPrice = 2400.0,
      units = 1.0,
      date = "2026-10-09"
    )
    assertFalse(lossTrade.isWin)
    assertTrue(lossTrade.netProfitLoss < 0.0)
  }

  @Test
  fun testPortfolioSummaryConsistency() {
    val trades = listOf(
      TradeRecord("T1", "BTC", "Crypto", 60000.0, 66000.0, 0.1, date = "2026-10-01"),
      TradeRecord("T2", "ETH", "Crypto", 2400.0, 2600.0, 1.0, date = "2026-10-02"),
      TradeRecord("T3", "SOL", "Crypto", 140.0, 135.0, 5.0, date = "2026-10-03")
    )

    val summary = PortfolioManager.calculateSummary(trades)
    assertEquals(3, summary.totalTrades)
    assertEquals(2, summary.winningTrades)
    assertEquals(1, summary.losingTrades)
    assertEquals(66.66, summary.winRatePercent, 0.5)
    assertTrue(summary.totalProfitLoss > 0.0)
    assertTrue(summary.consistencyScore in 10..99)
  }

  @Test
  fun testTechnicalIndicatorsMath() {
    val prices = listOf(10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0)
    val result = RealMarketService.calculateIndicators(prices)
    assertEquals(13.0, result.sma7, 0.01)
    assertTrue(result.rsi14 in 0.0..100.0)
    assertTrue(result.upperBollinger >= result.lowerBollinger)
  }
}
