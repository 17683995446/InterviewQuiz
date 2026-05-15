
package com.interviewquiz.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.interviewquiz.ui.navigation.InterviewQuizNavGraph
import com.interviewquiz.ui.theme.InterviewQuizTheme
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalAnimationApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            InterviewQuizTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    InterviewQuizNavGraph()
                }
            }
        }
    }
}
