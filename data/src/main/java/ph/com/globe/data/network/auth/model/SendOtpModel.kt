/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.model.account.*
import ph.com.globe.model.auth.OtpType
import ph.com.globe.model.auth.SendOtpParams
import ph.com.globe.model.auth.SendOtpResult
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.BRAND_KEY
import ph.com.globe.model.util.brand.StringAsAccountBrandType

@JsonClass(generateAdapter = true)
data class SendOtpRequest(
    val type: String,
    val accountNumber: String? = null,
    val landlineNumber: String? = null,
    val mobileNumber: String? = null,
    val categoryIdentifier: List<String>,
    @Json(name = BRAND_KEY)
    @StringAsAccountBrandType
    val brandType: AccountBrandType,
    val segment: String,
    val patternId: String,
    val patternName: String? = PATTERN_NAME_OTP
)

fun SendOtpParams.toSendOtpRequest(): SendOtpRequest =
    SendOtpRequest(
        type = type.name,
        accountNumber = msisdn.takeIf { it.isAccountNumber() },
        landlineNumber = msisdn.takeIf { it.isLandlineNumber() },
        mobileNumber = msisdn.takeIf { it.isMobileNumber() },
        categoryIdentifier = categoryIdentifier,
        brandType = brandType,
        segment = segment.toString(),
        patternId = type.toPatternId(),
        patternName = patternName
    )

@JsonClass(generateAdapter = true)
data class SendOtpResponse(
    val result: SendOtpResult
)

fun OtpType.toPatternId(): String =
    when (this) {
        is OtpType.SMS -> BuildConfig.SEND_OTP_SMS_PATTERN_ID
        is OtpType.Email -> BuildConfig.SEND_OTP_EMAIL_PATTERN_ID
    }

const val PATTERN_NAME_OTP = "OTP"
