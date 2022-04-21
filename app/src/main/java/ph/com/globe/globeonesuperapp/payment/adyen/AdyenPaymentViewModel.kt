/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.adyen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.payment.PaymentDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.model.payment.*
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class AdyenPaymentViewModel @Inject constructor(
    private val paymentDomainManager: PaymentDomainManager,
) : BaseViewModel() {

    private val _paymentResult = MutableLiveData<OneTimeEvent<AdyenPaymentResult>>()
    val paymentResult: LiveData<OneTimeEvent<AdyenPaymentResult>> = _paymentResult

    private var pollingJob: Job? = null

    fun checkAdyenPaymentAndTryPurchase() {
        pollingJob = viewModelScope.launch {
            paymentDomainManager.checkPaymentSuccessful(ThirdPartyPaymentResult.AdyenResult.AdyenResultAuthorised)
                .fold(
                    { result ->
                        dLog("Current payment successful.")
                        _paymentResult.value =
                            OneTimeEvent(
                                AdyenPaymentResult.PurchaseSuccessful(
                                    result.accounts[0].transactions.toTransactionResultList(),
                                    result.accounts[0].refund == null || result.accounts[0].refund?.status == "REFUND_SUCCESSFUL"
                                )
                            )
                    }, {
                        dLog("Current payment unsuccessful.")
                        _paymentResult.value = OneTimeEvent(AdyenPaymentResult.GenericPaymentError)
                    }
                )
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
    }

    override val logTag = "AdyenPaymentViewModel"
}

sealed class AdyenPaymentResult {
    class PurchaseSuccessful(
        val transactionsResult: List<TransactionResult>,
        val refundSuccessful: Boolean = true
    ) : AdyenPaymentResult()

    object PurchaseFailed : AdyenPaymentResult()
    object GenericPaymentError : AdyenPaymentResult()
    object BillPaymentError : AdyenPaymentResult()
}


