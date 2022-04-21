/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.auth

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterSocialParams(
    var email: String,
    val socialProvider: String,
    val socialToken: String
)

sealed class RegisterSocialResult {
    class RegisterSuccessful(val token: String? = null): RegisterSocialResult()
    class LoginSuccessful(val token: String? = null): RegisterSocialResult()
}

@JsonClass(generateAdapter = true)
data class RegisterSocialRequest(
    val type : String,
    val register: String
)

@JsonClass(generateAdapter = true)
data class RegisterSocialResponse(
    val result: String
)
