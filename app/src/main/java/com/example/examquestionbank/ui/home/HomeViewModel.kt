package com.example.examquestionbank.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examquestionbank.domain.model.QuestionBank
import com.example.examquestionbank.domain.repository.QuestionBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val banks: List<QuestionBank> = emptyList(),
    val totalQuestions: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val questionBankRepository: QuestionBankRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            questionBankRepository.getAllFlow().collect { banks ->
                val totalQuestions = banks.sumOf { it.questionCount }
                _uiState.value = _uiState.value.copy(
                    banks = banks,
                    totalQuestions = totalQuestions,
                    isLoading = false
                )
            }
        }
    }

    fun deleteBank(bankId: Long) {
        viewModelScope.launch {
            questionBankRepository.delete(bankId)
        }
    }
}
