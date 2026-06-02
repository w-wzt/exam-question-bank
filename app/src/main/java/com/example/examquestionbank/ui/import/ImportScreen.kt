package com.example.examquestionbank.ui.import

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.examquestionbank.domain.model.Question
import com.example.examquestionbank.domain.model.QuestionBank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onNavigateBack: () -> Unit,
    onImportComplete: () -> Unit = onNavigateBack,
    viewModel: ImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.parseFile(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入题库") },
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
                .padding(16.dp)
        ) {
            when (uiState.step) {
                ImportStep.SELECT_FILE -> SelectFileStep(
                    onSelectFile = {
                        fileLauncher.launch(
                            arrayOf("text/plain", "text/csv", "text/tab-separated-values", "*/*")
                        )
                    }
                )

                ImportStep.PREVIEW -> PreviewStep(
                    fileName = uiState.fileName,
                    questions = uiState.parsedQuestions,
                    onConfirm = { viewModel.goToSelectBank() },
                    onBack = { viewModel.reset() }
                )

                ImportStep.SELECT_BANK -> SelectBankStep(
                    banks = uiState.banks,
                    selectedBankId = uiState.selectedBankId,
                    newBankName = uiState.newBankName,
                    onSelectBank = { viewModel.selectBank(it) },
                    onNewBankNameChange = { viewModel.setNewBankName(it) },
                    onConfirm = { viewModel.startImport() },
                    onBack = { viewModel.goBackToPreview() }
                )

                ImportStep.IMPORTING -> ImportingStep()

                ImportStep.RESULT -> ResultStep(
                    result = uiState.importResult,
                    onDone = {
                        viewModel.reset()
                        onImportComplete()
                    }
                )
            }

            uiState.error?.let { error ->
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        error,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { viewModel.clearError() }) { Text("关闭") }
            }
        }
    }
}

@Composable
private fun SelectFileStep(onSelectFile: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("选择题库文件", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "支持 txt、csv、tsv 格式",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onSelectFile) { Text("选择文件") }
        Spacer(Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "文件格式说明",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "列顺序：题目内容 | 分类 | 答案 | 选项A | 选项B | 选项C | 选项D",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(4.dp))
                Text("分隔符：Tab 或 逗号", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Text("判断题答案：T(正确) / F(错误)", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Text("首行含"题目/序号"等关键词将跳过", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun PreviewStep(
    fileName: String,
    questions: List<Question>,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val typeCount = questions.groupingBy { it.type }.eachCount()
    val singleCount = typeCount["single"] ?: 0
    val multipleCount = typeCount["multiple"] ?: 0
    val judgeCount = typeCount["judge"] ?: 0

    Column(modifier = Modifier.fillMaxSize()) {
        Text("文件：$fileName", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "共解析到 ${questions.size} 道题目",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("单选 $singleCount", color = MaterialTheme.colorScheme.primary)
            Text("多选 $multipleCount", color = MaterialTheme.colorScheme.tertiary)
            Text("判断 $judgeCount", color = MaterialTheme.colorScheme.secondary)
        }
        Spacer(Modifier.height(16.dp))
        Text("前5题预览：", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(questions.take(5)) { q ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(q.content, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                        Text(
                            "答案：${q.answer}  类型：${q.type}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("重新选择") }
            Button(onClick = onConfirm, modifier = Modifier.weight(1f)) { Text("下一步") }
        }
    }
}

@Composable
private fun SelectBankStep(
    banks: List<QuestionBank>,
    selectedBankId: Long?,
    newBankName: String,
    onSelectBank: (Long) -> Unit,
    onNewBankNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("选择目标题库", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))

        if (banks.isNotEmpty()) {
            Text("导入到已有题库：", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(banks) { bank ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedBankId == bank.id)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(bank.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${bank.questionCount} 题",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            RadioButton(
                                selected = selectedBankId == bank.id,
                                onClick = { onSelectBank(bank.id) }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
        }

        Text("或创建新题库：", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = newBankName,
            onValueChange = { onNewBankNameChange(it) },
            label = { Text("新题库名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("上一步") }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                enabled = selectedBankId != null || newBankName.isNotBlank()
            ) { Text("开始导入") }
        }
    }
}

@Composable
private fun ImportingStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("正在导入...", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ResultStep(result: ImportResult?, onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        result?.let {
            Text(
                "导入完成",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text("成功导入 ${it.successCount} 道题目", style = MaterialTheme.typography.bodyLarge)
            if (it.failedCount > 0) {
                Spacer(Modifier.height(8.dp))
                Text("失败 ${it.failedCount} 道", color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onDone) { Text("返回首页") }
    }
}
