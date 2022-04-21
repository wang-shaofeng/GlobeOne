/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.payment_loading_session

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonArray
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.balance.BalanceDomainManager
import ph.com.globe.domain.payment.PaymentDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.model.balance.CheckAmaxWalletBalanceSufficiencyParams
import ph.com.globe.model.payment.CreatePaymentSessionParams
import ph.com.globe.model.payment.CreatePaymentSessionResult
import ph.com.globe.model.payment.PurchaseType
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class PaymentLoadingSessionViewModel @Inject constructor(
    private val paymentDomainManager: PaymentDomainManager,
    private val balanceDomainManager: BalanceDomainManager
) : BaseViewModel() {

    private val _checkoutUrl = MutableLiveData<String>()
    val checkoutUrl: LiveData<String> = _checkoutUrl

    private val _paymentResultGcash = MutableLiveData<OneTimeEvent<GCashPaymentResult>>()
    val paymentResultGcash: LiveData<OneTimeEvent<GCashPaymentResult>> = _paymentResultGcash

    private val _paymentMethodsJSON = MutableLiveData<JSONObject>()
    val paymentMethodsJSON: LiveData<JSONObject> = _paymentMethodsJSON

    private val _paymentResultAdyen = MutableLiveData<OneTimeEvent<AdyenPaymentResult>>()
    val paymentResultAdyen: LiveData<OneTimeEvent<AdyenPaymentResult>> = _paymentResultAdyen

    private var paymentSessionRunning: Boolean = false

    fun createGCashPaymentSession(
        createPaymentSessionParams: CreatePaymentSessionParams
    ) {
        if (!paymentSessionRunning) {
            paymentSessionRunning = true
            viewModelScope.launch {
                if (createPaymentSessionParams.purchaseType is PurchaseType.BuyLoad) {
                    balanceDomainManager.checkAmaxWalletBalanceSufficiency(
                        CheckAmaxWalletBalanceSufficiencyParams(
                            amount = if (createPaymentSessionParams.amountAfterDiscount != null)
                                createPaymentSessionParams.amountAfterDiscount.toString()
                            else
                                createPaymentSessionParams.price
                        )
                    ).fold({ isSufficient ->
                        dLog("Amax wallet balance call successful.")
                        if (!isSufficient) {
                            _paymentResultGcash.value =
                                OneTimeEvent(GCashPaymentResult.GenericPaymentError)
                        }
                    }, {
                        dLog("Amax wallet balance call unsuccessful.")
                        _paymentResultGcash.value =
                            OneTimeEvent(GCashPaymentResult.GenericPaymentError)
                    })
                }
                paymentDomainManager.createGCashPaymentSession(
                    createPaymentSessionParams
                ).fold(
                    {
                        dLog("Payment session checkoutUrl created.")
                        _paymentResultGcash.value =
                            OneTimeEvent(GCashPaymentResult.PaymentSessionCreated((it as CreatePaymentSessionResult.CreateSessionCheckOutUrlSuccess).createPaymentSessionResult.token))
                        _checkoutUrl.value =
                            it.checkoutUrl
                    }, {
                        dLog("Failed to create payment session token.")
                        _paymentResultGcash.value =
                            OneTimeEvent(GCashPaymentResult.GenericPaymentError)
                    }
                )
            }
        }
    }

    fun createAdyenPaymentSession(
        createPaymentSessionParams: CreatePaymentSessionParams
    ) {
        if (!paymentSessionRunning) {
            paymentSessionRunning = true
            viewModelScope.launch {
                if (createPaymentSessionParams.purchaseType is PurchaseType.BuyLoad) {
                    balanceDomainManager.checkAmaxWalletBalanceSufficiency(
                        CheckAmaxWalletBalanceSufficiencyParams(amount = createPaymentSessionParams.price)
                    ).fold({ isSufficient ->
                        dLog("Amax wallet balance call successful.")
                        if (!isSufficient) {
                            _paymentResultAdyen.value =
                                OneTimeEvent(AdyenPaymentResult.GenericPaymentError)
                        }
                    }, {
                        dLog("Amax wallet balance call unsuccessful.")
                        _paymentResultAdyen.value =
                            OneTimeEvent(AdyenPaymentResult.GenericPaymentError)
                    })
                }
                paymentDomainManager.createAdyenPaymentSession(
                    createPaymentSessionParams
                ).fold(
                    {
                        dLog("Payment session token created.")
                        _paymentResultAdyen.value =
                            OneTimeEvent(AdyenPaymentResult.PaymentSessionCreated((it as CreatePaymentSessionResult.CreateAdyenDropInSessionSuccess).createPaymentSessionResult.token))
                        val paymentMethodsArray =
                            Gson().fromJson(it.paymentMethods, JsonArray::class.java)
                        val paymentMethodsString =
                            Gson().toJson(PaymentMethodWrapper(paymentMethodsArray))
                        val paymentMethodsJSON = JSONObject(paymentMethodsString)
                        _paymentMethodsJSON.value = paymentMethodsJSON
                    }, {
                        dLog("Failed to create payment session token.")
                        _paymentResultAdyen.value =
                            OneTimeEvent(AdyenPaymentResult.GenericPaymentError)
                    }
                )
            }
        }
    }

    override val logTag = "GCashPaymentViewModel"

    sealed class GCashPaymentResult {
        class PaymentSessionCreated(
            val tokenPaymentId: String
        ) : GCashPaymentResult()

        object GenericPaymentError : GCashPaymentResult()
    }

    sealed class AdyenPaymentResult {
        class PaymentSessionCreated(
            val tokenPaymentId: String
        ) : AdyenPaymentResult()

        object GenericPaymentError : AdyenPaymentResult()
    }
}

// data class used to wrap the paymentMethods JSONArray
// as that is the format DropIn is expecting to get
data class PaymentMethodWrapper(
    val paymentMethods: JsonArray
)
