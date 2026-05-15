package com.interviewquiz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.interviewquiz.core.model.Question
import com.interviewquiz.ui.theme.Error
import com.interviewquiz.ui.theme.InterviewQuizTheme
import com.interviewquiz.ui.theme.Primary
import com.interviewquiz.ui.theme.Success

@Composable
fun QuizScreen(
    onNavigate: (String) -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val score by viewModel.score.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val isAnswerChecked by viewModel.isAnswerChecked.collectAsState()

    if (questions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val currentQuestion = questions[currentQuestionIndex]
    val progress = (currentQuestionIndex + 1) * 100f / questions.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Progress Bar
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Question Counter and Score
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "题目 ${currentQuestionIndex + 1}/${questions.size}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "得分: $score",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Question Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    currentQuestion.question,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Difficulty and Category
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = getDifficultyColor(currentQuestion.difficulty.value),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            currentQuestion.difficulty.value,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            currentQuestion.category,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Options
        currentQuestion.options.forEachIndexed { index, option ->
            val isSelected = selectedAnswer == index
            val isCorrect = index == currentQuestion.correctAnswerIndex
            val showResult = isAnswerChecked

            val backgroundColor = when {
                showResult && isCorrect -> Success.copy(alpha = 0.1f)
                showResult && isSelected && !isCorrect -> Error.copy(alpha = 0.1f)
                isSelected -> Primary.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }

            val borderColor = when {
                showResult && isCorrect -> Success
                showResult && isSelected && !isCorrect -> Error
                isSelected -> Primary
                else -> MaterialTheme.colorScheme.outline
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(enabled = !isAnswerChecked) {
                        viewModel.selectAnswer(index)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = borderColor
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = if (!isAnswerChecked) {
                            { viewModel.selectAnswer(index) }
                        } else null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = if (showResult && isCorrect) Success else Primary,
                            unselectedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        option,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (showResult) {
                        Spacer(modifier = Modifier.weight(1f))
                        if (isCorrect) {
                            Text(
                                "✓ 正确",
                                color = Success,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        } else if (isSelected) {
                            Text(
                                "✗ 错误",
                                color = Error,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Explanation (shown after answer is checked)
        if (isAnswerChecked) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "答案解析",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        currentQuestion.explanation,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Action Buttons
        if (!isAnswerChecked) {
            Button(
                onClick = { viewModel.checkAnswer() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedAnswer != null,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                )
            ) {
                Text(
                    "检查答案",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            if (currentQuestionIndex < questions.size - 1) {
                Button(
                    onClick = { viewModel.nextQuestion() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    )
                ) {
                    Text(
                        "下一题",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = {
                        onNavigate("result/${score}/${questions.size}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Success
                    )
                ) {
                    Text(
                        "查看结果",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun getDifficultyColor(difficulty: String): Color {
    return when (difficulty) {
        "简单" -> Color(0xFF4CAF50)
        "中等" -> Color(0xFFFF9800)
        "困难" -> Color(0xFFF44336)
        else -> Color.Gray
    }
}

@Preview(showBackground = true, name = "Quiz Screen")
@Composable
fun QuizScreenPreview() {
    InterviewQuizTheme {
        QuizScreen(
            onNavigate = {}
        )
    }
}
