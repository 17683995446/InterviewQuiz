package com.interviewquiz.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun NavHostController.rememberAppNavController(): NavHostController {
    return rememberNavController()
}
