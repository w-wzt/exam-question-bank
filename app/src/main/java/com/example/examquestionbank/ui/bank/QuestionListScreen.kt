package com.example.examquestionbank.ui.bank

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.examquestionbank.domain.model.Question

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionListScreen(
    bankId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToPractice: (Long) -> Unit,
    onNavigateToExam: (Long) -> Unit,
    viewModel: BankViewModel = hiltViewModel()
) {
    LaunchedEffect(bankId) { viewModel.loadBank(bankId) }
    val uiState by viewModel.uiState.collectAsState()
    var expandedQuestionId by remember { mutableStateOf<Long?>(null) }
    var showSearch by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.bank?.name ?: "题目列表") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
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
            // 快捷操作按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onNavigateToPractice(bankId) },
                    modifier = Modifier.weight(1f)
                ) { Text("刷题") }
                OutlinedButton(
                    onClick = { onNavigateToExam(bankId) },
                    modifier = Modifier.weight(1f)
                ) { Text("考试") }
            }

            // 搜索栏
            AnimatedVisibility(visible = showSearch) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        viewModel.setSearchKeyword(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    placeholder = { Text("搜索题目") },
                    singleLine = true
                )
            }

            // 题型筛选
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = uiState.selectedType == null, onClick = { viewModel.setType(null) }, label = { Text("全部") })
                FilterChip(selected = uiState.selectedType == "single", onClick = { viewModel.setType("single") }, label = { Text("单选") })
                FilterChip(selected = uiState.selectedType == "multiple", onClick = { viewModel.setType("multiple") }, label = { Text("多选") })
                FilterChip(selected = uiState.selectedType == "judge", onClick = { viewModel.setType("judge") }, label = { Text("判断") })
            }

            // 题目列表
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "共 ${uiState.questions.size} 题",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    items(uiState.questions, key = { it.id }) { question ->
                        QuestionItem(
                            question = question,
                            isExpanded = expandedQuestionId == question.id,
                            onClick = {
                                expandedQuestionId = if (expandedQuestionId == question.id) null else question.id
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionItem(question: Question, isExpanded: Boolean, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TypeTag(type = question.type)
                Spacer(Modifier.width(8.dp))
                Text(
                    question.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    // 选项
                    question.options.forEach { opt ->
                        Text(
                            "${opt.label}. ${opt.content}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    // 答案
                    Text(
                        "答案：${question.answer}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    // 解析
                    if (question.explanation.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "解析：${question.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
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
