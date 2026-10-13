package com.example.school

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Audio feedback helper for School Tasks & Study Timers.
 * Uses Android system ToneGenerator so no external mp3 assets are required,
 * ensuring high reliability and zero extra APK overhead.
 */
object TaskSoundAlertHelper {
  private var toneGenerator: ToneGenerator? = null

  init {
    try {
      toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
    } catch (_: Exception) {
      // Safe fallback if audio hardware is restricted
    }
  }

  /**
   * Sound alert played when a student has pending/uncompleted school tasks.
   * Produces an audible double alert tone so the user doesn't forget their homework.
   */
  fun playPendingTaskBeep() {
    try {
      toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
    } catch (_: Exception) {}
  }

  /**
   * Sound alert played when a study/homework countdown timer finishes.
   * Emits a clear alert tone indicating study session time is up.
   */
  fun playTimerFinishedAlert() {
    try {
      toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 700)
    } catch (_: Exception) {}
  }

  /**
   * Sound chime played when a new school assignment is saved or updated.
   */
  fun playTaskSavedChime() {
    try {
      toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
    } catch (_: Exception) {}
  }
}
