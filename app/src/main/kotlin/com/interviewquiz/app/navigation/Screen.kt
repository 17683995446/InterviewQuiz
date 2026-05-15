package com.interviewquiz.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    data object Home : Screen(
        route = "home",
        title = "首页",
        icon = Icons.Filled.Home
    )

    data object Quiz : Screen(
        route = "quiz",
        title = "测验"
    ) {
        const val routeWithArgs = "quiz?category={category}&difficulty={difficulty}&questionCount={questionCount}"

        val arguments = listOf(
            androidx.navigation.NavArgument("category") {
                type = androidx.navigation.NavType.StringType
                nullable = true
                defaultValue = null
            },
            androidx.navigation.NavArgument("difficulty") {
                type = androidx.navigation.NavType.StringType
                nullable = true
                defaultValue = null
            },
            androidx.navigation.NavArgument("questionCount") {
                type = androidx.navigation.NavType.IntType
                defaultValue = 10
            }
        )

        fun createRoute(category: String?, difficulty: String?, questionCount: Int): String {
            return buildString {
                append("quiz")
                append("?category=$category")
                append("&difficulty=$difficulty")
                append("&questionCount=$questionCount")
            }
        }
    }

    data object QuizResult : Screen(
        route = "quiz_result",
        title = "测验结果"
    ) {
        const val routeWithArgs = "quiz_result/{score}/{correctAnswers}/{totalQuestions}"

        val arguments = listOf(
            androidx.navigation.NavArgument("score") {
                type = androidx.navigation.NavType.FloatType
            },
            androidx.navigation.NavArgument("correctAnswers") {
                type = androidx.navigation.NavType.IntType
            },
            androidx.navigation.NavArgument("totalQuestions") {
                type = androidx.navigation.NavType.IntType
            }
        )

        fun createRoute(score: Float, correctAnswers: Int, totalQuestions: Int): String {
            return "quiz_result/$score/$correctAnswers/$totalQuestions"
        }
    }

    data object Categories : Screen(
        route = "categories",
        title = "分类",
        icon = Icons.Filled.Category
    )

    data object Statistics : Screen(
        route = "statistics",
        title = "统计",
        icon = Icons.Filled.BarChart
    )

    data object Settings : Screen(
        route = "settings",
        title = "设置",
        icon = Icons.Filled.Settings
    )

    data object Bookmarks : Screen(
        route = "bookmarks",
        title = "收藏",
        icon = Icons.Filled.Bookmark
    )

    companion object {
        val bottomNavItems = listOf(Home, Categories, Statistics, Settings)
    }
}
