/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterJsonRequest(
    val type: String,
    val register: String
)

@JsonClass(generateAdapter = true)
data class RegisterJsonResponse(
    val result: RegisterJsonResult
)

@JsonClass(generateAdapter = true)
data class RegisterJsonResult(
    val userToken: String,
    val expiresIn: Long,
    val tokenType: String
)
