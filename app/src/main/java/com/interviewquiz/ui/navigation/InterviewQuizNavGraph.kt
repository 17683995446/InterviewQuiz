package com.interviewquiz.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.interviewquiz.ui.screens.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InterviewQuizNavGraph(
    navController: NavHostController = androidx.navigation.compose.rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartQuiz = { category, difficulty ->
                    val route = buildString {
                        append(Screen.Quiz.route)
                        if (category != null) {
                            append("/$category")
                        } else {
                            append("/all")
                        }
                        if (difficulty != null) {
                            append("/$difficulty")
                        } else {
                            append("/all")
                        }
                    }
                    navController.navigate(route)
                },
                onViewCategories = { navController.navigate(Screen.Categories.route) },
                onViewStatistics = { navController.navigate(Screen.Statistics.route) },
                onViewBookmarks = { navController.navigate(Screen.Bookmarks.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        composable(
            Screen.quizWithArgsRoute,
            arguments = listOf(
                navArgument(Screen.ARG_CATEGORY) { type = NavType.StringType },
                navArgument(Screen.ARG_DIFFICULTY) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString(Screen.ARG_CATEGORY)
            val difficulty = backStackEntry.arguments?.getString(Screen.ARG_DIFFICULTY)
            QuizScreen(
                onBack = { navController.popBackStack() },
                onFinish = { correct, total ->
                    navController.navigate("${Screen.Result.route}/$correct/$total") {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }
        
        composable(
            Screen.resultWithArgsRoute,
            arguments = listOf(
                navArgument(Screen.ARG_CORRECT_COUNT) { type = NavType.IntType },
                navArgument(Screen.ARG_TOTAL_COUNT) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val correct = backStackEntry.arguments?.getInt(Screen.ARG_CORRECT_COUNT) ?: 0
            val total = backStackEntry.arguments?.getInt(Screen.ARG_TOTAL_COUNT) ?: 0
            ResultScreen(
                correctCount = correct,
                totalCount = total,
                onRestart = { 
                    navController.navigate(Screen.Quiz.route + "/all/all") {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onBackHome = { navController.navigate(Screen.Home.route) { popUpTo(0) } }
            )
        }
        
        composable(Screen.Categories.route) {
            CategoriesScreen(
                onBack = { navController.popBackStack() },
                onCategorySelected = { category ->
                    navController.navigate("${Screen.Quiz.route}/$category/all")
                }
            )
        }
        
        composable(Screen.Statistics.route) {
            StatisticsScreen(onBack = { navController.popBackStack() })
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        
        composable(Screen.Bookmarks.route) {
            BookmarksScreen(onBack = { navController.popBackStack() })
        }
    }
}
