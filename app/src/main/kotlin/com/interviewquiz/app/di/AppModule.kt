package com.interviewquiz.app.di

import android.content.Context
import com.interviewquiz.app.core.dispatchers.AppDispatchers
import com.interviewquiz.app.core.dispatchers.DefaultAppDispatchers
import com.interviewquiz.app.data.QuestionRepository
import com.interviewquiz.app.data.QuestionRepositoryImpl
import com.interviewquiz.app.data.QuizProgressRepository
import com.interviewquiz.app.data.QuizProgressRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Core Module - 核心基础设施模块
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    @Provides
    @Singleton
    fun provideAppDispatchers(): AppDispatchers = DefaultAppDispatchers()
}

/**
 * Repository Module - 数据仓库模块
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindQuestionRepository(impl: QuestionRepositoryImpl): QuestionRepository
    
    @Binds
    @Singleton
    abstract fun bindQuizProgressRepository(impl: QuizProgressRepositoryImpl): QuizProgressRepository
}

/**
 * Context Module - 上下文模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ContextModule {
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
}

