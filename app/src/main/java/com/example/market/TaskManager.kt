package com.example.market

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.ToneGenerator
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MarketTask(
  val id: String,
  val title: String,
  val assetSymbol: String, // e.g. "IHSG", "BBCA", "BTC", "SOL", "UMUM"
  val assetType: String, // "Saham", "Crypto", "Teknikal", "Keamanan"
  val pageNumber: Int,
  val createdAtFormatted: String,
  val description: String,
  val imageUri: String? = null,
  val isCompleted: Boolean = false,
  val hasTimer: Boolean = true,
  val timerDurationSeconds: Int = 900, // Default 15 minutes
  val notes: String = ""
)

object TaskSoundHelper {
  private var toneGenerator: ToneGenerator? = null

  private fun getToneGen(): ToneGenerator {
    if (toneGenerator == null) {
      try {
        toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
      } catch (_: Exception) {}
    }
    return toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 85)
  }

  fun playReminderBeep() {
    try {
      getToneGen().startTone(ToneGenerator.TONE_PROP_BEEP2, 280)
    } catch (_: Exception) {}
  }

  fun playTimerDoneAlert() {
    try {
      getToneGen().startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
    } catch (_: Exception) {}
  }

  fun playSuccessChime() {
    try {
      getToneGen().startTone(ToneGenerator.TONE_PROP_ACK, 220)
    } catch (_: Exception) {}
  }
}

object TaskManager {
  private const val PREFS_NAME = "gamesec_market_tasks_storage"
  private const val KEY_TASKS_JSON = "tasks_list_json"
  private var prefs: SharedPreferences? = null

  fun init(context: Context) {
    if (prefs == null) {
      prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      // Check if seeded
      val existing = prefs?.getString(KEY_TASKS_JSON, null)
      if (existing.isNullOrEmpty()) {
        seedInitialTasks()
      }
    }
  }

  fun getTasks(symbolFilter: String? = null): List<MarketTask> {
    val jsonString = prefs?.getString(KEY_TASKS_JSON, null) ?: return emptyList()
    val list = mutableListOf<MarketTask>()
    try {
      val array = JSONArray(jsonString)
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val sym = obj.optString("assetSymbol", "UMUM")
        if (symbolFilter != null && symbolFilter != "ALL" && !sym.equals(symbolFilter, ignoreCase = true)) {
          continue
        }
        list.add(
          MarketTask(
            id = obj.optString("id", "task_$i"),
            title = obj.optString("title", "Tugas Analisis"),
            assetSymbol = sym,
            assetType = obj.optString("assetType", "Saham"),
            pageNumber = obj.optInt("pageNumber", 1),
            createdAtFormatted = obj.optString("createdAtFormatted", "12 Okt 2026, 17:00 WIB"),
            description = obj.optString("description", ""),
            imageUri = if (obj.has("imageUri") && !obj.isNull("imageUri")) obj.optString("imageUri") else null,
            isCompleted = obj.optBoolean("isCompleted", false),
            hasTimer = obj.optBoolean("hasTimer", true),
            timerDurationSeconds = obj.optInt("timerDurationSeconds", 900),
            notes = obj.optString("notes", "")
          )
        )
      }
    } catch (_: Exception) {}
    return list.sortedByDescending { it.id }
  }

  fun addTask(
    title: String,
    assetSymbol: String,
    assetType: String,
    pageNumber: Int,
    description: String,
    imageUri: String? = null,
    hasTimer: Boolean = true,
    timerDurationSeconds: Int = 900
  ) {
    val current = getTasks().toMutableList()
    val now = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).format(Date())
    val newTask = MarketTask(
      id = "task_${System.currentTimeMillis()}",
      title = title.ifBlank { "Tugas Soal $assetSymbol" },
      assetSymbol = assetSymbol.uppercase().trim(),
      assetType = assetType,
      pageNumber = pageNumber,
      createdAtFormatted = now,
      description = description,
      imageUri = imageUri,
      isCompleted = false,
      hasTimer = hasTimer,
      timerDurationSeconds = timerDurationSeconds,
      notes = ""
    )
    current.add(0, newTask)
    saveTasks(current)
  }

  fun toggleTaskCompleted(id: String) {
    val current = getTasks().toMutableList()
    val idx = current.indexOfFirst { it.id == id }
    if (idx != -1) {
      val item = current[idx]
      val updated = item.copy(isCompleted = !item.isCompleted)
      current[idx] = updated
      saveTasks(current)
      if (updated.isCompleted) {
        TaskSoundHelper.playSuccessChime()
      }
    }
  }

  fun updateTaskNotes(id: String, notes: String) {
    val current = getTasks().toMutableList()
    val idx = current.indexOfFirst { it.id == id }
    if (idx != -1) {
      current[idx] = current[idx].copy(notes = notes)
      saveTasks(current)
    }
  }

  fun deleteTask(id: String) {
    val current = getTasks().toMutableList()
    current.removeAll { it.id == id }
    saveTasks(current)
  }

  private fun saveTasks(tasks: List<MarketTask>) {
    val array = JSONArray()
    for (t in tasks) {
      val obj = JSONObject()
      obj.put("id", t.id)
      obj.put("title", t.title)
      obj.put("assetSymbol", t.assetSymbol)
      obj.put("assetType", t.assetType)
      obj.put("pageNumber", t.pageNumber)
      obj.put("createdAtFormatted", t.createdAtFormatted)
      obj.put("description", t.description)
      obj.put("imageUri", t.imageUri ?: JSONObject.NULL)
      obj.put("isCompleted", t.isCompleted)
      obj.put("hasTimer", t.hasTimer)
      obj.put("timerDurationSeconds", t.timerDurationSeconds)
      obj.put("notes", t.notes)
      array.put(obj)
    }
    prefs?.edit()?.putString(KEY_TASKS_JSON, array.toString())?.apply()
  }

  private fun seedInitialTasks() {
    val initial = listOf(
      MarketTask(
        id = "task_init_1",
        title = "Analisis Golden Cross MA 5 & MA 20 IHSG",
        assetSymbol = "IHSG",
        assetType = "Saham",
        pageNumber = 1,
        createdAtFormatted = "12 Okt 2026, 14:15 WIB",
        description = "Bandingkan grafik IHSG pada overlay MA 5 dan MA 20. Tentukan apakah terjadi sinyal Golden Cross atau Dead Cross serta konfirmasi volume.",
        imageUri = null,
        isCompleted = false,
        hasTimer = true,
        timerDurationSeconds = 900
      ),
      MarketTask(
        id = "task_init_2",
        title = "Pemeriksaan RSI Oversold & Support BBCA",
        assetSymbol = "BBCA",
        assetType = "Saham",
        pageNumber = 2,
        createdAtFormatted = "12 Okt 2026, 15:30 WIB",
        description = "Periksa indikator RSI(14) pada saham BBCA. Apakah indikator berada di area oversold (<30) dan bersiap melakukan reversal menuju target resisten?",
        imageUri = null,
        isCompleted = false,
        hasTimer = false,
        timerDurationSeconds = 0
      ),
      MarketTask(
        id = "task_init_3",
        title = "Deteksi Stochastic Rebound & Breakout BTC",
        assetSymbol = "BTC",
        assetType = "Crypto",
        pageNumber = 1,
        createdAtFormatted = "12 Okt 2026, 16:00 WIB",
        description = "Evaluasi Stochastic Oscillator (14, 3, 3) pada Bitcoin saat menembus level psikologis $63,000. Amati perpotongan garis %K dan %D.",
        imageUri = null,
        isCompleted = true,
        hasTimer = true,
        timerDurationSeconds = 1800
      ),
      MarketTask(
        id = "task_init_4",
        title = "Analisis Histogram MACD Solana (SOL)",
        assetSymbol = "SOL",
        assetType = "Crypto",
        pageNumber = 3,
        createdAtFormatted = "12 Okt 2026, 16:45 WIB",
        description = "Amati bar histogram MACD (12, 26, 9) pada koin Solana. Apakah batang histogram beralih dari merah ke hijau sebagai tanda momentum bullish?",
        imageUri = null,
        isCompleted = false,
        hasTimer = true,
        timerDurationSeconds = 1200
      )
    )
    saveTasks(initial)
  }
}
