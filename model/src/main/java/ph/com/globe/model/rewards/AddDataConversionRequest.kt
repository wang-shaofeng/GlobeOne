/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.rewards

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddDataConversionRequest(
    val referenceId: String,
    val mobileNumber: String,
    val rateId: String,
    val qualificationId: String,
    val amount: Int,
    val createdBy: String
)
