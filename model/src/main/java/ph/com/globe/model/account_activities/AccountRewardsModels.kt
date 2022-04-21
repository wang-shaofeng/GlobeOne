/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account_activities

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class AccountRewardsResponseModel(
    val result: AccountRewardsResultModel
)

@JsonClass(generateAdapter = true)
data class AccountRewardsResultModel(
    val transactions: List<AccountRewardsModel>?,
    val details: AccountRewardsPageModel?
)

@JsonClass(generateAdapter = true)
data class AccountRewardsModel(
    val transactionNo: Long,
    val transactionDate: String,
    val transactionType: String,
    val status: String,
    val partner: String,
    val totalPoints: Double?,
    val errorCode: String?
)

@JsonClass(generateAdapter = true)
data class AccountRewardsPageModel(
    val fault: String,
    val totalRecordCount: Int,
    val totalRecordCountLimited: Boolean,
    val pageFirstResult: Int
)

data class AccountRewards(
    val accountRewardsTransactions: List<AccountRewardsTransaction>,
    val page: AccountRewardsPageModel?
)

data class AccountRewardsTransaction(
    val transactionNo: Long,
    val rewardsTransactionType: RewardsTransactionType,
    val totalPoints: Double,
    val date: String
) : Serializable

sealed class RewardsTransactionType : Serializable {
    object EarnedPoints : RewardsTransactionType()
    object RedeemedReward : RewardsTransactionType()
    data class PaidWithPoints(val partner: String) : RewardsTransactionType()
    object ExpiredPoints : RewardsTransactionType()
    object RefundedPoints : RewardsTransactionType()
    object GiftedReward : RewardsTransactionType()
    object DeductedPoints : RewardsTransactionType()
}
