package com.example.examquestionbank.ui.wrong

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun WrongQuestionsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: WrongQuestionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isRedoMode) {
        WrongQuestionRedoContent(
            viewModel = viewModel
        )
    } else {
        WrongQuestionsListContent(
            uiState = uiState,
            onFilterChange = { viewModel.setFilter(it) },
            onStartRedo = { viewModel.startRedo() },
            onNavigateBack = onNavigateBack
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WrongQuestionsListContent(
    uiState: WrongQuestionsUiState,
    onFilterChange: (Int?) -> Unit,
    onStartRedo: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("错题本") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 筛选Chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filterMastery == null,
                    onClick = { onFilterChange(null) },
                    label = { Text("全部") }
                )
                FilterChip(
                    selected = uiState.filterMastery == 0,
                    onClick = { onFilterChange(0) },
                    label = { Text("未掌握") }
                )
                FilterChip(
                    selected = uiState.filterMastery == 1,
                    onClick = { onFilterChange(1) },
                    label = { Text("基本掌握") }
                )
            }

            // 重做按钮
            if (uiState.items.isNotEmpty()) {
                Button(
                    onClick = onStartRedo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text("重做错题")
                }
            }

            // 错题列表
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "暂无错题",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "答题过程中答错的题目会自动加入错题本",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "共 ${uiState.items.size} 题",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    items(uiState.items, key = { it.flag.questionId }) { item ->
                        item.question?.let { question ->
                            WrongQuestionCard(
                                question = question,
                                masteryLevel = item.flag.masteryLevel,
                                onClick = { onStartRedo() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WrongQuestionCard(
    question: Question,
    masteryLevel: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                WrongTypeTag(question.type)
                Spacer(Modifier.width(8.dp))
                MasteryTag(masteryLevel)
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                question.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WrongTypeTag(type: String) {
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

@Composable
private fun MasteryTag(masteryLevel: Int) {
    val (text, color) = when (masteryLevel) {
        0 -> "未掌握" to Color(0xFFF44336)
        1 -> "基本掌握" to Color(0xFFFF9800)
        2 -> "已掌握" to Color(0xFF4CAF50)
        else -> "未知" to Color.Gray
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WrongQuestionRedoContent(
    viewModel: WrongQuestionsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val questions = uiState.redoQuestions

    if (questions.isEmpty()) {
        viewModel.exitRedo()
        return
    }

    val currentIndex = uiState.redoIndex
    val question = questions[currentIndex]
    val isSubmitted = uiState.redoSubmittedMap.containsKey(currentIndex)
    val isCorrect = uiState.redoShowResult[currentIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("错题重做")
                        Text(
                            "${currentIndex + 1}/${questions.size}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.exitRedo() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "退出重做")
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
        ) {
            // 题目内容
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        WrongTypeTag(question.type)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "第${currentIndex + 1}题",
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

            Spacer(Modifier.height(16.dp))

            // 选项区域
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                when (question.type) {
                    "single" -> RedoSingleOptions(
                        options = question.options,
                        selected = uiState.redoUserAnswers[currentIndex] ?: "",
                        isSubmitted = isSubmitted,
                        correctAnswer = question.answer,
                        onSelected = { answer ->
                            viewModel.submitRedoAnswer(currentIndex, answer)
                        }
                    )
                    "multiple" -> RedoMultipleOptions(
                        options = question.options,
                        selected = uiState.redoUserAnswers[currentIndex] ?: "",
                        isSubmitted = isSubmitted,
                        correctAnswer = question.answer,
                        onSelected = { answer ->
                            viewModel.updateRedoUserAnswer(currentIndex, answer)
                        },
                        onConfirm = {
                            val answer = uiState.redoUserAnswers[currentIndex] ?: ""
                            if (answer.isNotBlank()) {
                                viewModel.submitRedoAnswer(currentIndex, answer)
                            }
                        }
                    )
                    "judge" -> RedoJudgeOptions(
                        selected = uiState.redoUserAnswers[currentIndex] ?: "",
                        isSubmitted = isSubmitted,
                        correctAnswer = question.answer,
                        onSelected = { answer ->
                            viewModel.submitRedoAnswer(currentIndex, answer)
                        }
                    )
                }
            }

            // 提交后显示结果和解析
            if (isSubmitted && isCorrect != null) {
                Spacer(Modifier.height(16.dp))
                RedoResultAndExplanation(question, isCorrect)
            }

            Spacer(Modifier.weight(1f))

            // 底部导航
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.redoGoToPrev() },
                    enabled = currentIndex > 0,
                    modifier = Modifier.weight(1f)
                ) { Text("上一题") }
                Button(
                    onClick = { viewModel.redoGoToNext() },
                    enabled = currentIndex < questions.size - 1,
                    modifier = Modifier.weight(1f)
                ) { Text("下一题") }
            }
        }
    }
}

@Composable
private fun RedoSingleOptions(
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
private fun RedoMultipleOptions(
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

        if (!isSubmitted) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedSet.isNotEmpty()
            ) { Text("确认提交") }
        }
    }
}

@Composable
private fun RedoJudgeOptions(
    selected: String,
    isSubmitted: Boolean,
    correctAnswer: String,
    onSelected: (String) -> Unit
) {
    val isCorrectAnswer = correctAnswer == "T" || correctAnswer.equals("true", true)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RedoJudgeButton(
            text = "正确",
            isSelected = selected == "T",
            isSubmitted = isSubmitted,
            isThisCorrect = isCorrectAnswer,
            modifier = Modifier.weight(1f),
            onClick = { onSelected("T") }
        )
        RedoJudgeButton(
            text = "错误",
            isSelected = selected == "F",
            isSubmitted = isSubmitted,
            isThisCorrect = !isCorrectAnswer,
            modifier = Modifier.weight(1f),
            onClick = { onSelected("F") }
        )
    }
}

@Composable
private fun RedoJudgeButton(
    text: String,
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
private fun RedoResultAndExplanation(question: Question, isCorrect: Boolean) {
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
                    if (isCorrect) "回答正确，掌握度提升" else "回答错误，掌握度降低",
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

private fun Modifier.background(color: Color) = this.then(
    androidx.compose.foundation.background(color)
)
