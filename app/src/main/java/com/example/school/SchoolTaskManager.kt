package com.example.school

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SchoolAssignment(
  val id: String,
  val subject: String,            // e.g. "Matematika", "Fisika", "Bahasa Inggris"
  val taskTitle: String,          // e.g. "PR Soal Aljabar Bab 4"
  val pageOrChapter: String,      // e.g. "Halaman 42 - 45 No. 1 - 10"
  val photoUris: List<String> = emptyList(), // URI foto soal / jawaban dari kamera/galeri
  val questionPhotoUri: String? = null,
  val answerPhotoUri: String? = null,
  val notes: String = "",
  val targetWorkDate: String = "", // Tanggal pengerjaan (bisa diisi atau dikosongkan)
  val createdAtFormatted: String = "", // Tanggal & jam saat tugas difoto/disimpan
  val isCompleted: Boolean = false,
  val timerMinutes: Int? = null    // null jika hanya simpan saja, atau 15, 25, 30, 45, 60 menit
)

class SchoolTaskManager(private val context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("school_tasks_db", Context.MODE_PRIVATE)

  private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

  fun getTasks(): List<SchoolAssignment> {
    val raw = prefs.getString("saved_school_tasks", null) ?: return defaultInitialTasks()
    val list = mutableListOf<SchoolAssignment>()
    try {
      val arr = JSONArray(raw)
      for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        val photosArr = obj.optJSONArray("photoUris")
        val photos = mutableListOf<String>()
        if (photosArr != null) {
          for (j in 0 until photosArr.length()) {
            photos.add(photosArr.getString(j))
          }
        }
        val timerVal = if (obj.has("timerMinutes") && !obj.isNull("timerMinutes")) {
          obj.optInt("timerMinutes")
        } else null

        list.add(
          SchoolAssignment(
            id = obj.getString("id"),
            subject = obj.optString("subject", "Pelajaran Umum"),
            taskTitle = obj.optString("taskTitle", "Tugas Sekolah"),
            pageOrChapter = obj.optString("pageOrChapter", "-"),
            photoUris = photos,
            questionPhotoUri = obj.optString("questionPhotoUri", null).takeIf { it != "null" && !it.isNullOrBlank() },
            answerPhotoUri = obj.optString("answerPhotoUri", null).takeIf { it != "null" && !it.isNullOrBlank() },
            notes = obj.optString("notes", ""),
            targetWorkDate = obj.optString("targetWorkDate", ""),
            createdAtFormatted = obj.optString("createdAtFormatted", dateFormat.format(Date())),
            isCompleted = obj.optBoolean("isCompleted", false),
            timerMinutes = timerVal
          )
        )
      }
    } catch (_: Exception) {
      return defaultInitialTasks()
    }
    return list
  }

  fun saveTasks(tasks: List<SchoolAssignment>) {
    val arr = JSONArray()
    for (t in tasks) {
      val obj = JSONObject()
      obj.put("id", t.id)
      obj.put("subject", t.subject)
      obj.put("taskTitle", t.taskTitle)
      obj.put("pageOrChapter", t.pageOrChapter)
      val pArr = JSONArray()
      t.photoUris.forEach { pArr.put(it) }
      obj.put("photoUris", pArr)
      obj.put("questionPhotoUri", t.questionPhotoUri ?: JSONObject.NULL)
      obj.put("answerPhotoUri", t.answerPhotoUri ?: JSONObject.NULL)
      obj.put("notes", t.notes)
      obj.put("targetWorkDate", t.targetWorkDate)
      obj.put("createdAtFormatted", t.createdAtFormatted)
      obj.put("isCompleted", t.isCompleted)
      obj.put("timerMinutes", t.timerMinutes ?: JSONObject.NULL)
      arr.put(obj)
    }
    prefs.edit().putString("saved_school_tasks", arr.toString()).apply()
  }

  fun addTask(
    subject: String,
    taskTitle: String,
    pageOrChapter: String,
    photoUris: List<String>,
    notes: String,
    targetWorkDate: String,
    timerMinutes: Int? = null
  ): SchoolAssignment {
    val nowFormatted = dateFormat.format(Date())
    val newAssignment = SchoolAssignment(
      id = "task_${System.currentTimeMillis()}",
      subject = subject.trim().ifBlank { "Umum" },
      taskTitle = taskTitle.trim().ifBlank { "Tugas Baru" },
      pageOrChapter = pageOrChapter.trim().ifBlank { "Hal. Belum diisi" },
      photoUris = photoUris,
      questionPhotoUri = photoUris.firstOrNull(),
      answerPhotoUri = photoUris.getOrNull(1),
      notes = notes.trim(),
      targetWorkDate = targetWorkDate.trim(),
      createdAtFormatted = nowFormatted,
      isCompleted = false,
      timerMinutes = timerMinutes
    )
    val current = getTasks().toMutableList()
    current.add(0, newAssignment)
    saveTasks(current)
    return newAssignment
  }

  fun updateTask(
    id: String,
    subject: String,
    taskTitle: String,
    pageOrChapter: String,
    targetWorkDate: String,
    notes: String,
    photoUris: List<String>,
    timerMinutes: Int? = null
  ) {
    val current = getTasks().map { existing ->
      if (existing.id == id) {
        existing.copy(
          subject = subject.trim().ifBlank { existing.subject },
          taskTitle = taskTitle.trim().ifBlank { existing.taskTitle },
          pageOrChapter = pageOrChapter.trim().ifBlank { existing.pageOrChapter },
          targetWorkDate = targetWorkDate.trim(),
          notes = notes.trim(),
          photoUris = photoUris,
          questionPhotoUri = photoUris.firstOrNull() ?: existing.questionPhotoUri,
          answerPhotoUri = photoUris.getOrNull(1) ?: existing.answerPhotoUri,
          timerMinutes = timerMinutes
        )
      } else {
        existing
      }
    }
    saveTasks(current)
  }

  fun toggleComplete(id: String) {
    val current = getTasks().map {
      if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
    }
    saveTasks(current)
  }

  fun deleteTask(id: String) {
    val current = getTasks().filterNot { it.id == id }
    saveTasks(current)
  }

  private fun defaultInitialTasks(): List<SchoolAssignment> {
    val now = dateFormat.format(Date())
    return listOf(
      SchoolAssignment(
        id = "sample_math",
        subject = "Matematika Peminatan",
        taskTitle = "Latihan Soal Matriks & Vektor",
        pageOrChapter = "Buku Paket Hal. 54 No. 1 - 8",
        photoUris = emptyList(),
        notes = "Kerjakan di buku tulis bergaris rapih dengan caranya lengkap.",
        targetWorkDate = "Besok Malam (19:30)",
        createdAtFormatted = now,
        isCompleted = false,
        timerMinutes = 30
      ),
      SchoolAssignment(
        id = "sample_fisika",
        subject = "Fisika",
        taskTitle = "Soal Hukum Newton & Gaya Gesek",
        pageOrChapter = "Modul Fisika Hal. 22 bagian B",
        photoUris = emptyList(),
        notes = "Rumus gaya gesek statis dan dinamis dicantumkan.",
        targetWorkDate = "Hari Rabu",
        createdAtFormatted = now,
        isCompleted = false,
        timerMinutes = null
      )
    )
  }
}
