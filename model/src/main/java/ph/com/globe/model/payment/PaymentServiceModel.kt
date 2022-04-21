package ph.com.globe.model.payment

import com.squareup.moshi.JsonClass

data class PaymentServiceParams(
    val paymentTokenId: String,
    val name: String,
    val paymentComponentJson: String? = null,
    val actionComponentJson: String? = null,
    val merchantAccount: String?
)

@JsonClass(generateAdapter = true)
data class PaymentServiceResult(
    val timestamp: String,
    val message: String,
    val data: PaymentServiceData
)

@JsonClass(generateAdapter = true)
data class PaymentServiceData(
    val paymentId: String,
    val message: String?,
    val pspReference: String?,
    val status: String?,
    val resultCode: String?,
    val reason: String?,
)
