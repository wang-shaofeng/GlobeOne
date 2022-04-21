/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.profile.response_models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerifyEmailModel(
    val verification: String,
    val verificationType: String = "registration"
)

@JsonClass(generateAdapter = true)
data class VerifyEmailCheckJson(
    val email: String,
    val code: String
)

@JsonClass(generateAdapter = true)
data class VerifyEmailResponse(
    val result: VerifyEmailJsonResult
)

@JsonClass(generateAdapter = true)
data class VerifyEmailJsonResult(
    val userToken: String,
    val expiresIn: Long,
    val tokenType: String
)
