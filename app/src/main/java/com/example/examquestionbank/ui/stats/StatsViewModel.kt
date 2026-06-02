package com.example.examquestionbank.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examquestionbank.domain.model.ExamSession
import com.example.examquestionbank.domain.model.QuestionBank
import com.example.examquestionbank.domain.repository.ExamRepository
import com.example.examquestionbank.domain.repository.QuestionBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryStat(
    val name: String,
    val accuracy: Float,
    val total: Int
)

data class StatsUiState(
    val totalBanks: Int = 0,
    val totalQuestions: Int = 0,
    val totalAnswered: Int = 0,
    val overallAccuracy: Float = 0f,
    val categoryStats: List<CategoryStat> = emptyList(),
    val recentSessions: List<ExamSession> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val questionBankRepository: QuestionBankRepository,
    private val examRepository: ExamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            combine(
                questionBankRepository.getAllFlow(),
                examRepository.getAllSessionsFlow()
            ) { banks, sessions ->
                val finishedSessions = sessions.filter { it.finishedAt != null }

                // 概览指标
                val totalBanks = banks.size
                val totalQuestions = banks.sumOf { it.questionCount }
                val totalAnswered = finishedSessions.sumOf { it.totalCount }
                val totalCorrect = finishedSessions.sumOf { it.correctCount }
                val overallAccuracy = if (totalAnswered > 0) {
                    totalCorrect.toFloat() / totalAnswered.toFloat() * 100f
                } else 0f

                // 分类正确率：按题库分组
                val categoryStats = buildCategoryStats(banks, finishedSessions)

                // 最近考试记录（取最近20条）
                val recentSessions = finishedSessions.sortedByDescending { it.finishedAt }.take(20)

                StatsUiState(
                    totalBanks = totalBanks,
                    totalQuestions = totalQuestions,
                    totalAnswered = totalAnswered,
                    overallAccuracy = overallAccuracy,
                    categoryStats = categoryStats,
                    recentSessions = recentSessions,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun buildCategoryStats(
        banks: List<QuestionBank>,
        sessions: List<ExamSession>
    ): List<CategoryStat> {
        val bankMap = banks.associateBy { it.id }
        val grouped = sessions.groupBy { it.bankId }

        return banks.map { bank ->
            val bankSessions = grouped[bank.id].orEmpty()
            val total = bankSessions.sumOf { it.totalCount }
            val correct = bankSessions.sumOf { it.correctCount }
            val accuracy = if (total > 0) correct.toFloat() / total.toFloat() * 100f else 0f
            CategoryStat(
                name = bank.name,
                accuracy = accuracy,
                total = total
            )
        }.filter { it.total > 0 }.sortedByDescending { it.total }
    }
}
