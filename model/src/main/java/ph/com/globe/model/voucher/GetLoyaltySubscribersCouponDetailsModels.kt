/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.voucher

import com.squareup.moshi.JsonClass

data class GetLoyaltySubscribersCouponDetailsParams(
    val subscriberId: String,
    val subscriberType: Int,
    val channel: String,
    val expiryDateFrom: String? = null,
    val expiryDateTo: String? = null,
    val offset: Int? = null,
    val limit: Int? = null
)

@JsonClass(generateAdapter = true)
data class LoyaltySubscriberCouponDetailsResponse(
    val result: LoyaltySubscriberCouponDetailsResult,
)

@JsonClass(generateAdapter = true)
data class LoyaltySubscriberCouponDetailsResult(
    val coupons: List<Coupon>?,
    val details: CouponDetails?
)

@JsonClass(generateAdapter = true)
data class Coupon(
    val couponId: Int,
    val couponNumber: String,
    val expiryDate: String?,
    val couponStatus: CouponStatus,
    val couponType: String,
    val couponDescription: String,
    val couponVisualUrl: String?,
    val couponBenefitType: String,
    val couponBenefitName: String,
    val couponBenefitAmount: String?
)

@JsonClass(generateAdapter = true)
data class CouponDetails(
    val fault: String,
    val totalRecordCount: String,
    val totalRecordCountLimited: Boolean,
    val pageFirstResult: String
)

enum class CouponStatus {
    // This corresponds to couponStatus: I
    VISIBLE,

    // This corresponds to couponStatus: E
    HIDE
}

const val DESCRIPTION_VOUCHER = "Voucher - "
const val DESCRIPTION_SOFT_BENEFIT = "Soft Benefit - "
const val VOUCHER_LINK_PREFIX = "glbe.ph/"
const val HTTPS_PREFIX = "https://"
