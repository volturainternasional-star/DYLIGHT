package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

sealed class GoogleAuthResult {
  data class Success(
    val idToken: String,
    val displayName: String,
    val email: String,
    val profilePictureUri: String? = null
  ) : GoogleAuthResult()

  data class Failure(
    val errorMessage: String,
    val technicalDetails: String = "",
    val isCancelled: Boolean = false
  ) : GoogleAuthResult()
}

object GoogleAuthHelper {
  private val authMutex = Mutex()

  private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
      if (current is Activity) return current
      current = current.baseContext
    }
    return null
  }

  suspend fun signIn(context: Context): GoogleAuthResult {
    // Prevent overlapping concurrent sign-in attempts
    if (!authMutex.tryLock()) {
      return GoogleAuthResult.Failure(
        errorMessage = "Proses login sedang berjalan. Silakan tunggu...",
        technicalDetails = "Concurrent signIn call prevented by Mutex."
      )
    }

    try {
      val targetContext = context.findActivity() ?: context
      val activeClientId = AuthConfig.getClientId(targetContext)

      return try {
        val credentialManager = CredentialManager.create(targetContext)

        val googleIdOption = GetGoogleIdOption.Builder()
          .setFilterByAuthorizedAccounts(false)
          .setServerClientId(activeClientId)
          .setAutoSelectEnabled(false)
          .build()

        val request = GetCredentialRequest.Builder()
          .addCredentialOption(googleIdOption)
          .build()

        val result = credentialManager.getCredential(
          request = request,
          context = targetContext
        )

        val credential = result.credential
        if (credential is CustomCredential &&
          credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
          val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
          val idToken = googleIdTokenCredential.idToken

          // Authenticate with Firebase Auth as well
          try {
            val fbCredential = GoogleAuthProvider.getCredential(idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(fbCredential).await()
          } catch (_: Exception) {}

          var email = googleIdTokenCredential.id
          var displayName = googleIdTokenCredential.displayName ?: ""
          val pictureUri = googleIdTokenCredential.profilePictureUri?.toString()

          // Fallback: If id is not formatted as email or displayName is empty, decode JWT payload
          if (!email.contains("@") || displayName.isBlank()) {
            val (jwtEmail, jwtName) = parseJwtPayload(idToken)
            if (!email.contains("@") && jwtEmail.isNotBlank()) {
              email = jwtEmail
            }
            if (displayName.isBlank() && jwtName.isNotBlank()) {
              displayName = jwtName
            }
          }

          if (email.isBlank()) {
            email = "volturainternasional@gmail.com"
          }
          if (displayName.isBlank()) {
            displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
          }

          GoogleAuthResult.Success(
            idToken = idToken,
            displayName = displayName,
            email = email,
            profilePictureUri = pictureUri
          )
        } else {
          GoogleAuthResult.Failure(
            errorMessage = "Kredensial tidak valid atau bukan tipe Google ID token.",
            technicalDetails = "Tipe kredensial: ${credential.type}"
          )
        }
      } catch (e: GetCredentialCancellationException) {
        GoogleAuthResult.Failure(
          errorMessage = "Pemilihan akun Google dibatalkan oleh pengguna.",
          technicalDetails = "GetCredentialCancellationException: User dismissed account selector dialog.",
          isCancelled = true
        )
      } catch (e: NoCredentialException) {
        if (targetContext is Activity) {
          try {
            val fbAuth = FirebaseAuth.getInstance()
            val pendingTask = fbAuth.pendingAuthResult
            val authResult = if (pendingTask != null) {
              pendingTask.await()
            } else {
              val provider = OAuthProvider.newBuilder("google.com")
              fbAuth.startActivityForSignInWithProvider(targetContext, provider.build()).await()
            }

            val user = authResult.user
            if (user != null) {
              val email = user.email ?: "volturainternasional@gmail.com"
              val displayName = user.displayName ?: email.substringBefore("@")
              val photoUri = user.photoUrl?.toString()
              return GoogleAuthResult.Success(
                idToken = user.uid,
                displayName = displayName,
                email = email,
                profilePictureUri = photoUri
              )
            }
          } catch (authEx: Exception) {
            val msg = authEx.message ?: ""
            if (msg.contains("A headful operation is already in progress", ignoreCase = true)) {
              return GoogleAuthResult.Failure(
                errorMessage = "Sesi login browser sedang berjalan di latar belakang. Silakan selesaikan di tab Chrome yang terbuka atau coba lagi dalam beberapa detik.",
                technicalDetails = "FirebaseAuth: A headful operation is already in progress."
              )
            }
            return GoogleAuthResult.Failure(
              errorMessage = "Kredensial Google belum cocok dengan Server Client ID ($activeClientId). Silakan gunakan Server Client ID default Firebase bawaan.",
              technicalDetails = "NoCredentialException: Play Services tidak mengembalikan akun untuk client $activeClientId. Catatan: ${authEx.message}"
            )
          }
        }
        GoogleAuthResult.Failure(
          errorMessage = "Kredensial Google belum cocok dengan Server Client ID ($activeClientId). Pastikan menggunakan Web Client ID Firebase default.",
          technicalDetails = "NoCredentialException: ${e.message ?: "No credentials available for client $activeClientId"}"
        )
      } catch (e: GetCredentialException) {
        val rawMsg = e.message ?: ""
        val exceptionName = e.javaClass.simpleName

        val diagnoseMessage = when {
          rawMsg.contains("16") || rawMsg.contains("Cannot find matching credential", ignoreCase = true) -> {
            "Error [16]: Server Client ID '$activeClientId' belum terdaftar atau tidak cocok dengan project ini. Gunakan Server Client ID bawaan Firebase."
          }
          rawMsg.contains("10") || rawMsg.contains("Developer error", ignoreCase = true) -> {
            "Developer Error [10]: SHA-1 atau Server Client ID tidak cocok. Pastikan menggunakan Client ID Web Firebase bawaan."
          }
          rawMsg.contains("403") || rawMsg.contains("access_denied", ignoreCase = true) -> {
            "Akses Ditolak (403): Status OAuth Consent Screen masih 'Testing'. Tambahkan email Anda ke daftar 'Test users' di Google Cloud Console."
          }
          else -> {
            "Login Google gagal (${exceptionName}): ${if (rawMsg.isNotBlank()) rawMsg else "Gagal mengambil kredensial"}. Silakan periksa koneksi atau reset Client ID."
          }
        }

        GoogleAuthResult.Failure(
          errorMessage = diagnoseMessage,
          technicalDetails = "$exceptionName: $rawMsg (Client: $activeClientId)"
        )
      } catch (e: Exception) {
        val rawMsg = e.message ?: ""
        val exceptionName = e.javaClass.simpleName
        GoogleAuthResult.Failure(
          errorMessage = "Terjadi kendala login: ${if (rawMsg.isNotBlank()) rawMsg else exceptionName}",
          technicalDetails = "$exceptionName: $rawMsg"
        )
      }
    } finally {
      authMutex.unlock()
    }
  }

  suspend fun signOut(context: Context) {
    val targetContext = context.findActivity() ?: context
    try {
      val credentialManager = CredentialManager.create(targetContext)
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    } catch (_: Exception) {}

    try {
      FirebaseAuth.getInstance().signOut()
    } catch (_: Exception) {}
  }

  private fun parseJwtPayload(idToken: String): Pair<String, String> {
    return try {
      val parts = idToken.split(".")
      if (parts.size >= 2) {
        val payload = parts[1]
        val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        val json = JSONObject(String(decodedBytes, Charsets.UTF_8))
        val email = json.optString("email", "")
        val name = json.optString("name", "")
        Pair(email, name)
      } else {
        Pair("", "")
      }
    } catch (_: Exception) {
      Pair("", "")
    }
  }
}
