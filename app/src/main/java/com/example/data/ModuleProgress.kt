package com.example.data

data class CompletedModuleRecord(
  val moduleIndex: Int,
  val moduleId: String,
  val numberLabel: String,
  val title: String,
  val completedAt: String,
  val notes: String = ""
)

data class StudentProgressSummary(
  val completedModules: Map<Int, CompletedModuleRecord> = emptyMap(),
  val totalModulesCount: Int = 10,
  val isSyncing: Boolean = false,
  val lastSyncedTime: String? = null,
  val syncStatusMessage: String = "Lokal & Cloud Tersinkron"
) {
  val completedCount: Int get() = completedModules.size
  val progressFraction: Float get() = if (totalModulesCount > 0) (completedCount.toFloat() / totalModulesCount).coerceIn(0f, 1f) else 0f
  val progressPercent: Int get() = (progressFraction * 100).toInt()
  fun isCompleted(index: Int): Boolean = completedModules.containsKey(index)
  fun getRecord(index: Int): CompletedModuleRecord? = completedModules[index]
}
