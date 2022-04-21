/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.globe.inappupdate.remote_config.RemoteConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ph.com.globe.analytics.events.NO_EMAIL_STORED
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.app_data.AppDataDomainManager
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.domain.rush.RushDomainManager
import ph.com.globe.domain.user_details.UserDetailsDomainManager
import ph.com.globe.errors.auth.LogoutError
import ph.com.globe.errors.profile.GetRegisteredUserError
import ph.com.globe.errors.profile.UpdateUserProfileError
import ph.com.globe.globeonesuperapp.dashboard.EarnedTicketsResult
import ph.com.globe.globeonesuperapp.dashboard.EarnedTicketsResult.EarnedTicketsSuccessfully
import ph.com.globe.globeonesuperapp.dashboard.EarnedTicketsResult.EarnedTicketsUnsuccessfully
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.shared_preferences.GET_STARTED_BUBBLE_SHOWN_KEY
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.profile.domain_models.RegisteredUser
import ph.com.globe.model.profile.response_models.UpdateUserProfileRequestParams
import ph.com.globe.util.fold
import ph.com.globe.util.nonEmptyOrNull
import ph.com.globe.util.onFailure
import ph.com.globe.util.toDateOrNull
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authDomainManager: AuthDomainManager,
    private val profileDomainManager: ProfileDomainManager,
    private val appDataDomainManager: AppDataDomainManager,
    private val rushDomainManager: RushDomainManager,
    private val remoteConfigManager: RemoteConfigManager,
    private val overlayAndDialogFactories: OverlayAndDialogFactories,
    private val sharedPreferences: SharedPreferences,
    private val userDetailsDomainManager: UserDetailsDomainManager
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _nickname = MutableLiveData<String>()
    val nickname: LiveData<String> = _nickname

    private val _showKYCBanner = MutableLiveData<Boolean>()
    val showKYCBanner: LiveData<Boolean> = _showKYCBanner

    var kycComplete = false
    var raffleInProggress = false
    var shouldRepopulateUI = true
    var dataCertified = false

    var updateUserProfileParams = UpdateUserProfileRequestParams()

    private val _registeredUser = MutableLiveData<RegisteredUser>()
    val registeredUser: LiveData<RegisteredUser> = _registeredUser

    private val _earnedTickets = MutableLiveData<OneTimeEvent<EarnedTicketsResult>>()
    val earnedTickets: LiveData<OneTimeEvent<EarnedTicketsResult>> = _earnedTickets

    private val _shouldShowGameVouchers = MutableLiveData<Boolean>()
    val shouldShowGameVouchers: LiveData<Boolean> = _shouldShowGameVouchers

    private val _gameVouchersUrlResult: MutableLiveData<OneTimeEvent<String>> = MutableLiveData()
    val gameVouchersUrlResult: LiveData<OneTimeEvent<String>> = _gameVouchersUrlResult

    var birthdateForApi: String? = ""

    var firstName = ""

    val encryptedUserEmail by lazy {
        userDetailsDomainManager.getEmail().fold({ email ->
            encryptData(email)
        }, {
            NO_EMAIL_STORED
        })
    }

    private val _logoutConfirmed: MutableLiveData<OneTimeEvent<Boolean>> = MutableLiveData()
    val logoutConfirmed: LiveData<OneTimeEvent<Boolean>> = _logoutConfirmed

    init {
        getRegisteredUser()
        checkIfShouldShowKYCBanner()
        shouldShowGameVouchersItem()
    }

    private fun encryptData(data: String) = userDetailsDomainManager.encryptData(data = data)

    private fun checkIfShouldShowKYCBanner() {
        val currentTime = System.currentTimeMillis()
        viewModelScope.launch {
            if (remoteConfigManager.getRafflesConfig()?.any {
                    it.startDate.toDateOrNull()?.time?.let { it < currentTime } == true
                            && it.endDate.toDateOrNull()?.time?.let { currentTime < it } == true
                } == true) {
                raffleInProggress = true
                profileDomainManager.checkCompleteKYC().fold({
                    kycComplete = it
                    _showKYCBanner.value = !it
                    dLog("CheckCompleteKYC success")
                }, {
                    _showKYCBanner.value = false
                    dLog("CheckCompleteKYC failure")
                })
            }
        }
    }

    private fun getRegisteredUser() = viewModelScope.launch {
        profileDomainManager.getRegisteredUser().collect {
            it.fold({
                it?.let { user -> _registeredUser.value = user }
                firstName = it?.firstName ?: ""
                _nickname.value = it?.nickname?.nonEmptyOrNull() ?: "User"
                dLog("Profile fetching success")
            }, {
                dLog("Profile fetching failure")
                if (it is GetRegisteredUserError.General)
                    handler.handleGeneralError(it.error)
            })
        }
    }

    private fun shouldShowGameVouchersItem() = viewModelScope.launch {
        profileDomainManager.getEnrolledAccounts().collect {
            it.fold({ accounts ->
                _shouldShowGameVouchers.value = accounts.isNotEmpty()
            }, {
                _shouldShowGameVouchers.value = false
            })
        }
    }

    fun updateUserProfile() =
        viewModelScope.launchWithLoadingOverlay(handler) {
            profileDomainManager.updateUserProfile(updateUserProfileParams).fold({
                appDataDomainManager.fetchRegisteredUser().fold(
                    {
                        getRegisteredUser()
                        if (dataCertified) {
                            dataCertified = false
                            profileDomainManager.getERaffleEntries().fold(
                                {
                                    if (it.claimed.total > 0)
                                        _earnedTickets.value = OneTimeEvent(
                                            EarnedTicketsSuccessfully(
                                                firstName,
                                                it.claimed.total
                                            )
                                        )
                                    else
                                        _earnedTickets.value =
                                            OneTimeEvent(EarnedTicketsUnsuccessfully)
                                    dLog("Raffle entries fetching success")
                                }, {
                                    _earnedTickets.value = OneTimeEvent(EarnedTicketsUnsuccessfully)
                                    dLog("Raffle entries fetching failure")
                                }
                            )
                        }
                        dLog("Profile fetching success")
                    }, {
                        dLog("Profile fetching failure")
                        if (it is GetRegisteredUserError.General)
                            handler.handleGeneralError(it.error)
                    }
                )
                dLog("Profile updating success")
            }, {
                dLog("Profile updating failure")
                if (it is UpdateUserProfileError.General)
                    handler.handleGeneralError(it.error)
            })
        }

    fun logout() {
        handler.handleDialog(
            overlayAndDialogFactories.createLogoutDialog {
                _logoutConfirmed.value = OneTimeEvent(true)
                viewModelScope.launchWithLoadingOverlay(handler) {

                    // Update bubble state on 'Get started' screen
                    sharedPreferences.edit().putBoolean(GET_STARTED_BUBBLE_SHOWN_KEY, false).apply()
                    // Make logout
                    authDomainManager.logout().onFailure {
                        if (it is LogoutError.General) {
                            handler.handleGeneralError(it.error)
                        }
                    }
                }
            }
        )
    }

    fun showIncompleteKYCConfirmationDialog(yesCallback: () -> Unit, noCallback: () -> Unit) {
        handler.handleDialog(
            overlayAndDialogFactories.createConfirmUncompleteKYCDialog(yesCallback, noCallback)
        )
    }

    fun getGameVouchersUrl() {
        viewModelScope.launch {
            remoteConfigManager.getRushData()?.let { rushData ->
                rushDomainManager.getGameVouchersUrl(rushData)?.let {
                    _gameVouchersUrlResult.postValue(OneTimeEvent(it))
                }
            }
        }
    }

    override val logTag = "ProfileMainViewModel"
}
