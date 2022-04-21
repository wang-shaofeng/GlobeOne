/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.group

import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.toUrlString

data class RetrieveGroupServiceParams(
    val accountAlias: String
)

fun RetrieveGroupServiceParams.toQueryMap(): Map<String, String> =
    mapOf("accountAlias" to accountAlias.toUrlString())

@JsonClass(generateAdapter = true)
data class RetrieveGroupServiceResponse(
    val result: RetrieveGroupServiceResult
)

@JsonClass(generateAdapter = true)
data class RetrieveGroupServiceResult(
    val groups: List<RetrieveGroupServiceGroupItem>?,
    val wallets: List<RetrieveGroupServiceWalletItem>?
)

@JsonClass(generateAdapter = true)
data class RetrieveGroupServiceGroupItem(
    val ownerMobileNumber: String,
    val members: List<RetrieveGroupServiceMemberItem>
)

@JsonClass(generateAdapter = true)
data class RetrieveGroupServiceMemberItem(
    val mobileNumber: String,
    val allocationId: String?,
    val totalAllocated: String?,
    val unit: String?,
)

@JsonClass(generateAdapter = true)
data class RetrieveGroupServiceWalletItem(
    val id: String,
    val status: String,
    val ownerMobileNumber: String,
    val startDate: String,
    val endDate: String,
)
