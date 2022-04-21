/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.rewards

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class RedeemLoyaltyRewardsRequestModel(
    val rewardDetails: List<RewardDetailsRequestModel>
)

@JsonClass(generateAdapter = true)
data class RewardDetailsRequestModel(
    val type: String,
    val item: String
)

@JsonClass(generateAdapter = true)
data class RedeemLoyaltyRewardsResponseModel(
    val result: RedeemLoyaltyRewardsResult
)

@JsonClass(generateAdapter = true)
data class RedeemLoyaltyRewardsResult(
    val userIdType: String,
    val userId: String,
    val loyaltyPoints: List<String>
): Serializable
