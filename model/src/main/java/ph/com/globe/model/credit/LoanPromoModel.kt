/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.credit

import com.squareup.moshi.JsonClass

data class LoanPromoParams(
    val referenceId: String?,
    val loanPromoRequest: LoanPromoRequest
)

@JsonClass(generateAdapter = true)
data class LoanPromoRequest(
    val keyword: String,
    val transactionId: String,
    val mobileNumber: String
)

fun LoanPromoParams.toHeadersMap() = mapOf("otpreferenceid" to (referenceId ?: ""))
