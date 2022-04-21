/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account

import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment
import java.io.Serializable

data class EnrollAccountParams(
    val referenceId: String,
    val msisdn: String,
    var accountAlias: String,
    val brandType: AccountBrandType,
    val segment: AccountSegment,
    val channel: List<String>,
    val verificationType: String? = null
) : Serializable

@JsonClass(generateAdapter = true)
data class EnrollAccountRequest(
    val accountNumber: String?,
    val mobileNumber: String?,
    val landlineNumber: String?,
    var accountAlias: String,
    val brand: String,
    val segment: String,
    val channel: List<String>
) : Serializable

fun EnrollAccountParams.toEnrollAccountRequest(): EnrollAccountRequest =
    EnrollAccountRequest(
        accountNumber = msisdn.takeIf { it.isAccountNumber() },
        landlineNumber = msisdn.takeIf { it.isLandlineNumber() },
        mobileNumber = msisdn.takeIf { it.isMobileNumber() },
        brand = brandType.toString(),
        channel = channel,
        segment = segment.toString(),
        accountAlias = accountAlias
    )
