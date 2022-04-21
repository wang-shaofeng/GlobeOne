/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.credit

import com.squareup.moshi.JsonClass

data class GetCreditInfoParams(
    val otpReferenceId: String?,
    val mobileNumber: String
)

fun GetCreditInfoParams.toHeadersMap(): Map<String, String> = mapOf("OTPReferenceId" to (otpReferenceId ?: ""))

@JsonClass(generateAdapter = true)
data class GetCreditInfoResponse(
    val result: GetCreditInfoResult
)

@JsonClass(generateAdapter = true)
data class GetCreditInfoResult(
    val loanedAmount: String,
    val loanedDenomination: String,
    val maximumLoanableAmount: String,
    val hasLoan: Boolean,
)
