package ph.com.globe.model.payment

sealed class ConfirmAdyenPaymentResult {
    class SubmitAdyenPaymentFinished(val resultCode: ThirdPartyPaymentResult.AdyenResult?) : ConfirmAdyenPaymentResult()
}
