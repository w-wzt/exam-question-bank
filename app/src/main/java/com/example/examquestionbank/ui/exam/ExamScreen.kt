package com.example.examquestionbank.ui.exam

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.examquestionbank.domain.model.OptionItem
import com.example.examquestionbank.domain.model.Question

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
    bankId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToResult: (String) -> Unit,
    viewModel: ExamViewModel = hiltViewModel()
) {
    var examStarted by remember { mutableStateOf(false) }
    var questionCount by remember { mutableStateOf(20) }
    var timeLimitMinutes by remember { mutableStateOf(30) }

    if (!examStarted) {
        ExamSetupContent(
            questionCount = questionCount,
            timeLimitMinutes = timeLimitMinutes,
            onQuestionCountChange = { questionCount = it },
            onTimeLimitChange = { timeLimitMinutes = it },
            onStart = {
                viewModel.startExam(bankId, questionCount, timeLimitMinutes * 60)
                examStarted = true
            },
            onBack = onNavigateBack
        )
    } else {
        ExamContent(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onNavigateToResult = onNavigateToResult
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExamSetupContent(
    questionCount: Int,
    timeLimitMinutes: Int,
    onQuestionCountChange: (Int) -> Unit,
    onTimeLimitChange: (Int) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("考试设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("考试配置", style = MaterialTheme.typography.headlineSmall)

            // 题目数量
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("题目数量：$questionCount", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(10, 20, 30, 50).forEach { count ->
            FilterChip(
                selected = questionCount == count,
                onClick = { onQuestionCountChange(count) },
                label = { Text("${count}题") }
            )
        }
                    }
                }
            }

            // 时间限制
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("时间限制：${timeLimitMinutes}分钟", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 30, 45, 60, 90).forEach { minutes ->
            FilterChip(
                selected = timeLimitMinutes == minutes,
                onClick = { onTimeLimitChange(minutes) },
                label = { Text("${minutes}分钟") }
            )
        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("开始考试", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExamContent(
    viewModel: ExamViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // 交卷确认对话框
    var showFinishDialog by remember { mutableStateOf(false) }

    // 考试结束自动跳转
    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished && uiState.session != null) {
            onNavigateToResult(uiState.session.id)
        }
    }

    if (showFinishDialog) {
        val unanswered = uiState.questions.size - uiState.userAnswers.size
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("确认交卷") },
            text = {
                if (unanswered > 0) {
                    Text("还有 $unanswered 题未作答，确定交卷吗？")
                } else {
                    Text("所有题目已作答，确定交卷吗？")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showFinishDialog = false
                    viewModel.finishExam()
                }) { Text("交卷") }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) { Text("继续答题") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${uiState.currentIndex + 1}/${uiState.questions.size}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        // 倒计时
                        if (uiState.remainingSeconds > 0) {
                            val timeColor = if (uiState.remainingSeconds <= 60) {
                                Color(0xFFF44336)
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                            Text(
                                viewModel.formatTime(),
                                style = MaterialTheme.typography.titleMedium,
                                color = timeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        showFinishDialog = true
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "交卷")
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // 题目内容
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    ExamQuestionContent(question, uiState.currentIndex)

                    Spacer(Modifier.height(16.dp))

                    // 选项区域（考试模式不显示正确答案）
                    ExamOptionsSection(
                        question = question,
                        userAnswer = uiState.userAnswers[uiState.currentIndex] ?: "",
                        onAnswerSelected = { answer ->
                            viewModel.submitAnswer(uiState.currentIndex, answer)
                        }
                    )
                }

                // 底部：答题卡 + 导航
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.toggleAnswerCard() },
                        modifier = Modifier.weight(1f)
                    ) { Text("答题卡") }
                    OutlinedButton(
                        onClick = { viewModel.goToPrev() },
                        enabled = uiState.currentIndex > 0,
                        modifier = Modifier.weight(1f)
                    ) { Text("上一题") }
                    Button(
                        onClick = { viewModel.goToNext() },
                        enabled = uiState.currentIndex < uiState.questions.size - 1,
                        modifier = Modifier.weight(1f)
                    ) { Text("下一题") }
                }
            }

            // 答题卡弹窗
            if (uiState.showAnswerCard) {
                AnswerCardSheet(
                    questions = uiState.questions,
                    userAnswers = uiState.userAnswers,
                    currentIndex = uiState.currentIndex,
                    onIndexClick = { index ->
                        viewModel.goToIndex(index)
                        viewModel.toggleAnswerCard()
                    },
                    onDismiss = { viewModel.toggleAnswerCard() },
                    onFinish = {
                        viewModel.toggleAnswerCard()
                        showFinishDialog = true
                    }
                )
            }
        }
    }
}

@Composable
private fun ExamQuestionContent(question: Question, index: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExamTypeTag(question.type)
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
private fun ExamOptionsSection(
    question: Question,
    userAnswer: String,
    onAnswerSelected: (String) -> Unit
) {
    when (question.type) {
        "single" -> ExamSingleOptions(
            options = question.options,
            selected = userAnswer,
            onSelected = onAnswerSelected
        )
        "multiple" -> ExamMultipleOptions(
            options = question.options,
            selected = userAnswer,
            onSelected = onAnswerSelected
        )
        "judge" -> ExamJudgeOptions(
            selected = userAnswer,
            onSelected = onAnswerSelected
        )
    }
}

@Composable
private fun ExamSingleOptions(
    options: List<OptionItem>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = selected == option.label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelected(option.label) }
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
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
                Text("${option.label}. ${option.content}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ExamMultipleOptions(
    options: List<OptionItem>,
    selected: String,
    onSelected: (String) -> Unit
) {
    val selectedSet = remember(selected) {
        if (selected.isBlank()) emptySet()
        else selected.chunked(1).filter { it in listOf("A", "B", "C", "D", "E", "F", "G", "H") }.toSet()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = option.label in selectedSet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        val newSet = if (isSelected) selectedSet - option.label else selectedSet + option.label
                        onSelected(newSet.sorted().joinToString(""))
                    }
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
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
                Text("${option.label}. ${option.content}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ExamJudgeOptions(
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ExamJudgeButton(
            text = "正确",
            isSelected = selected == "T",
            modifier = Modifier.weight(1f),
            onClick = { onSelected("T") }
        )
        ExamJudgeButton(
            text = "错误",
            isSelected = selected == "F",
            modifier = Modifier.weight(1f),
            onClick = { onSelected("F") }
        )
    }
}

@Composable
private fun ExamJudgeButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                2.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AnswerCardSheet(
    questions: List<Question>,
    userAnswers: Map<Int, String>,
    currentIndex: Int,
    onIndexClick: (Int) -> Unit,
    onDismiss: () -> Unit,
    onFinish: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("答题卡")
                Text(
                    "${userAnswers.size}/${questions.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        text = {
            // 题号网格
            Column {
                val rows = questions.chunked(5)
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { question ->
                            val index = questions.indexOf(question)
                            val isAnswered = userAnswers.containsKey(index)
                            val isCurrent = index == currentIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        2.dp,
                                        when {
                                            isCurrent -> MaterialTheme.colorScheme.primary
                                            isAnswered -> Color(0xFF4CAF50)
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        },
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        when {
                                            isCurrent -> MaterialTheme.colorScheme.primaryContainer
                                            isAnswered -> Color(0xFFE8F5E9)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onIndexClick(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${index + 1}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isCurrent -> MaterialTheme.colorScheme.primary
                                        isAnswered -> Color(0xFF2E7D32)
                                        else -> MaterialTheme.colorScheme.outline
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // 图例
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LegendItem("当前", MaterialTheme.colorScheme.primary)
                    LegendItem("已答", Color(0xFF4CAF50))
                    LegendItem("未答", MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                }
            }
        },
        confirmButton = {
            Button(onClick = onFinish) { Text("交卷") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
private fun LegendItem(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .border(1.dp, color, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun ExamTypeTag(type: String) {
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
