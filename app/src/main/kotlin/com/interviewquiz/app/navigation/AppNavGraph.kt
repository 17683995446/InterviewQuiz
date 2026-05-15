package com.interviewquiz.app.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.interviewquiz.app.Screen
import com.interviewquiz.feature.bookmarks.presentation.BookmarksScreen
import com.interviewquiz.feature.bookmarks.presentation.BookmarksViewModel
import com.interviewquiz.feature.categories.presentation.CategoriesScreen
import com.interviewquiz.feature.categories.presentation.CategoriesViewModel
import com.interviewquiz.feature.home.presentation.HomeScreen
import com.interviewquiz.feature.home.presentation.HomeViewModel
import com.interviewquiz.feature.quiz.presentation.QuizScreen
import com.interviewquiz.feature.quiz.presentation.QuizViewModel
import com.interviewquiz.feature.quiz.presentation.QuizResultScreen
import com.interviewquiz.feature.settings.presentation.SettingsScreen
import com.interviewquiz.feature.settings.presentation.SettingsViewModel
import com.interviewquiz.feature.statistics.presentation.StatisticsScreen
import com.interviewquiz.feature.statistics.presentation.StatisticsViewModel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            HomeScreen(
                uiState = uiState,
                onQuickActionClick = { action ->
                    when (action.actionType) {
                        com.interviewquiz.feature.home.presentation.QuickActionType.QUICK_QUIZ -> {
                            navController.navigate(Screen.Quiz.createRoute(null, null, 5))
                        }
                        com.interviewquiz.feature.home.presentation.QuickActionType.PRACTICE -> {
                            navController.navigate(Screen.Categories.route)
                        }
                        else -> {
                            navController.navigate(Screen.Quiz.createRoute(null, null, 10))
                        }
                    }
                },
                onCategoryClick = { category ->
                    navController.navigate(Screen.Quiz.createRoute(category, null, 10))
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                },
                onNavigateToBookmarks = {
                    navController.navigate(Screen.Bookmarks.route)
                }
            )
        }

        composable(Screen.Categories.route) {
            val viewModel: CategoriesViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            CategoriesScreen(
                uiState = uiState,
                onCategoryClick = { category ->
                    navController.navigate(Screen.Quiz.createRoute(category.name, null, 10))
                },
                onRefresh = { viewModel.refresh() }
            )
        }

        composable(
            route = Screen.Quiz.routeWithArgs,
            arguments = Screen.Quiz.arguments
        ) { backStackEntry ->
            val viewModel: QuizViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            val category = backStackEntry.arguments?.getString("category")
            val difficulty = backStackEntry.arguments?.getString("difficulty")
            val questionCount = backStackEntry.arguments?.getInt("questionCount") ?: 10

            if (!uiState.isQuizCompleted && uiState.totalQuestions == 0) {
                viewModel.startQuiz(category, difficulty, questionCount)
            }

            QuizScreen(
                uiState = uiState,
                onSelectAnswer = { viewModel.selectAnswer(it) },
                onNextQuestion = { viewModel.nextQuestion() },
                onPreviousQuestion = { viewModel.previousQuestion() },
                onSubmitQuiz = { viewModel.submitQuiz() },
                onToggleBookmark = { viewModel.toggleBookmark() },
                onExitQuiz = {
                    viewModel.exitQuiz()
                    navController.popBackStack()
                }
            )

            LaunchedEffect(uiState.isQuizCompleted) {
                if (uiState.isQuizCompleted) {
                    navController.navigate(
                        Screen.QuizResult.createRoute(
                            uiState.score,
                            uiState.correctAnswers,
                            uiState.totalQuestions
                        )
                    ) {
                        popUpTo(Screen.Quiz.route) { inclusive = true }
                    }
                }
            }
        }

        composable(
            route = Screen.QuizResult.routeWithArgs,
            arguments = Screen.QuizResult.arguments
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getFloat("score") ?: 0f
            val correctAnswers = backStackEntry.arguments?.getInt("correctAnswers") ?: 0
            val totalQuestions = backStackEntry.arguments?.getInt("totalQuestions") ?: 0

            QuizResultScreen(
                score = score,
                correctAnswers = correctAnswers,
                totalQuestions = totalQuestions,
                onRetryQuiz = {
                    navController.navigate(Screen.Quiz.createRoute(null, null, 10)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onGoHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onViewStatistics = {
                    navController.navigate(Screen.Statistics.route)
                }
            )
        }

        composable(Screen.Statistics.route) {
            val viewModel: StatisticsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            StatisticsScreen(
                uiState = uiState,
                onRefresh = { viewModel.refresh() }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            SettingsScreen(
                uiState = uiState,
                onUpdateDarkMode = { viewModel.updateDarkMode(it) },
                onUpdateNotifications = { viewModel.updateNotifications(it) },
                onUpdateSound = { viewModel.updateSound(it) },
                onUpdateVibration = { viewModel.updateVibration(it) },
                onUpdateAutoPlay = { viewModel.updateAutoPlay(it) },
                onUpdateShowExplanation = { viewModel.updateShowExplanation(it) },
                onUpdateQuestionCount = { viewModel.updateQuestionCount(it) },
                onResetStatistics = { viewModel.resetStatistics() },
                onAbout = { viewModel.showAbout() }
            )
        }

        composable(Screen.Bookmarks.route) {
            val viewModel: BookmarksViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            BookmarksScreen(
                uiState = uiState,
                onRemoveBookmark = { viewModel.removeBookmark(it) },
                onStartPractice = { viewModel.startPractice(it) },
                onRefresh = { viewModel.refresh() }
            )
        }
    }
}
