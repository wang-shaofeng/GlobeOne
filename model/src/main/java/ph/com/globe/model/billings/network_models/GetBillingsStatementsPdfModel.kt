package ph.com.globe.model.billings.network_models

import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.brand.AccountSegment

data class GetBillingsStatementsPdfParams(
    val responseType: ResponseType,
    val billingStatementId: String? = null,
    val accountNumber: String? = null,
    val mobileNumber: String? = null,
    val landlineNumber: String? = null,
    val segment: AccountSegment,
    val format: String = "pdf",
    val verificationToken: String? = null
)

sealed class ResponseType {

    object JsonFormat : ResponseType()

    object PdfFormat : ResponseType()

    object Other : ResponseType()
}

fun ResponseType.fromObjectToString() =
    when (this) {
        is ResponseType.JsonFormat -> "application/json"
        is ResponseType.PdfFormat -> "application/pdf"
        else -> ""
    }

fun String.fromStringToObject() =
    when (this) {
        "application/json" -> ResponseType.JsonFormat
        "application/pdf" -> ResponseType.PdfFormat
        else -> ResponseType.Other
    }

@JsonClass(generateAdapter = true)
data class GetBillingsStatementsPdfResponse(
    val result: String
)
