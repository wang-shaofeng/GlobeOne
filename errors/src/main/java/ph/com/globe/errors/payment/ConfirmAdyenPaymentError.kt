package ph.com.globe.errors.payment

sealed class ConfirmAdyenPaymentError {
    object General : ConfirmAdyenPaymentError()
}
