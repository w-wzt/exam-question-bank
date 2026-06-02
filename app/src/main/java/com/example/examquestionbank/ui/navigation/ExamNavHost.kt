package com.example.examquestionbank.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.examquestionbank.ui.bank.QuestionListScreen
import com.example.examquestionbank.ui.exam.ExamResultScreen
import com.example.examquestionbank.ui.exam.ExamScreen
import com.example.examquestionbank.ui.home.HomeScreen
import com.example.examquestionbank.ui.import.ImportScreen
import com.example.examquestionbank.ui.practice.PracticeScreen
import com.example.examquestionbank.ui.settings.SettingsScreen
import com.example.examquestionbank.ui.stats.StatsScreen
import com.example.examquestionbank.ui.wrong.WrongQuestionsScreen

@Composable
fun ExamNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home.route,
        modifier = modifier
    ) {
        composable(Route.Home.route) {
            HomeScreen(
                onNavigateToBankDetail = { bankId ->
                    navController.navigate(Route.QuestionBankDetail.createRoute(bankId))
                },
                onNavigateToImport = {
                    navController.navigate(Route.Import.route)
                },
                onNavigateToStats = {
                    navController.navigate(Route.Stats.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Route.Settings.route)
                },
                onNavigateToWrongQuestions = {
                    navController.navigate(Route.WrongQuestions.route)
                }
            )
        }
        composable(Route.QuestionBankList.route) {
            // TODO: 独立题库管理页
        }
        composable(Route.QuestionBankDetail.route) { backStackEntry ->
            val bankId = backStackEntry.arguments?.getString("bankId")?.toLongOrNull() ?: return@composable
            QuestionListScreen(
                bankId = bankId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPractice = { bankId ->
                    navController.navigate(Route.Practice.createRoute(bankId))
                },
                onNavigateToExam = { bankId ->
                    navController.navigate(Route.Exam.createRoute(bankId))
                }
            )
        }
        composable(Route.Practice.route) { backStackEntry ->
            val bankId = backStackEntry.arguments?.getString("bankId")?.toLongOrNull() ?: return@composable
            PracticeScreen(
                bankId = bankId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Route.Exam.route) { backStackEntry ->
            val bankId = backStackEntry.arguments?.getString("bankId")?.toLongOrNull() ?: return@composable
            ExamScreen(
                bankId = bankId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { sessionId ->
                    navController.navigate(Route.ExamResult.createRoute(sessionId))
                }
            )
        }
        composable(Route.ExamResult.route) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
            ExamResultScreen(
                sessionId = sessionId,
                onNavigateHome = {
                    navController.popBackStack(Route.Home.route, inclusive = false)
                }
            )
        }
        composable(Route.WrongQuestions.route) {
            WrongQuestionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Route.WrongQuestionPractice.route) { backStackEntry ->
            val bankId = backStackEntry.arguments?.getString("bankId")?.toLongOrNull() ?: return@composable
            WrongQuestionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Route.Stats.route) {
            StatsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Route.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Route.Import.route) {
            ImportScreen(
                onNavigateBack = { navController.popBackStack() },
                onImportComplete = {
                    navController.popBackStack(Route.Home.route, inclusive = false)
                }
            )
        }
    }
}
