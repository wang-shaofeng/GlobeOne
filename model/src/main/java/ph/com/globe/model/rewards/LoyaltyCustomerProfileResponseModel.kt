/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.rewards

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoyaltyCustomerProfileResponseModel(
    val result: LoyaltyCustomerProfileResultModel
)

@JsonClass(generateAdapter = true)
data class LoyaltyCustomerProfileResultModel(
    val loyaltyProgramId: String
)

data class LoyaltyCustomerProfileModel(
    val loyaltyProgramId: LoyaltyProgramId
)
