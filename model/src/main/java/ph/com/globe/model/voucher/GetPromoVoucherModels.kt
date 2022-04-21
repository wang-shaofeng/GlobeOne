/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.voucher

import com.squareup.moshi.JsonClass

data class GetPromoVouchersParams(
    val pageNumber: Int,
    val pageSize: Int,
    val mobileNumber: String,
)

@JsonClass(generateAdapter = true)
data class PromoVouchersResponse(
    val result: PromoVouchersResult
)

@JsonClass(generateAdapter = true)
data class PromoVouchersResult(
    val pageNumber: String,
    val totalRecords: String,
    val totalPages: String,
    val vouchers: List<PromoVoucher>?
)

@JsonClass(generateAdapter = true)
data class PromoVoucher(
    val contentPartnerName: String,
    val category: String,
    val code: String,
    val serialNumber: String,
    val validityStartDate: String,
    val validityEndDate: String,
    val description: String,
    val paidAmount: String,
    val dispenseDate: String,
)
