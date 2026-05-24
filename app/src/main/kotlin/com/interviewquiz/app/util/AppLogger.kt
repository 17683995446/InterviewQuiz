package com.interviewquiz.app.util

import android.util.Log
import com.interviewquiz.app.BuildConfig
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogger @Inject constructor() {
    
    companion object {
        private const val MAX_MESSAGE_LENGTH = 2000
        private const val MAX_TAG_LENGTH = 23
        
        // 敏感信息关键词
        private val SENSITIVE_PATTERNS = listOf(
            "password", "pwd", "secret", "token", "key",
            "api_key", "apikey", "auth", "credential",
            "ssn", "credit_card", "card_number", "cvv"
        )
        
        // 性能监控开关
        private const val ENABLE_PERFORMANCE_MONITORING = true
        private const val PERFORMANCE_THRESHOLD_MS = 100L
    }
    
    private val isDebug = BuildConfig.DEBUG
    
    private data class PerformanceMetric(
        val operation: String,
        val startTime: Long,
        val tag: String
    )
    
    // 使用 ConcurrentHashMap 保证性能指标的线程安全
    private val performanceMetrics = ConcurrentHashMap<String, PerformanceMetric>()
    
    fun v(tag: String, message: String) {
        if (isDebug) Log.v(sanitizeTag(tag), sanitizeMessage(message))
    }
    
    fun d(tag: String, message: String) {
        if (isDebug) Log.d(sanitizeTag(tag), sanitizeMessage(message))
    }
    
    fun i(tag: String, message: String) {
        if (isDebug) Log.i(sanitizeTag(tag), sanitizeMessage(message))
    }
    
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (isDebug) {
            if (throwable != null) {
                Log.w(sanitizeTag(tag), sanitizeMessage(message), throwable)
            } else {
                Log.w(sanitizeTag(tag), sanitizeMessage(message))
            }
        }
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isDebug) {
            if (throwable != null) {
                Log.e(sanitizeTag(tag), sanitizeMessage(message), throwable)
            } else {
                Log.e(sanitizeTag(tag), sanitizeMessage(message))
            }
        }
    }
    
    fun wtf(tag: String, message: String, throwable: Throwable? = null) {
        if (isDebug) {
            if (throwable != null) {
                Log.wtf(sanitizeTag(tag), sanitizeMessage(message), throwable)
            } else {
                Log.wtf(sanitizeTag(tag), sanitizeMessage(message))
            }
        }
    }
    
    fun logErrorWithContext(tag: String, message: String, context: Map<String, Any>? = null) {
        if (isDebug) {
            val contextString = context?.entries?.joinToString(", ") { 
                "${it.key}=${it.value}" 
            } ?: "no context"
            Log.e(sanitizeTag(tag), "[${sanitizeMessage(message)}] Context: $contextString")
        }
    }
    
    fun measurePerformance(operationName: String, tag: String, block: () -> Unit) {
        if (!ENABLE_PERFORMANCE_MONITORING || !isDebug) {
            block()
            return
        }
        
        val startTime = System.currentTimeMillis()
        val metric = PerformanceMetric(operationName, startTime, tag)
        performanceMetrics[operationName] = metric
        
        try {
            block()
        } finally {
            val duration = System.currentTimeMillis() - startTime
            
            if (duration > PERFORMANCE_THRESHOLD_MS) {
                w(tag, "⚠️ Slow operation detected: $operationName took ${duration}ms")
            }
            
            performanceMetrics.remove(operationName)
        }
    }
    
    fun <T> measurePerformance(operationName: String, tag: String, block: () -> T): T {
        if (!ENABLE_PERFORMANCE_MONITORING || !isDebug) {
            return block()
        }
        
        val startTime = System.currentTimeMillis()
        val metric = PerformanceMetric(operationName, startTime, tag)
        performanceMetrics[operationName] = metric
        
        return try {
            block()
        } finally {
            val duration = System.currentTimeMillis() - startTime
            
            if (duration > PERFORMANCE_THRESHOLD_MS) {
                w(tag, "⚠️ Slow operation detected: $operationName took ${duration}ms")
            }
            
            performanceMetrics.remove(operationName)
        }
    }
    
    fun getPerformanceReport(): String {
        val sb = StringBuilder()
        sb.appendLine("Performance Report:")
        performanceMetrics.forEach { (operation, metric) ->
            val duration = System.currentTimeMillis() - metric.startTime
            sb.appendLine("  $operation: ${duration}ms")
        }
        return sb.toString()
    }
    
    private fun sanitizeTag(tag: String): String {
        return if (tag.length > MAX_TAG_LENGTH) {
            tag.take(MAX_TAG_LENGTH - 3) + "..."
        } else {
            tag
        }
    }
    
    private fun sanitizeMessage(message: String): String {
        var sanitized = message
        
        // 检查并警告敏感信息
        SENSITIVE_PATTERNS.forEach { pattern ->
            if (sanitized.lowercase().contains(pattern)) {
                sanitized = sanitized.replace(Regex("(?i)($pattern)[=:]\\s*[^\\s]+"), "$1=***REDACTED***")
                w("Security", "⚠️ Sensitive data pattern detected in log: $pattern")
            }
        }
        
        return if (sanitized.length > MAX_MESSAGE_LENGTH) {
            sanitized.take(MAX_MESSAGE_LENGTH) + "... [TRUNCATED]"
        } else {
            sanitized
        }
    }
    
    private fun sanitizeUserInput(input: String): String {
        return input
            .replace(Regex("[<>]"), "") // 移除潜在的 HTML 标签
            .replace(Regex("[;'\"]"), "") // 移除潜在的 SQL 注入字符
            .trim()
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
