/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.profile.response_models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class GetCustomerInterestsResponse(
    val results: List<String>
)
