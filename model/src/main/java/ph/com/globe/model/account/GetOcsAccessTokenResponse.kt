/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetOcsAccessTokenResponse(
    val access_token: String,
    val expires_in: Int?,
    val token_type: String?
)
