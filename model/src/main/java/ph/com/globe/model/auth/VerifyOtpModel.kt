/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.auth

import com.squareup.moshi.JsonClass
import ph.com.globe.model.account.isAccountNumber
import ph.com.globe.model.account.isLandlineNumber
import ph.com.globe.model.account.isMobileNumber
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment

@JsonClass(generateAdapter = true)
data class VerifyOtpParams(
    val source: String = "cxs",
    val msisdn: String,
    val referenceId: String,
    val code: String,
    val brandType: AccountBrandType,
    val segment: AccountSegment,
    val categoryIdentifier: List<String>
)

@JsonClass(generateAdapter = true)
data class VerifyOtpRequest(
    val source: String = "cxs",
    val accountNumber: String?,
    val landlineNumber: String?,
    val mobileNumber: String?,
    val referenceId: String,
    val code: String,
    val brand: String,
    val segment: String,
    val categoryIdentifier: List<String>
)

@JsonClass(generateAdapter = true)
data class VerifyOtpWithThirdPartyParams(
    val source: String = "third-party",
    val referenceId: String,
    val code: String
)

@JsonClass(generateAdapter = true)
data class VerifyOtpResult(
    val cxsMessageId: String
)

fun VerifyOtpParams.toVerifyOtpRequest(): VerifyOtpRequest =
    VerifyOtpRequest(
        accountNumber = msisdn.takeIf { it.isAccountNumber() },
        landlineNumber = msisdn.takeIf { it.isLandlineNumber() },
        mobileNumber = msisdn.takeIf { it.isMobileNumber() },
        categoryIdentifier = categoryIdentifier,
        brand = brandType.toString(),
        segment = segment.toString(),
        code = code,
        source = source,
        referenceId = referenceId
    )
