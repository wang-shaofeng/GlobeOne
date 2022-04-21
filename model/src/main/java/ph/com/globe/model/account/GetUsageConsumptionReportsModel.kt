package ph.com.globe.model.account

import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.GB_STRING
import ph.com.globe.model.util.KB_IN_GB
import ph.com.globe.model.util.KB_IN_MB
import ph.com.globe.model.util.MB_STRING
import ph.com.globe.model.util.brand.BRAND_KEY
import ph.com.globe.model.util.brand.SEGMENT_KEY

data class GetUsageConsumptionReportsParams(
    val segment: String,
    val brand: String,
    val msisdn: String
)

fun GetUsageConsumptionReportsParams.toQueryMap(): Map<String, String> =
    mapOf("msisdn" to msisdn, SEGMENT_KEY to segment, BRAND_KEY to brand)

@JsonClass(generateAdapter = true)
data class GetUsageConsumptionReportsResponse(
    val result: UsageConsumptionResult
)

@JsonClass(generateAdapter = true)
data class UsageConsumptionResult(
    val promos: PromosConsumption
)

@JsonClass(generateAdapter = true)
data class PromosConsumption(
    val mainData: List<PromoUsageModel>
)

@JsonClass(generateAdapter = true)
data class PromoUsageModel(
    val dataTotal: String,
    val dataUsage: String,
    val dataRemaining: String,
    val postpaidPromoType: String
)

fun UsageUIModel.applyUsageConsumptionResult(
    usageConsumption: UsageConsumptionResult
): UsageUIModel {

    for (bucket in usageConsumption.promos.mainData) {
        dataTotal += bucket.dataTotal.extractDataFromFormattedString()
        dataRemaining += bucket.dataRemaining.extractDataFromFormattedString()
    }

    hasDataSubscriptions = dataTotal > 0 || isDataUnlimited

    usageFetched = true
    isLoading = false

    return this
}

private fun String.extractDataFromFormattedString(): Int =
    when {
        endsWith(MB_STRING) -> (substring(0, length - 2).toFloatOrNull()?.toInt() ?: 0) * KB_IN_MB
        endsWith(GB_STRING) -> (substring(0, length - 2).toFloatOrNull()?.toInt() ?: 0) * KB_IN_GB
        else -> 0
    }
