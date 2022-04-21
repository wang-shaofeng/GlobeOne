/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.gocreate.select_account

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.model.account.AccountsLoadingState
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class GoCreateSelectAccountViewModel @Inject constructor(
    private val accountDomainManager: AccountDomainManager,
    private val profileDomainManager: ProfileDomainManager
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _enrolledAccounts = MutableStateFlow<List<EnrolledAccount>>(emptyList())

    private val _selectedNumber = MutableStateFlow("")

    val enrolledAccountItems = _enrolledAccounts
        .combine(accountDomainManager.getPersistentBrands()) { enrolledAccounts, brands ->
            enrolledAccounts.map { account ->
                AccountItem(
                    account.accountAlias,
                    account.primaryMsisdn,
                    brands.find { account.primaryMsisdn == it.primaryMsisdn }?.brand
                )
            }
        }
        .combine(_selectedNumber) { items, selectedNumber ->
            items.map {
                it.copy(selected = it.msisdn == selectedNumber)
            }.sortedByDescending { item ->
                item.brand == GO_CREATE_ELIGIBLE_BRAND
            }
        }
        .asLiveData(Dispatchers.Default)

    init {
        viewModelScope.launch {
            accountDomainManager.getAccountsLoadingState().collect { loadingState ->
                when (loadingState) {
                    is AccountsLoadingState.Loading -> handler.startLoading()
                    else -> handler.endLoading()
                }
            }
        }
    }

    fun initEnrolledAccounts(selectedNumber: String?) {
        viewModelScope.launch {

            // Init with previously selected number
            selectedNumber?.let { _selectedNumber.emit(it) }

            profileDomainManager.getEnrolledAccounts()
                .collect {
                    it.fold({ enrolledAccountsList ->
                        dLog("Fetched enrolled accounts")
                        _enrolledAccounts.emit(enrolledAccountsList)
                    }, {
                        dLog("Failed to fetch enrolled accounts")
                        _enrolledAccounts.emit(emptyList())
                    })
                }
        }
    }

    fun selectMobileNumber(number: String) {
        viewModelScope.launch {
            _selectedNumber.emit(number)
        }
    }

    fun getSelectedMobileNumber(): String = _selectedNumber.value

    override val logTag = "GoCreateSelectAccountViewModel"
}

data class AccountItem(
    val name: String,
    val msisdn: String,
    val brand: AccountBrand?,
    var selected: Boolean = false
)

val GO_CREATE_ELIGIBLE_BRAND = AccountBrand.GhpPrepaid
