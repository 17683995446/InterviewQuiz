
package com.interviewquiz.di

import android.content.Context
import com.interviewquiz.core.database.InterviewQuizDatabase
import com.interviewquiz.core.database.dao.QuestionDao
import com.interviewquiz.core.database.dao.QuizRecordDao
import com.interviewquiz.core.database.dao.QuizSessionDao
import com.interviewquiz.core.datastore.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InterviewQuizDatabase {
        return InterviewQuizDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideQuestionDao(database: InterviewQuizDatabase): QuestionDao {
        return database.questionDao()
    }

    @Provides
    @Singleton
    fun provideQuizRecordDao(database: InterviewQuizDatabase): QuizRecordDao {
        return database.quizRecordDao()
    }

    @Provides
    @Singleton
    fun provideQuizSessionDao(database: InterviewQuizDatabase): QuizSessionDao {
        return database.quizSessionDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(@ApplicationContext context: Context): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }
}
