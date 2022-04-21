/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.enrolled_accounts

import androidx.room.Entity
import androidx.room.PrimaryKey
import ph.com.globe.data.db.enrolled_accounts.EnrolledAccountEntity.Companion.TABLE_NAME
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.response_models.EnrolledAccountJson
import ph.com.globe.model.profile.response_models.pickPrimaryMsisdn
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment

@Entity(
    tableName = TABLE_NAME
)
data class EnrolledAccountEntity(
    @PrimaryKey
    val primaryMsisdn: String,
    val mobileNumber: String?,
    val accountNumber: String?,
    val landlineNumber: String?,
    val accountAlias: String,
    val brandType: AccountBrandType,
    val segment: AccountSegment,
    val channel: List<String>,
    val isGcashLinked: Boolean
) {
    companion object {
        const val TABLE_NAME = "enrolled_accounts"
    }
}

fun EnrolledAccountJson.toEntity() =
    EnrolledAccountEntity(
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

fun EnrolledAccountEntity.toDomain() =
    EnrolledAccount(
        primaryMsisdn = primaryMsisdn,
        mobileNumber = mobileNumber,
        accountNumber = accountNumber,
        landlineNumber = landlineNumber,
        accountAlias = accountAlias,
        brandType = brandType,
        segment = segment,
        channel = channel,
        isGcashLinked = isGcashLinked
    )
