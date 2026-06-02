package com.example.examquestionbank.ui.import

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examquestionbank.domain.model.Question
import com.example.examquestionbank.domain.model.QuestionBank
import com.example.examquestionbank.domain.repository.QuestionBankRepository
import com.example.examquestionbank.domain.repository.QuestionRepository
import com.example.examquestionbank.util.FileParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImportUiState(
    val step: ImportStep = ImportStep.SELECT_FILE,
    val fileName: String = "",
    val parsedQuestions: List<Question> = emptyList(),
    val banks: List<QuestionBank> = emptyList(),
    val selectedBankId: Long? = null,
    val newBankName: String = "",
    val isImporting: Boolean = false,
    val importResult: ImportResult? = null,
    val error: String? = null
)

enum class ImportStep {
    SELECT_FILE,
    PREVIEW,
    SELECT_BANK,
    IMPORTING,
    RESULT
}

data class ImportResult(
    val successCount: Int,
    val failedCount: Int,
    val errors: List<String>
)

@HiltViewModel
class ImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val questionBankRepository: QuestionBankRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    init {
        loadBanks()
    }

    private fun loadBanks() {
        viewModelScope.launch {
            questionBankRepository.getAllFlow().collect { banks ->
                _uiState.value = _uiState.value.copy(banks = banks)
            }
        }
    }

    fun parseFile(uri: Uri) {
        viewModelScope.launch {
            try {
                val fileName = getFileName(uri) ?: "unknown"
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("无法打开文件")
                val questions = FileParser.parseFile(inputStream, fileName)
                inputStream.close()

                if (questions.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        error = "未解析到有效题目，请检查文件格式"
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    step = ImportStep.PREVIEW,
                    fileName = fileName,
                    parsedQuestions = questions,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "解析失败: ${e.message}"
                )
            }
        }
    }

    fun selectBank(bankId: Long) {
        _uiState.value = _uiState.value.copy(selectedBankId = bankId)
    }

    fun setNewBankName(name: String) {
        _uiState.value = _uiState.value.copy(newBankName = name)
    }

    fun goToSelectBank() {
        _uiState.value = _uiState.value.copy(step = ImportStep.SELECT_BANK)
    }

    fun goBackToPreview() {
        _uiState.value = _uiState.value.copy(step = ImportStep.PREVIEW)
    }

    fun startImport() {
        val state = _uiState.value
        val bankId = state.selectedBankId
        val newBankName = state.newBankName.trim()

        if (bankId == null && newBankName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "请选择题库或输入新题库名称")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, step = ImportStep.IMPORTING)

            try {
                val targetBankId = if (bankId != null) {
                    bankId
                } else {
                    val now = java.text.SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date())
                    questionBankRepository.insert(
                        QuestionBank(name = newBankName, createdAt = now, updatedAt = now)
                    )
                }

                val questions = state.parsedQuestions.map { it.copy(bankId = targetBankId) }
                questionRepository.insertAll(questions)

                val totalCount = questionRepository.getCountByBankId(targetBankId)
                val now = java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    java.util.Locale.getDefault()
                ).format(java.util.Date())
                questionBankRepository.updateQuestionCount(targetBankId, totalCount, now)

                _uiState.value = _uiState.value.copy(
                    step = ImportStep.RESULT,
                    isImporting = false,
                    importResult = ImportResult(
                        successCount = questions.size,
                        failedCount = 0,
                        errors = emptyList()
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = "导入失败: ${e.message}"
                )
            }
        }
    }

    fun reset() {
        _uiState.value = ImportUiState(banks = _uiState.value.banks)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) return it.getString(nameIndex)
            }
        }
        return uri.lastPathSegment
    }
}
