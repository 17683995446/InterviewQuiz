
package com.interviewquiz.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.interviewquiz.core.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    val totalScore: Flow&lt;Int&gt; = userPreferencesRepository.totalScore
    val totalQuizzes: Flow&lt;Int&gt; = userPreferencesRepository.totalQuizzes
    val isFirstLaunch: Flow&lt;Boolean&gt; = userPreferencesRepository.isFirstLaunch
    
    fun setFirstLaunchComplete() {
        viewModelScope.launch {
            userPreferencesRepository.setFirstLaunchComplete()
        }
    }
}
