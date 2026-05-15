package com.interviewquiz.app.di

import android.content.Context
import androidx.room.Room
import com.interviewquiz.core.database.config.DatabaseConfig
import com.interviewquiz.core.database.dao.QuestionDao
import com.interviewquiz.core.database.dao.QuizSessionDao
import com.interviewquiz.core.database.dao.UserStatisticsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabaseConfig(
        @ApplicationContext context: Context
    ): DatabaseConfig {
        return Room.databaseBuilder(
            context,
            DatabaseConfig::class.java,
            DatabaseConfig.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideQuestionDao(database: DatabaseConfig): QuestionDao {
        return database.questionDao()
    }

    @Provides
    @Singleton
    fun provideQuizSessionDao(database: DatabaseConfig): QuizSessionDao {
        return database.quizSessionDao()
    }

    @Provides
    @Singleton
    fun provideUserStatisticsDao(database: DatabaseConfig): UserStatisticsDao {
        return database.userStatisticsDao()
    }
}
