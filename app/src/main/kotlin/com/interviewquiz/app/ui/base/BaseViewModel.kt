package com.interviewquiz.app.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.interviewquiz.app.util.AppLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

abstract class BaseViewModel : ViewModel() {
    
    private val activeJobs = ConcurrentHashMap<String, Job>()
    
    protected val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        AppLogger.e("BaseViewModel", "Unhandled coroutine exception", exception)
        handleException(exception)
    }
    
    protected abstract fun handleException(exception: Throwable)
    
    protected fun launchSafe(
        block: suspend CoroutineScope.() -> Unit,
        onError: ((Throwable) -> Unit)? = null,
        jobKey: String? = null
    ): Job {
        // 先取消旧 job（如果有）
        jobKey?.let { 
            activeJobs[it]?.cancel()
        }
        
        val job = viewModelScope.launch(exceptionHandler) {
            try {
                block()
            } catch (e: Exception) {
                AppLogger.e("BaseViewModel", "Coroutine exception", e)
                onError?.invoke(e) ?: handleException(e)
            }
        }
        
        job.invokeOnCompletion { throwable ->
            throwable?.let { cause ->
                if (cause !is kotlinx.coroutines.CancellationException) {
                    AppLogger.e("BaseViewModel", "Job completed with exception", cause)
                }
            }
            jobKey?.let { activeJobs.remove(it) }
        }
        
        // 注册新 job
        jobKey?.let { 
            activeJobs[it] = job
        }
        
        return job
    }
    
    protected fun launchWithLoading(
        isLoading: (Boolean) -> Unit,
        block: suspend CoroutineScope.() -> Unit,
        jobKey: String? = null
    ): Job {
        return launchSafe(
            block = {
                isLoading(true)
                try {
                    block()
                } finally {
                    isLoading(false)
                }
            },
            jobKey = jobKey
        )
    }
    
    protected fun cancelJob(key: String) {
        activeJobs[key]?.cancel()
        activeJobs.remove(key)
    }
    
    protected fun cancelAllJobs() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
    }
    
    override fun onCleared() {
        super.onCleared()
        cancelAllJobs()
    }
}
