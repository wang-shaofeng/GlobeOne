/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.profile.response_models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddCustomerNicknameRequest(
    val nickname: String
)

@JsonClass(generateAdapter = true)
data class AddCustomerNicknameResponse(
    val CxsMessageId: String,
    val CxsRequestDateTime: String,
    val CxsResponseDateTime: String
)
