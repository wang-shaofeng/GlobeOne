/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.payment_processing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.balance.BalanceDomainManager
import ph.com.globe.domain.payment.PaymentDomainManager
import ph.com.globe.errors.payment.PurchaseError
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.model.balance.CheckBalanceSufficiencyParams
import ph.com.globe.model.payment.PurchaseParams
import ph.com.globe.model.payment.PurchaseType
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.util.fold
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class PaymentProcessingViewModel @Inject constructor(
    private val balanceDomainManager: BalanceDomainManager,
    private val paymentDomainManager: PaymentDomainManager
) : BaseViewModel() {

    private val _processingResult = MutableLiveData<OneTimeEvent<ProcessingPaymentResult>>()
    val processingResult: LiveData<OneTimeEvent<ProcessingPaymentResult>> = _processingResult

    private val _processingIterations = MutableLiveData<OneTimeEvent<ProcessingIterations>>()
    val processingIterations: LiveData<OneTimeEvent<ProcessingIterations>> = _processingIterations

    fun checkPrepaidBalanceSufficiency(msisdn: String, amount: String) {
        viewModelScope.launch {
            balanceDomainManager.checkBalanceSufficiency(
                CheckBalanceSufficiencyParams(msisdn, amount)
            ).fold({ isSufficient ->
                if (isSufficient) {
                    _processingResult.value =
                        OneTimeEvent(ProcessingPaymentResult.SufficientBalance)
                } else {
                    _processingResult.value = OneTimeEvent(ProcessingPaymentResult.NotEnoughBalance)
                }
                dLog("Check prepaid balance sufficiency success")
            }, {
                _processingResult.value =
                    OneTimeEvent(ProcessingPaymentResult.GeneralProcessingError)
                dLog("Check prepaid balance sufficiency failure")
            })
        }
    }

    fun provisionContentPromo(params: PurchaseParams) {
        viewModelScope.launch {
            paymentDomainManager.purchaseUseCase(params).fold(
                { result ->
                    dLog("Current purchase successful.")
                    _processingResult.value = OneTimeEvent(
                        ProcessingPaymentResult.ProvisionContentPromoSuccessful
                    )
                }, { provisionError ->
                    _processingResult.value = OneTimeEvent(
                        ProcessingPaymentResult.ProvisionContentPromoFailed(provisionError)
                    )
                    dLog("Current purchase unsuccessful.")
                }
            )
        }
    }

    fun startProcessingTransitions() {
        viewModelScope.launch {

            // first iteration trough waiting screens
            _processingIterations.value =
                OneTimeEvent(ProcessingIterations.InitialProcessingIteration)
            delay(PROCESSING_SEQUENCE_DURATION)
            _processingIterations.value =
                OneTimeEvent(ProcessingIterations.JustAMomentProcessingIteration)
            delay(PROCESSING_SEQUENCE_DURATION)
            _processingIterations.value =
                OneTimeEvent(ProcessingIterations.HangInThereProcessingIteration)
            delay(PROCESSING_SEQUENCE_DURATION)

            // second iteration trough waiting screens
            _processingIterations.value =
                OneTimeEvent(ProcessingIterations.InitialProcessingIteration)
            delay(PROCESSING_SEQUENCE_DURATION)
            _processingIterations.value =
                OneTimeEvent(ProcessingIterations.JustAMomentProcessingIteration)
            delay(PROCESSING_SEQUENCE_DURATION)
            _processingIterations.value =
                OneTimeEvent(ProcessingIterations.HangInThereProcessingIteration)
            delay(PROCESSING_SEQUENCE_DURATION)

            // here we can assume that the polling of the api reached the timeout and can
            // send the user to the final screen
            _processingIterations.value =
                OneTimeEvent(ProcessingIterations.FinalProcessingIteration)
        }
    }

    override val logTag = "PaymentProcessingViewModel"
}

sealed class ProcessingEntryPoint : Serializable {
    object GCashPaymentProcessing : ProcessingEntryPoint()
    object AdyenPaymentProcessing : ProcessingEntryPoint()
    data class ChargeToLoadPurchasePromo(
        val mobileNumber: String,
        val purchaseType: PurchaseType,
        val otpReferenceId: String = "",
        val brand: AccountBrand? = null
    ) : ProcessingEntryPoint()

    object CheckBalanceSufficiency : ProcessingEntryPoint()
    data class ProvisionContentPromoWithOTP(val otpReferenceId: String) : ProcessingEntryPoint()
}

sealed class ProcessingPaymentResult {
    object ProvisionContentPromoSuccessful : ProcessingPaymentResult()
    class ProvisionContentPromoFailed(val error: PurchaseError) :
        ProcessingPaymentResult()

    object SufficientBalance : ProcessingPaymentResult()
    object NotEnoughBalance : ProcessingPaymentResult()
    object GeneralProcessingError : ProcessingPaymentResult()
}

sealed class ProcessingIterations {
    object InitialProcessingIteration : ProcessingIterations()
    object JustAMomentProcessingIteration : ProcessingIterations()
    object HangInThereProcessingIteration : ProcessingIterations()
    object FinalProcessingIteration : ProcessingIterations()
}

// sequence duration defined as a time of single UI info iteration
private const val PROCESSING_SEQUENCE_DURATION = 10000L
