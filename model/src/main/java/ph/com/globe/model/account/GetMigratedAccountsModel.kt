/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account

import com.squareup.moshi.JsonClass
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.util.brand.*

data class GetMigratedAccountsParams(
    val email: String,
    val dataSource: String = "globeOne"
)

fun GetMigratedAccountsParams.toQueryMap(): Map<String, String> =
    mapOf("email" to email, "dataSource" to dataSource)

@JsonClass(generateAdapter = true)
data class GetMigratedAccountsResponse(
    val result: List<GetMigratedAccountsResultArray>
)

@JsonClass(generateAdapter = true)
data class GetMigratedAccountsResultArray(
    val dataSource: String?,
    val accounts: List<MigratedAccount>
)

@JsonClass(generateAdapter = true)
data class MigratedAccount(
    val mobileNumber: String,
    val accountNumber: String,
    val brand: AccountBrand,
    val accountAlias: String,
)

fun List<MigratedAccount>.toListOfEnrolledAccounts(): List<EnrolledAccount> {
    return this.map { it.toEnrolledAccount() }
}

fun MigratedAccount.toEnrolledAccount(): EnrolledAccount {
    return EnrolledAccount(
        mobileNumber,
        mobileNumber,
        accountNumber,
        "",
        accountAlias,
        brand.brandType,
        if (brand == AccountBrand.Hpw) AccountSegment.Broadband else AccountSegment.Mobile,
        arrayListOf(ENROLL_ACCOUNT_CHANNEL),
        false
    )
}

const val ENROLL_ACCOUNT_CHANNEL = "SuperApp"
