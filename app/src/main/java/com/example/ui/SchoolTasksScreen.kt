package com.example.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.school.SchoolAssignment
import com.example.school.SchoolTaskManager
import com.example.school.TaskSoundAlertHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val DarkCardBg = Color(0xFF131722)
private val DarkCardBorder = Color(0xFF2A2E39)
private val AccentGreen = Color(0xFF26A69A)
private val AccentOrange = Color(0xFFFF9800)
private val AccentRed = Color(0xFFEF5350)
private val AccentBlue = Color(0xFF2962FF)
private val TextWhite = Color(0xFFE0E3EB)
private val TextMuted = Color(0xFF787B86)

@Composable
fun SchoolTasksScreen(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val taskManager = remember { SchoolTaskManager(context) }
  var tasks by remember { mutableStateOf(taskManager.getTasks()) }

  var showAddDialog by remember { mutableStateOf(false) }
  var editingTask by remember { mutableStateOf<SchoolAssignment?>(null) }
  var viewingPhotoUri by remember { mutableStateOf<String?>(null) }
  var filterCategory by rememberSaveable { mutableStateOf("Semua") }

  val uncompletedCount = tasks.count { !it.isCompleted }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .testTag("school_tasks_screen"),
    containerColor = Color(0xFF0F121A)
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      // Header Section
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DarkCardBorder)
      ) {
        Column(
          modifier = Modifier.padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Buku PR & Tugas Sekolah",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = TextWhite,
                  letterSpacing = 0.5.sp
                )
              )
              Text(
                text = "Foto soal & jawaban tugas, atur jadwal pengerjaan, dan timer pengerjaan.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
              )
            }
            // Sound notification button to check pending tasks
            IconButton(
              onClick = {
                if (uncompletedCount > 0) {
                  TaskSoundAlertHelper.playPendingTaskBeep()
                  Toast.makeText(
                    context,
                    "Bunyi Pengingat: Masih ada $uncompletedCount tugas yang belum diselesaikan!",
                    Toast.LENGTH_SHORT
                  ).show()
                } else {
                  TaskSoundAlertHelper.playConfirmationChime()
                  Toast.makeText(
                    context,
                    "Hebat! Semua tugas sekolah sudah selesai.",
                    Toast.LENGTH_SHORT
                  ).show()
                }
              },
              modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF1E222D), CircleShape)
                .testTag("btn_sound_reminder")
            ) {
              Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = "Bunyikan Pengingat",
                tint = if (uncompletedCount > 0) AccentOrange else AccentGreen
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Filter bar & add button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              FilterChip(
                selected = filterCategory == "Semua",
                onClick = { filterCategory = "Semua" },
                label = { Text("Semua (${tasks.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = AccentBlue,
                  selectedLabelColor = Color.White,
                  containerColor = Color(0xFF1E222D),
                  labelColor = TextMuted
                ),
                border = BorderStroke(1.dp, if (filterCategory == "Semua") AccentBlue else DarkCardBorder)
              )
              FilterChip(
                selected = filterCategory == "Belum",
                onClick = { filterCategory = "Belum" },
                label = { Text("Belum Selesai ($uncompletedCount)", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = AccentOrange,
                  selectedLabelColor = Color.Black,
                  containerColor = Color(0xFF1E222D),
                  labelColor = TextMuted
                ),
                border = BorderStroke(1.dp, if (filterCategory == "Belum") AccentOrange else DarkCardBorder)
              )
              FilterChip(
                selected = filterCategory == "Selesai",
                onClick = { filterCategory = "Selesai" },
                label = { Text("Selesai (${tasks.count { it.isCompleted }})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = AccentGreen,
                  selectedLabelColor = Color.Black,
                  containerColor = Color(0xFF1E222D),
                  labelColor = TextMuted
                ),
                border = BorderStroke(1.dp, if (filterCategory == "Selesai") AccentGreen else DarkCardBorder)
              )
            }

            Button(
              onClick = { showAddDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
              modifier = Modifier.testTag("btn_tambah_tugas")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Foto/Catat", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        }
      }

      // Tasks List
      val displayedTasks = remember(tasks, filterCategory) {
        when (filterCategory) {
          "Belum" -> tasks.filter { !it.isCompleted }
          "Selesai" -> tasks.filter { it.isCompleted }
          else -> tasks
        }
      }

      if (displayedTasks.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.MenuBook,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(54.dp)
            )
            Text(
              text = if (filterCategory == "Semua") "Belum ada daftar tugas sekolah." else "Tidak ada tugas dalam kategori ini.",
              style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
            )
            Button(
              onClick = { showAddDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Tambah Tugas Sekarang", color = Color.White)
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(bottom = 24.dp)
        ) {
          items(displayedTasks, key = { it.id }) { task ->
            SchoolTaskCard(
              task = task,
              onToggleComplete = {
                taskManager.toggleComplete(task.id)
                tasks = taskManager.getTasks()
                TaskSoundAlertHelper.playConfirmationChime()
              },
              onEdit = { editingTask = task },
              onDelete = {
                taskManager.deleteTask(task.id)
                tasks = taskManager.getTasks()
                Toast.makeText(context, "Tugas berhasil dihapus", Toast.LENGTH_SHORT).show()
              },
              onViewPhoto = { uri -> viewingPhotoUri = uri }
            )
          }
        }
      }
    }
  }

  // Dialog Tambah Tugas Baru
  if (showAddDialog) {
    TaskFormDialog(
      title = "Tambah Tugas Sekolah Baru",
      initialTask = null,
      onDismiss = { showAddDialog = false },
      onSave = { subject, title, page, targetDate, notes, photos, timerMinutes ->
        taskManager.addTask(
          subject = subject,
          taskTitle = title,
          pageOrChapter = page,
          photoUris = photos,
          notes = notes,
          targetWorkDate = targetDate,
          timerMinutes = timerMinutes
        )
        tasks = taskManager.getTasks()
        showAddDialog = false
        TaskSoundAlertHelper.playConfirmationChime()
        Toast.makeText(context, "Tugas berhasil dicatat!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // Dialog Edit Tugas
  editingTask?.let { task ->
    TaskFormDialog(
      title = "Edit Tugas Sekolah",
      initialTask = task,
      onDismiss = { editingTask = null },
      onSave = { subject, title, page, targetDate, notes, photos, timerMinutes ->
        taskManager.updateTask(
          id = task.id,
          subject = subject,
          taskTitle = title,
          pageOrChapter = page,
          targetWorkDate = targetDate,
          notes = notes,
          photoUris = photos,
          timerMinutes = timerMinutes
        )
        tasks = taskManager.getTasks()
        editingTask = null
        TaskSoundAlertHelper.playConfirmationChime()
        Toast.makeText(context, "Tugas berhasil diperbarui!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // Photo Fullscreen Viewer Dialog
  viewingPhotoUri?.let { uriStr ->
    Dialog(onDismissRequest = { viewingPhotoUri = null }) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222D)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DarkCardBorder)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Pratinjau Foto Soal / Jawaban",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextWhite
              )
            )
            IconButton(onClick = { viewingPhotoUri = null }) {
              Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextWhite)
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
          AsyncImage(
            model = ImageRequest.Builder(context)
              .data(Uri.parse(uriStr))
              .crossfade(true)
              .build(),
            contentDescription = "Foto Tugas Full",
            modifier = Modifier
              .fillMaxWidth()
              .height(340.dp)
              .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
          )
        }
      }
    }
  }
}

@Composable
private fun SchoolTaskCard(
  task: SchoolAssignment,
  onToggleComplete: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onViewPhoto: (String) -> Unit
) {
  val context = LocalContext.current
  var isTimerRunning by remember { mutableStateOf(false) }
  var remainingSeconds by remember(task.timerMinutes) {
    mutableIntStateOf((task.timerMinutes ?: 0) * 60)
  }

  // Coroutine timer countdown
  LaunchedEffect(isTimerRunning, remainingSeconds) {
    if (isTimerRunning && remainingSeconds > 0) {
      delay(1000L)
      remainingSeconds--
      if (remainingSeconds == 0) {
        isTimerRunning = false
        TaskSoundAlertHelper.playTimerFinishedAlert()
        Toast.makeText(
          context,
          "Waktu Pengerjaan Habis! Cek tugas: ${task.taskTitle}",
          Toast.LENGTH_LONG
        ).show()
      }
    }
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("task_card_${task.id}"),
    colors = CardDefaults.cardColors(
      containerColor = if (task.isCompleted) Color(0xFF101C1A) else DarkCardBg
    ),
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(
      1.dp,
      if (task.isCompleted) AccentGreen.copy(alpha = 0.5f) else DarkCardBorder
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header: Subject, Date, Checkbox
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .background(
                  if (task.isCompleted) AccentGreen.copy(alpha = 0.2f) else AccentBlue.copy(alpha = 0.2f),
                  RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = task.subject,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (task.isCompleted) AccentGreen else AccentBlue
                )
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = task.pageOrChapter,
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = TextMuted
              )
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = task.taskTitle,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = if (task.isCompleted) TextWhite.copy(alpha = 0.6f) else TextWhite
            )
          )
        }

        // Complete checkbox
        IconButton(
          onClick = onToggleComplete,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = "Status Selesai",
            tint = if (task.isCompleted) AccentGreen else TextMuted
          )
        }
      }

      // Information tags: Tanggal difoto & Kapan ingin dikerjakan
      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Difoto: ${task.createdAtFormatted}",
            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
          )
          if (task.targetWorkDate.isNotBlank()) {
            Text(
              text = "Rencana Kerjakan: ${task.targetWorkDate}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = AccentOrange,
                fontWeight = FontWeight.Medium
              )
            )
          }
        }

        // Action icons: Edit & Delete
        Row {
          IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Tugas", tint = TextMuted, modifier = Modifier.size(18.dp))
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Hapus Tugas", tint = AccentRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
          }
        }
      }

      // Notes
      if (task.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E222D), RoundedCornerShape(6.dp))
            .padding(10.dp)
        ) {
          Text(
            text = task.notes,
            style = MaterialTheme.typography.bodySmall.copy(color = TextWhite.copy(alpha = 0.9f))
          )
        }
      }

      // Photos section (Rapi, bisa diklik untuk preview)
      if (task.photoUris.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "Lampiran Foto Soal & Catatan (${task.photoUris.size})",
          style = MaterialTheme.typography.labelSmall.copy(
            color = TextMuted,
            fontWeight = FontWeight.Bold
          )
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(task.photoUris) { uriStr ->
            Box(
              modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                .clickable { onViewPhoto(uriStr) }
            ) {
              AsyncImage(
                model = ImageRequest.Builder(context)
                  .data(Uri.parse(uriStr))
                  .crossfade(true)
                  .build(),
                contentDescription = "Foto Soal",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
            }
          }
        }
      }

      // Timer Section (Timer di setiap tugas atau hanya simpan saja)
      if (task.timerMinutes != null && task.timerMinutes > 0) {
        Spacer(modifier = Modifier.height(14.dp))
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF181C26)),
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.dp, Color(0xFF2A2E39))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = if (isTimerRunning) AccentOrange else AccentGreen,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Timer Pengerjaan (${task.timerMinutes} Menit)",
                  style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
                val m = remainingSeconds / 60
                val s = remainingSeconds % 60
                val timeString = String.format("%02d:%02d", m, s)
                Text(
                  text = timeString,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (remainingSeconds == 0) AccentRed else if (isTimerRunning) AccentOrange else TextWhite
                  )
                )
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              IconButton(
                onClick = { isTimerRunning = !isTimerRunning },
                modifier = Modifier
                  .size(36.dp)
                  .background(if (isTimerRunning) AccentOrange else AccentGreen, CircleShape)
              ) {
                Icon(
                  imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                  contentDescription = if (isTimerRunning) "Jeda" else "Mulai",
                  tint = Color.Black,
                  modifier = Modifier.size(20.dp)
                )
              }

              IconButton(
                onClick = {
                  isTimerRunning = false
                  remainingSeconds = (task.timerMinutes) * 60
                },
                modifier = Modifier
                  .size(36.dp)
                  .background(Color(0xFF2A2E39), CircleShape)
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Reset",
                  tint = TextWhite,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TaskFormDialog(
  title: String,
  initialTask: SchoolAssignment?,
  onDismiss: () -> Unit,
  onSave: (
    subject: String,
    title: String,
    page: String,
    targetDate: String,
    notes: String,
    photos: List<String>,
    timerMinutes: Int?
  ) -> Unit
) {
  val context = LocalContext.current
  var subject by rememberSaveable { mutableStateOf(initialTask?.subject ?: "Matematika") }
  var taskTitle by rememberSaveable { mutableStateOf(initialTask?.taskTitle ?: "") }
  var pageOrChapter by rememberSaveable { mutableStateOf(initialTask?.pageOrChapter ?: "") }
  var targetWorkDate by rememberSaveable { mutableStateOf(initialTask?.targetWorkDate ?: "") }
  var notes by rememberSaveable { mutableStateOf(initialTask?.notes ?: "") }
  var selectedPhotos by remember { mutableStateOf(initialTask?.photoUris ?: emptyList()) }

  // Timer configuration: null (Hanya menyimpan saja) atau preset menit
  var selectedTimerOption by rememberSaveable {
    mutableStateOf(initialTask?.timerMinutes?.toString() ?: "none")
  }

  // Photo Picker
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
  ) { uris ->
    if (uris.isNotEmpty()) {
      val stringUris = uris.map { it.toString() }
      selectedPhotos = (selectedPhotos + stringUris).distinct()
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131722)),
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(1.dp, DarkCardBorder)
    ) {
      LazyColumn(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = title,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextWhite
              )
            )
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextMuted)
            }
          }
        }

        // Mata Pelajaran
        item {
          OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Mata Pelajaran (Contoh: Matematika, Fisika)", color = TextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AccentBlue,
              unfocusedBorderColor = DarkCardBorder,
              focusedTextColor = TextWhite,
              unfocusedTextColor = TextWhite
            )
          )
        }

        // Nama Tugas
        item {
          OutlinedTextField(
            value = taskTitle,
            onValueChange = { taskTitle = it },
            label = { Text("Nama Tugas (Contoh: PR Bab Aljabar)", color = TextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AccentBlue,
              unfocusedBorderColor = DarkCardBorder,
              focusedTextColor = TextWhite,
              unfocusedTextColor = TextWhite
            )
          )
        }

        // Halaman / Nomor Soal
        item {
          OutlinedTextField(
            value = pageOrChapter,
            onValueChange = { pageOrChapter = it },
            label = { Text("Halaman / Nomor Soal (Contoh: Hal 32 No 1-10)", color = TextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AccentBlue,
              unfocusedBorderColor = DarkCardBorder,
              focusedTextColor = TextWhite,
              unfocusedTextColor = TextWhite
            )
          )
        }

        // Kapan Pengen Dikerjakan (Bisa diisi atau dikosongkan)
        item {
          OutlinedTextField(
            value = targetWorkDate,
            onValueChange = { targetWorkDate = it },
            label = { Text("Kapan Mau Dikerjakan? (Opsional, boleh kosong)", color = TextMuted) },
            placeholder = { Text("Contoh: Nanti jam 20:00 / Besok Sore", color = TextMuted.copy(alpha = 0.5f)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AccentOrange,
              unfocusedBorderColor = DarkCardBorder,
              focusedTextColor = TextWhite,
              unfocusedTextColor = TextWhite
            )
          )
        }

        // Opsi Timer atau Hanya Simpan Saja
        item {
          Text(
            text = "Mode Timer atau Simpan Saja:",
            style = MaterialTheme.typography.labelMedium.copy(color = TextWhite, fontWeight = FontWeight.SemiBold)
          )
          Spacer(modifier = Modifier.height(4.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val timerOptions = listOf(
              "none" to "Simpan Saja (Tanpa Timer)",
              "15" to "Timer 15 Menit",
              "25" to "Timer 25 Menit (Pomodoro)",
              "30" to "Timer 30 Menit",
              "45" to "Timer 45 Menit",
              "60" to "Timer 60 Menit"
            )
            items(timerOptions) { (key, label) ->
              FilterChip(
                selected = selectedTimerOption == key,
                onClick = { selectedTimerOption = key },
                label = { Text(label, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = if (key == "none") AccentBlue else AccentGreen,
                  selectedLabelColor = if (key == "none") Color.White else Color.Black,
                  containerColor = Color(0xFF1E222D),
                  labelColor = TextMuted
                ),
                border = BorderStroke(
                  1.dp,
                  if (selectedTimerOption == key) AccentGreen else DarkCardBorder
                )
              )
            }
          }
        }

        // Foto Soal & Jawaban
        item {
          Text(
            text = "Foto Soal & Catatan Buku:",
            style = MaterialTheme.typography.labelMedium.copy(color = TextWhite, fontWeight = FontWeight.SemiBold)
          )
          Spacer(modifier = Modifier.height(6.dp))

          OutlinedButton(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
            border = BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(Icons.Default.Image, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pilih / Tambah Foto Soal dari Galeri & Kamera")
          }

          if (selectedPhotos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(selectedPhotos) { uriStr ->
                Box(
                  modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                ) {
                  AsyncImage(
                    model = ImageRequest.Builder(context)
                      .data(Uri.parse(uriStr))
                      .crossfade(true)
                      .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                  )
                  // Tombol hapus foto
                  IconButton(
                    onClick = { selectedPhotos = selectedPhotos.filterNot { it == uriStr } },
                    modifier = Modifier
                      .size(22.dp)
                      .align(Alignment.TopEnd)
                      .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                  ) {
                    Icon(Icons.Default.Close, contentDescription = "Hapus Foto", tint = Color.White, modifier = Modifier.size(14.dp))
                  }
                }
              }
            }
          }
        }

        // Catatan Tambahan
        item {
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Catatan / Instruksi Guru (Opsional)", color = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AccentBlue,
              unfocusedBorderColor = DarkCardBorder,
              focusedTextColor = TextWhite,
              unfocusedTextColor = TextWhite
            )
          )
        }

        // Tombol Simpan
        item {
          Spacer(modifier = Modifier.height(8.dp))
          Button(
            onClick = {
              if (taskTitle.isBlank()) {
                Toast.makeText(context, "Mohon isi nama tugas!", Toast.LENGTH_SHORT).show()
                return@Button
              }
              val timerM = if (selectedTimerOption == "none") null else selectedTimerOption.toIntOrNull()
              onSave(
                subject,
                taskTitle,
                pageOrChapter,
                targetWorkDate,
                notes,
                selectedPhotos,
                timerM
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Simpan Tugas", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
