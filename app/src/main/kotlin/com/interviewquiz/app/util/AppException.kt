package com.interviewquiz.app.util

sealed class AppException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    class NetworkException(
        message: String = "网络连接失败",
        cause: Throwable? = null
    ) : AppException(message, cause)
    
    class DatabaseException(
        message: String = "数据库操作失败",
        cause: Throwable? = null
    ) : AppException(message, cause)
    
    class ParseException(
        message: String = "数据解析失败",
        cause: Throwable? = null
    ) : AppException(message, cause)
    
    class ValidationException(
        message: String = "数据验证失败",
        cause: Throwable? = null
    ) : AppException(message, cause)
    
    class UnknownException(
        message: String = "未知错误",
        cause: Throwable? = null
    ) : AppException(message, cause)
}

fun Throwable.toAppException(): AppException {
    return when (this) {
        is AppException -> this
        else -> AppException.UnknownException(
            message = this.message ?: "未知错误",
            cause = this
        )
    }
}

fun Throwable.getUserFriendlyMessage(): String {
    return when (this) {
        is AppException.NetworkException -> "网络连接失败，请检查您的网络"
        is AppException.DatabaseException -> "数据保存失败，请重试"
        is AppException.ParseException -> "数据处理失败"
        is AppException.ValidationException -> "输入数据无效"
        else -> "发生了一些问题，请稍后重试"
    }
}
