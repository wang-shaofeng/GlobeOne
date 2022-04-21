/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.allrewards.enrolled_accounts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.withLoadingOverlay
import ph.com.globe.model.account.GetAccountBrandParams
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class EnrolledAccountsViewModel @Inject constructor(
    private val profileDomainManager: ProfileDomainManager,
    private val accountDomainManager: AccountDomainManager
) : BaseViewModel() {

    private val handler = GeneralEventsHandlerProvider.generalEventsHandler

    val navigateUp = MutableLiveData<OneTimeEvent<EnrolledAccount>>()

    private val _accountsUIModels = MutableStateFlow<List<EnrolledAccountUiModel>>(emptyList())
    private val _selectedPrimaryMsisdn = MutableStateFlow("")
    private var _eligibleBrands: Array<AccountBrand>? = null

    val accountsUIModels = _accountsUIModels.combine(_selectedPrimaryMsisdn) { accounts, selectedPrimaryMsisdn ->
        accounts.map { account ->
            account.copy(
                selected = account.enrolledAccount.primaryMsisdn == selectedPrimaryMsisdn,
                availableToSelect = _eligibleBrands?.any { account.brand == it } ?: true
            )
        }.sortedByDescending { it.availableToSelect }
    }.asLiveData(Dispatchers.Default)

    fun initializeEnrolledAccount() {
        viewModelScope.launch {
            profileDomainManager.getEnrolledAccounts()
                .withLoadingOverlay(handler)
                .collect {
                    it.fold({ enrolledAccounts ->
                        _accountsUIModels.emit(enrolledAccounts.map {
                            EnrolledAccountUiModel(
                                enrolledAccount = it
                            )
                        })
                    }, {
                        handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                    })
                }
        }
    }

    fun initializeWithEligibleBrand(brands: Array<AccountBrand>, cachedAccounts: List<EnrolledAccountUiModel>) {

        // Set eligible brand value
        _eligibleBrands = brands

        viewModelScope.launch {
            // If we have cached accounts with loaded brands, we can reuse these models
            if (cachedAccounts.isNotEmpty() && !cachedAccounts.any { it.brand == null }) {
                _accountsUIModels.emit(cachedAccounts)
            } else {
                handler.startLoading()
                delay(150) // Loading overlay requires some time to create
                profileDomainManager.getEnrolledAccounts()
                    .collect {
                        it.fold({ enrolledAccounts ->

                            // Get brand for each enrolled account
                            val results = enrolledAccounts.map { account ->
                                async {
                                    accountDomainManager.getAccountBrand(
                                        GetAccountBrandParams(account.primaryMsisdn)
                                    ).fold({ response ->
                                        dLog("Get account brand success")
                                        LfResult.success(
                                            EnrolledAccountUiModel(
                                                brand = response.result.brand,
                                                enrolledAccount = account
                                            )
                                        )
                                    }, {
                                        dLog("Get account brand failure")
                                        LfResult.failure(
                                            it, EnrolledAccountUiModel(
                                                enrolledAccount = account
                                            )
                                        )
                                    })
                                }
                            }.awaitAll()

                            _accountsUIModels.emit(results.mapNotNull { it.value })
                            handler.endLoading()

                        }, {
                            handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                            handler.endLoading()
                        })
                    }
            }
        }
    }

    fun selectMobileNumber(number: String) {
        viewModelScope.launch { _selectedPrimaryMsisdn.emit(number) }
    }

    fun done() {
        viewModelScope.launch(Dispatchers.Default) {
            val account = accountsUIModels.value?.find { it.selected }?.enrolledAccount

            account.let { navigateUp.postValue(OneTimeEvent(it)) }
        }
    }

    override val logTag: String = "EnrolledAccountsViewModel"
}

data class EnrolledAccountUiModel(
    val selected: Boolean = false,
    val availableToSelect: Boolean = true,
    val brand: AccountBrand? = null,
    val enrolledAccount: EnrolledAccount,
)
