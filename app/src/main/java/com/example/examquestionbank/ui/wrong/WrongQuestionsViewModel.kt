package com.example.examquestionbank.ui.wrong

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examquestionbank.domain.model.Question
import com.example.examquestionbank.domain.model.QuestionFlag
import com.example.examquestionbank.domain.repository.QuestionRepository
import com.example.examquestionbank.domain.repository.WrongQuestionRepository
import com.example.examquestionbank.util.QuestionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class WrongQuestionItem(
    val flag: QuestionFlag,
    val question: Question?
)

data class WrongQuestionsUiState(
    val items: List<WrongQuestionItem> = emptyList(),
    val filterMastery: Int? = null,  // null=全部, 0=未掌握, 1=基本掌握
    val isLoading: Boolean = true,
    // 重做模式
    val isRedoMode: Boolean = false,
    val redoQuestions: List<Question> = emptyList(),
    val redoIndex: Int = 0,
    val redoUserAnswers: Map<Int, String> = emptyMap(),
    val redoSubmittedMap: Map<Int, String> = emptyMap(),
    val redoShowResult: Map<Int, Boolean> = emptyMap()
)

@HiltViewModel
class WrongQuestionsViewModel @Inject constructor(
    private val wrongQuestionRepository: WrongQuestionRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WrongQuestionsUiState())
    val uiState: StateFlow<WrongQuestionsUiState> = _uiState.asStateFlow()

    private var allFlags: List<QuestionFlag> = emptyList()

    init {
        loadWrongQuestions()
    }

    private fun loadWrongQuestions() {
        viewModelScope.launch {
            wrongQuestionRepository.getWrongQuestionsFlow().collect { flags ->
                allFlags = flags
                applyFilter()
            }
        }
    }

    fun setFilter(mastery: Int?) {
        _uiState.value = _uiState.value.copy(filterMastery = mastery)
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = when (_uiState.value.filterMastery) {
            null -> allFlags.filter { it.masteryLevel < 2 }
            0 -> allFlags.filter { it.masteryLevel == 0 }
            1 -> allFlags.filter { it.masteryLevel == 1 }
            else -> allFlags.filter { it.masteryLevel < 2 }
        }
        viewModelScope.launch {
            val items = filtered.map { flag ->
                val question = questionRepository.getById(flag.questionId)
                WrongQuestionItem(flag = flag, question = question)
            }
            _uiState.value = _uiState.value.copy(
                items = items,
                isLoading = false
            )
        }
    }

    fun startRedo(bankId: Long? = null) {
        val questions = _uiState.value.items
            .mapNotNull { it.question }
            .let { list ->
                if (bankId != null) list.filter { it.bankId == bankId } else list
            }
            .shuffled()

        if (questions.isEmpty()) return

        _uiState.value = _uiState.value.copy(
            isRedoMode = true,
            redoQuestions = questions,
            redoIndex = 0,
            redoUserAnswers = emptyMap(),
            redoSubmittedMap = emptyMap(),
            redoShowResult = emptyMap()
        )
    }

    fun submitRedoAnswer(questionIndex: Int, answer: String) {
        val questions = _uiState.value.redoQuestions
        if (questionIndex !in questions.indices) return

        val question = questions[questionIndex]
        val correctAnswer = QuestionUtils.parseAnswer(question.answer, question.type)
        val userAnswer = QuestionUtils.parseAnswer(answer, question.type)
        val isCorrect = QuestionUtils.checkAnswer(userAnswer, correctAnswer, question.type)

        _uiState.value = _uiState.value.copy(
            redoUserAnswers = _uiState.value.redoUserAnswers + (questionIndex to answer),
            redoSubmittedMap = _uiState.value.redoSubmittedMap + (questionIndex to answer),
            redoShowResult = _uiState.value.redoShowResult + (questionIndex to isCorrect)
        )

        // 更新掌握度
        updateMastery(question.id, isCorrect)
    }

    fun updateRedoUserAnswer(questionIndex: Int, answer: String) {
        _uiState.value = _uiState.value.copy(
            redoUserAnswers = _uiState.value.redoUserAnswers + (questionIndex to answer)
        )
    }

    fun redoGoToNext() {
        val next = _uiState.value.redoIndex + 1
        if (next < _uiState.value.redoQuestions.size) {
            _uiState.value = _uiState.value.copy(redoIndex = next)
        }
    }

    fun redoGoToPrev() {
        val prev = _uiState.value.redoIndex - 1
        if (prev >= 0) {
            _uiState.value = _uiState.value.copy(redoIndex = prev)
        }
    }

    fun exitRedo() {
        _uiState.value = _uiState.value.copy(
            isRedoMode = false,
            redoQuestions = emptyList(),
            redoIndex = 0,
            redoUserAnswers = emptyMap(),
            redoSubmittedMap = emptyMap(),
            redoShowResult = emptyMap()
        )
        applyFilter()
    }

    private fun updateMastery(questionId: Long, isCorrect: Boolean) {
        viewModelScope.launch {
            val existing = wrongQuestionRepository.getFlagByQuestionAndFlagType(questionId, "wrong")
            val now = currentTimeStr()
            if (existing != null) {
                val newMastery = if (isCorrect) {
                    when (existing.masteryLevel) {
                        0 -> 1
                        1 -> 2  // 掌握度2自动从列表隐藏
                        else -> existing.masteryLevel
                    }
                } else {
                    when (existing.masteryLevel) {
                        1 -> 0
                        2 -> 1
                        else -> existing.masteryLevel
                    }
                }
                wrongQuestionRepository.updateMastery(questionId, "wrong", newMastery, existing.note, now)
                // 掌握度2自动移除
                if (newMastery == 2) {
                    wrongQuestionRepository.deleteByQuestionAndFlagType(questionId, "wrong")
                }
            }
        }
    }

    fun deleteWrongQuestion(questionId: Long) {
        viewModelScope.launch {
            wrongQuestionRepository.deleteByQuestionAndFlagType(questionId, "wrong")
        }
    }

    private fun currentTimeStr(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
}
