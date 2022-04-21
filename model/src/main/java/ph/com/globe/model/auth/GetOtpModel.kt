/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.auth

import com.squareup.moshi.JsonClass
import ph.com.globe.model.account.toHeaderPair

data class GetOtpParams(
    val msisdn: String,
    val referenceId: String,
    val categoryIdentifiers: String
)

fun GetOtpParams.toQueryMap(): Map<String, String> = mapOf(
    msisdn.toHeaderPair(),
    "referenceId" to referenceId
)

@JsonClass(generateAdapter = true)
data class GetOtpResponse(
    val result: GetOtpResult
)

@JsonClass(generateAdapter = true)
data class GetOtpResult(
    val otp: String
)
