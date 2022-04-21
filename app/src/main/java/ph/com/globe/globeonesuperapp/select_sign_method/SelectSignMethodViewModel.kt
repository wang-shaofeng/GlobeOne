/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.select_sign_method

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.com.globe.domain.database.DatabaseDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.shared_preferences.GET_STARTED_BUBBLE_SHOWN_KEY
import ph.com.globe.globeonesuperapp.utils.shared_preferences.NICKNAME
import javax.inject.Inject

@HiltViewModel
class SelectSignMethodViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val databaseDomainManager: DatabaseDomainManager
) : BaseViewModel() {

    private val _bubbleVisibilityState = MutableLiveData(false)
    val bubbleVisibilityState: LiveData<Boolean> = _bubbleVisibilityState

    private val getStartedBubbleShown: Boolean
        get() = sharedPreferences.getBoolean(GET_STARTED_BUBBLE_SHOWN_KEY, false)

    init {
        if (!getStartedBubbleShown) {
            showQuickLinksBubble()
        }
        clearNecessarySharedPreferencesValues()
        deleteEnrolledAccounts()
    }

    private fun showQuickLinksBubble() {
        viewModelScope.launch {
            delay(BUBBLE_INITIAL_DELAY)
            _bubbleVisibilityState.value = true
            delay(BUBBLE_VISIBILITY_DURATION)
            _bubbleVisibilityState.value = false

            // Update bubble state in preferences
            sharedPreferences.edit().putBoolean(GET_STARTED_BUBBLE_SHOWN_KEY, true).apply()
        }
    }

    private fun clearNecessarySharedPreferencesValues() {
        sharedPreferences.edit().remove(NICKNAME).apply()
    }

    private fun deleteEnrolledAccounts() {
        viewModelScope.launch(Dispatchers.Default) {
            databaseDomainManager.clearAllData()
        }
    }

    override val logTag = "SelectUserViewModel"
}

private const val BUBBLE_INITIAL_DELAY = 500L
private const val BUBBLE_VISIBILITY_DURATION = 3000L
