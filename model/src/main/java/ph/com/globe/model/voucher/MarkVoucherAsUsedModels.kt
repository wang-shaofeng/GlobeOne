/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.voucher

import com.squareup.moshi.JsonClass

data class MarkVouchersAsUsedParams(
    val mobileNumber: String? = null,
    val accountNumber: String? = null,
    val requestParams: MarkVoucherAsUsedRequest
)

@JsonClass(generateAdapter = true)
data class MarkVoucherAsUsedRequest(
    val vouchers: List<Voucher>
)

@JsonClass(generateAdapter = true)
data class Voucher(
    val id: String,
    val code: String,
    val type: String,
    val expiryDate: String?
)
