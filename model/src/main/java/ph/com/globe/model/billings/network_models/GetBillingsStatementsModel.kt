package ph.com.globe.model.billings.network_models

import com.squareup.moshi.JsonClass
import ph.com.globe.model.billings.domain_models.BillingStatement
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.model.util.brand.SEGMENT_KEY

data class GetBillingsStatementsParams(
    val mobileNumber: String,
    val segment: AccountSegment,
    val pageSize: Int,
    val format: String = "list"
)

fun GetBillingsStatementsParams.toQueryMap(): Map<String, String> =
    mapOf(
        "mobileNumber" to mobileNumber,
        SEGMENT_KEY to segment.toString(),
        "format" to format,
        "pageSize" to pageSize.toString()
    )

@JsonClass(generateAdapter = true)
data class GetBillingsStatementsResponse(
    val result: GetBillingsStatementsResult
)

@JsonClass(generateAdapter = true)
data class GetBillingsStatementsResult(
    val billingStatements: List<BillingStatementItemResult>,
    val verificationToken: String? = null
)

@JsonClass(generateAdapter = true)
data class BillingStatementItemResult(
    val id: String?,
    val type: String?,
    val status: String?,
    val billingDate: String?,
    val billStartDate: String?,
    val billEndDate: String?,
    val baNo: String?,
    val dueDate: String?,
    val totalAmount: Double?,
    val invoiceAmount: Double?,
    val invoiceNetAmount: Double?
)

fun GetBillingsStatementsResponse.toDomain() =
    result.billingStatements.map {
        BillingStatement(
            it.id,
            it.type,
            it.status,
            it.billingDate,
            it.billStartDate,
            it.billEndDate,
            it.baNo,
            it.dueDate,
            it.totalAmount,
            it.invoiceAmount,
            it.invoiceNetAmount,
            verificationToken = result.verificationToken
        )
    }
