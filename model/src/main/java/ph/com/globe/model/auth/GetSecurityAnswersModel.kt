/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.auth

import com.squareup.moshi.JsonClass

data class GetSecurityAnswersParams(
    val referenceId: String,
)

@JsonClass(generateAdapter = true)
data class GetSecurityAnswersResponse(
    val result: List<SecurityAnswer>
)

@JsonClass(generateAdapter = true)
data class SecurityAnswer(
    val answer: String,
    val questionId: String
)
