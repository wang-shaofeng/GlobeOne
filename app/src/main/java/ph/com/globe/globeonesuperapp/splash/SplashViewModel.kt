/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.splash

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globe.inappupdate.usecases.InAppUpdateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.domain.connectivity.ConnectivityDomainManager
import ph.com.globe.domain.database.DatabaseDomainManager
import ph.com.globe.globeonesuperapp.build_version.BuildVersionProvider
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.shared_preferences.RATING_APP_VERSION
import ph.com.globe.globeonesuperapp.utils.shared_preferences.RATING_INSTALL_TIME
import ph.com.globe.globeonesuperapp.utils.shared_preferences.RATING_OPEN_TIME
import ph.com.globe.model.app_update.InAppUpdateResult
import ph.com.globe.model.auth.LoginStatus
import ph.com.globe.model.network.NetworkConnectionStatus
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authDomainManager: AuthDomainManager,
    private val buildVersionProvider: BuildVersionProvider,
    private val inAppUpdateUseCase: InAppUpdateUseCase,
    private val sharedPreferences: SharedPreferences,
    private val connectivityDomainManager: ConnectivityDomainManager,
    private val databaseDomainManager: DatabaseDomainManager
) : ViewModel() {

    private val _isLoggedIn: MutableLiveData<LoginStatus> = MutableLiveData()
    val isLoggedIn: LiveData<LoginStatus> = _isLoggedIn

    private val _appUpdateStatusEvent: MutableLiveData<OneTimeEvent<InAppUpdateResult>> =
        MutableLiveData()
    val appUpdateStatusEvent: LiveData<OneTimeEvent<InAppUpdateResult>> = _appUpdateStatusEvent

    internal lateinit var appUpdateStatusValue: InAppUpdateResult

    internal var pendingUpdate: InAppUpdateResult? = null
    internal var checkedForUpdate: Boolean = false
    internal var userSentToPlaystore: Boolean = false

    init {
        viewModelScope.launch {
            databaseDomainManager.clearAllData()
            inAppUpdateUseCase.execute(buildVersionProvider.provideBuildVersionCode()).fold(
                {
                    if (it is InAppUpdateResult.NoUpdate) {
                        initLoginData()
                    } else {
                        _appUpdateStatusEvent.postValue(OneTimeEvent(it))
                        appUpdateStatusValue = it
                    }
                }, {
                    // if the InAppUpdate check fails we will proceed to the app
                    initLoginData()
                }
            )
        }
    }

    internal fun initLoginData() {
        authDomainManager.removeUserDataIfRefreshUserTokenExpired()
        val status = authDomainManager.getLoginStatus()
        _isLoggedIn.postValue(status)
    }

    internal fun noInternet(): Boolean {
        return connectivityDomainManager.getNetworkStatus() is NetworkConnectionStatus.NotConnectedToInternet
    }

    fun setRatingParams(installTime: Long) {
        sharedPreferences.edit()
            .putLong(RATING_INSTALL_TIME, installTime)
            .putInt(RATING_APP_VERSION, buildVersionProvider.provideBuildVersionCode())
            .putLong(RATING_OPEN_TIME, System.currentTimeMillis())
            .apply()
    }
}
