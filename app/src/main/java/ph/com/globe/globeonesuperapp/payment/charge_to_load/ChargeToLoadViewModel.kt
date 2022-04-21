/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.charge_to_load

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.domain.balance.BalanceDomainManager
import ph.com.globe.domain.payment.PaymentDomainManager
import ph.com.globe.errors.balance.CheckBalanceSufficiencyError
import ph.com.globe.errors.payment.CreateServiceOrderError
import ph.com.globe.errors.payment.PurchaseError
import ph.com.globe.globeonesuperapp.payment.PaymentParameters
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.convertToClassicNumberFormat
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.balance.CheckBalanceSufficiencyParams
import ph.com.globe.model.payment.MultiplePurchasePromoResult
import ph.com.globe.model.payment.PurchaseParams
import ph.com.globe.model.payment.PurchaseResult
import ph.com.globe.model.payment.PurchaseType
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class ChargeToLoadViewModel @Inject constructor(
    private val paymentDomainManager: PaymentDomainManager,
    private val accountDomainManager: AccountDomainManager,
    private val balanceDomainManager: BalanceDomainManager,
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _paymentResult = MutableLiveData<OneTimeEvent<ChargeToLoadPaymentResult>>()
    val paymentResult: LiveData<OneTimeEvent<ChargeToLoadPaymentResult>> = _paymentResult

    fun tryPurchaseViaShareFlow(
        sourceNumber: String,
        targetNumber: String,
        purchaseType: PurchaseType,
        otpReferenceId: String
    ) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            paymentDomainManager.purchaseUseCase(
                PurchaseParams(
                    sourceNumber = sourceNumber,
                    targetNumber = targetNumber,
                    purchaseType = purchaseType,
                    chargeToLoad = true,
                    otpReferenceId = otpReferenceId
                )
            ).fold(
                { result ->
                    dLog("OTP sent for share a load/promo.")
                    _paymentResult.value =
                        OneTimeEvent(
                            ChargeToLoadPaymentResult.ShareLoadPromoOTPSent(
                                (result as PurchaseResult.ShareALoadPromoResult).createServiceIdResult.referenceId
                            )
                        )
                },
                { error ->
                    if (error is PurchaseError.ShareLoadPromoOtpSendingError) {
                        dLog("OTP sending failed for share a load/promo.")
                        when (error.error) {
                            is CreateServiceOrderError.General -> handler.handleGeneralError(
                                (error.error as CreateServiceOrderError.General).error
                            )
                            is CreateServiceOrderError.InsufficientFunds -> _paymentResult.value =
                                OneTimeEvent(ChargeToLoadPaymentResult.NotEnoughBalance)
                            else -> Unit
                        }
                    }
                }
            )
        }
    }

    fun tryPurchaseGoCreatePromo(
        sourceNumber: String,
        purchaseType: PurchaseType,
        otpReferenceId: String,
        totalAmount: String
    ) = viewModelScope.launch {
        balanceDomainManager.checkBalanceSufficiency(
            CheckBalanceSufficiencyParams(
                sourceNumber.convertToClassicNumberFormat(),
                totalAmount
            )
        ).fold({ isSufficient ->
            if (isSufficient) {
                tryPurchasePromo(sourceNumber, purchaseType, otpReferenceId)
            } else {
                _paymentResult.value = OneTimeEvent(ChargeToLoadPaymentResult.NotEnoughBalance)
            }
        }, {
            if (it is CheckBalanceSufficiencyError.General)
                handler.handleGeneralError(it.error)
        })
    }

    fun tryPurchasePromo(
        sourceNumber: String,
        purchaseType: PurchaseType,
        otpReferenceId: String
    ) {
        viewModelScope.launch {
            paymentDomainManager.purchaseUseCase(
                PurchaseParams(
                    sourceNumber = sourceNumber,
                    targetNumber = sourceNumber,
                    purchaseType = purchaseType,
                    chargeToLoad = true,
                    otpReferenceId = otpReferenceId
                )
            ).fold(
                { result ->
                    dLog("Current purchase successful.")
                    _paymentResult.value =
                        OneTimeEvent(
                            ChargeToLoadPaymentResult.PurchaseSuccessful(
                                (result as PurchaseResult.GeneralResult).multiplePurchasePromoResult
                            )
                        )
                },
                {
                    dLog("Current purchase unsuccessful.")
                    _paymentResult.value =
                        OneTimeEvent(ChargeToLoadPaymentResult.PurchaseFailed)
                }
            )
        }
    }

    fun checkPrepaidBalanceSufficiency(
        sourceNumber: String,
        totalAmount: String,
        isExclusive: Boolean = false
    ) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            balanceDomainManager.checkBalanceSufficiency(
                CheckBalanceSufficiencyParams(
                    sourceNumber,
                    totalAmount
                )
            ).fold({ isSufficient ->
                if (isSufficient) {

                    if (isExclusive) {
                        _paymentResult.value = OneTimeEvent(ChargeToLoadPaymentResult.SufficientBalanceForExclusive)
                        return@launchWithLoadingOverlay
                    }

                    _paymentResult.value = OneTimeEvent(ChargeToLoadPaymentResult.SufficientBalance)
                } else {
                    _paymentResult.value = OneTimeEvent(ChargeToLoadPaymentResult.NotEnoughBalance)
                }
            }, {
                if (it is CheckBalanceSufficiencyError.General)
                    handler.handleGeneralError(it.error)
            })
        }
    }

    fun purchaseExclusivePromo(paymentParameters: PaymentParameters) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            accountDomainManager.purchaseCampaignPromo(
                paymentParameters.apiProvisioningKeyword ?: "",
                paymentParameters.primaryMsisdn,
                paymentParameters.chargePromoParam ?: "",
                paymentParameters.chargePromoId ?: "",
                paymentParameters.availMode
            ).fold(
                { result ->
                    dLog("Current purchase Exclusive promo successful.")
                    _paymentResult.value =
                        OneTimeEvent(
                            ChargeToLoadPaymentResult.PurchaseSuccessful(
                                (result as PurchaseResult.GeneralResult).multiplePurchasePromoResult
                            )
                        )
                },
                {
                    dLog("Current purchase unsuccessful.")
                    _paymentResult.value =
                        OneTimeEvent(ChargeToLoadPaymentResult.PurchaseFailed)
                }
            )
        }
    }

    override val logTag = "ChargeToLoadViewModel"
}

sealed class ChargeToLoadPaymentResult {
    class PurchaseSuccessful(val result: MultiplePurchasePromoResult) : ChargeToLoadPaymentResult()
    object PurchaseFailed : ChargeToLoadPaymentResult()
    object SufficientBalance : ChargeToLoadPaymentResult()
    object SufficientBalanceForExclusive : ChargeToLoadPaymentResult()
    object NotEnoughBalance : ChargeToLoadPaymentResult()
    class ShareLoadPromoOTPSent(val referenceId: String) : ChargeToLoadPaymentResult()
    object GenericPaymentError : ChargeToLoadPaymentResult()
}
