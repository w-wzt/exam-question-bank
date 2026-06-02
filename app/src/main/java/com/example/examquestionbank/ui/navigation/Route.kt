package com.example.examquestionbank.ui.navigation

sealed class Route(val route: String) {
    data object Home : Route("home")
    data object QuestionBankList : Route("question_bank_list")
    data object QuestionBankDetail : Route("question_bank_detail/{bankId}") {
        fun createRoute(bankId: Long): String = "question_bank_detail/$bankId"
    }
    data object Practice : Route("practice/{bankId}") {
        fun createRoute(bankId: Long): String = "practice/$bankId"
    }
    data object Exam : Route("exam/{bankId}") {
        fun createRoute(bankId: Long): String = "exam/$bankId"
    }
    data object ExamResult : Route("exam_result/{sessionId}") {
        fun createRoute(sessionId: String): String = "exam_result/$sessionId"
    }
    data object WrongQuestions : Route("wrong_questions")
    data object WrongQuestionPractice : Route("wrong_question_practice/{bankId}") {
        fun createRoute(bankId: Long): String = "wrong_question_practice/$bankId"
    }
    data object Stats : Route("stats")
    data object Settings : Route("settings")
    data object Import : Route("import")
}
