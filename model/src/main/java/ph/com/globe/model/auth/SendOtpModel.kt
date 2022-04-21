/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.auth

import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class SendOtpParams(
    val type: OtpType,
    val msisdn: String,
    val categoryIdentifier: List<String>,
    val brandType: AccountBrandType,
    val segment: AccountSegment,
    val patternName: String? = "OTP"
)

@JsonClass(generateAdapter = true)
data class SendOtpResult(
    val referenceId: String
)

sealed class OtpType : Serializable {
    abstract val name: String

    object SMS : OtpType() {
        override val name: String = SMS_NAME
    }

    object Email : OtpType() {
        override val name: String = EMAIL_NAME
    }
}

const val SMS_NAME = "sms"
const val EMAIL_NAME = "email"
