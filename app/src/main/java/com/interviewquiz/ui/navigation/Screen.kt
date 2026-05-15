package com.interviewquiz.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Quiz : Screen("quiz")
    object Result : Screen("result")
    object Categories : Screen("categories")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
    object Bookmarks : Screen("bookmarks")
    
    companion object {
        const val ARG_CATEGORY = "category"
        const val ARG_DIFFICULTY = "difficulty"
        const val ARG_CORRECT_COUNT = "correct_count"
        const val ARG_TOTAL_COUNT = "total_count"
        
        val quizWithArgsRoute = 
            "$Quiz/{${ARG_CATEGORY}}/{${ARG_DIFFICULTY}}"
        
        val resultWithArgsRoute = 
            "$Result/{${ARG_CORRECT_COUNT}}/{${ARG_TOTAL_COUNT}}"
    }
}
