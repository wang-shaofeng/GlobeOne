/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.auth

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RefreshUserTokenResponse(
    val result: GetUserTokenResult
)

@JsonClass(generateAdapter = true)
data class GetUserTokenResult(
    val userToken: String,
    val expiresIn: Int,
    val tokenType: String
)
