/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.rush

import com.squareup.moshi.JsonClass

data class CreateRushUserParams(
    val token: String,
    val identifier: String
)

@JsonClass(generateAdapter = true)
data class CreateRushUserRequestModel(
    val identifier: String,
    val state: String = "active"
)

@JsonClass(generateAdapter = true)
data class CreateRushUserResponseModel(
    val data: CreateRushUserData
)

@JsonClass(generateAdapter = true)
data class CreateRushUserData(
    val identifier: String
)
