/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.gcash

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
class GCashPaymentViewModel @Inject constructor(
    private val paymentDomainManager: PaymentDomainManager) : BaseViewModel() {

    private val _paymentResult = MutableLiveData<OneTimeEvent<GCashPaymentResult>>()
    val paymentResult: LiveData<OneTimeEvent<GCashPaymentResult>> = _paymentResult

    private var pollingJob: Job? = null


    fun checkGCashPaymentAndTryPurchase() {
        pollingJob = viewModelScope.launch {
            paymentDomainManager.checkPaymentSuccessful(ThirdPartyPaymentResult.GCashResult.GCashResultAuthorised)
                .fold(
                    { result ->
                        dLog("Current payment successful.")
                        _paymentResult.value =
                            OneTimeEvent(
                                GCashPaymentResult.PurchaseSuccessful(
                                    result.accounts[0].transactions.toTransactionResultList(),
                                    result.accounts[0].refund == null || result.accounts[0].refund.isSuccessful() ,
                                    result.tokenPaymentId
                                )
                            )
                    }, {
                        dLog("Current payment unsuccessful.")
                        _paymentResult.value = OneTimeEvent(GCashPaymentResult.GenericPaymentError)
                    }
                )
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
    }

    override val logTag = "GCashPaymentViewModel"
}

sealed class GCashPaymentResult {
    class PurchaseSuccessful(
        val transactionsResult: List<TransactionResult>,
        val refundSuccessful: Boolean = true,
        val tokenPaymentId: String
    ) : GCashPaymentResult()

    object GenericPaymentError : GCashPaymentResult()
}
