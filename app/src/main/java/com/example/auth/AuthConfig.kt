package com.example.auth

import android.content.Context

object AuthConfig {
  const val FIREBASE_WEB_CLIENT_ID =
    "60841919047-dbt9bg20ss2qi6799akjtmhf11spd2jd.apps.googleusercontent.com"

  const val DEFAULT_GOOGLE_CLIENT_ID = FIREBASE_WEB_CLIENT_ID

  const val PACKAGE_NAME = "com.aistudio.gameseclab.vtyqzm"
  const val ADMIN_EMAIL = "volturainternasional@gmail.com"

  const val SHA1_FINGERPRINT = "3F:4C:F5:F1:61:5C:09:17:EA:76:5B:D5:C0:44:DE:33:FE:B5:1B:CD"
  const val SHA256_FINGERPRINT = "E5:32:F0:96:D9:3C:A4:0F:93:34:31:4C:A7:87:C3:49:1E:78:25:B6:8F:38:73:58:14:B3:BD:4E:CD:2F:2C:52"

  private const val PREFS_NAME = "auth_config_prefs"
  private const val KEY_CUSTOM_CLIENT_ID = "custom_google_client_id"

  fun getDefaultClientId(context: Context): String {
    return try {
      val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
      if (resId != 0) {
        context.getString(resId)
      } else {
        FIREBASE_WEB_CLIENT_ID
      }
    } catch (_: Exception) {
      FIREBASE_WEB_CLIENT_ID
    }
  }

  fun getClientId(context: Context): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val custom = prefs.getString(KEY_CUSTOM_CLIENT_ID, null)?.trim()
    // Auto-migrate from the mismatched 758958046036 ID to the valid project client ID
    if (custom.isNullOrBlank() || custom.contains("758958046036")) {
      return getDefaultClientId(context)
    }
    return custom
  }

  fun setClientId(context: Context, newClientId: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_CUSTOM_CLIENT_ID, newClientId.trim()).apply()
  }

  fun resetClientId(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().remove(KEY_CUSTOM_CLIENT_ID).apply()
  }
}
