/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.enterotp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.errors.account.GetAccountDetailsError
import ph.com.globe.errors.account.GetPlanDetailsError
import ph.com.globe.globeonesuperapp.utils.AppConstants.PLATINUM
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.account.GetAccountDetailsParams
import ph.com.globe.model.account.GetPlanDetailsParams
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.model.util.convertToAccountStatusString
import ph.com.globe.util.fold
import ph.com.globe.util.nonEmptyOrNull
import javax.inject.Inject

@HiltViewModel
class AddAccountEnterOtpMobileNumberViewModel @Inject constructor(
    private val overlayAndDialogFactories: OverlayAndDialogFactories,
    private val accountDomainManager: AccountDomainManager
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _loadAccountPlanDetailsResult = MutableLiveData<Boolean>()
    val loadAccountPlanDetailsResult: LiveData<Boolean> = _loadAccountPlanDetailsResult

    var isPremiumAccount: Boolean = false
    var accountStatus: String? = null
    var accountName: String? = null
    var accountNumber: String? = null
    var landlineNumber: String? = null

    fun cancelAddingAccount(yesCallback: () -> Unit, noCallback: () -> Unit) {
        handler.handleDialog(
            overlayAndDialogFactories.createAddAccountMobileNumberCancelDialog(
                yesCallback,
                noCallback
            )
        )
    }

    fun loadAccountAndPlanDetails(msisdn: String, segment: AccountSegment, referenceId: String) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            val getAccountDetailsDeferred = viewModelScope.async {
                accountDomainManager.getAccountDetails(
                    GetAccountDetailsParams(msisdn = msisdn, segment = segment, referenceId)
                ).fold({
                    accountStatus = it.statusDescription?.convertToAccountStatusString()
                    accountName = "${it.firstName ?: ""} ${it.lastName ?: ""}"
                    accountNumber = it.accountNumber.nonEmptyOrNull()
                    landlineNumber = it.landlineNumber?.nonEmptyOrNull()
                    true
                }, {
                    if (it is GetAccountDetailsError.General)
                        handler.handleGeneralError(it.error)
                    else
                        handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                    false
                })
            }
            val getPlanDetailsDeferred = viewModelScope.async {
                // if we are enrolling the broadband account we shall skip the GetPlanDetails api call
                // since broadband account can not be premium
                if (segment == AccountSegment.Broadband) return@async true

                val params = GetPlanDetailsParams(msisdn, segment, referenceId)
                when (segment) {
                    AccountSegment.Mobile -> accountDomainManager.getMobilePlanDetails(params)
                        .fold({
                            isPremiumAccount = it.plan.planType == PLATINUM
                            true
                        }, {
                            if (it is GetPlanDetailsError.General)
                                handler.handleGeneralError(it.error)
                            else
                                handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                            false
                        })
                    AccountSegment.Broadband -> accountDomainManager.getBroadbandPlanDetails(params)
                        .fold({
                            isPremiumAccount = it.plan.planType == PLATINUM
                            true
                        }, {
                            if (it is GetPlanDetailsError.General)
                                handler.handleGeneralError(it.error)
                            else
                                handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                            false
                        })
                    else -> false
                }
            }
            val getAccountDetailsResult = getAccountDetailsDeferred.await()
            val getPlanDetailsResult = getPlanDetailsDeferred.await()
            _loadAccountPlanDetailsResult.postValue(getAccountDetailsResult && getPlanDetailsResult)
        }
    }

    override val logTag = "AddAccountEnterOtpMobileNumberViewModel"
}
