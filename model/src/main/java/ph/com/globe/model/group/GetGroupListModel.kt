/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.group

import com.squareup.moshi.JsonClass

data class GetGroupListParams(
    val accountAlias: String,
    val isGroupOwner: Boolean,
    val groupName: String? = null
)

@JsonClass(generateAdapter = true)
data class GetGroupListResponse(
    val result: GetGroupListResult
)

@JsonClass(generateAdapter = true)
data class GetGroupListResult(
    val groups: List<GroupItemJson>
)

@JsonClass(generateAdapter = true)
data class GroupItemJson(
    val groupId: String,
    val groupOwner: String,
    val ownerMobileNumber: String,
    val wallets: List<WalletItemJson>,
    val affliateDate: String,
    val withdrawalDate: String,
    val memberStatus: String,
    val memberLimit: String
)

@JsonClass(generateAdapter = true)
data class WalletItemJson(
    val id: String,
    val ownerMobileNumber: String,
    val name: String,
    val keyword: String,
    val affiliateDate: String,
    val withdrawalDate: String,
    val status: String
)
