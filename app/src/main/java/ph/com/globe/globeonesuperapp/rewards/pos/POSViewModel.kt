/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.pos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.domain.rewards.RewardsDomainManager
import ph.com.globe.errors.rewards.GetMerchantDetailsError
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.setOneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.account.GetAccountBrandParams
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.rewards.GetMerchantDetailsResult
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class POSViewModel @Inject constructor(
    private val profileDomainManager: ProfileDomainManager,
    private val accountDomainManager: AccountDomainManager,
    private val rewardsDomainManager: RewardsDomainManager
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _enrolledAccountUiModel =
        MutableStateFlow(emptyList<EnrolledAccountWithBrandAndPointsUiModel>())

    val enrolledAccountUiModel = _enrolledAccountUiModel.asLiveData(Dispatchers.Default)

    var selectedAccount: EnrolledAccountWithBrandAndPointsUiModel? = null

    var chosenAccount: EnrolledAccountWithPoints? = null

    var merchantDetails: GetMerchantDetailsResult? = null

    private val _merchantStatus =
        MutableLiveData<OneTimeEvent<LfResult<GetMerchantDetailsResult, GetMerchantDetailsError>>>()
    val merchantStatus =
        _merchantStatus as LiveData<OneTimeEvent<LfResult<GetMerchantDetailsResult, GetMerchantDetailsError>>>

    private val _errorPoints = MutableLiveData<OneTimeEvent<Boolean>>()
    val errorPoints = _errorPoints as LiveData<OneTimeEvent<Boolean>>

    private val _posSuccessStatus = MutableLiveData<OneTimeEvent<POSSuccessStatus>>()
    val posSuccessStatus = _posSuccessStatus as LiveData<OneTimeEvent<POSSuccessStatus>>

    var totalPoints: Float = 0f

    private val mutex = Mutex()

    var countJob: Job? = null

    fun getEnrolledAccounts() = viewModelScope.launch(Dispatchers.Default) {
        profileDomainManager.getEnrolledAccounts().collect {
            it.successOrNull()?.let { enrolledAccount ->
                processingData {
                    enrolledAccount.map {
                        EnrolledAccountWithBrandAndPointsUiModel(
                            EnrolledAccountWithPoints(it), true, false, false
                        )
                    }
                }
                for (account in enrolledAccount) {
                    viewModelScope.launchWithLoadingOverlay(handler) {
                        accountDomainManager.getAccountBrand(GetAccountBrandParams(account.primaryMsisdn))
                            .successOrNull()?.result?.brand?.let { brand ->
                                processingData {
                                    it.map {
                                        if (it.enrolledAccountWithPoints.enrolledAccount.primaryMsisdn == account.primaryMsisdn)
                                            it.copy(
                                                enrolledAccountWithPoints = it.enrolledAccountWithPoints.copy(
                                                    brand = brand
                                                )
                                            )
                                        else it
                                    }
                                }
                            }
                    }

                    viewModelScope.launchWithLoadingOverlay(handler) {
                        rewardsDomainManager.getRewardPoints(
                            account.primaryMsisdn,
                            account.segment.toString()
                        )
                            .successOrNull()?.let { rewardPoints ->
                                processingData {
                                    it.map {
                                        if (it.enrolledAccountWithPoints.enrolledAccount.primaryMsisdn == account.primaryMsisdn) {
                                            it.copy(
                                                enrolledAccountWithPoints = it.enrolledAccountWithPoints.copy(
                                                    points = rewardPoints.total,
                                                    expirationDate = rewardPoints.expirationDate,
                                                    expiringAmount = if (rewardPoints.expiringAmount.isEmpty()) "0" else rewardPoints.expiringAmount
                                                ),
                                                loadingPoints = false
                                            )
                                        } else it
                                    }
                                }
                            } ?: run {
                            processingData {
                                it.map {
                                    if (it.enrolledAccountWithPoints.enrolledAccount.primaryMsisdn == account.primaryMsisdn) {
                                        it.copy(loadingPoints = false, isError = true)
                                    } else it
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun select(enrolledAccountData: EnrolledAccountWithBrandAndPointsUiModel) {
        viewModelScope.launch(Dispatchers.Default) {
            mutex.withLock {
                selectedAccount = enrolledAccountData
                setData(getData().map {
                    it.copy(isSelected = enrolledAccountData.enrolledAccountWithPoints == it.enrolledAccountWithPoints)
                })
            }
        }
    }

    fun redeemPoints(points: Float) {
        val account = chosenAccount ?: selectedAccount?.enrolledAccountWithPoints
        when {
            points < merchantDetails?.minimumPoints?.toFloat() ?: 0f -> {
                // if user types points less then minimum points
                _errorPoints.value = OneTimeEvent(false)
            }
            points > account?.points ?: 0f -> {
                // if user types points more then account has
                _errorPoints.value = OneTimeEvent(true)
            }
            else -> {
                viewModelScope.launchWithLoadingOverlay(handler) {
                    rewardsDomainManager.redeemPoints(
                        account?.enrolledAccount?.primaryMsisdn ?: "",
                        merchantDetails?.mobileNumber ?: "",
                        points
                    ).fold({
                        dLog("redeem rewards points success")
                        totalPoints = points
                        _posSuccessStatus.value =
                            OneTimeEvent(POSSuccessStatus.POSSuccessful(it.transactionNumber))
                    }, {
                        dLog("redeem rewards points failure")
                        _posSuccessStatus.value = OneTimeEvent(POSSuccessStatus.POSUnsuccessful)
                    })
                }
            }
        }
    }

    private suspend fun processingData(block: (List<EnrolledAccountWithBrandAndPointsUiModel>) -> List<EnrolledAccountWithBrandAndPointsUiModel>) {
        mutex.withLock {
            setData(block(getData()))
        }
    }

    private suspend fun getData() =
        _enrolledAccountUiModel.first().map { it.copy() }

    private suspend fun setData(list: List<EnrolledAccountWithBrandAndPointsUiModel>) {
        _enrolledAccountUiModel.emit(list.map { it.copy() })
    }

    fun chosenAccount(enrolledAccountWithPoints: EnrolledAccountWithPoints?) {
        chosenAccount = enrolledAccountWithPoints
    }

    fun getMerchantUsingUUID(uuid: String) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            stopCounting()

            rewardsDomainManager.getMerchantDetailsUsingUUID(uuid).fold({
                merchantDetails = it
                _merchantStatus.value = OneTimeEvent(LfResult.success(it))
            }, {
                _merchantStatus.value = OneTimeEvent(LfResult.failure(it))
            })
        }
    }

    fun getMerchantUsingMobileNumber(mobileNumber: String) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            rewardsDomainManager.getMerchantDetailsUsingMobileNumber(mobileNumber).fold({
                merchantDetails = it
                _merchantStatus.value = OneTimeEvent(LfResult.success(it))
            }, {
                _merchantStatus.value = OneTimeEvent(LfResult.failure(it))
            })
        }
    }

    fun startCounting() {
        countJob = viewModelScope.launch(Dispatchers.Main) {
            delay(TEN_SECONDS)

            _merchantStatus.setOneTimeEvent(LfResult.failure(GetMerchantDetailsError.TenSeconds))
        }
    }

    fun stopCounting() {
        if (countJob?.isCancelled == false)
            countJob?.cancel()
        countJob = null
    }

    override val logTag: String = "POSViewModel"

    companion object {
        private const val TEN_SECONDS = 10000L
    }
}

data class EnrolledAccountWithBrandAndPointsUiModel(
    val enrolledAccountWithPoints: EnrolledAccountWithPoints,
    val loadingPoints: Boolean,
    val isError: Boolean,
    val isSelected: Boolean
) : Serializable

data class EnrolledAccountWithPoints(
    val enrolledAccount: EnrolledAccount,
    val brand: AccountBrand? = null,
    val points: Float = 0f,
    val expirationDate: String? = null,
    val expiringAmount: String? = null
) : Serializable

sealed class POSSuccessStatus {
    class POSSuccessful(val transactionId: String) : POSSuccessStatus()
    object POSUnsuccessful : POSSuccessStatus()
}
