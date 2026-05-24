package com.interviewquiz.app.core.usecase

import com.interviewquiz.app.core.dispatchers.AppDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/**
 * 基础 UseCase - 无参数，返回 Flow
 */
abstract class FlowUseCase<out Output>(
    private val appDispatchers: AppDispatchers
) {
    abstract operator fun invoke(): Flow<Output>

    protected fun <T> Flow<T>.flowOnDefault(): Flow<T> = flowOn(appDispatchers.default)
    protected fun <T> Flow<T>.flowOnIo(): Flow<T> = flowOn(appDispatchers.io)
}

/**
 * 基础 UseCase - 带参数，返回 Flow
 */
abstract class FlowWithParamsUseCase<in Params, out Output>(
    private val appDispatchers: AppDispatchers
) {
    abstract operator fun invoke(params: Params): Flow<Output>

    protected fun <T> Flow<T>.flowOnDefault(): Flow<T> = flowOn(appDispatchers.default)
    protected fun <T> Flow<T>.flowOnIo(): Flow<T> = flowOn(appDispatchers.io)
}

/**
 * 基础 UseCase - 无参数，返回挂起函数
 */
abstract class SuspendUseCase<out Output>(
    private val appDispatchers: AppDispatchers
) {
    abstract suspend operator fun invoke(): Output

    protected val defaultDispatcher get() = appDispatchers.default
    protected val ioDispatcher get() = appDispatchers.io
    protected val mainDispatcher get() = appDispatchers.main
}

/**
 * 基础 UseCase - 带参数，返回挂起函数
 */
abstract class SuspendWithParamsUseCase<in Params, out Output>(
    private val appDispatchers: AppDispatchers
) {
    abstract suspend operator fun invoke(params: Params): Output

    protected val defaultDispatcher get() = appDispatchers.default
    protected val ioDispatcher get() = appDispatchers.io
    protected val mainDispatcher get() = appDispatchers.main
}
