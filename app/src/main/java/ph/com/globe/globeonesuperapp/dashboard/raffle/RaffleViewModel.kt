/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.dashboard.raffle

import android.content.SharedPreferences
import androidx.lifecycle.*
import com.globe.inappupdate.remote_config.RemoteConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.CompositeUxLogger.dLog
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.domain.rewards.RewardsDomainManager
import ph.com.globe.globeonesuperapp.utils.shared_preferences.DASHBOARD_RAFFLE_BUBBLE_SHOWN_KEY
import ph.com.globe.model.profile.response_models.RaffleSet
import ph.com.globe.model.rewards.RewardsCategory
import ph.com.globe.util.fold
import ph.com.globe.util.toDateOrNull
import javax.inject.Inject

@HiltViewModel
class RaffleViewModel @Inject constructor(
    private val profileDomainManager: ProfileDomainManager,
    private val remoteConfigManager: RemoteConfigManager,
    private val sharedPreferences: SharedPreferences,
    private val rewardsDomainManager: RewardsDomainManager
) : ViewModel() {

    private val rewardsFlow = rewardsDomainManager.getAllRewards()

    private var kycComplete = false
    private val _kycCompleteLiveData = MutableLiveData<Boolean>()
    val kycCompleteLiveData: LiveData<Boolean> = _kycCompleteLiveData

    private var raffleInProgress = false
    private val _raffleInProgressFlow = MutableStateFlow(false)
    val raffleInProgressLiveData =
        combine(rewardsFlow, _raffleInProgressFlow) { rewards, raffleInProgress ->
            rewards.any { it.category == RewardsCategory.RAFFLE } to raffleInProgress
        }.asLiveData()

    private var raffleTickets = 0
    private val _ticketCount = MutableLiveData<Int>()
    val ticketCount: LiveData<Int> = _ticketCount

    private val _tickets = MutableLiveData<List<RaffleSet>>()
    val tickets: LiveData<List<RaffleSet>> = _tickets

    private val _bubbleVisibilityState = MutableLiveData(false)
    val bubbleVisibilityState: LiveData<Boolean> = _bubbleVisibilityState

    val raffleBubbleShown: Boolean
        get() = sharedPreferences.getBoolean(DASHBOARD_RAFFLE_BUBBLE_SHOWN_KEY, false)

    init {
        checkKYCComplete()
        checkIfItIsRaffleInProgress()
    }

    private fun checkIfItIsRaffleInProgress() = viewModelScope.launch {
        val currentTime = System.currentTimeMillis()
        raffleInProgress = remoteConfigManager.getRafflesConfig()?.any {
            it.startDate.toDateOrNull()?.time?.let { it < currentTime } == true
                    && it.endDate.toDateOrNull()?.time?.let { currentTime < it } == true
        } == true
        _raffleInProgressFlow.value = raffleInProgress
        getRaffleTickets()
    }

    private fun getRaffleTickets() = viewModelScope.launch {
        if (raffleInProgress) {
            profileDomainManager.getERaffleEntries().fold(
                { entries ->
                    raffleTickets = entries.claimed.total
                    _ticketCount.value = entries.claimed.total
                    _tickets.value = entries.claimed.sets
                    dLog("Raffle entries fetching success")
                }, {
                    dLog("Raffle entries fetching failure")
                }
            )
        }
    }

    private fun checkKYCComplete() = viewModelScope.launch {
        profileDomainManager.checkCompleteKYC().fold({
            kycComplete = it
            _kycCompleteLiveData.value = it
            dLog("CheckCompleteKYC success")
        }, {
            kycComplete = false
            _kycCompleteLiveData.value = false
            dLog("CheckCompleteKYC failure")
        })
    }

    fun showRaffleBubble() = viewModelScope.launch {
        _bubbleVisibilityState.value = true
        delay(BUBBLE_VISIBILITY_DURATION)
        _bubbleVisibilityState.value = false

        // Update bubble state in preferences
        sharedPreferences.edit().putBoolean(DASHBOARD_RAFFLE_BUBBLE_SHOWN_KEY, true).apply()
    }
}

private const val BUBBLE_VISIBILITY_DURATION = 5000L
