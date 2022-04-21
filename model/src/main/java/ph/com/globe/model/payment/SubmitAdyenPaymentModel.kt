package ph.com.globe.model.payment

sealed class SubmitAdyenPaymentResult {
    data class SubmitAdyenPaymentSuccessNeedAction(
        val actionObject: String
    ) : SubmitAdyenPaymentResult()

    class SubmitAdyenPaymentFinished(val resultCode: ThirdPartyPaymentResult.AdyenResult?) : SubmitAdyenPaymentResult()
}
