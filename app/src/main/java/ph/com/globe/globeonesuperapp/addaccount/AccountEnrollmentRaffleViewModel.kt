/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.globe.inappupdate.remote_config.RemoteConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.globeonesuperapp.addaccount.AccountEnrollmentRaffleViewModel.RaffleResult.NoRaffle
import ph.com.globe.globeonesuperapp.addaccount.AccountEnrollmentRaffleViewModel.RaffleResult.RaffleSuccess
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.util.fold
import ph.com.globe.util.toDateOrNull
import javax.inject.Inject

@HiltViewModel
class AccountEnrollmentRaffleViewModel @Inject constructor(
    private val profileDomainManager: ProfileDomainManager,
    private val remoteConfigManager: RemoteConfigManager,
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _raffleResult = MutableLiveData<OneTimeEvent<RaffleResult>>()
    val raffleResult: LiveData<OneTimeEvent<RaffleResult>> = _raffleResult

    fun getInfo() = viewModelScope.launchWithLoadingOverlay(handler) {
        val currentTime = System.currentTimeMillis()
        val raffleInProgress = remoteConfigManager.getRafflesConfig()?.any {
            it.startDate.toDateOrNull()?.time?.let { it < currentTime } == true
                    && it.endDate.toDateOrNull()?.time?.let { currentTime < it } == true
        } == true

        if (raffleInProgress) {
            profileDomainManager.getERaffleEntries().fold(
                { entries ->
                    if (entries.claimed.total > 0) {
                        profileDomainManager.getUserFirstName().collect {
                            it.fold(
                                { firstName ->
                                    _raffleResult.value = OneTimeEvent(
                                        RaffleSuccess(
                                            firstName ?: "",
                                            entries.claimed.total
                                        )
                                    )
                                }, {
                                    dLog("Profile fetching failure")
                                }
                            )
                        }
                    } else {
                        _raffleResult.value = OneTimeEvent(NoRaffle)
                    }
                    dLog("Raffle entries fetching success")
                }, {
                    dLog("Raffle entries fetching failure")
                    _raffleResult.value = OneTimeEvent(NoRaffle)
                }
            )
        } else {
            _raffleResult.value = OneTimeEvent(NoRaffle)
        }
    }

    sealed class RaffleResult {

        object NoRaffle : RaffleResult()

        data class RaffleSuccess(
            val profileName: String,
            val numOfTickets: Int
        ) : RaffleResult()
    }

    override val logTag = "AccountEnrollmentRaffleViewModel"
}
