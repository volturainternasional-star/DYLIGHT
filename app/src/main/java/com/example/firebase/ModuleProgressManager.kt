package com.example.firebase

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.R
import com.example.auth.AuthManager
import com.example.data.CompletedModuleRecord
import com.example.data.GameSecRepository
import com.example.data.LearningModule
import com.example.data.StudentProgressSummary
import com.example.school.TaskSoundAlertHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Singleton manager to track student progress through the 10 Game Security Modules,
 * persisting data both locally (offline-first SharedPreferences) and in Cloud Firestore
 * with real-time bidirectional synchronization.
 */
object ModuleProgressManager {
  private const val TAG = "ModuleProgressManager"
  private const val PREFS_NAME = "gamesec_module_progress_prefs"
  private const val KEY_COMPLETED_JSON = "completed_modules_json"
  private const val KEY_LAST_SYNC = "last_sync_timestamp"

  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private var prefs: SharedPreferences? = null
  private var snapshotListener: ListenerRegistration? = null
  private var currentListeningUserId: String? = null

  private val _progressState = MutableStateFlow(
    StudentProgressSummary(totalModulesCount = GameSecRepository.modules.size)
  )
  val progressState: StateFlow<StudentProgressSummary> = _progressState.asStateFlow()
  var targetModuleIndex = androidx.compose.runtime.mutableIntStateOf(0)

  fun init(context: Context) {
    if (prefs == null) {
      prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      loadFromLocalStorage()
    }
    attachCloudListener(context.applicationContext)
  }

  private fun getEffectiveUserId(): String {
    val session = AuthManager.sessionState.value
    if (session.isLoggedIn && session.email.isNotBlank()) {
      return session.email.trim()
    }
    val fbUser = FirebaseAuth.getInstance().currentUser
    if (fbUser != null) {
      val email = fbUser.email
      if (!email.isNullOrBlank()) return email.trim()
      return fbUser.uid
    }
    return "student_local_account"
  }

  private fun getFirestore(context: Context): FirebaseFirestore {
    return try {
      val dbId = context.getString(R.string.firestore_database_id)
      if (dbId.isNotBlank()) {
        FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
      } else {
        FirebaseFirestore.getInstance()
      }
    } catch (_: Throwable) {
      try {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
      } catch (_: Throwable) {
        FirebaseFirestore.getInstance()
      }
    }
  }

  private fun loadFromLocalStorage() {
    val rawJson = prefs?.getString(KEY_COMPLETED_JSON, null)
    val lastSync = prefs?.getString(KEY_LAST_SYNC, null)
    val map = mutableMapOf<Int, CompletedModuleRecord>()

    if (!rawJson.isNullOrBlank()) {
      try {
        val arr = JSONArray(rawJson)
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          val idx = obj.getInt("moduleIndex")
          map[idx] = CompletedModuleRecord(
            moduleIndex = idx,
            moduleId = obj.optString("moduleId", "modul_$idx"),
            numberLabel = obj.optString("numberLabel", "MODUL ${idx + 1}"),
            title = obj.optString("title", "Modul ${idx + 1}"),
            completedAt = obj.optString("completedAt", ""),
            notes = obj.optString("notes", "")
          )
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error parsing local module progress json", e)
      }
    }

    _progressState.value = _progressState.value.copy(
      completedModules = map,
      totalModulesCount = GameSecRepository.modules.size,
      lastSyncedTime = lastSync,
      syncStatusMessage = if (lastSync != null) "Tersinkron ($lastSync)" else "Tersimpan Lokal"
    )
  }

  private fun saveToLocalStorage(map: Map<Int, CompletedModuleRecord>, syncTime: String? = null) {
    try {
      val arr = JSONArray()
      for ((_, item) in map) {
        val obj = JSONObject().apply {
          put("moduleIndex", item.moduleIndex)
          put("moduleId", item.moduleId)
          put("numberLabel", item.numberLabel)
          put("title", item.title)
          put("completedAt", item.completedAt)
          put("notes", item.notes)
        }
        arr.put(obj)
      }
      prefs?.edit()?.apply {
        putString(KEY_COMPLETED_JSON, arr.toString())
        if (syncTime != null) {
          putString(KEY_LAST_SYNC, syncTime)
        }
        apply()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error saving local module progress", e)
    }
  }

  fun attachCloudListener(context: Context) {
    val userId = getEffectiveUserId()
    if (snapshotListener != null && currentListeningUserId == userId) {
      return
    }

    snapshotListener?.remove()
    currentListeningUserId = userId

    val appContext = context.applicationContext
    scope.launch {
      try {
        val db = getFirestore(appContext)
        val modulesCollection = db.collection("users")
          .document(userId)
          .collection("completed_modules")

        snapshotListener = modulesCollection.addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w(TAG, "Firestore snapshot listener error: ${error.message}")
            return@addSnapshotListener
          }

          if (snapshot != null) {
            val cloudMap = mutableMapOf<Int, CompletedModuleRecord>()
            for (doc in snapshot.documents) {
              val idx = doc.getLong("moduleIndex")?.toInt() ?: continue
              val modId = doc.getString("moduleId") ?: doc.id
              val label = doc.getString("numberLabel") ?: "MODUL ${idx + 1}"
              val title = doc.getString("title") ?: ""
              val completedAt = doc.getString("completedAt") ?: ""
              val notes = doc.getString("notes") ?: ""

              cloudMap[idx] = CompletedModuleRecord(
                moduleIndex = idx,
                moduleId = modId,
                numberLabel = label,
                title = title,
                completedAt = completedAt,
                notes = notes
              )
            }

            // Merge with local if cloud had updates
            val currentMap = _progressState.value.completedModules.toMutableMap()
            // If cloud has documents, apply cloud truth
            if (cloudMap.isNotEmpty()) {
              currentMap.putAll(cloudMap)
            }

            val nowStr = SimpleDateFormat("HH:mm:ss", Locale("id", "ID")).format(Date())
            saveToLocalStorage(currentMap, nowStr)
            _progressState.value = _progressState.value.copy(
              completedModules = currentMap,
              lastSyncedTime = nowStr,
              isSyncing = false,
              syncStatusMessage = "Tersinkron Cloud ($nowStr)"
            )
          }
        }
      } catch (e: Exception) {
        Log.w(TAG, "Could not attach Firestore listener: ${e.message}")
      }
    }
  }

  fun toggleModuleCompletion(context: Context, moduleIndex: Int, module: LearningModule) {
    val isCurrentlyCompleted = _progressState.value.isCompleted(moduleIndex)
    if (isCurrentlyCompleted) {
      unmarkModule(context, moduleIndex)
    } else {
      markModule(context, moduleIndex, module)
    }
  }

  fun markModule(context: Context, moduleIndex: Int, module: LearningModule) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
    val record = CompletedModuleRecord(
      moduleIndex = moduleIndex,
      moduleId = "modul_$moduleIndex",
      numberLabel = module.numberLabel,
      title = module.title,
      completedAt = dateStr
    )

    val updatedMap = _progressState.value.completedModules.toMutableMap()
    updatedMap[moduleIndex] = record

    val nowTimeStr = SimpleDateFormat("HH:mm:ss", Locale("id", "ID")).format(Date())
    saveToLocalStorage(updatedMap, nowTimeStr)

    _progressState.value = _progressState.value.copy(
      completedModules = updatedMap,
      lastSyncedTime = nowTimeStr,
      syncStatusMessage = "Menyimpan ke Cloud..."
    )

    // Play pleasant chime feedback
    TaskSoundAlertHelper.playConfirmationChime()

    // Push to Firestore
    val appContext = context.applicationContext
    val userId = getEffectiveUserId()
    scope.launch {
      try {
        _progressState.value = _progressState.value.copy(isSyncing = true)
        val db = getFirestore(appContext)

        val docData = mapOf(
          "moduleIndex" to moduleIndex,
          "moduleId" to "modul_$moduleIndex",
          "numberLabel" to module.numberLabel,
          "title" to module.title,
          "subtitle" to module.subtitle,
          "completedAt" to dateStr,
          "userId" to userId,
          "status" to "COMPLETED",
          "updatedAt" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        )

        // Save module document
        db.collection("users")
          .document(userId)
          .collection("completed_modules")
          .document("modul_$moduleIndex")
          .set(docData, SetOptions.merge())
          .await()

        // Update user root document summary
        val userSummary = mapOf(
          "email" to userId,
          "completedModuleCount" to updatedMap.size,
          "totalModules" to GameSecRepository.modules.size,
          "progressPercent" to ((updatedMap.size.toFloat() / GameSecRepository.modules.size) * 100).toInt(),
          "lastProgressUpdate" to dateStr,
          "platform" to "Android GameSec Lab"
        )
        db.collection("users")
          .document(userId)
          .set(userSummary, SetOptions.merge())
          .await()

        _progressState.value = _progressState.value.copy(
          isSyncing = false,
          syncStatusMessage = "Tersimpan di Firestore ($nowTimeStr)"
        )
      } catch (e: Exception) {
        Log.w(TAG, "Error syncing marked module to Firestore: ${e.message}")
        _progressState.value = _progressState.value.copy(
          isSyncing = false,
          syncStatusMessage = "Tersimpan Lokal (Offline)"
        )
      }
    }
  }

  fun unmarkModule(context: Context, moduleIndex: Int) {
    val updatedMap = _progressState.value.completedModules.toMutableMap()
    updatedMap.remove(moduleIndex)

    val nowTimeStr = SimpleDateFormat("HH:mm:ss", Locale("id", "ID")).format(Date())
    saveToLocalStorage(updatedMap, nowTimeStr)

    _progressState.value = _progressState.value.copy(
      completedModules = updatedMap,
      lastSyncedTime = nowTimeStr,
      syncStatusMessage = "Menghapus dari Cloud..."
    )

    val appContext = context.applicationContext
    val userId = getEffectiveUserId()
    scope.launch {
      try {
        _progressState.value = _progressState.value.copy(isSyncing = true)
        val db = getFirestore(appContext)

        db.collection("users")
          .document(userId)
          .collection("completed_modules")
          .document("modul_$moduleIndex")
          .delete()
          .await()

        // Update user root document summary
        val userSummary = mapOf(
          "completedModuleCount" to updatedMap.size,
          "totalModules" to GameSecRepository.modules.size,
          "progressPercent" to ((updatedMap.size.toFloat() / GameSecRepository.modules.size) * 100).toInt(),
          "lastProgressUpdate" to SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
        )
        db.collection("users")
          .document(userId)
          .set(userSummary, SetOptions.merge())
          .await()

        _progressState.value = _progressState.value.copy(
          isSyncing = false,
          syncStatusMessage = "Tersinkron Cloud ($nowTimeStr)"
        )
      } catch (e: Exception) {
        Log.w(TAG, "Error removing module from Firestore: ${e.message}")
        _progressState.value = _progressState.value.copy(
          isSyncing = false,
          syncStatusMessage = "Tersimpan Lokal"
        )
      }
    }
  }

  suspend fun manualSync(context: Context): Result<String> {
    val appContext = context.applicationContext
    val userId = getEffectiveUserId()

    return try {
      _progressState.value = _progressState.value.copy(isSyncing = true)
      val db = getFirestore(appContext)

      // 1. Push any local progress to cloud
      val localCompleted = _progressState.value.completedModules
      for ((idx, rec) in localCompleted) {
        val docData = mapOf(
          "moduleIndex" to idx,
          "moduleId" to rec.moduleId,
          "numberLabel" to rec.numberLabel,
          "title" to rec.title,
          "completedAt" to rec.completedAt,
          "userId" to userId,
          "status" to "COMPLETED"
        )
        db.collection("users")
          .document(userId)
          .collection("completed_modules")
          .document("modul_$idx")
          .set(docData, SetOptions.merge())
          .await()
      }

      // 2. Fetch all from cloud to ensure complete sync
      val snapshot = db.collection("users")
        .document(userId)
        .collection("completed_modules")
        .get()
        .await()

      val mergedMap = localCompleted.toMutableMap()
      for (doc in snapshot.documents) {
        val idx = doc.getLong("moduleIndex")?.toInt() ?: continue
        mergedMap[idx] = CompletedModuleRecord(
          moduleIndex = idx,
          moduleId = doc.getString("moduleId") ?: doc.id,
          numberLabel = doc.getString("numberLabel") ?: "MODUL ${idx + 1}",
          title = doc.getString("title") ?: "",
          completedAt = doc.getString("completedAt") ?: ""
        )
      }

      val nowStr = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID")).format(Date())
      saveToLocalStorage(mergedMap, nowStr)

      _progressState.value = _progressState.value.copy(
        completedModules = mergedMap,
        lastSyncedTime = nowStr,
        isSyncing = false,
        syncStatusMessage = "Sukses Sinkron ($nowStr)"
      )

      Result.success("Progress berhasil disinkronkan ke Firestore: ${mergedMap.size} modul selesai tercatat.")
    } catch (e: Exception) {
      _progressState.value = _progressState.value.copy(
        isSyncing = false,
        syncStatusMessage = "Gagal Sinkron: ${e.message ?: "Offline"}"
      )
      Result.failure(e)
    }
  }
}
