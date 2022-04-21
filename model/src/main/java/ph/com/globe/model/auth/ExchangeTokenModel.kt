/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.auth

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExchangeSocialAccessTokenWithGlobeSocialTokenRequestModel(
    val provider: String,
    val token: String
)

@JsonClass(generateAdapter = true)
data class ExchangeSocialAccessTokenWithGlobeSocialTokenResponseModel(
    val stat: String,
    val token: String
)
