package com.interviewquiz.app.core.result

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext

/**
 * 通用 Result 类型 - 标准化结果处理
 */
sealed interface Result<out T> {
    data class Success<out T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
    data object Loading : Result<Nothing>
}

/**
 * Repository 基类 - 标准化数据操作
 */
abstract class BaseRepository(
    protected val appDispatchers: com.interviewquiz.app.core.dispatchers.AppDispatchers
) {

    protected suspend fun <T> safeIo(block: suspend () -> T): Result<T> {
        return withContext(appDispatchers.io) {
            try {
                Result.Success(block())
            } catch (e: Throwable) {
                Result.Error(e)
            }
        }
    }

    protected fun <T> safeFlow(block: suspend () -> T): Flow<Result<T>> = flow {
        emit(Result.Loading)
        try {
            val data = block()
            emit(Result.Success(data))
        } catch (e: Throwable) {
            emit(Result.Error(e))
        }
    }
}
