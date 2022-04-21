/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account.network_models

import com.squareup.moshi.JsonClass
import ph.com.globe.model.account.REMAINING_UNLIMITED
import ph.com.globe.model.account.UsageUIModel

data class GetPrepaidPromoSubscriptionUsageParams(
    val token: String,
    val request: GetPrepaidPromoSubscriptionUsageRequest
)

@JsonClass(generateAdapter = true)
data class GetPrepaidPromoSubscriptionUsageRequest(
    val serviceNumber: String,
    val forceRefresh: Boolean? = true
)

@JsonClass(generateAdapter = true)
data class GetPrepaidPromoSubscriptionUsageResponse(
    val promoSubscriptionUsage: PromoSubscriptionUsageJson
)

@JsonClass(generateAdapter = true)
data class PromoSubscriptionUsageJson(
    val mainData: List<DataItemJson>?,
    val appData: List<DataItemJson>?
)

@JsonClass(generateAdapter = true)
data class DataItemJson(
    val skelligCategory: String?,
    val skelligWallet: String,
    val dataRemaining: Int?,
    val dataTotal: Int?,
    val endDate: String,
    val type: String?,
    val details: List<DataItemDetails>
)

@JsonClass(generateAdapter = true)
data class DataItemDetails(
    val skelligKeyword: String?,
    val promoName: String?,
    val promoDescription: String?,
    val dataTotal: Int?,
    val quantity: Int
)

data class PromoSubscriptionUsageResult(
    val data: List<DataItemJson>
)

fun UsageUIModel.applySubscriptionUsage(
    subscriptionUsage: PromoSubscriptionUsageResult,
): UsageUIModel {
    with(this) {
        for (bucket in subscriptionUsage.data) {
            val bucketTotal = bucket.dataTotal ?: -1
            val bucketRemaining = bucket.dataRemaining ?: -1
            isDataUnlimited =
                with(bucket) { type == UNLIMITED_TYPE || dataRemaining == null || dataTotal == null }
            if (!isDataUnlimited) {
                dataTotal += bucketTotal
                dataRemaining += bucketRemaining
            }
        }

        hasDataSubscriptions = dataTotal > 0 || isDataUnlimited

        return this
    }
}

fun UsageUIModel.applyGroupData(
    bucketTotal: Int,
    bucketRemaining: Int
): UsageUIModel {
    with(this) {
        if (bucketRemaining == REMAINING_UNLIMITED)
            isDataUnlimited = true
        else {
            dataTotal += bucketTotal
            dataRemaining += bucketRemaining
        }

        hasDataSubscriptions = dataTotal > 0 || isDataUnlimited

        return this
    }
}

private const val UNLIMITED_TYPE = "Unli"
