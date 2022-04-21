/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.auth

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginEmailParams(
    val email: String,
    val password: String,
    val mergeToken: String? = null,
    val socialProvider: String? = null,
    val merge: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class LoginSocialParams(
    val socialProvider: String,
    val socialToken: String,
    val mergeToken: String? = null,
    val merge: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val result: LoginResponseResult
)

@JsonClass(generateAdapter = true)
data class LoginResponseResult(
    val userToken: String,
    val expiresIn: Int,
    val tokenType: String
)

sealed class LoginSocialResult {
    object SocialRegisterSuccessful : LoginSocialResult()
    data class SocialLoginSuccessful(
        val userToken: String?,
        val email: String?,
        val emailVerified: String?,
        val isNew: Boolean?
    ) : LoginSocialResult()
}

sealed class LoginResult {
    object LoginSuccessful : LoginResult()
}
