/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.group

import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.toUrlString

data class RetrieveGroupUsageParams(
    val accountAlias: String,
    val promoKeyword: String
)

fun RetrieveGroupUsageParams.toQueryMap(): Map<String, String> = mapOf(
    "accountAlias" to accountAlias.toUrlString(),
    "promoKeyword" to promoKeyword
)

@JsonClass(generateAdapter = true)
data class RetrieveGroupUsageResponse(
    val result: RetrieveGroupUsageResult
)

@JsonClass(generateAdapter = true)
data class RetrieveGroupUsageResult(
    val walletId: String?,
    val owner: String?,
    val startDate: String?,
    val endDate: String?,
    val status: String?,
    val volumeRemaining: String?,
    val totalAllocated: String?,
    val volumeUsed: String?,
    val rolloverAmount: String?,
    val type: String?,
    val unit: String?,
    val subscriptions: List<GroupSubscription>?,
    val members: List<GroupMember>?
)

@JsonClass(generateAdapter = true)
data class GroupSubscription(
    val id: String,
    val keyword: String?,
    val startDate: String?,
    val endDate: String?,
    val walletId: String?,
    val volume: String?,
    val unit: String?,
)

@JsonClass(generateAdapter = true)
data class GroupMember(
    val mobileNumber: String,
    val allocationId: String,
    val totalAllocated: String,
    val unit: String
)
