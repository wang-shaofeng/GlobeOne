/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.rush

import com.squareup.moshi.JsonClass

data class GetRushUserAccessTokenParams(
    val identifier: String
)

@JsonClass(generateAdapter = true)
data class GetRushAccessTokenResponseModel(
    val access_token: String,
    val expires_in: Long,
    val created_at: Long?
)
