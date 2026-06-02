package com.example.examquestionbank.ui.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examquestionbank.domain.model.ExamSession
import com.example.examquestionbank.domain.model.Question
import com.example.examquestionbank.domain.repository.ExamRepository
import com.example.examquestionbank.domain.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExamResultUiState(
    val session: ExamSession? = null,
    val questions: List<Question> = emptyList(),
    val userAnswers: Map<Int, String> = emptyMap(),
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val unansweredCount: Int = 0,
    val accuracy: Float = 0f,
    val grade: String = "",
    val durationText: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class ExamResultViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamResultUiState())
    val uiState: StateFlow<ExamResultUiState> = _uiState.asStateFlow()

    fun loadResult(sessionId: String) {
        viewModelScope.launch {
            try {
                val session = examRepository.getSessionById(sessionId) ?: run {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }
                val records = examRepository.getAnswerRecordsBySession(sessionId)

                // 加载题目
                val questionIds = records.map { it.questionId }.distinct()
                val questions = questionIds.mapNotNull { questionRepository.getById(it) }

                // 构建用户答案映射
                val userAnswers = mutableMapOf<Int, String>()
                records.forEach { record ->
                    val index = questions.indexOfFirst { it.id == record.questionId }
                    if (index >= 0) {
                        userAnswers[index] = record.userAnswer
                    }
                }

                val correctCount = records.count { it.isCorrect == true }
                val wrongCount = records.count { it.isCorrect == false }
                val unansweredCount = records.count { it.isCorrect == null }
                val totalCount = session.totalCount
                val accuracy = if (totalCount > 0) correctCount.toFloat() / totalCount * 100 else 0f
                val grade = when {
                    accuracy >= 90 -> "优秀"
                    accuracy >= 80 -> "良好"
                    accuracy >= 60 -> "及格"
                    else -> "不及格"
                }
                val durationText = calculateDuration(session.startedAt, session.finishedAt)

                _uiState.value = ExamResultUiState(
                    session = session,
                    questions = questions,
                    userAnswers = userAnswers,
                    correctCount = correctCount,
                    wrongCount = wrongCount,
                    unansweredCount = unansweredCount,
                    accuracy = accuracy,
                    grade = grade,
                    durationText = durationText,
                    isLoading = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun calculateDuration(startAt: String, finishedAt: String?): String {
        if (finishedAt == null) return "--"
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val start = sdf.parse(startAt)?.time ?: return "--"
            val end = sdf.parse(finishedAt)?.time ?: return "--"
            val diffMs = end - start
            val minutes = diffMs / 60000
            val seconds = (diffMs % 60000) / 1000
            if (minutes > 0) "${minutes}分${seconds}秒" else "${seconds}秒"
        } catch (_: Exception) {
            "--"
        }
    }
}
