/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.brand.*

@JsonClass(generateAdapter = true)
data class EnrollMigratedAccountsParams(
    val enrollAccounts: List<EnrollMigratedAccountsJson>
)

@JsonClass(generateAdapter = true)
data class EnrollMigratedAccountsJson(
    val accountNumber: String?,
    val mobileNumber: String?,
    val landlineNumber: String?,
    var accountAlias: String,
    val brand: String,
    val brandDetail: String,
    val segment: String,
    val channel: List<String>
)

@JsonClass(generateAdapter = true)
data class EnrollMigratedAccountsResponse(
    val result: List<EnrollMigratedAccountsResult>
)

@JsonClass(generateAdapter = true)
data class EnrollMigratedAccountsResult(
    val account: AccountResponse,
    val status: String,
    val message: String
)

@JsonClass(generateAdapter = true)
data class AccountResponse(
    val accountNumber: String?,
    val mobileNumber: String?,
    val landlineNumber: String?,
    var accountAlias: String,
    @StringAsAccountBrandType
    @Json(name = BRAND_KEY)
    val brandType: AccountBrandType,
    @StringAsAccountSegment
    val segment: AccountSegment,
    val channel: List<String>,
    val createdAt: String?
)

fun AccountResponse.pickPrimaryMsisdn(): String =
    when {
        segment == AccountSegment.Mobile || (segment == AccountSegment.Broadband && brandType == AccountBrandType.Prepaid) -> mobileNumber!!
        segment == AccountSegment.Broadband && brandType == AccountBrandType.Postpaid -> accountNumber!!
        else -> throw IllegalStateException("'GetEnrolledAccounts' response is not in a valid format!")
    }
