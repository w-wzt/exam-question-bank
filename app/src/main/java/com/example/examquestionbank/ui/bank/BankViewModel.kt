package com.example.examquestionbank.ui.bank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examquestionbank.domain.model.Question
import com.example.examquestionbank.domain.model.QuestionBank
import com.example.examquestionbank.domain.repository.QuestionBankRepository
import com.example.examquestionbank.domain.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BankUiState(
    val bank: QuestionBank? = null,
    val questions: List<Question> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val selectedType: String? = null,
    val searchKeyword: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class BankViewModel @Inject constructor(
    private val questionBankRepository: QuestionBankRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BankUiState())
    val uiState: StateFlow<BankUiState> = _uiState.asStateFlow()

    private var currentBankId: Long = 0

    fun loadBank(bankId: Long) {
        currentBankId = bankId
        viewModelScope.launch {
            val bank = questionBankRepository.getById(bankId)
            _uiState.value = _uiState.value.copy(bank = bank)
        }
        loadQuestions()
        loadCategories()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            questionRepository.getByBankIdFlow(currentBankId).collect { questions ->
                _uiState.value = _uiState.value.copy(
                    questions = filterQuestions(questions),
                    isLoading = false
                )
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val categories = questionRepository.getCategoriesByBankId(currentBankId)
            _uiState.value = _uiState.value.copy(categories = categories)
        }
    }

    fun setCategory(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        refreshFilter()
    }

    fun setType(type: String?) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        refreshFilter()
    }

    fun setSearchKeyword(keyword: String) {
        _uiState.value = _uiState.value.copy(searchKeyword = keyword)
        refreshFilter()
    }

    private fun refreshFilter() {
        viewModelScope.launch {
            val all = questionRepository.getByBankId(currentBankId)
            _uiState.value = _uiState.value.copy(questions = filterQuestions(all))
        }
    }

    private fun filterQuestions(questions: List<Question>): List<Question> {
        var filtered = questions
        _uiState.value.selectedType?.let { type ->
            filtered = filtered.filter { it.type == type }
        }
        _uiState.value.selectedCategory?.let { cat ->
            filtered = filtered.filter { it.category == cat }
        }
        if (_uiState.value.searchKeyword.isNotBlank()) {
            val keyword = _uiState.value.searchKeyword
            filtered = filtered.filter { it.content.contains(keyword, ignoreCase = true) }
        }
        return filtered
    }

    fun deleteQuestion(questionId: Long) {
        viewModelScope.launch {
            questionRepository.deleteById(questionId)
            // 重新统计题库计数
            val newCount = questionRepository.getCountByBankId(currentBankId)
            val now = java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()
            ).format(java.util.Date())
            questionBankRepository.updateQuestionCount(currentBankId, newCount, now)
        }
    }
}
