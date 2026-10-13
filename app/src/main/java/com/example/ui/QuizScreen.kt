package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun QuizScreen(
  modifier: Modifier = Modifier
) {
  val questions = GameSecRepository.quizQuestions
  var currentQuestionIndex by rememberSaveable { mutableIntStateOf(0) }
  // Map of questionIndex to user selectedOptionIndex
  val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
  var showSummary by rememberSaveable { mutableStateOf(false) }

  val currentQuestion = questions[currentQuestionIndex]
  val selectedAnswer = selectedAnswers[currentQuestionIndex]
  val isAnswered = selectedAnswer != null

  val totalScore = remember(selectedAnswers.size, showSummary) {
    questions.indices.count { idx ->
      selectedAnswers[idx] == questions[idx].correctIndex
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("quiz_screen")
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Kuis Evaluasi",
          style = MaterialTheme.typography.titleLarge,
          color = TextPrimary
        )
        Text(
          text = "Evaluasi pemahaman konsep arsitektur dan sistem deteksi",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary
        )
      }

      if (!showSummary) {
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = DarkSurfaceVariant,
          border = BorderStroke(1.dp, DarkCardBorder)
        ) {
          Text(
            text = "${currentQuestionIndex + 1}/${questions.size}",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AccentBlue,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }
    }

    if (showSummary) {
      // Summary View
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        item {
          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Text(
                text = "HASIL EVALUASI",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AccentBlue
              )

              Text(
                text = "$totalScore / ${questions.size}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )

              val percentage = (totalScore.toFloat() / questions.size) * 100
              Text(
                text = "Tingkat Akurasi: ${percentage.toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                color = if (percentage >= 75) StatusSuccess else TextSecondary
              )

              Text(
                text = if (percentage >= 75)
                  "Pemahaman menyeluruh mengenai authoritative server, batasan client, dan validasi heuristik telah tercapai."
                else
                  "Dianjurkan meninjau kembali Modul 1 dan Modul 4 untuk memperkuat pemahaman mengenai alur pertahanan server.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                lineHeight = 20.sp
              )

              Spacer(modifier = Modifier.height(6.dp))

              Button(
                onClick = {
                  selectedAnswers.clear()
                  currentQuestionIndex = 0
                  showSummary = false
                },
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                modifier = Modifier.testTag("btn_restart_quiz")
              ) {
                Icon(
                  imageVector = Icons.Default.RestartAlt,
                  contentDescription = null,
                  tint = DarkBackground,
                  modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                  text = "Ulangi Kuis",
                  color = DarkBackground,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        item {
          Text(
            text = "Daftar Review Jawaban",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
          )
        }

        // List of all questions with results
        items(questions.size) { qIdx ->
          val q = questions[qIdx]
          val userChoice = selectedAnswers[qIdx]
          val isCorrect = userChoice == q.correctIndex

          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(
              1.dp,
              if (isCorrect) StatusSuccess.copy(alpha = 0.5f) else StatusDanger.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Soal ${qIdx + 1}",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = AccentBlue
                )
                Text(
                  text = if (isCorrect) "BENAR" else "SALAH",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isCorrect) StatusSuccess else StatusDanger
                )
              }

              Text(
                text = q.question,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
              )

              // Explanation
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(4.dp))
                  .background(DarkSurfaceVariant)
                  .padding(10.dp)
              ) {
                Text(
                  text = "Pembahasan: " + q.explanation,
                  style = MaterialTheme.typography.bodyMedium,
                  fontSize = 12.sp,
                  color = TextSecondary,
                  lineHeight = 18.sp
                )
              }
            }
          }
        }

        item {
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    } else {
      // Question Form
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .weight(1f)
          .testTag("quiz_question_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Question Card
        item {
          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "PERTANYAAN ${currentQuestionIndex + 1} DARI ${questions.size}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AccentBlue
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = currentQuestion.question,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                lineHeight = 24.sp
              )
            }
          }
        }

        // Options List
        items(currentQuestion.options.size) { optIndex ->
          val optionText = currentQuestion.options[optIndex]
          val isSelectedOption = selectedAnswer == optIndex
          val isCorrectOption = optIndex == currentQuestion.correctIndex

          val borderColor = when {
            !isAnswered -> if (isSelectedOption) AccentBlue else DarkCardBorder
            isCorrectOption -> StatusSuccess
            isSelectedOption -> StatusDanger
            else -> DarkCardBorder
          }

          val optionBg = when {
            !isAnswered -> if (isSelectedOption) DarkSurfaceVariant else DarkSurface
            isCorrectOption -> StatusSuccess.copy(alpha = 0.12f)
            isSelectedOption -> StatusDanger.copy(alpha = 0.12f)
            else -> DarkSurface
          }

          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = optionBg),
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .clickable(enabled = !isAnswered) {
                selectedAnswers[currentQuestionIndex] = optIndex
              }
              .testTag("quiz_option_${optIndex}")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.Top,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              val optionLabels = listOf("A", "B", "C", "D")
              val labelChar = optionLabels.getOrElse(optIndex) { "${optIndex + 1}" }

              Surface(
                shape = RoundedCornerShape(4.dp),
                color = when {
                  !isAnswered -> DarkSurfaceVariant
                  isCorrectOption -> StatusSuccess
                  isSelectedOption -> StatusDanger
                  else -> DarkSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  if (isAnswered && isCorrectOption) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  } else if (isAnswered && isSelectedOption) {
                    Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  } else {
                    Text(
                      text = labelChar,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 11.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isSelectedOption) AccentBlue else TextSecondary
                    )
                  }
                }
              }

              Text(
                text = optionText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                lineHeight = 21.sp,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Explanation Box (visible after user answers)
        if (isAnswered) {
          item {
            AnimatedVisibility(
              visible = true,
              enter = fadeIn()
            ) {
              Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(
                  1.dp,
                  if (selectedAnswer == currentQuestion.correctIndex) StatusSuccess else AccentBlue
                ),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(14.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Text(
                      text = if (selectedAnswer == currentQuestion.correctIndex)
                        "JAWABAN TEPAT"
                      else
                        "JAWABAN KURANG TEPAT",
                      fontFamily = FontFamily.Monospace,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (selectedAnswer == currentQuestion.correctIndex) StatusSuccess else StatusDanger
                    )
                  }

                  Text(
                    text = "Pembahasan Teknis:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue
                  )

                  Text(
                    text = currentQuestion.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 20.sp
                  )
                }
              }
            }
          }
        }

        // Navigation Row
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (currentQuestionIndex > 0) {
              OutlinedButton(
                onClick = { currentQuestionIndex-- },
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, DarkCardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                modifier = Modifier.testTag("btn_prev_quiz")
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = null,
                  modifier = Modifier.padding(end = 4.dp)
                )
                Text("Sebelumnya")
              }
            } else {
              Spacer(modifier = Modifier.width(1.dp))
            }

            if (currentQuestionIndex < questions.lastIndex) {
              Button(
                onClick = { currentQuestionIndex++ },
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                modifier = Modifier.testTag("btn_next_quiz")
              ) {
                Text("Berikutnya", color = DarkBackground, fontWeight = FontWeight.Bold)
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = null,
                  tint = DarkBackground,
                  modifier = Modifier.padding(start = 4.dp)
                )
              }
            } else {
              Button(
                onClick = { showSummary = true },
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                modifier = Modifier.testTag("btn_finish_quiz")
              ) {
                Text("Lihat Hasil Akhir", color = DarkBackground, fontWeight = FontWeight.Bold)
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
}
