package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.auth.AuthConfig
import com.example.auth.UserSession
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Standard Google Sign-In Button compliant with Google Identity branding guidelines.
 * Features the official 4-color Google "G" logo, responsive loading state,
 * and high-contrast Material 3 styling.
 */
@Composable
fun GoogleSignInButton(
  modifier: Modifier = Modifier,
  isLoading: Boolean = false,
  text: String = "Masuk dengan Google",
  onClick: () -> Unit
) {
  Button(
    onClick = onClick,
    enabled = !isLoading,
    shape = RoundedCornerShape(8.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = Color.White,
      contentColor = Color(0xFF1F1F1F),
      disabledContainerColor = Color(0xFFE0E0E0),
      disabledContentColor = Color(0xFF9E9E9E)
    ),
    border = BorderStroke(1.dp, Color(0xFFDADCE0)),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    modifier = modifier
      .fillMaxWidth()
      .height(50.dp)
      .testTag("btn_google_sign_in")
  ) {
    if (isLoading) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(20.dp),
          color = Color(0xFF4285F4),
          strokeWidth = 2.2.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = "Menghubungkan Akun...",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF5F6368)
        )
      }
    } else {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.ic_google_logo),
          contentDescription = "Logo Google",
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = text,
          fontSize = 14.5.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF1F1F1F)
        )
      }
    }
  }
}

/**
 * Display Card showing authenticated user state (name, email, role badge, last login)
 * and an explicit Sign Out trigger.
 */
@Composable
fun GoogleUserAccountCard(
  session: UserSession,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isAdmin = session.email.trim().equals(AuthConfig.ADMIN_EMAIL, ignoreCase = true)

  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = BorderStroke(
      1.dp,
      if (isAdmin) AccentGreen else AccentBlue.copy(alpha = 0.5f)
    ),
    modifier = modifier
      .fillMaxWidth()
      .testTag("user_account_card")
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // User Avatar Circle
        Surface(
          shape = CircleShape,
          color = if (isAdmin) AccentGreen.copy(alpha = 0.15f) else AccentBlue.copy(alpha = 0.15f),
          border = BorderStroke(1.dp, if (isAdmin) AccentGreen else AccentBlue),
          modifier = Modifier.size(48.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = if (isAdmin) Icons.Default.Security else Icons.Default.Person,
              contentDescription = null,
              tint = if (isAdmin) AccentGreen else AccentBlue,
              modifier = Modifier.size(26.dp)
            )
          }
        }

        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = session.displayName.ifBlank { "Pengguna Google" },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            // Status Badge
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = if (isAdmin) AccentGreen else AccentBlue
            ) {
              Text(
                text = if (isAdmin) "ADMIN" else "TERTAUT",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBackground,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Text(
            text = session.email,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = if (isAdmin) AccentGreen else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(6.dp),
        color = DarkSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = StatusSuccess,
              modifier = Modifier.size(14.dp)
            )
            Text(
              text = "Otentikasi Google Credential Manager Aktif",
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              color = TextMuted
            )
          }

          if (session.lastLoginDate.isNotBlank()) {
            Text(
              text = session.lastLoginDate,
              fontFamily = FontFamily.Monospace,
              fontSize = 9.sp,
              color = TextMuted
            )
          }
        }
      }

      OutlinedButton(
        onClick = onSignOut,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, DarkCardBorder),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = TextSecondary
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("btn_sign_out")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ExitToApp,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
          tint = TextMuted
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Keluar dari Akun Google",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = TextSecondary
        )
      }
    }
  }
}
