/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.data_as_currency

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.globe.inappupdate.remote_config.RemoteConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.rewards.RewardsDomainManager
import ph.com.globe.errors.rewards.GetConversionQualificationError
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.rewards.*
import ph.com.globe.util.fold
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class DataAsCurrencyViewModel @Inject constructor(
    private val rewardsDomainManager: RewardsDomainManager,
    private val remoteConfigManager: RemoteConfigManager
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _qualifications = MutableLiveData<List<QualificationDetails>>()
    val qualifications: LiveData<List<QualificationDetails>> = _qualifications

    private val _selectedQualification = MutableLiveData<QualificationDetails>()
    val selectedQualification: LiveData<QualificationDetails> = _selectedQualification

    val selectedAmount = MutableLiveData(0)

    private val _conversionResult = MutableLiveData<OneTimeEvent<ConversionResult>>()
    val conversionResult: LiveData<OneTimeEvent<ConversionResult>> = _conversionResult

    private val _rewardPoints = MutableLiveData<Float>()
    val rewardPoints: LiveData<Float> = _rewardPoints

    private val _expireDate = MutableLiveData<String>()
    val expireDate: LiveData<String> = _expireDate

    val randomRewardsFromEachCategory =
        rewardsDomainManager.getRandomFromEachCategory()
            .asLiveData(Dispatchers.Default)

    fun selectQualification(qualification: QualificationDetails) {
        _selectedQualification.value = qualification
    }

    init {
        getConversionQualifications()

        viewModelScope.launch {
            remoteConfigManager.getDataAsCurrencyConfig()?.let { dacConfig ->
                _expireDate.value = dacConfig.expireDate
            }
        }
    }

    private fun getConversionQualifications() {
        viewModelScope.launchWithLoadingOverlay(handler) {
            rewardsDomainManager.getConversionQualification().fold({ qualifications ->
                _qualifications.value = qualifications
                dLog("Fetched qualifications")
            }, {
                dLog("Failed to fetch qualifications")
                when (it) {
                    is GetConversionQualificationError.General -> handler.handleGeneralError(it.error)
                    else -> handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                }
            })
        }
    }

    fun convertData() {
        viewModelScope.launch {
            _selectedQualification.value?.let { selectedQualification ->
                selectedAmount.value?.let { amount ->
                    rewardsDomainManager.addDataConversion(
                        AddDataConversionRequest(
                            referenceId = "1",
                            selectedQualification.number,
                            selectedQualification.rateId,
                            selectedQualification.qualificationId,
                            amount,
                            createdBy = "SuperApp"
                        )
                    ).fold({ conversionId ->
                        delay(CONVERSION_DETAILS_DELAY)
                        getDataConversionDetails(conversionId)
                        dLog("Add data conversion success")
                    }, {
                        _conversionResult.value = OneTimeEvent(ConversionResult.ConversionFailure)
                        dLog("Add data conversion failure")
                    })
                }
            }
        }
    }

    private fun getDataConversionDetails(conversionId: String) {
        viewModelScope.launch {
            rewardsDomainManager.getDataConversionDetails(conversionId)
                .fold({ conversionResult ->
                    when (conversionResult.status) {
                        CONVERSION_STATUS_SUCCESS -> {
                            onSuccessfulDataConversion(conversionDelay = false)
                        }
                        CONVERSION_STATUS_CREATED,
                        CONVERSION_STATUS_QUALIFICATION,
                        CONVERSION_STATUS_DECREMENT,
                        CONVERSION_STATUS_INCREMENT -> {
                            onSuccessfulDataConversion(conversionDelay = true)
                        }
                        CONVERSION_STATUS_FAILED -> {
                            when {
                                conversionResult.isSuccessfulErrorCode() -> {
                                    onSuccessfulDataConversion(conversionDelay = true)
                                }
                                conversionResult.isNotEnoughDataErrorCode() -> {
                                    _conversionResult.value =
                                        OneTimeEvent(ConversionResult.NotEnoughData)
                                }
                                else -> {
                                    _conversionResult.value =
                                        OneTimeEvent(ConversionResult.ConversionFailure)
                                }
                            }
                        }
                        else -> {
                            _conversionResult.value =
                                OneTimeEvent(ConversionResult.ConversionFailure)
                        }
                    }
                    dLog("Get data conversion details success")
                }, {
                    _conversionResult.value = OneTimeEvent(ConversionResult.ConversionFailure)
                    dLog("Get data conversion details failure")
                })
        }
    }

    private fun onSuccessfulDataConversion(conversionDelay: Boolean) {
        _conversionResult.value = OneTimeEvent(ConversionResult.ConversionSuccess(conversionDelay))
        getRewardPoints()
    }

    private fun getRewardPoints() {
        viewModelScope.launch {
            selectedQualification.value?.let { qualification ->
                rewardsDomainManager.getRewardPoints(
                    qualification.number,
                    qualification.segment.toString()
                ).fold({ rewardPoints ->
                    _rewardPoints.value = rewardPoints.total
                    dLog("Get reward points success")
                }, {
                    dLog("Get reward points failure")
                })
            }
        }
    }

    override val logTag = "DataAsCurrencyViewModel"
}

sealed class ConversionResult : Serializable {
    data class ConversionSuccess(val conversionDelay: Boolean) : ConversionResult()
    object ConversionFailure : ConversionResult()
    object NotEnoughData : ConversionResult()
}

const val REWARDS_DETAILS_URL = "https://www.globe.com.ph/help/rewards/gbs-to-points.html"
const val CONVERSION_DETAILS_DELAY = 5000L
