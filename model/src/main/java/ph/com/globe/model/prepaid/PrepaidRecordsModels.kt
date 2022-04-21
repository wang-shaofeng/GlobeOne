package ph.com.globe.model.prepaid

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PrepaidLedgerResponseModel (
    val result: PrepaidLedgerTransaction
)

@JsonClass(generateAdapter = true)
data class PrepaidLedgerTransaction(
    val transactions: List<PrepaidLedgerModel>,
    val lastTransactionKey: String,
    val morePage: String
)

@JsonClass(generateAdapter = true)
data class PrepaidLedgerModel(
    val sourceMobileNumber: String?,
    val targetMobileNumber: String?,
    val chargeAmount: String?,
    val transactionCount: String?,
    val eventStartDate: String,
    val durationCount: String?,
    val dataVolumeCount: String?,
    val unitOfMeasurementCode: String?,
    val topupCut: String?,
    val serviceCharge: String?,
    val channel: String?,
    val promoCut: String?,
    val paymentMode: String?,
    val serviceName: String?,
    val refundIndicator: String?,
    val usage: Usage?,
    val aggregationDetails: AggregationDetails?
)

@JsonClass(generateAdapter = true)
data class Usage(
    val directionCode: String,
    val networkRoamingTypeCode: String,
    val destinationTypeCode: String,
    val networkTypeDescription: String,
)

@JsonClass(generateAdapter = true)
data class AggregationDetails (
    val lastTransactionTimestamp: String,
    val date: String,
)
