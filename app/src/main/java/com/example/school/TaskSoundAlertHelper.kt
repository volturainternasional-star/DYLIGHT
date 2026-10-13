package com.example.school

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Audio feedback helper for School Tasks & Study Timers.
 * Uses Android system ToneGenerator so no external mp3 assets are required,
 * ensuring high performance, low APK size, and reliable instant sound playback.
 */
object TaskSoundAlertHelper {

  fun playPendingTaskBeep() {
    try {
      val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
      toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 280)
    } catch (_: Exception) {
      // Graceful fallback if device audio service is temporarily restricted
    }
  }

  fun playTimerFinishedAlert() {
    try {
      val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
      toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 650)
    } catch (_: Exception) {
      // Fallback
    }
  }

  fun playConfirmationChime() {
    try {
      val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
      toneGen.startTone(ToneGenerator.TONE_PROP_PROMPT, 180)
    } catch (_: Exception) {
      // Fallback
    }
  }
}
