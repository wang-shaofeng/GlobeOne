/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginJsonRequest(
    val type: String,
    val login: String,
    val merge: Boolean? = null
)
