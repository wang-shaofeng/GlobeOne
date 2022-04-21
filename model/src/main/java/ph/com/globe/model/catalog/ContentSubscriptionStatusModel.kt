/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.catalog

import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.model.util.brand.MOBILE_SEGMENT
import ph.com.globe.model.util.brand.SEGMENT_KEY

data class ContentSubscriptionStatusParams(
    val mobileNumber: String,
    val serviceId: String,
    val segment: AccountSegment = AccountSegment.Mobile
)

fun ContentSubscriptionStatusParams.toQueryMap(): Map<String, String> =
    mapOf(
        "mobileNumber" to mobileNumber,
        SEGMENT_KEY to segment.toString()
    )

@JsonClass(generateAdapter = true)
data class ContentSubscriptionStatusResponse(
    val result: ContentSubscriptionStatusResult
)

@JsonClass(generateAdapter = true)
data class ContentSubscriptionStatusResult(
    val activationStatus: Boolean
)
