/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.voucher_pocket

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.domain_models.isBroadband
import ph.com.globe.model.profile.domain_models.isMobile
import ph.com.globe.model.profile.domain_models.isPrepaid
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class VouchersViewModel @Inject constructor(
    private val profileDomainManager: ProfileDomainManager,
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private var accounts = listOf<VoucherAccountUIModel>()
    private val _enrolledAccounts = MutableLiveData<List<VoucherAccountUIModel>>()
    val enrolledAccounts: LiveData<List<VoucherAccountUIModel>> = _enrolledAccounts

    private val _selectedAccountLiveData = MutableLiveData<SelectedAccountUIModel>()
    val selectedAccountLiveData: LiveData<SelectedAccountUIModel> = _selectedAccountLiveData
    private var selectedAccount: SelectedAccountUIModel = SelectedAccountUIModel("", "", false)
        set(value) {
            field = value
            _selectedAccountLiveData.postValue(field)
        }

    fun setEnrolledAccount(enrolledAccount: EnrolledAccount) {
        if (_selectedAccountLiveData.value == null) {
            with(enrolledAccount) {
                selectedAccount = SelectedAccountUIModel(
                    accountAlias,
                    primaryMsisdn,
                    isMobile() || isBroadband() && isPrepaid()
                )
            }
        }
    }

    fun accountClick(accountNumber: String) {
        accounts = accounts.map {
            it.copy(selected = it.account.primaryMsisdn == accountNumber)
        }
        _enrolledAccounts.value = accounts
    }

    fun selectOtherAccount() {
        accounts.first { it.selected }.account.apply {
            selectedAccount = SelectedAccountUIModel(
                accountAlias,
                primaryMsisdn,
                isMobile() || isBroadband() && isPrepaid()
            )
        }
    }

    fun getEnrolledAccounts() = viewModelScope.launchWithLoadingOverlay(handler) {
        profileDomainManager.getEnrolledAccounts().first {
            it.fold(
                { enrolledAccountsList ->
                    dLog("Fetched enrolled accounts.")
                    accounts = enrolledAccountsList.map {
                        VoucherAccountUIModel(
                            it,
                            it.primaryMsisdn == selectedAccount.msisdn
                        )
                    }
                    _enrolledAccounts.value = accounts
                }, {
                    dLog("Failed to fetch enrolled accounts $it")
                    accounts = emptyList()
                    _enrolledAccounts.value = accounts
                }
            )
            true
        }
    }

    data class VoucherAccountUIModel(
        val account: EnrolledAccount,
        var selected: Boolean
    )

    data class SelectedAccountUIModel(
        val accountName: String,
        val msisdn: String,
        val isContentAvailable: Boolean // the Content tab is available or show coming soon
    )

    override val logTag = "VouchersViewModel"
}
