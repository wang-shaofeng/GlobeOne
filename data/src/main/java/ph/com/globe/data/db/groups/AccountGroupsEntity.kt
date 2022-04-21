/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.groups

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import ph.com.globe.data.db.shop.AppItemEntity
import ph.com.globe.data.db.shop.toDomain
import ph.com.globe.model.group.domain_models.AccountDetailsGroups
import ph.com.globe.model.group.domain_models.UsageItem

@Entity(tableName = AccountGroupsEntity.TABLE_NAME)
@JsonClass(generateAdapter = true)
data class AccountGroupsEntity(
    @PrimaryKey(autoGenerate = false)
    val primaryMsisdn: String,
    val usageItems: List<UsageItemEntity>?
) {
    companion object {
        const val TABLE_NAME = "account_groups"
    }
}

@JsonClass(generateAdapter = true)
data class UsageItemEntity(
    val title: String,
    val category: String,
    val left: Int,
    val total: Int,
    val expiration: String,
    val accountNumber: String,
    val accountName: String,
    val accountRole: String,
    val skelligWallet: String,
    val skelligCategory: String,
    val groupOwnerMobileNumber: String,
    val isUnlimited: Boolean = false,
    val addOnData: Boolean = false,
    val addOnDataType: String = "",
    val apps: List<AppItemEntity>?,
    val used: Int = -1
)

fun AccountGroupsEntity.toDomain() =
    AccountDetailsGroups(
        groups = usageItems?.map {
            with(it) {
                UsageItem(
                    title,
                    category,
                    left,
                    total,
                    expiration,
                    accountNumber,
                    accountName,
                    accountRole,
                    skelligWallet,
                    skelligCategory,
                    groupOwnerMobileNumber,
                    isUnlimited,
                    addOnData,
                    addOnDataType,
                    emptyList(),
                    apps?.toDomain(),
                    used
                )
            }
        }
    )
