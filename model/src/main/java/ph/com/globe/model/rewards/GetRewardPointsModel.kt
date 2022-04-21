/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.rewards

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetRewardPointsResponse(
    val result: GetRewardsPointsResultObject
)

@JsonClass(generateAdapter = true)
data class GetRewardsPointsResultObject(
    val userIdType: String,
    val userId: String,
    val loyaltyProgramDetails: List<LoyalityProgramDetails>
)

@JsonClass(generateAdapter = true)
data class LoyalityProgramDetails(
    val totalPoints: String,
    val loyaltyPointsDetails: List<LoyaltyPointsDetails>
)

@JsonClass(generateAdapter = true)
data class LoyaltyPointsDetails(
    val type: String,
    val total: String,
    val wallets: List<LoyaltyPointsWallet>
)

@JsonClass(generateAdapter = true)
data class LoyaltyPointsWallet(
    val points: String,
    val expirationDate: String,
)

data class GetRewardPointsModel(
    val total: Float,
    val expiringAmount: String,
    val expirationDate: String
)
