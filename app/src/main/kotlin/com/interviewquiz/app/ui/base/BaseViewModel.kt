package com.interviewquiz.app.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    
    private val jobs = mutableListOf<Job>()
    
    protected val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        handleException(exception)
    }
    
    protected abstract fun handleException(exception: Throwable)
    
    protected fun launchSafe(
        block: suspend CoroutineScope.() -> Unit,
        onError: ((Throwable) -> Unit)? = null
    ): Job {
        val job = viewModelScope.launch(exceptionHandler) {
            block()
        }
        
        job.invokeOnCompletion { throwable ->
            throwable?.let { cause ->
                onError?.invoke(cause)
            }
        }
        
        jobs.add(job)
        return job
    }
    
    protected fun launchWithLoading(
        isLoading: (Boolean) -> Unit,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return launchSafe(
            block = {
                isLoading(true)
                try {
                    block()
                } finally {
                    isLoading(false)
                }
            }
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        jobs.forEach { it.cancel() }
        jobs.clear()
    }
}
