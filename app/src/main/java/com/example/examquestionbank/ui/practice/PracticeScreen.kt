package com.example.examquestionbank.ui.practice

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.examquestionbank.domain.model.OptionItem
import com.example.examquestionbank.domain.model.Question

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    bankId: Long,
    onNavigateBack: () -> Unit,
    viewModel: PracticeViewModel = hiltViewModel()
) {
    LaunchedEffect(bankId) {
        viewModel.checkAndLoadProgress(bankId)
    }

    val uiState by viewModel.uiState.collectAsState()
    var showResumeDialog by remember { mutableStateOf(true) }

    // 续答对话框
    if (uiState.hasProgress && showResumeDialog) {
        AlertDialog(
            onDismissRequest = {
                showResumeDialog = false
                viewModel.dismissProgress()
            },
            title = { Text("发现未完成的练习") },
            text = { Text("检测到上次练习进度，是否继续？") },
            confirmButton = {
                TextButton(onClick = {
                    showResumeDialog = false
                    viewModel.restoreProgress()
                }) { Text("继续练习") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showResumeDialog = false
                    viewModel.dismissProgress()
                }) { Text("重新开始") }
            }
        )
    }

    // 退出时保存进度
    DisposableEffect(Unit) {
        onDispose { viewModel.exitPractice() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            uiState.bankName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (uiState.questions.isNotEmpty()) {
                            Text(
                                "${uiState.currentIndex + 1}/${uiState.questions.size}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.questions.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无题目", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            val question = uiState.questions[uiState.currentIndex]
            val isSubmitted = uiState.submittedMap.containsKey(uiState.currentIndex)
            val isCorrect = uiState.showResult[uiState.currentIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // 题目内容
                QuestionContentSection(question, uiState.currentIndex)

                Spacer(Modifier.height(16.dp))

                // 选项区域
                OptionsSection(
                    question = question,
                    userAnswer = uiState.userAnswers[uiState.currentIndex] ?: "",
                    isSubmitted = isSubmitted,
                    isCorrect = isCorrect,
                    onAnswerSelected = { answer ->
                        if (question.type == "single" || question.type == "judge") {
                            viewModel.submitAnswer(uiState.currentIndex, answer)
                        } else {
                            viewModel.updateUserAnswer(uiState.currentIndex, answer)
                        }
                    },
                    onConfirmMultiple = {
                        val answer = uiState.userAnswers[uiState.currentIndex] ?: ""
                        if (answer.isNotBlank()) {
                            viewModel.submitAnswer(uiState.currentIndex, answer)
                        }
                    }
                )

                // 提交后显示结果和解析
                if (isSubmitted && isCorrect != null) {
                    Spacer(Modifier.height(16.dp))
                    ResultAndExplanationSection(question, isCorrect)
                }

                Spacer(Modifier.weight(1f))

                // 底部导航按钮
                NavigationButtons(
                    currentIndex = uiState.currentIndex,
                    totalCount = uiState.questions.size,
                    onPrev = { viewModel.goToPrev() },
                    onNext = { viewModel.goToNext() }
                )
            }
        }
    }
}

@Composable
private fun QuestionContentSection(question: Question, index: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TypeTag(question.type)
                Spacer(Modifier.width(8.dp))
                Text(
                    "第${index + 1}题",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                question.content,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun OptionsSection(
    question: Question,
    userAnswer: String,
    isSubmitted: Boolean,
    isCorrect: Boolean?,
    onAnswerSelected: (String) -> Unit,
    onConfirmMultiple: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        when (question.type) {
            "single" -> SingleOptions(
                options = question.options,
                selected = userAnswer,
                isSubmitted = isSubmitted,
                correctAnswer = question.answer,
                onSelected = onAnswerSelected
            )
            "multiple" -> MultipleOptions(
                options = question.options,
                selected = userAnswer,
                isSubmitted = isSubmitted,
                correctAnswer = question.answer,
                onSelected = onAnswerSelected,
                onConfirm = onConfirmMultiple
            )
            "judge" -> JudgeOptions(
                selected = userAnswer,
                isSubmitted = isSubmitted,
                correctAnswer = question.answer,
                onSelected = onAnswerSelected
            )
        }
    }
}

@Composable
private fun SingleOptions(
    options: List<OptionItem>,
    selected: String,
    isSubmitted: Boolean,
    correctAnswer: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = selected == option.label
            val isCorrectOption = correctAnswer == option.label
            val bgColor = when {
                isSubmitted && isCorrectOption -> Color(0xFFE8F5E9)
                isSubmitted && isSelected && !isCorrectOption -> Color(0xFFFFEBEE)
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val borderColor = when {
                isSubmitted && isCorrectOption -> Color(0xFF4CAF50)
                isSubmitted && isSelected && !isCorrectOption -> Color(0xFFF44336)
                isSelected -> MaterialTheme.colorScheme.primary
                else -> Color.Transparent
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .clickable(enabled = !isSubmitted) { onSelected(option.label) }
                    .background(bgColor)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 圆形选项标识
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "${option.label}. ${option.content}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isSubmitted && isCorrectOption -> Color(0xFF2E7D32)
                        isSubmitted && isSelected && !isCorrectOption -> Color(0xFFC62828)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}

@Composable
private fun MultipleOptions(
    options: List<OptionItem>,
    selected: String,
    isSubmitted: Boolean,
    correctAnswer: String,
    onSelected: (String) -> Unit,
    onConfirm: () -> Unit
) {
    val selectedSet = remember(selected) {
        if (selected.isBlank()) emptySet()
        else selected.chunked(1).filter { it in listOf("A", "B", "C", "D", "E", "F", "G", "H") }.toSet()
    }
    val correctSet = remember(correctAnswer) {
        if (correctAnswer.startsWith("[")) {
            try {
                com.google.gson.Gson().fromJson(correctAnswer, List::class.java)
                    .filterIsInstance<String>().toSet()
            } catch (_: Exception) {
                correctAnswer.chunked(1).filter { it in listOf("A", "B", "C", "D", "E", "F", "G", "H") }.toSet()
            }
        } else {
            correctAnswer.chunked(1).filter { it in listOf("A", "B", "C", "D", "E", "F", "G", "H") }.toSet()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = option.label in selectedSet
            val isCorrectOption = option.label in correctSet
            val bgColor = when {
                isSubmitted && isCorrectOption -> Color(0xFFE8F5E9)
                isSubmitted && isSelected && !isCorrectOption -> Color(0xFFFFEBEE)
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val borderColor = when {
                isSubmitted && isCorrectOption -> Color(0xFF4CAF50)
                isSubmitted && isSelected && !isCorrectOption -> Color(0xFFF44336)
                isSelected -> MaterialTheme.colorScheme.primary
                else -> Color.Transparent
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .clickable(enabled = !isSubmitted) {
                        val newSet = if (isSelected) selectedSet - option.label else selectedSet + option.label
                        onSelected(newSet.sorted().joinToString(""))
                    }
                    .background(bgColor)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 方形选项标识
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(
                            2.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "${option.label}. ${option.content}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isSubmitted && isCorrectOption -> Color(0xFF2E7D32)
                        isSubmitted && isSelected && !isCorrectOption -> Color(0xFFC62828)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }

        // 确认按钮
        if (!isSubmitted) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedSet.isNotEmpty()
            ) {
                Text("确认提交")
            }
        }
    }
}

@Composable
private fun JudgeOptions(
    selected: String,
    isSubmitted: Boolean,
    correctAnswer: String,
    onSelected: (String) -> Unit
) {
    val isCorrectAnswer = correctAnswer == "T" || correctAnswer.equals("true", true)
    val selectedIsTrue = selected == "T"
    val selectedIsFalse = selected == "F"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 正确按钮 (A → T)
        JudgeButton(
            text = "正确",
            label = "A",
            isSelected = selectedIsTrue,
            isSubmitted = isSubmitted,
            isThisCorrect = isCorrectAnswer,
            modifier = Modifier.weight(1f),
            onClick = { onSelected("T") }
        )
        // 错误按钮 (B → F)
        JudgeButton(
            text = "错误",
            label = "B",
            isSelected = selectedIsFalse,
            isSubmitted = isSubmitted,
            isThisCorrect = !isCorrectAnswer,
            modifier = Modifier.weight(1f),
            onClick = { onSelected("F") }
        )
    }
}

@Composable
private fun JudgeButton(
    text: String,
    label: String,
    isSelected: Boolean,
    isSubmitted: Boolean,
    isThisCorrect: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSubmitted && isThisCorrect -> Color(0xFFE8F5E9)
        isSubmitted && isSelected && !isThisCorrect -> Color(0xFFFFEBEE)
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val borderColor = when {
        isSubmitted && isThisCorrect -> Color(0xFF4CAF50)
        isSubmitted && isSelected && !isThisCorrect -> Color(0xFFF44336)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = !isSubmitted) { onClick() }
            .background(bgColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = when {
                isSubmitted && isThisCorrect -> Color(0xFF2E7D32)
                isSubmitted && isSelected && !isThisCorrect -> Color(0xFFC62828)
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun ResultAndExplanationSection(question: Question, isCorrect: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isCorrect) "回答正确" else "回答错误",
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
            if (!isCorrect) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "正确答案：${question.answer}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2E7D32)
                )
            }
            if (question.explanation.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "解析：${question.explanation}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NavigationButtons(
    currentIndex: Int,
    totalCount: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onPrev,
            enabled = currentIndex > 0,
            modifier = Modifier.weight(1f)
        ) { Text("上一题") }
        Button(
            onClick = onNext,
            enabled = currentIndex < totalCount - 1,
            modifier = Modifier.weight(1f)
        ) { Text("下一题") }
    }
}

@Composable
private fun TypeTag(type: String) {
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun Modifier.background(color: Color) = this.then(
    androidx.compose.foundation.background(color)
)
