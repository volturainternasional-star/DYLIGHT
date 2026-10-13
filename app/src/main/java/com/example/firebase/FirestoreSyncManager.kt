package com.example.firebase

import android.content.Context
import android.util.Log
import com.example.auth.AuthManager
import com.example.market.PortfolioManager
import com.example.market.TradeRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirestoreSyncManager {
  private const val TAG = "FirestoreSync"

  suspend fun syncUserData(context: Context): Result<String> = withContext(Dispatchers.IO) {
    val session = AuthManager.sessionState.value
    if (!session.isLoggedIn || session.email.isBlank()) {
      return@withContext Result.failure(IllegalStateException("Pengguna belum login akun Google."))
    }

    try {
      val db = FirestoreDatabaseProvider.get(context)
      val userDoc = db.collection("users").document(session.email)

      val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
      val profileData = mapOf(
        "email" to session.email,
        "displayName" to session.displayName,
        "lastActive" to dateStr,
        "platform" to "Android GameSec Lab"
      )
      userDoc.set(profileData, SetOptions.merge()).await()

      val localTrades = PortfolioManager.getTrades(session.email)
      var syncedCount = 0
      for (trade in localTrades) {
        val tradeMap = mapOf(
          "id" to trade.id,
          "symbol" to trade.symbol,
          "assetType" to trade.assetType,
          "entryPrice" to trade.entryPrice,
          "exitPrice" to trade.exitPrice,
          "units" to trade.units,
          "netProfitLoss" to trade.netProfitLoss,
          "date" to trade.date,
          "notes" to trade.notes
        )
        userDoc.collection("portfolio").document(trade.id).set(tradeMap, SetOptions.merge()).await()
        syncedCount++
      }

      Result.success("Sinkronisasi cloud berhasil: $syncedCount trade tersimpan ke Firestore.")
    } catch (e: Exception) {
      Log.w(TAG, "Firestore sync note: ${e.message}")
      Result.failure(e)
    }
  }
}
