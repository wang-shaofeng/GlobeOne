/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.voucher

import com.squareup.moshi.JsonClass

data class RetrieveUsedVouchersParams(
    val mobileNumber: String? = null,
    val accountNumber: String? = null
)

@JsonClass(generateAdapter = true)
data class RetrieveUsedVouchersResponse(
    val result: RetrieveUsedVouchersResult
)

@JsonClass(generateAdapter = true)
data class RetrieveUsedVouchersResult(
    val vouchers: List<RetrieveUsedVoucherItem>?
)

@JsonClass(generateAdapter = true)
data class RetrieveUsedVoucherItem(
    val id: String,
    val code: String,
    val type: String,
    val expiryDate: String?
)
