package com.interviewquiz

import android.app.Application
import com.interviewquiz.core.database.repository.QuestionRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class InterviewQuizApplication : Application() {

    @Inject
    lateinit var questionRepository: QuestionRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        
        // Initialize database with questions
        applicationScope.launch {
            try {
                questionRepository.initializeDatabaseIfNeeded()
            } catch (e: Exception) {
                // Handle initialization error gracefully
                e.printStackTrace()
            }
        }
    }
}
