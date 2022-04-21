/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile.payment_methods.gcash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.payment.PaymentDomainManager
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.errors.payment.LinkingGCashError
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.model.payment.LinkingGCashAccountParams
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class LinkGCashAccountProcessingViewModel @Inject constructor(
    private val profileDomainManager: ProfileDomainManager,
    private val paymentDomainManager: PaymentDomainManager,
) : BaseViewModel() {

    private val _manageGCashLinkingResult =
        MutableLiveData<OneTimeEvent<GCashLinkingResult>>()
    val manageGCashLinkingResult: LiveData<OneTimeEvent<GCashLinkingResult>> =
        _manageGCashLinkingResult

    fun linkGCashAccount(accountAlias: String, referenceId: String) {
        viewModelScope.launch {
            paymentDomainManager.linkGCashAccountUseCase(
                LinkingGCashAccountParams(
                    accountAlias = accountAlias,
                    referenceId = referenceId
                )
            )
                .fold(
                    {
                        dLog("Successfully linked a GCash account.")
                        _manageGCashLinkingResult.value =
                            OneTimeEvent(GCashLinkingResult.GCashLinkedSuccessfully)
                    },
                    { error ->
                        dLog("Failed to link a GCash account.")
                        _manageGCashLinkingResult.value =
                            OneTimeEvent(GCashLinkingResult.GCashLinkingFailed(error))
                    }
                )
        }
    }

    fun refreshEnrolledAccounts() =
        viewModelScope.launch {
            profileDomainManager.invalidateEnrolledAccounts()
        }

    override val logTag: String = "LinkGCashAccountProcessingViewModel"
}

sealed class GCashLinkingResult {
    object GCashLinkedSuccessfully : GCashLinkingResult()
    class GCashLinkingFailed(val error: LinkingGCashError) : GCashLinkingResult()
}
