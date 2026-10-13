package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSession(
  val isLoggedIn: Boolean = false,
  val email: String = "",
  val displayName: String = "",
  val lastLoginDate: String = "",
  val profilePictureUri: String? = null,
  val idToken: String? = null
)

object AuthManager {
  private const val PREFS_NAME = "gamesec_auth_prefs"
  private const val KEY_IS_LOGGED_IN = "is_logged_in"
  private const val KEY_EMAIL = "user_email"
  private const val KEY_DISPLAY_NAME = "display_name"
  private const val KEY_PROFILE_PIC = "profile_picture_uri"
  private const val KEY_ID_TOKEN = "id_token"
  private const val KEY_RECORDED_EMAILS = "recorded_emails"
  private const val KEY_LAST_LOGIN = "last_login"

  private var prefs: SharedPreferences? = null

  private val _sessionState = MutableStateFlow(UserSession())
  val sessionState: StateFlow<UserSession> = _sessionState.asStateFlow()

  fun init(context: Context) {
    if (prefs == null) {
      prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      val loggedIn = prefs?.getBoolean(KEY_IS_LOGGED_IN, false) ?: false
      val email = prefs?.getString(KEY_EMAIL, "") ?: ""
      val name = prefs?.getString(KEY_DISPLAY_NAME, "") ?: ""
      val last = prefs?.getString(KEY_LAST_LOGIN, "") ?: ""
      val picture = prefs?.getString(KEY_PROFILE_PIC, null)
      val token = prefs?.getString(KEY_ID_TOKEN, null)
      _sessionState.value = UserSession(
        isLoggedIn = loggedIn,
        email = email,
        displayName = name,
        lastLoginDate = last,
        profilePictureUri = picture,
        idToken = token
      )
    }
  }

  fun login(
    email: String,
    displayName: String,
    idToken: String? = null,
    profilePictureUri: String? = null
  ) {
    val cleanEmail = email.trim()
    val cleanName = displayName.ifBlank { cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() } }
    val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())

    prefs?.edit()?.apply {
      putBoolean(KEY_IS_LOGGED_IN, true)
      putString(KEY_EMAIL, cleanEmail)
      putString(KEY_DISPLAY_NAME, cleanName)
      putString(KEY_LAST_LOGIN, dateStr)
      if (profilePictureUri != null) putString(KEY_PROFILE_PIC, profilePictureUri) else remove(KEY_PROFILE_PIC)
      if (idToken != null) putString(KEY_ID_TOKEN, idToken) else remove(KEY_ID_TOKEN)

      // Add to recorded emails list
      val existing = prefs?.getStringSet(KEY_RECORDED_EMAILS, emptySet())?.toMutableSet() ?: mutableSetOf()
      if (cleanEmail.isNotEmpty()) {
        existing.add(cleanEmail)
      }
      putStringSet(KEY_RECORDED_EMAILS, existing)
      apply()
    }

    _sessionState.value = UserSession(
      isLoggedIn = true,
      email = cleanEmail,
      displayName = cleanName,
      lastLoginDate = dateStr,
      profilePictureUri = profilePictureUri,
      idToken = idToken
    )
  }

  fun logout() {
    prefs?.edit()?.apply {
      putBoolean(KEY_IS_LOGGED_IN, false)
      putString(KEY_EMAIL, "")
      putString(KEY_DISPLAY_NAME, "")
      remove(KEY_PROFILE_PIC)
      remove(KEY_ID_TOKEN)
      apply()
    }
    _sessionState.value = UserSession(
      isLoggedIn = false,
      email = "",
      displayName = "",
      lastLoginDate = "",
      profilePictureUri = null,
      idToken = null
    )
  }

  fun getRecordedEmails(): List<String> {
    val set = prefs?.getStringSet(KEY_RECORDED_EMAILS, emptySet()) ?: emptySet()
    val list = set.toMutableList()
    // Ensure default demo emails are present for admin view
    if (!list.contains("volturainternasional@gmail.com")) {
      list.add("volturainternasional@gmail.com")
    }
    if (!list.contains("analis-lab@gamesec.id")) {
      list.add("analis-lab@gamesec.id")
    }
    return list.sorted()
  }

  fun isAdmin(): Boolean {
    val current = _sessionState.value
    return current.isLoggedIn && current.email.trim().equals(AuthConfig.ADMIN_EMAIL, ignoreCase = true)
  }
}
