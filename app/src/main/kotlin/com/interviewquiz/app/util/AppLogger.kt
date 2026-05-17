package com.interviewquiz.app.util

import android.util.Log
import com.interviewquiz.app.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogger @Inject constructor() {
    
    private val isDebug = BuildConfig.DEBUG
    
    fun v(tag: String, message: String) {
        if (isDebug) Log.v(tag, message)
    }
    
    fun d(tag: String, message: String) {
        if (isDebug) Log.d(tag, message)
    }
    
    fun i(tag: String, message: String) {
        if (isDebug) Log.i(tag, message)
    }
    
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (isDebug) {
            if (throwable != null) {
                Log.w(tag, message, throwable)
            } else {
                Log.w(tag, message)
            }
        }
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isDebug) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }
    
    fun wtf(tag: String, message: String, throwable: Throwable? = null) {
        if (isDebug) {
            if (throwable != null) {
                Log.wtf(tag, message, throwable)
            } else {
                Log.wtf(tag, message)
            }
        }
    }
}

object LoggerTags {
    const val APP = "InterviewQuiz"
    const val SECURITY = "Security"
    const val REPOSITORY = "Repository"
    const val VIEWMODEL = "ViewModel"
    const val UI = "UI"
    const val NETWORK = "Network"
    const val DATABASE = "Database"
}
