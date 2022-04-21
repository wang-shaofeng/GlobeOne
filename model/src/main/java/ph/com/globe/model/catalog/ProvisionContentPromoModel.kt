/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.catalog

import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.brand.MOBILE_SEGMENT
import ph.com.globe.model.util.brand.PREPAID_BRAND_TYPE

data class ProvisionContentPromoParams(
    val mobileNumber: String,
    val serviceId: String,
    val otpReferenceId: String? = null
)

@JsonClass(generateAdapter = true)
data class ProvisionContentPromoRequest(
    val brand: String = PREPAID_BRAND_TYPE,
    val segment: String = MOBILE_SEGMENT,
    val mobileNumber: String,
    val serviceId: String
)

data class UnsubscribeContentPromoParams(
    val mobileNumber: String,
    val serviceId: String
)

@JsonClass(generateAdapter = true)
data class UnsubscribeContentPromoRequest(
    val mobileNumber: String,
    val serviceId: String,
    val brand: String = PREPAID_BRAND_TYPE
)
