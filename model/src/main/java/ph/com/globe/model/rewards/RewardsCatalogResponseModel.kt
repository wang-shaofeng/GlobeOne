/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.model.rewards

import com.squareup.moshi.JsonClass
import ph.com.globe.model.SearchItem
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class RewardsCatalogResponseModel(
    val result: ResultRewardsCatalogModel
)

@JsonClass(generateAdapter = true)
data class ResultRewardsCatalogModel(
    val rewards: List<RewardsCatalogModel>
)

@JsonClass(generateAdapter = true)
data class RewardsCatalogModel(
    val type: String,
    val name: String,
    val description: String?,
    val status: String,
    val id: String,
    val pointsCost: String,
    val category: String
)

data class RewardsCatalogItem(
    val type: String,
    override val name: String,
    val description: String?,
    val status: String,
    val id: String,
    val pointsCost: String,
    val category: RewardsCategory,
    val loyaltyProgramIds: List<LoyaltyProgramId>
) : SearchItem, Serializable

enum class LoyaltyProgramId {
    TM, PREPAID, POSTPAID, HPW, GAH, ALL
}

enum class RewardsCategory {
    NONE,
    RAFFLE,
    OTHER, // cost == 0 -> Freebie, cost != 0 -> Voucher
    DONATION, // Donation
    PROMO; // Telco

    companion object {
        fun toInt(category: RewardsCategory) = when (category) {
            NONE -> 0
            RAFFLE -> 1
            PROMO -> 2
            DONATION -> 3
            OTHER -> 4
        }

        fun toCategory(num: Int) = when (num) {
            0 -> NONE
            1 -> RAFFLE
            2 -> PROMO
            3 -> DONATION
            4 -> OTHER
            else -> NONE
        }
    }
}

const val RAFFLE_NAME = "Chance to win"
const val FREE_GB_NAME = "FREE 2GB data - G Music Fest Live"
