/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.auth

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterEmailParams(
    val email: String,
    val password: String,
    val passwordConfirm: String
)

data class RegisterEmailResult(
    val token: String
)
