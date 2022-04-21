/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.app_data.AppDataDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class AppDataViewModel @Inject constructor(
    private val appDataDomainManager: AppDataDomainManager
) : BaseViewModel() {

    private val _fetchRegisteredUserCompleted = MutableLiveData(false)
    val fetchRegisteredUserCompleted: LiveData<Boolean> = _fetchRegisteredUserCompleted

    private val _lottie = MutableLiveData<LottieComposition>()
    val lottie: LiveData<LottieComposition> = _lottie

    fun preloadLottie(context: Context) = viewModelScope.launch {
        async {
            LottieCompositionFactory.fromRawRes(context, R.raw.username_loading).addListener {
                _lottie.value = it
            }.addFailureListener {
                dLog("Username loading animation failure")
            }
        }
    }

    fun fetchAllInfo() = viewModelScope.launch {
        async {
            appDataDomainManager.fetchRegisteredUser().fold(
                {
                    _fetchRegisteredUserCompleted.value = true
                    dLog("Registered user fetching success")
                }, {
                    _fetchRegisteredUserCompleted.value = true
                    dLog("Registered user fetching failure")
                }
            )
        }
    }

    fun refreshDataAfterTransaction(msisdn: String) = viewModelScope.launch {
        appDataDomainManager.refreshAccountDetailsData(msisdn)
    }

    fun refreshAccountDetailsData(msisdn: String) = viewModelScope.launch {
        appDataDomainManager.refreshAccountDetailsData(msisdn)
    }

    override val logTag = "AppDataViewModel"
}
