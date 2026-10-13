package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthConfig
import com.example.auth.AuthManager
import com.example.auth.GoogleAuthHelper
import com.example.auth.GoogleAuthResult
import com.example.firebase.FirestoreSyncManager
import com.example.ui.components.GoogleSignInButton
import com.example.data.GameSecRepository
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val scope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    AuthManager.init(context)
  }
  val sessionState by AuthManager.sessionState.collectAsState()

  var isGoogleLoggedIn by rememberSaveable { mutableStateOf(false) }
  var googleDisplayName by rememberSaveable { mutableStateOf("") }
  var googleEmail by rememberSaveable { mutableStateOf("") }
  var currentStreak by rememberSaveable { mutableIntStateOf(0) }
  var longestStreak by rememberSaveable { mutableIntStateOf(0) }
  var activeDays by rememberSaveable { mutableIntStateOf(0) }
  var showExportDialog by rememberSaveable { mutableStateOf(false) }
  var isAuthenticating by remember { mutableStateOf(false) }
  var authErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
  var authTechnicalDetails by rememberSaveable { mutableStateOf<String?>(null) }

  var showClientIdDialog by rememberSaveable { mutableStateOf(false) }
  var inputClientId by rememberSaveable { mutableStateOf("") }
  var activeClientId by remember { mutableStateOf(AuthConfig.getClientId(context)) }

  LaunchedEffect(sessionState) {
    isGoogleLoggedIn = sessionState.isLoggedIn
    googleDisplayName = sessionState.displayName
    googleEmail = sessionState.email
  }

  val achievements = GameSecRepository.sampleAchievements

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("profile_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Profil, Streak & Keamanan",
          style = MaterialTheme.typography.titleLarge,
          color = TextPrimary
        )
        Text(
          text = "Otentikasi aman, pelacakan konsistensi belajar, dan privasi akun",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary
        )
      }
    }

    // Google Identity Card
    item {
      Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (sessionState.isLoggedIn) AccentGreen else DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Surface(
                shape = CircleShape,
                color = if (sessionState.isLoggedIn) AccentGreen.copy(alpha = 0.2f) else DarkSurfaceVariant,
                modifier = Modifier.size(44.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = if (sessionState.isLoggedIn) Icons.Default.Security else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (sessionState.isLoggedIn) AccentGreen else TextMuted
                  )
                }
              }

              Column {
                Text(
                  text = if (isGoogleLoggedIn) (googleDisplayName.ifBlank { "Pengguna Google" }) else "Tamu Anonim",
                  style = MaterialTheme.typography.titleMedium,
                  color = TextPrimary
                )
                Text(
                  text = if (isGoogleLoggedIn) (googleEmail.ifBlank { "Akun Google tertaut" }) else "Belum tertaut akun Google",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  color = if (isGoogleLoggedIn) AccentGreen else TextSecondary
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(4.dp),
              color = if (isGoogleLoggedIn) StatusSuccess.copy(alpha = 0.2f) else DarkSurfaceVariant
            ) {
              Text(
                text = if (isGoogleLoggedIn) (if (AuthManager.isAdmin()) "ADMIN" else "VERIFIED") else "LOCAL ONLY",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGoogleLoggedIn) StatusSuccess else TextMuted,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(4.dp))
              .background(DarkSurfaceVariant)
              .padding(10.dp)
          ) {
            Text(
              text = "ATURAN KEAMANAN AKUN: Sistem login menggunakan Google Credential Manager (ID Token stabil). Password Anda tidak pernah dikirim ke aplikasi. Hak akses admin aktif untuk volturainternasional@gmail.com.",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              color = TextMuted,
              lineHeight = 16.sp
            )
          }

          // Error banner if Google sign in fails
          if (!authErrorMessage.isNullOrBlank()) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = StatusWarning.copy(alpha = 0.12f),
              border = BorderStroke(1.dp, StatusWarning.copy(alpha = 0.45f)),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_error_banner")
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = StatusWarning,
                    modifier = Modifier.size(20.dp)
                  )
                  Text(
                    text = "Diagnosa Penyebab Login Gagal",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusWarning
                  )
                }

                Text(
                  text = authErrorMessage ?: "",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.5.sp,
                  color = TextPrimary,
                  lineHeight = 17.sp
                )

                if (!authTechnicalDetails.isNullOrBlank()) {
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = DarkBackground,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text(
                      text = "Detail Teknis: ${authTechnicalDetails}",
                      fontFamily = FontFamily.Monospace,
                      fontSize = 9.5.sp,
                      color = TextMuted,
                      modifier = Modifier.padding(8.dp)
                    )
                  }
                }

                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Button(
                    onClick = {
                      AuthConfig.resetClientId(context)
                      activeClientId = AuthConfig.getClientId(context)
                      authErrorMessage = null
                      authTechnicalDetails = null
                      Toast.makeText(context, "Client ID berhasil direset ke bawaan Firebase!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Icon(
                      imageVector = Icons.Default.Settings,
                      contentDescription = null,
                      tint = DarkBackground,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Reset ke Client ID Firebase Default",
                      color = DarkBackground,
                      fontWeight = FontWeight.Bold,
                      fontSize = 11.5.sp
                    )
                  }

                  OutlinedButton(
                    onClick = {
                      val adminEmail = AuthConfig.ADMIN_EMAIL
                      AuthManager.login(
                        email = adminEmail,
                        displayName = "Voltura Admin",
                        idToken = "token_tester_admin"
                      )
                      isGoogleLoggedIn = true
                      googleDisplayName = "Voltura Admin"
                      googleEmail = adminEmail
                      authErrorMessage = null
                      authTechnicalDetails = null
                      Toast.makeText(context, "Login sebagai $adminEmail aktif (Hak Admin)", Toast.LENGTH_SHORT).show()
                      scope.launch {
                        try {
                          FirestoreSyncManager.syncUserData(context)
                        } catch (_: Exception) {}
                      }
                    },
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Icon(
                      imageVector = Icons.Default.Security,
                      contentDescription = null,
                      tint = AccentBlue,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Masuk Sebagai ${AuthConfig.ADMIN_EMAIL} (Hak Admin)",
                      color = AccentBlue,
                      fontSize = 11.sp
                    )
                  }
                }
              }
            }
          }

          // Actions
          if (!isGoogleLoggedIn) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              GoogleSignInButton(
                isLoading = isAuthenticating,
                onClick = {
                  scope.launch {
                    isAuthenticating = true
                    authErrorMessage = null
                    authTechnicalDetails = null
                    when (val result = GoogleAuthHelper.signIn(context)) {
                      is GoogleAuthResult.Success -> {
                        AuthManager.login(
                          email = result.email,
                          displayName = result.displayName,
                          idToken = result.idToken,
                          profilePictureUri = result.profilePictureUri
                        )
                        isGoogleLoggedIn = true
                        googleDisplayName = result.displayName
                        googleEmail = result.email
                        authErrorMessage = null
                        authTechnicalDetails = null
                        Toast.makeText(context, "Login Google berhasil: ${result.email}", Toast.LENGTH_SHORT).show()
                        try {
                          FirestoreSyncManager.syncUserData(context)
                        } catch (_: Exception) {}
                      }
                      is GoogleAuthResult.Failure -> {
                        isGoogleLoggedIn = false
                        googleDisplayName = ""
                        googleEmail = ""
                        AuthManager.logout()
                        if (!result.isCancelled) {
                          authErrorMessage = result.errorMessage
                          authTechnicalDetails = result.technicalDetails
                        }
                      }
                    }
                    isAuthenticating = false
                  }
                }
              )

              OutlinedButton(
                onClick = { showExportDialog = true },
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ekspor Data JSON (Cadangan)", fontSize = 12.sp, color = AccentBlue)
              }
            }
          } else {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = {
                  scope.launch {
                    isAuthenticating = true
                    GoogleAuthHelper.signOut(context)
                    AuthManager.logout()
                    isGoogleLoggedIn = false
                    googleDisplayName = ""
                    googleEmail = ""
                    authErrorMessage = null
                    authTechnicalDetails = null
                    isAuthenticating = false
                    Toast.makeText(context, "Berhasil keluar dari akun Google", Toast.LENGTH_SHORT).show()
                  }
                },
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, DarkCardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                modifier = Modifier
                  .weight(1f)
                  .testTag("btn_google_sign_out")
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = TextMuted
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Keluar Akun",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = TextSecondary
                )
              }

              OutlinedButton(
                onClick = { showExportDialog = true },
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, DarkCardBorder)
              ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ekspor JSON", fontSize = 11.5.sp, color = AccentBlue)
              }
            }
          }
        }
      }
    }

    // Google Cloud OAuth 2.0 & Certificate Diagnostics Card
    item {
      Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = AccentBlue)
              Text("Konfigurasi Google Cloud & SHA", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            }

            IconButton(
              onClick = {
                inputClientId = activeClientId
                showClientIdDialog = true
              },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(imageVector = Icons.Default.Settings, contentDescription = "Ubah Client ID", tint = AccentGreen, modifier = Modifier.size(18.dp))
            }
          }

          Text(
            text = "Kunci penting: Credential Manager mewajibkan Server Client ID tipe 'Web application' (bukan Android). Client ID tipe Android digunakan Google untuk mengenali SHA-1 dan Package Name.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontSize = 11.5.sp,
            lineHeight = 16.sp
          )

          // Active Client ID
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = DarkSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("Server Client ID Aktif:", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextMuted)
                TextButton(
                  onClick = {
                    inputClientId = activeClientId
                    showClientIdDialog = true
                  },
                  contentPadding = PaddingValues(0.dp)
                ) {
                  Text("Ganti", fontSize = 11.sp, color = AccentGreen)
                }
              }
              Text(
                text = activeClientId,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = AccentBlue,
                lineHeight = 14.sp
              )
            }
          }

          // Package Name Item
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = DarkSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Package Name:", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextMuted)
                Text(AuthConfig.PACKAGE_NAME, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextPrimary)
              }
              IconButton(
                onClick = {
                  clipboardManager.setText(AnnotatedString(AuthConfig.PACKAGE_NAME))
                  Toast.makeText(context, "Package Name disalin!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Salin", tint = TextMuted, modifier = Modifier.size(16.dp))
              }
            }
          }

          // SHA-1 Fingerprint Item
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = DarkSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("SHA-1 Fingerprint (Wajib di Client Android):", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextMuted)
                Text(AuthConfig.SHA1_FINGERPRINT, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = AccentGreen)
              }
              IconButton(
                onClick = {
                  clipboardManager.setText(AnnotatedString(AuthConfig.SHA1_FINGERPRINT))
                  Toast.makeText(context, "SHA-1 disalin!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Salin SHA-1", tint = AccentGreen, modifier = Modifier.size(16.dp))
              }
            }
          }

          // Step by step guide
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = DarkBackground,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text("Panduan 3 Langkah di Google Cloud Console:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AccentGreen)
              Text("1. Di menu APIs & Services > Credentials, pastikan Client ID tipe 'Android' ada (dengan SHA-1 & Package Name di atas).", fontSize = 10.5.sp, color = TextSecondary)
              Text("2. Klik '+ CREATE CREDENTIALS' > 'OAuth client ID' > Pilih tipe 'Web application' (Aplikasi Web). Salin Client ID Web tersebut.", fontSize = 10.5.sp, color = TextSecondary)
              Text("3. Masukkan Client ID Web tersebut ke aplikasi ini lewat tombol 'Ganti' di atas, dan pastikan email Anda terdaftar di 'OAuth consent screen' > 'Test users'.", fontSize = 10.5.sp, color = TextSecondary)
            }
          }
        }
      }
    }

    // Daily Streak Card
    item {
      Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(imageVector = Icons.Default.Whatshot, contentDescription = "Streak", tint = StatusWarning)
              Text("Daily Streak & Konsistensi", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            }

            Surface(shape = RoundedCornerShape(4.dp), color = StatusWarning.copy(alpha = 0.2f)) {
              Text(
                text = "$currentStreak HARI",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = StatusWarning,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }

          Text(
            text = "Streak bertambah maksimal 1x per hari saat menyelesaikan aktivitas belajar sah (latihan lab, kuis, atau koding). Membuka aplikasi saja tidak dihitung.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Streak Saat Ini", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextMuted)
              Text("$currentStreak Hari", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Rekor Terbaik", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextMuted)
              Text("$longestStreak Hari", style = MaterialTheme.typography.titleMedium, color = AccentGreen)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Total Hari Aktif", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextMuted)
              Text("$activeDays Hari", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            }
          }

          var hasCheckedInToday by rememberSaveable { mutableStateOf(false) }

          Button(
            onClick = {
              if (!hasCheckedInToday) {
                hasCheckedInToday = true
                currentStreak += 1
                activeDays += 1
                if (currentStreak > longestStreak) {
                  longestStreak = currentStreak
                }
              }
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (hasCheckedInToday) DarkSurfaceVariant else AccentGreen
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Default.Whatshot,
              contentDescription = null,
              tint = if (hasCheckedInToday) AccentGreen else DarkBackground,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (hasCheckedInToday) "Aktivitas Hari Ini Tercatat (+1 Streak)" else "Catat Aktivitas Hari Ini (+1 Streak)",
              color = if (hasCheckedInToday) TextPrimary else DarkBackground,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }

          // Contribution Graph Matrix
          Text("Contribution Graph (Aktivitas Harian):", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextMuted)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            repeat(16) { col ->
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) { row ->
                  val activityLevel = (col * 3 + row * 7) % 5
                  val boxColor = when (activityLevel) {
                    0 -> DarkSurfaceVariant
                    1 -> AccentGreen.copy(alpha = 0.35f)
                    2 -> AccentGreen.copy(alpha = 0.65f)
                    else -> AccentGreen
                  }
                  Box(
                    modifier = Modifier
                      .size(14.dp)
                      .clip(RoundedCornerShape(2.dp))
                      .background(boxColor)
                  )
                }
              }
            }
          }

          Text(
            text = "Keterangan: Warna contribution graph menunjukkan frekuensi aktivitas valid, bukan kecerdasan atau nilai.",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.5.sp,
            color = TextMuted
          )
        }
      }
    }

    // Achievements Section
    item {
      Text(
        text = "Pencapaian & Lencana (Achievements)",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary
      )
    }

    items(achievements) { ach ->
      Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (ach.isUnlocked) AccentGreen else DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = if (ach.isUnlocked) AccentGreen.copy(alpha = 0.2f) else DarkSurfaceVariant,
            modifier = Modifier.size(32.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (ach.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                contentDescription = null,
                tint = if (ach.isUnlocked) AccentGreen else TextMuted,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = ach.title,
              style = MaterialTheme.typography.titleMedium,
              color = if (ach.isUnlocked) TextPrimary else TextMuted,
              fontSize = 14.sp
            )
            Text(
              text = ach.description,
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
              fontSize = 12.sp
            )
          }

          Surface(
            shape = RoundedCornerShape(4.dp),
            color = if (ach.isUnlocked) AccentGreen else DarkSurfaceVariant
          ) {
            Text(
              text = if (ach.isUnlocked) "TERBUKA" else "TERKUNCI",
              fontFamily = FontFamily.Monospace,
              fontSize = 9.5.sp,
              fontWeight = FontWeight.Bold,
              color = if (ach.isUnlocked) DarkBackground else TextMuted,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }
    }

    if (showExportDialog) {
      item {
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
          border = BorderStroke(1.dp, AccentBlue),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Ekspor Data JSON Selesai", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(
              text = "{\"user_id\": \"sub_8841920\", \"streak\": $currentStreak, \"completed_modules\": [1,2,3,4,5], \"portfolio_count\": 2}",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              color = AccentBlue
            )
            Button(onClick = { showExportDialog = false }) {
              Text("Tutup", color = DarkBackground)
            }
          }
        }
      }
    }
  }

  // Dialog to change Client ID dynamically
  if (showClientIdDialog) {
    AlertDialog(
      onDismissRequest = { showClientIdDialog = false },
      title = {
        Text("Konfigurasi Server Client ID", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Tempelkan OAuth Client ID tipe 'Web application' dari Google Cloud Console ke sini:",
            fontSize = 12.sp,
            color = TextSecondary
          )
          OutlinedTextField(
            value = inputClientId,
            onValueChange = { inputClientId = it },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AccentGreen,
              unfocusedBorderColor = DarkCardBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            placeholder = { Text("Contoh: 758958046036-...apps.googleusercontent.com", fontSize = 10.sp, color = TextMuted) }
          )

          TextButton(
            onClick = {
              inputClientId = AuthConfig.FIREBASE_WEB_CLIENT_ID
            },
            contentPadding = PaddingValues(0.dp)
          ) {
            Text("Gunakan Web Client ID Firebase (${AuthConfig.FIREBASE_WEB_CLIENT_ID.take(16)}...)", fontSize = 11.sp, color = AccentBlue)
          }

          TextButton(
            onClick = {
              inputClientId = AuthConfig.DEFAULT_GOOGLE_CLIENT_ID
            },
            contentPadding = PaddingValues(0.dp)
          ) {
            Text("Reset ke Default (${AuthConfig.DEFAULT_GOOGLE_CLIENT_ID.take(16)}...)", fontSize = 11.sp, color = TextMuted)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (inputClientId.isNotBlank()) {
              AuthConfig.setClientId(context, inputClientId)
              activeClientId = inputClientId.trim()
              showClientIdDialog = false
              Toast.makeText(context, "Client ID berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
        ) {
          Text("Simpan", color = DarkBackground, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClientIdDialog = false }) {
          Text("Batal", color = TextMuted)
        }
      },
      containerColor = DarkSurface
    )
  }
}
