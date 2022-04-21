/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.group

import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.toUrlString

data class RetrieveMemberUsageParams(
    val isGroupOwner: Boolean,
    val memberAccountAlias: String,
    val keyword: String,
    val memberMobileNumber: String? = null,
    val ownerMobileNumber: String? = null
)

fun RetrieveMemberUsageParams.toQueryMap(): Map<String, String> = mutableMapOf(
    "isGroupOwner" to isGroupOwner.toString(),
    "accountAlias" to memberAccountAlias.toUrlString(),
    "keywords" to keyword,
).apply {
    if (isGroupOwner) {
        memberMobileNumber?.let {
            this["memberMobileNumber"] = memberMobileNumber
        }
    } else {
        ownerMobileNumber?.let {
            this["ownerMobileNumber"] = ownerMobileNumber
        }
    }
}

@JsonClass(generateAdapter = true)
data class RetrieveMemberUsageResponse(
    val result: RetrieveMemberUsageResult
)

@JsonClass(generateAdapter = true)
data class RetrieveMemberUsageResult(
    val walletId: String,
    val mobileNumber: String?,
    val startDate: String,
    val endDate: String,
    val volumeRemaining: String,
    val totalAllocated: String,
    val volumeUsed: String,
    val rolloverAmount: String?,
    val type: String,
    val unit: String
)
