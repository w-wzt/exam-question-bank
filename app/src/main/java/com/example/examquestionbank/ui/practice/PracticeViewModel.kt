package com.example.examquestionbank.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examquestionbank.domain.model.PracticeProgress
import com.example.examquestionbank.domain.model.Question
import com.example.examquestionbank.domain.model.QuestionFlag
import com.example.examquestionbank.domain.repository.PracticeProgressRepository
import com.example.examquestionbank.domain.repository.QuestionBankRepository
import com.example.examquestionbank.domain.repository.QuestionRepository
import com.example.examquestionbank.domain.repository.WrongQuestionRepository
import com.example.examquestionbank.util.QuestionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class PracticeUiState(
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val userAnswers: Map<Int, String> = emptyMap(),
    val submittedMap: Map<Int, String> = emptyMap(),
    val showResult: Map<Int, Boolean> = emptyMap(),
    val hasProgress: Boolean = false,
    val bankName: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val questionBankRepository: QuestionBankRepository,
    private val practiceProgressRepository: PracticeProgressRepository,
    private val wrongQuestionRepository: WrongQuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    private var currentBankId: Long = 0
    private var sessionId: String = ""
    private var saveJob: Job? = null

    fun loadQuestions(bankId: Long, category: String? = null, type: String? = null) {
        currentBankId = bankId
        sessionId = "practice_${bankId}_${UUID.randomUUID().toString().take(8)}"
        viewModelScope.launch {
            val bank = questionBankRepository.getById(bankId)
            val questions = when {
                category != null && type != null -> questionRepository.getByBankIdTypeAndCategory(bankId, type, category)
                category != null -> questionRepository.getByBankIdAndCategory(bankId, category)
                type != null -> questionRepository.getByBankIdAndType(bankId, type)
                else -> questionRepository.getByBankId(bankId)
            }
            _uiState.value = _uiState.value.copy(
                questions = questions,
                bankName = bank?.name ?: "",
                isLoading = false
            )
        }
    }

    fun checkAndLoadProgress(bankId: Long) {
        currentBankId = bankId
        sessionId = "practice_${bankId}"
        viewModelScope.launch {
            val has = practiceProgressRepository.hasProgress(sessionId)
            _uiState.value = _uiState.value.copy(hasProgress = has)
        }
    }

    fun restoreProgress() {
        viewModelScope.launch {
            val progress = practiceProgressRepository.getBySessionId(sessionId) ?: return@launch
            val questionIds = progress.questionIds
            val questions = questionIds.mapNotNull { questionRepository.getById(it) }
            _uiState.value = _uiState.value.copy(
                questions = questions,
                currentIndex = progress.currentIndex,
                userAnswers = progress.userAnswers.mapKeys { it.key.toInt() },
                submittedMap = progress.submittedMap.mapKeys { it.key.toInt() },
                showResult = buildShowResultMap(questions, progress.submittedMap.mapKeys { it.key.toInt() }),
                hasProgress = false,
                isLoading = false
            )
        }
    }

    fun dismissProgress() {
        _uiState.value = _uiState.value.copy(hasProgress = false)
        loadQuestions(currentBankId)
    }

    fun submitAnswer(questionIndex: Int, answer: String) {
        val questions = _uiState.value.questions
        if (questionIndex !in questions.indices) return

        val question = questions[questionIndex]
        val correctAnswer = QuestionUtils.parseAnswer(question.answer, question.type)
        val userAnswer = QuestionUtils.parseAnswer(answer, question.type)
        val isCorrect = QuestionUtils.checkAnswer(userAnswer, correctAnswer, question.type)

        _uiState.value = _uiState.value.copy(
            userAnswers = _uiState.value.userAnswers + (questionIndex to answer),
            submittedMap = _uiState.value.submittedMap + (questionIndex to answer),
            showResult = _uiState.value.showResult + (questionIndex to isCorrect)
        )

        // 答错添加错题标记
        if (!isCorrect) {
            markWrong(question.id)
        }

        debounceSaveProgress()
    }

    fun updateUserAnswer(questionIndex: Int, answer: String) {
        _uiState.value = _uiState.value.copy(
            userAnswers = _uiState.value.userAnswers + (questionIndex to answer)
        )
    }

    fun goToNext() {
        val next = _uiState.value.currentIndex + 1
        if (next < _uiState.value.questions.size) {
            _uiState.value = _uiState.value.copy(currentIndex = next)
            debounceSaveProgress()
        }
    }

    fun goToPrev() {
        val prev = _uiState.value.currentIndex - 1
        if (prev >= 0) {
            _uiState.value = _uiState.value.copy(currentIndex = prev)
            debounceSaveProgress()
        }
    }

    fun goToIndex(index: Int) {
        if (index in _uiState.value.questions.indices) {
            _uiState.value = _uiState.value.copy(currentIndex = index)
            debounceSaveProgress()
        }
    }

    fun saveProgress() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.questions.isEmpty()) return@launch
            val progress = PracticeProgress(
                sessionId = sessionId,
                bankId = currentBankId,
                bankName = state.bankName,
                currentIndex = state.currentIndex,
                questionIds = state.questions.map { it.id },
                userAnswers = state.userAnswers.mapKeys { it.key.toString() },
                submittedMap = state.submittedMap.mapKeys { it.key.toString() },
                savedAt = currentTimeStr()
            )
            practiceProgressRepository.save(progress)
        }
    }

    fun exitPractice() {
        saveJob?.cancel()
        saveProgress()
    }

    private fun debounceSaveProgress() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(300)
            saveProgress()
        }
    }

    private suspend fun markWrong(questionId: Long) {
        val existing = wrongQuestionRepository.getFlagByQuestionAndFlagType(questionId, "wrong")
        val now = currentTimeStr()
        if (existing != null) {
            val newMastery = when (existing.masteryLevel) {
                1 -> 0
                2 -> 1
                else -> 0
            }
            wrongQuestionRepository.updateMastery(questionId, "wrong", newMastery, existing.note, now)
        } else {
            wrongQuestionRepository.insertOrUpdateFlag(
                QuestionFlag(
                    questionId = questionId,
                    flagType = "wrong",
                    masteryLevel = 0,
                    note = "",
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    private fun buildShowResultMap(questions: List<Question>, submitted: Map<Int, String>): Map<Int, Boolean> {
        val result = mutableMapOf<Int, Boolean>()
        submitted.forEach { (idx, answer) ->
            if (idx in questions.indices) {
                val q = questions[idx]
                val correct = QuestionUtils.parseAnswer(q.answer, q.type)
                val user = QuestionUtils.parseAnswer(answer, q.type)
                result[idx] = QuestionUtils.checkAnswer(user, correct, q.type)
            }
        }
        return result
    }

    private fun currentTimeStr(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
}
