package ph.com.globe.data.network.payment.model

import com.squareup.moshi.JsonClass
import ph.com.globe.model.payment.GetPaymentSessionResult

@JsonClass(generateAdapter = true)
data class GetPaymentSessionResponse(
    val result: GetPaymentSessionResult
)
