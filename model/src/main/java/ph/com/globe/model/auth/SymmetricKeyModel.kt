/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.auth

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SymmetricKeyResponse(
    val secretKey: String
)
