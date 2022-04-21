package ph.com.globe.errors.payment

import ph.com.globe.errors.GeneralError

sealed class PaymentServiceError {
    object NoInternetConnectionError: PaymentServiceError()
    data class General(val error: GeneralError) : PaymentServiceError()
}

fun PaymentServiceError.toSubmitAdyenPaymentError(): SubmitAdyenPaymentError =
    when(this) {
        is PaymentServiceError.NoInternetConnectionError -> SubmitAdyenPaymentError.NoInternetConnectionError
        else -> SubmitAdyenPaymentError.General
    }
