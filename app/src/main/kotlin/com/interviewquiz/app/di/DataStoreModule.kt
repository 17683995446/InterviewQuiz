package com.interviewquiz.app.di

import android.content.Context
import com.interviewquiz.core.datastore.preferences.QuizPreferencesDataStore
import com.interviewquiz.core.datastore.preferences.UserPreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context
    ): UserPreferencesDataStore {
        return UserPreferencesDataStore(context)
    }

    @Provides
    @Singleton
    fun provideQuizPreferencesDataStore(
        @ApplicationContext context: Context
    ): QuizPreferencesDataStore {
        return QuizPreferencesDataStore(context)
    }
}
