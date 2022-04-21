/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.group

import com.squareup.moshi.JsonClass

data class AddGroupMemberParams(
    val groupId: String,
    val memberMSISDN: String,
    val accountAlias: String
)

fun AddGroupMemberParams.toNetworkParams() = AddGroupMemberNetworkParams(memberMSISDN, accountAlias)

@JsonClass(generateAdapter = true)
data class AddGroupMemberNetworkParams(
    val memberMSISDN: String,
    val accountAlias: String
)

@JsonClass(generateAdapter = true)
data class AddGroupMemberResponse(
    val result: AddGroupMemberResult
)

@JsonClass(generateAdapter = true)
data class AddGroupMemberResult(
    val mobileNumber: String,
    val affiliateDate: String,
    val addedDate: String,
    val status: String
)
