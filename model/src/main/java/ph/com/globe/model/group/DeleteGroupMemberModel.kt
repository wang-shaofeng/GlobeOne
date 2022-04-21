/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.group

import com.squareup.moshi.JsonClass

data class DeleteGroupMemberParams(
    val isGroupOwner: Boolean,
    val groupId: String,
    val accountAlias: String,
    val memberMobileNumber: String? = null
)

fun DeleteGroupMemberParams.toNetworkParams() =
    DeleteGroupMemberNetworkParams(isGroupOwner, accountAlias, memberMobileNumber)

@JsonClass(generateAdapter = true)
data class DeleteGroupMemberNetworkParams(
    val isGroupOwner: Boolean,
    val accountAlias: String,
    val memberMobileNumber: String? = null
)

@JsonClass(generateAdapter = true)
data class DeleteGroupMemberResponse(
    val result: DeleteGroupMemberResult
)

@JsonClass(generateAdapter = true)
data class DeleteGroupMemberResult(
    val mobileNumber: String,
    val affiliateDate: String,
    val removalDate: String,
    val status: String,
)
