/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.errors.account.GetAccountStatusError
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.account.GetAccountStatusParams
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class AddAccountBroadbandNumberViewModel @Inject constructor(
    private val overlayAndDialogFactories: OverlayAndDialogFactories,
    private val accountDomainManager: AccountDomainManager
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _rawBrand = MutableLiveData<AccountBrand>()
    val rawBrand: LiveData<AccountBrand> = _rawBrand

    private val _checkBrandResult = MutableLiveData<OneTimeEvent<CheckBrandResult>>()
    val checkBrandResult: LiveData<OneTimeEvent<CheckBrandResult>> = _checkBrandResult

    var addBroadbandManually = false
    private val _addManually = MutableLiveData<OneTimeEvent<Boolean>>()
    val addManually: LiveData<OneTimeEvent<Boolean>> = _addManually

    fun updateAddBroadbandManuallyValue(value: Boolean) {
        addBroadbandManually = value
        _addManually.value = OneTimeEvent(addBroadbandManually)
    }

    fun skipAddingAccount(yesCallback: () -> Unit, noCallback: () -> Unit) {
        handler.handleDialog(
            overlayAndDialogFactories.createAddAccountMobileNumberSkipDialog(
                yesCallback,
                noCallback
            )
        )
    }

    fun checkBrand(msisdn: String, segment: AccountSegment) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            accountDomainManager.getAccountStatus(GetAccountStatusParams(msisdn, segment))
                .fold({ response ->
                    _rawBrand.value = response.brand
                    _checkBrandResult.value = OneTimeEvent(
                        CheckBrandResult.SuccessfulBrandCheck(
                            brandType = response.brandType,
                            brand = response.brand,
                            accountNumber = response.accountNumber,
                            alternativeMobileNumber = response.alternativeMobileNumber,
                            emailAddress = response.email
                        )
                    )
                    dLog("Add account OTP sent, brand: ${response.brand}")
                }, {
                    dLog("Failed to sent otp $it")
                    when (it) {
                        is GetAccountStatusError.InvalidAccount -> {
                            _checkBrandResult.value =
                                OneTimeEvent(CheckBrandResult.NotGlobeNumber)
                        }
                        is GetAccountStatusError.InactiveAccount -> {
                            _checkBrandResult.value =
                                OneTimeEvent(CheckBrandResult.InactiveAccount)
                        }
                        is GetAccountStatusError.NoLongerInSystemAccount ->{
                            _checkBrandResult.value =
                                OneTimeEvent(CheckBrandResult.NoLongerInSystemAccount)
                        }
                        is GetAccountStatusError.General -> handler.handleGeneralError(it.error)
                        else -> handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                    }
                })
        }
    }

    override val logTag = "AddAccountBroadbandNumberViewModel"
}

sealed class CheckBrandResult {
    object NotAMobileNumber : CheckBrandResult()

    object NotABroadbandNumber : CheckBrandResult()

    object NotGlobeNumber : CheckBrandResult()

    object InactiveAccount : CheckBrandResult()

    object NoLongerInSystemAccount: CheckBrandResult()

    class SuccessfulBrandCheck(
        val brandType: AccountBrandType,
        val brand: AccountBrand,
        val accountNumber: String?,
        val alternativeMobileNumber: String? = null,
        val emailAddress: String? = null
    ) : CheckBrandResult()
}
