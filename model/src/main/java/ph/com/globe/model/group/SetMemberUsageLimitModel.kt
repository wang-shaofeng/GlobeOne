/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.group

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SetMemberUsageLimitParams(
    val memberMobileNumber: String,
    val accountAlias: String,
    val keyword: String,
    val groupName: String?,
    val usageLimit: String
)
