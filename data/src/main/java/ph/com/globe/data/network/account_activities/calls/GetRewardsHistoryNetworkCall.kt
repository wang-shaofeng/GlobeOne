/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account_activities.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.account_activities.AccountActivitiesRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.account_activities.GetRewardsHistoryError
import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.model.account_activities.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetRewardsHistoryNetworkCall @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val accountActivitiesRetrofit: AccountActivitiesRetrofit
) : HasLogTag {

    suspend fun execute(
        msisdn: String,
        dateFrom: String,
        dateTo: String,
        offset: Int,
        subscriberType: Int
    ): LfResult<AccountRewards, GetRewardsHistoryError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(
                GetRewardsHistoryError.General(GeneralError.NotLoggedIn)
            )
        }.plus("Channel" to "Z").plus("x-api-key" to BuildConfig.X_API_KEY)

        val response = runCatching {
            accountActivitiesRetrofit.getLoyaltySubscribersTransactionHistory(
                headers = headers,
                msisdn = msisdn,
                dateFrom = dateFrom,
                dateTo = dateTo,
                offset = offset,
                subscriberType = subscriberType
            )
        }.fold(Response<AccountRewardsResponseModel>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold({
            logSuccessfulNetworkCall()

            val transactionsWithoutError = it.result.copy(
                transactions = it.result.transactions?.mapNotNull {
                    if (it.errorCode == null || it.errorCode == "null") {
                        it
                    } else null
                }
            )

            val transactions = AccountRewards(
                page = transactionsWithoutError.details,
                accountRewardsTransactions = transactionsWithoutError.transactions?.mapNotNull {
                    val type = when {
                        it.transactionType == "Redemption" -> RewardsTransactionType.RedeemedReward
                        it.transactionType == "Direct redemption" -> RewardsTransactionType.PaidWithPoints(
                            it.partner
                        )
                        it.transactionType == "Send gift" -> RewardsTransactionType.GiftedReward
                        it.transactionType == "Sale" || it.transactionType == "Points Corrections" && (it.totalPoints
                            ?: 0.0) >= 0.0 -> RewardsTransactionType.EarnedPoints
                        it.transactionType == "Points Corrections" && (it.totalPoints
                            ?: 0.0) < 0.0 -> RewardsTransactionType.DeductedPoints
                        it.transactionType == "Points Expiration" -> RewardsTransactionType.ExpiredPoints
                        it.transactionType == "Reversal" -> RewardsTransactionType.RefundedPoints
                        else -> return@mapNotNull null
                    }

                    AccountRewardsTransaction(
                        it.transactionNo,
                        type,
                        it.totalPoints ?: 0.0,
                        it.transactionDate
                    )
                } ?: emptyList()
            )

            LfResult.success(transactions)
        }, {
            logFailedNetworkCall(it)
            LfResult.failure(
                GetRewardsHistoryError.General(
                    GeneralError.Other(
                        it
                    )
                )
            )
        })
    }

    override val logTag: String = "GetLoyaltySubscribersTransactionHistoryNetworkCall"
}
