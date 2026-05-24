package com.interviewquiz.app

import android.app.Application
import android.util.Log
import com.interviewquiz.app.security.SecurityManager
import com.interviewquiz.app.security.Severity
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class InterviewQuizApplication : Application() {

    @Inject
    lateinit var securityManager: SecurityManager

    override fun onCreate() {
        super.onCreate()
        initializeSecurity()
        performSecurityChecks()
    }

    private fun initializeSecurity() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Application started in DEBUG mode")
        }
    }

    private fun performSecurityChecks() {
        val issues = securityManager.getSecurityIssues()

        for (issue in issues) {
            when (issue.severity) {
                Severity.HIGH,
                Severity.CRITICAL -> {
                    Log.w(TAG, "Security Issue: ${issue.description}")
                }
                else -> {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Security Notice: ${issue.description}")
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "InterviewQuiz"
    }
}
