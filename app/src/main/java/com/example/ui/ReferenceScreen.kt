package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameSecRepository
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReferenceScreen(
  modifier: Modifier = Modifier
) {
  var searchQuery by rememberSaveable { mutableStateOf("") }
  val allReferences = GameSecRepository.references

  val filteredReferences = if (searchQuery.isBlank()) {
    allReferences
  } else {
    allReferences.filter {
      it.title.contains(searchQuery, ignoreCase = true) ||
        it.summary.contains(searchQuery, ignoreCase = true) ||
        it.keyTerms.any { term -> term.contains(searchQuery, ignoreCase = true) }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("reference_screen")
  ) {
    // Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Text(
        text = "Referensi Konsep",
        style = MaterialTheme.typography.titleLarge,
        color = TextPrimary
      )
      Text(
        text = "Kompilasi topik arsitektur dan sistem pertahanan game multiplayer",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Search Box
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = {
          Text(
            text = "Cari konsep, misal: prediction, integrity...",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Cari",
            tint = AccentBlue
          )
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DarkSurface,
          unfocusedContainerColor = DarkSurface,
          focusedBorderColor = AccentBlue,
          unfocusedBorderColor = DarkCardBorder,
          cursorColor = AccentBlue,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("search_reference_input")
      )
    }

    // Reference List
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .weight(1f)
        .testTag("reference_list"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(filteredReferences, key = { it.id }) { item ->
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          border = BorderStroke(1.dp, DarkCardBorder),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("ref_card_${item.id}")
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Text(
              text = item.title,
              style = MaterialTheme.typography.titleMedium,
              color = TextPrimary,
              fontWeight = FontWeight.SemiBold
            )

            Text(
              text = item.summary,
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
              lineHeight = 21.sp
            )

            // Key terms chips
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              item.keyTerms.forEach { term ->
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = DarkSurfaceVariant,
                  border = BorderStroke(1.dp, DarkCardBorder)
                ) {
                  Text(
                    text = term,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = AccentBlue,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                  )
                }
              }
            }

            // Industrial Practice Note Box
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(DarkSurfaceVariant)
                .padding(12.dp)
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "KONTEKS REKAYASA INDUSTRI:",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextMuted
                )
                Text(
                  text = item.industrialPractice,
                  style = MaterialTheme.typography.bodyMedium,
                  fontSize = 12.5.sp,
                  color = TextSecondary,
                  lineHeight = 18.sp
                )
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}
