package ph.com.globe.errors.payment

sealed class SubmitAdyenPaymentError {
    object NoInternetConnectionError : SubmitAdyenPaymentError()
    object General : SubmitAdyenPaymentError()
}
