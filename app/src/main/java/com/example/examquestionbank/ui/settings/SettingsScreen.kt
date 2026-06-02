package com.example.examquestionbank.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    // 消息提示
    uiState.message?.let { msg ->
        LaunchedEffect(msg) {
            // 短暂展示后清除
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }
        Snackbar(
            modifier = Modifier.padding(16.dp)
        ) { Text(msg) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
                .verticalScroll(rememberScrollState())
        ) {
            // 字体大小
            SettingsSectionTitle("字体大小")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FontSizeOption("小", "small", uiState.fontSize, viewModel::setFontSize, Modifier.weight(1f))
                FontSizeOption("中", "medium", uiState.fontSize, viewModel::setFontSize, Modifier.weight(1f))
                FontSizeOption("大", "large", uiState.fontSize, viewModel::setFontSize, Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            // 主题
            SettingsSectionTitle("主题")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOption("浅色", "light", uiState.theme, viewModel::setTheme, Modifier.weight(1f))
                ThemeOption("深色", "dark", uiState.theme, viewModel::setTheme, Modifier.weight(1f))
                ThemeOption("跟随系统", "system", uiState.theme, viewModel::setTheme, Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            // 数据管理
            SettingsSectionTitle("数据管理")
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedButton(
                    onClick = { /* 备份功能预留 */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("创建备份")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { /* 恢复功能预留 */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("恢复备份")
                }
            }

            Spacer(Modifier.height(16.dp))

            // 关于
            SettingsSectionTitle("关于")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("版本号", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        uiState.version,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 清除数据
            Button(
                onClick = { showClearDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("清除所有答题记录")
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // 清除数据确认对话框
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清除数据") },
            text = { Text("确定清除所有答题记录？该操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDialog = false
                    }
                ) {
                    Text("清除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        title,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun FontSizeOption(
    label: String,
    value: String,
    currentValue: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = currentValue == value,
        onClick = { onSelect(value) },
        label = { Text(label) },
        modifier = modifier
    )
}

@Composable
private fun ThemeOption(
    label: String,
    value: String,
    currentValue: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = currentValue == value,
        onClick = { onSelect(value) },
        label = { Text(label) },
        modifier = modifier
    )
}
