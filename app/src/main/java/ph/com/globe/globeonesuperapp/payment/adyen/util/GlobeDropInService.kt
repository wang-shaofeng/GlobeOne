package ph.com.globe.globeonesuperapp.payment.adyen.util

import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import ph.com.globe.domain.payment.PaymentDomainManager
import ph.com.globe.errors.payment.ConfirmAdyenPaymentError
import ph.com.globe.errors.payment.SubmitAdyenPaymentError
import ph.com.globe.util.exhaustive
import ph.com.globe.model.payment.ConfirmAdyenPaymentResult
import ph.com.globe.model.payment.SubmitAdyenPaymentResult
import ph.com.globe.model.payment.ThirdPartyPaymentResult
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

@AndroidEntryPoint
class GlobeDropInService @Inject constructor() : DropInService() {

    @Inject
    lateinit var paymentDomainManager: PaymentDomainManager

    // Handling for submitting a payment request
    override fun makePaymentsCall(paymentComponentJson: JSONObject): DropInServiceResult {
        val submitAdyenPaymentResult: LfResult<SubmitAdyenPaymentResult, SubmitAdyenPaymentError> =
            runBlocking {
                paymentDomainManager.submitAdyenPayment(
                    paymentComponentJson.getJSONObject("paymentMethod").toString()
                )
            }
        return submitAdyenPaymentResult.fold({ result ->
            when (result) {
                is SubmitAdyenPaymentResult.SubmitAdyenPaymentFinished ->
                    DropInServiceResult.Finished(result.resultCode?.result.toString())
                is SubmitAdyenPaymentResult.SubmitAdyenPaymentSuccessNeedAction ->
                    DropInServiceResult.Action((result.actionObject))
            }.exhaustive
        }, { error ->
            when (error) {
                is SubmitAdyenPaymentError.NoInternetConnectionError ->
                    DropInServiceResult.Finished(ThirdPartyPaymentResult.AdyenResult.AdyenResultNoConnection.result)
                is SubmitAdyenPaymentError.General ->
                    DropInServiceResult.Finished(ThirdPartyPaymentResult.AdyenResult.AdyenResultError.result)
            }
        })
    }

    // Handling for submitting additional payment details request after the redirection flow completes
    override fun makeDetailsCall(actionComponentJson: JSONObject): DropInServiceResult {
        val submitAdyenPaymentResult: LfResult<ConfirmAdyenPaymentResult, ConfirmAdyenPaymentError> =
            runBlocking {
                paymentDomainManager.confirmAdyenPayment((actionComponentJson.getJSONObject("details")).toString())
            }
        return submitAdyenPaymentResult.fold({ result ->
            when (result) {
                is ConfirmAdyenPaymentResult.SubmitAdyenPaymentFinished ->
                    DropInServiceResult.Finished(result.resultCode?.result.toString())
            }.exhaustive
        }, {
            DropInServiceResult.Finished(ThirdPartyPaymentResult.AdyenResult.AdyenResultError.result)
        })
    }
}
