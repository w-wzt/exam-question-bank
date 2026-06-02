package com.example.examquestionbank.ui.exam

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.examquestionbank.domain.model.Question
import com.example.examquestionbank.util.QuestionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamResultScreen(
    sessionId: String,
    onNavigateHome: () -> Unit,
    viewModel: ExamResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.loadResult(sessionId)
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val session = uiState.session
    val questions = uiState.questions
    val userAnswers = uiState.userAnswers

    val totalCount = session?.totalCount ?: questions.size
    val correctCount = uiState.correctCount
    val wrongCount = uiState.wrongCount
    val unansweredCount = uiState.unansweredCount
    val accuracy = uiState.accuracy

    val grade = when {
        accuracy >= 90 -> "优秀" to Color(0xFF4CAF50)
        accuracy >= 80 -> "良好" to Color(0xFF2196F3)
        accuracy >= 60 -> "及格" to Color(0xFFFF9800)
        else -> "不及格" to Color(0xFFF44336)
    }

    var showDetails by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("考试结果") },
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 正确率大字
            Text(
                String.format("%.1f%%", accuracy),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                fontWeight = FontWeight.Bold,
                color = grade.second
            )
            Spacer(Modifier.height(8.dp))

            // 评级标签
            Surface(
                color = grade.second.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    grade.first,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = grade.second,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(32.dp))

            // 统计行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("总题数", "$totalCount", MaterialTheme.colorScheme.onSurface)
                StatItem("正确", "$correctCount", Color(0xFF4CAF50))
                StatItem("错误", "$wrongCount", Color(0xFFF44336))
                StatItem("未答", "$unansweredCount", Color(0xFFFF9800))
            }

            Spacer(Modifier.height(24.dp))

            // 用时
            if (session != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("用时：", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            uiState.durationText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 查看详情按钮
            OutlinedButton(
                onClick = { showDetails = !showDetails },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showDetails) "收起详情" else "查看详情")
            }

            if (showDetails) {
                Spacer(Modifier.height(16.dp))
                questions.forEachIndexed { index, question ->
                    val userAnswer = userAnswers[index] ?: ""
                    val isCorrect = if (userAnswer.isBlank()) null else {
                        val correct = QuestionUtils.parseAnswer(question.answer, question.type)
                        val user = QuestionUtils.parseAnswer(userAnswer, question.type)
                        QuestionUtils.checkAnswer(user, correct, question.type)
                    }
                    DetailQuestionItem(
                        index = index,
                        question = question,
                        userAnswer = userAnswer,
                        isCorrect = isCorrect
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // 返回首页按钮
            Button(
                onClick = onNavigateHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("返回首页", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = color.copy(alpha = 0.7f))
    }
}

@Composable
private fun DetailQuestionItem(
    index: Int,
    question: Question,
    userAnswer: String,
    isCorrect: Boolean?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCorrect == true -> Color(0xFFE8F5E9)
                isCorrect == false -> Color(0xFFFFEBEE)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when {
                        isCorrect == true -> Icons.Default.Check
                        isCorrect == false -> Icons.Default.Close
                        else -> Icons.Default.Close
                    },
                    contentDescription = null,
                    tint = when {
                        isCorrect == true -> Color(0xFF4CAF50)
                        isCorrect == false -> Color(0xFFF44336)
                        else -> Color.Gray
                    },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "第${index + 1}题",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.width(8.dp))
                ResultTypeTag(question.type)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                question.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row {
                Text(
                    "你的答案：${if (userAnswer.isBlank()) "未作答" else userAnswer}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        isCorrect == true -> Color(0xFF2E7D32)
                        isCorrect == false -> Color(0xFFC62828)
                        else -> Color.Gray
                    }
                )
                Spacer(Modifier.width(16.dp))
                if (isCorrect != true) {
                    Text(
                        "正确答案：${question.answer}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultTypeTag(type: String) {
    val (text, color) = when (type) {
        "single" -> "单选" to Color(0xFF2196F3)
        "multiple" -> "多选" to Color(0xFFFF9800)
        "judge" -> "判断" to Color(0xFF4CAF50)
        else -> type to Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
