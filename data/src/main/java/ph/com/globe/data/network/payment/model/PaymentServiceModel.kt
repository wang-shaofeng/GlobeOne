package ph.com.globe.data.network.payment.model

import androidx.annotation.StringDef
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.squareup.moshi.JsonClass
import okhttp3.MediaType
import okhttp3.RequestBody
import ph.com.globe.model.payment.PaymentServiceParams
import ph.com.globe.model.payment.PaymentServiceResult

// request section
@JsonClass(generateAdapter = true)
data class PaymentServiceRequest(
    val command: Command
)

@JsonClass(generateAdapter = true)
data class Command(
    @PaymentServiceRequestType
    val name: String,
    val payload: Payload?
)

@JsonClass(generateAdapter = true)
data class Payload(
    val shopperReference: String? = null,
    val otherInfo: String? = null,
    val paymentId: String? = null,
    val paymentMethod: JsonObject? = null,
    val details: JsonObject? = null,
    val amount: Double? = null,
    val merchantAccount: String? = null
)

fun PaymentServiceParams.toPaymentServiceRequestBody(uuid: String?): RequestBody =
    RequestBody.create(
        MediaType.parse("application/json"),
        Gson().toJson(
            PaymentServiceRequest(
                Command(
                    name = name,
                    payload = when (name) {
                        DROPIN_PAYMENT_DATA_CREATED_REQUEST -> Payload(
                            shopperReference = uuid,
                            paymentId = paymentTokenId,
                            merchantAccount = merchantAccount,
                            paymentMethod = JsonParser.parseString(paymentComponentJson).asJsonObject
                        )
                        DROPIN_PAYMENT_DETAILS_REQUEST ->
                            Payload(
                                details = JsonParser.parseString(actionComponentJson).asJsonObject
                            )
                        else -> null
                    }
                )
            )
        )
    )

const val VERIFY_PAYMENT_TOKEN_REQUEST = "VerifyPaymentToken"
const val PAYMENT_REFUND_SESSION_REQUEST = "PaymentRefundSession"
const val DROPIN_PAYMENT_DETAILS_REQUEST = "DropinPaymentDetails"
const val DROPIN_PAYMENT_DATA_CREATED_REQUEST = "DropinPaymentDataCreated"

@StringDef(
    VERIFY_PAYMENT_TOKEN_REQUEST,
    PAYMENT_REFUND_SESSION_REQUEST,
    DROPIN_PAYMENT_DETAILS_REQUEST,
    DROPIN_PAYMENT_DATA_CREATED_REQUEST
)
annotation class PaymentServiceRequestType

// end request section
// response section

@JsonClass(generateAdapter = true)
data class PaymentServiceResponse(
    val result: PaymentServiceResult
)
