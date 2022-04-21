/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.profile.response_models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.util.brand.*
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class GetEnrolledAccountsResponse(
    val result: List<EnrolledAccountJson>
)

@JsonClass(generateAdapter = true)
data class EnrolledAccountJson(
    val mobileNumber: String?,
    val accountNumber: String?,
    val landlineNumber: String?,
    val accountAlias: String,
    @StringAsAccountBrandType
    @Json(name = BRAND_KEY)
    val brandType: AccountBrandType,
    @StringAsAccountSegment
    val segment: AccountSegment,
    val channel: List<String>,
    val isGcashLinked: Boolean
) : Serializable

fun EnrolledAccountJson.pickPrimaryMsisdn(): String =
    try {
        when {
            segment == AccountSegment.Mobile || (segment == AccountSegment.Broadband && brandType == AccountBrandType.Prepaid) -> mobileNumber!!
            segment == AccountSegment.Broadband && brandType == AccountBrandType.Postpaid -> accountNumber!!
            else -> throw IllegalStateException("'GetEnrolledAccounts' response is not in a valid format! :: segment = $segment & brand = $brandType")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }

fun EnrolledAccountJson.toDomain() =
    EnrolledAccount(
        primaryMsisdn = pickPrimaryMsisdn(),
        mobileNumber = mobileNumber,
        accountNumber = accountNumber,
        landlineNumber = landlineNumber,
        accountAlias = accountAlias,
        brandType = brandType,
        segment = segment,
        channel = channel,
        isGcashLinked = isGcashLinked
    )
