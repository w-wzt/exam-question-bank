package com.example.examquestionbank.ui.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examquestionbank.domain.model.AnswerRecord
import com.example.examquestionbank.domain.model.ExamSession
import com.example.examquestionbank.domain.model.Question
import com.example.examquestionbank.domain.model.QuestionFlag
import com.example.examquestionbank.domain.repository.ExamRepository
import com.example.examquestionbank.domain.repository.QuestionRepository
import com.example.examquestionbank.domain.repository.WrongQuestionRepository
import com.example.examquestionbank.util.CountdownTimer
import com.example.examquestionbank.util.QuestionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class ExamUiState(
    val session: ExamSession? = null,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val userAnswers: Map<Int, String> = emptyMap(),
    val remainingSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val showAnswerCard: Boolean = false,
    val isFinished: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val questionRepository: QuestionRepository,
    private val wrongQuestionRepository: WrongQuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamUiState())
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

    private var countdownTimer: CountdownTimer? = null

    fun startExam(bankId: Long, count: Int, timeLimitSeconds: Int, category: String? = null) {
        viewModelScope.launch {
            val allQuestions = if (category != null) {
                questionRepository.getByBankIdAndCategory(bankId, category)
            } else {
                questionRepository.getByBankId(bankId)
            }
            // 随机抽取指定数量
            val questions = allQuestions.shuffled().take(count)

            val sessionId = UUID.randomUUID().toString()
            val now = currentTimeStr()
            val session = ExamSession(
                id = sessionId,
                mode = "exam",
                bankId = bankId,
                totalCount = questions.size,
                startedAt = now
            )
            examRepository.createSession(session)

            _uiState.value = _uiState.value.copy(
                session = session,
                questions = questions,
                isLoading = false
            )

            // 启动倒计时
            if (timeLimitSeconds > 0) {
                startCountdown(timeLimitSeconds)
            }
        }
    }

    private fun startCountdown(totalSeconds: Int) {
        countdownTimer?.stop()
        countdownTimer = CountdownTimer(totalSeconds.toLong() * 1000, 1000L).also { timer ->
            timer.start()
            viewModelScope.launch {
                timer.remainingMs.collect { ms ->
                    val seconds = (ms / 1000).toInt()
                    _uiState.value = _uiState.value.copy(
                        remainingSeconds = seconds,
                        isTimerRunning = timer.isRunning.value
                    )
                    if (seconds <= 0 && timer.isRunning.value.not()) {
                        // 时间到，自动交卷
                        finishExam()
                    }
                }
            }
        }
    }

    fun submitAnswer(questionIndex: Int, answer: String) {
        val questions = _uiState.value.questions
        if (questionIndex !in questions.indices) return
        _uiState.value = _uiState.value.copy(
            userAnswers = _uiState.value.userAnswers + (questionIndex to answer)
        )
    }

    fun goToNext() {
        val next = _uiState.value.currentIndex + 1
        if (next < _uiState.value.questions.size) {
            _uiState.value = _uiState.value.copy(currentIndex = next)
        }
    }

    fun goToPrev() {
        val prev = _uiState.value.currentIndex - 1
        if (prev >= 0) {
            _uiState.value = _uiState.value.copy(currentIndex = prev)
        }
    }

    fun goToIndex(index: Int) {
        if (index in _uiState.value.questions.indices) {
            _uiState.value = _uiState.value.copy(currentIndex = index)
        }
    }

    fun toggleAnswerCard() {
        _uiState.value = _uiState.value.copy(
            showAnswerCard = !_uiState.value.showAnswerCard
        )
    }

    fun finishExam() {
        val state = _uiState.value
        val session = state.session ?: return
        if (state.isFinished) return

        countdownTimer?.stop()

        val questions = state.questions
        val userAnswers = state.userAnswers

        // 计算正确数
        var correctCount = 0
        val answerRecords = mutableListOf<AnswerRecord>()
        val now = currentTimeStr()

        questions.forEachIndexed { index, question ->
            val userAnswer = userAnswers[index] ?: ""
            val isCorrect = if (userAnswer.isBlank()) null else {
                val correctParsed = QuestionUtils.parseAnswer(question.answer, question.type)
                val userParsed = QuestionUtils.parseAnswer(userAnswer, question.type)
                QuestionUtils.checkAnswer(userParsed, correctParsed, question.type)
            }
            if (isCorrect == true) correctCount++
            answerRecords.add(
                AnswerRecord(
                    questionId = question.id,
                    sessionId = session.id,
                    mode = "exam",
                    userAnswer = userAnswer,
                    isCorrect = isCorrect,
                    answeredAt = now
                )
            )

            // 答错添加错题标记
            if (isCorrect == false) {
                markWrong(question.id)
            }
        }

        // 计算分数（正确率百分比）
        val score = if (questions.isNotEmpty()) {
            (correctCount.toFloat() / questions.size) * 100
        } else 0f

        viewModelScope.launch {
            examRepository.finishSession(session.id, correctCount, score, now)
            examRepository.insertAnswerRecords(answerRecords)
            _uiState.value = _uiState.value.copy(
                isFinished = true,
                session = session.copy(
                    correctCount = correctCount,
                    score = score,
                    finishedAt = now
                )
            )
        }
    }

    private fun markWrong(questionId: Long) {
        viewModelScope.launch {
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
    }

    fun formatTime(): String {
        val seconds = _uiState.value.remainingSeconds
        val min = seconds / 60
        val sec = seconds % 60
        return String.format("%02d:%02d", min, sec)
    }

    override fun onCleared() {
        super.onCleared()
        countdownTimer?.stop()
    }

    private fun currentTimeStr(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
}
