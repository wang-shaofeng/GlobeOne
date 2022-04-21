/*
  * Copyright (C) 2021 LotusFlare
  * All Rights Reserved.
  * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
  */

package ph.com.globe.model.shop

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ValidateRetailerRequest(
    val serviceNumber: String
)

@JsonClass(generateAdapter = true)
data class ValidateRetailerResponse(
    val isRetailer: Boolean
)
