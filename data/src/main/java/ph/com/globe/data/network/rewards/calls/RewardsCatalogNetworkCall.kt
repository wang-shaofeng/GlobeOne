/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.rewards.calls

import kotlinx.coroutines.*
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.rewards.RewardsRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.rewards.FetchRewardsCatalogError
import ph.com.globe.model.rewards.*
import ph.com.globe.model.rewards.LoyaltyProgramId.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.onFailure
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class RewardsCatalogNetworkCall @Inject constructor(
    private val rewardsRetrofit: RewardsRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {
    suspend fun execute(): LfResult<List<RewardsCatalogItem>, FetchRewardsCatalogError> =
        withContext(Dispatchers.IO + NonCancellable) {

            // User token is optional. If user doesn't logged in, this call won't include user token.
            val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
                hashMapOf()
            }

            val tmJob = async {
                kotlin.runCatching {
                    rewardsRetrofit.getRewardsCatalog(headers, LOYALTY_ID_TM)
                }.fold(
                    Response<RewardsCatalogResponseModel>::toLfSdkResult,
                    Throwable::toLFSdkResult
                )
            }

            val prepaidJob = async {
                kotlin.runCatching {
                    rewardsRetrofit.getRewardsCatalog(headers, LOYALTY_ID_GLOBE_PREPAID)
                }.fold(
                    Response<RewardsCatalogResponseModel>::toLfSdkResult,
                    Throwable::toLFSdkResult
                )
            }

            val postpaidJob = async {
                kotlin.runCatching {
                    rewardsRetrofit.getRewardsCatalog(headers, LOYALTY_ID_GLOBE_POSTPAID)
                }.fold(
                    Response<RewardsCatalogResponseModel>::toLfSdkResult,
                    Throwable::toLFSdkResult
                )
            }

            val hpwJob = async {
                kotlin.runCatching {
                    rewardsRetrofit.getRewardsCatalog(headers, LOYALTY_ID_HPW)
                }.fold(
                    Response<RewardsCatalogResponseModel>::toLfSdkResult,
                    Throwable::toLFSdkResult
                )
            }

            val gahJob = async {
                kotlin.runCatching {
                    rewardsRetrofit.getRewardsCatalog(headers, LOYALTY_ID_GAH)
                }.fold(
                    Response<RewardsCatalogResponseModel>::toLfSdkResult,
                    Throwable::toLFSdkResult
                )
            }

            val responseTm = tmJob.await().onFailure { logFailedNetworkCall(it) }

            val responsePrepaid = prepaidJob.await().onFailure { logFailedNetworkCall(it) }

            val responsePostpaid = postpaidJob.await().onFailure { logFailedNetworkCall(it) }

            val responseHpw = hpwJob.await().onFailure { logFailedNetworkCall(it) }

            val responseGah = gahJob.await().onFailure { logFailedNetworkCall(it) }

            val list =
                ((responseTm.value?.result?.rewards?.map { LOYALTY_ID_TM to it } ?: emptyList()) +
                        (responsePrepaid.value?.result?.rewards?.map { LOYALTY_ID_GLOBE_PREPAID to it }
                            ?: emptyList()) +
                        (responsePostpaid.value?.result?.rewards?.map { LOYALTY_ID_GLOBE_POSTPAID to it }
                            ?: emptyList()) +
                        (responseHpw.value?.result?.rewards?.map { LOYALTY_ID_HPW to it }
                            ?: emptyList()) +
                        (responseGah.value?.result?.rewards?.map { LOYALTY_ID_GAH to it }
                            ?: emptyList()))
                    .mapNotNull {
                        it.second.toRewardsItem(it.first)
                            .takeIf { it.category != RewardsCategory.NONE }
                    }

            val setOfRewards = list.groupBy { it.id }.mapValues { (_, value) ->
                if (value.size > 1) {
                    val reward = value[0]
                    reward.copy(loyaltyProgramIds = value.map { it.loyaltyProgramIds[0] })
                } else value[0]
            }.values.toList()

            val notSuccess = responseTm.errorOrNull() ?: responsePostpaid.errorOrNull()
            ?: responseHpw.errorOrNull() ?: responsePrepaid.errorOrNull()
            ?: responseGah.errorOrNull()

            return@withContext notSuccess?.let { LfResult.failure(notSuccess.toSpecific(), list) }
                ?: LfResult.success(setOfRewards)
        }

    private fun RewardsCatalogModel.toRewardsItem(loyaltyProgramId: String) =
        RewardsCatalogItem(
            type,
            name,
            description,
            status,
            id,
            pointsCost,
            if (name.startsWith(RAFFLE_NAME))
                RewardsCategory.RAFFLE
            else
                when (category) {
                    "10065" -> RewardsCategory.PROMO // Telco products
                    "10066" -> RewardsCategory.OTHER // Points Conversion
                    "10100" -> RewardsCategory.DONATION // Donation
                    "10101" -> RewardsCategory.PROMO // CLOAD
                    "10164" -> RewardsCategory.OTHER // VOUCHER
                    else -> RewardsCategory.NONE
                },
            listOf(loyaltyProgramId.toLoyaltyProgramIdEnum())
        )

    override val logTag: String = "RewardsCatalogNetworkCall"
}

internal const val LOYALTY_ID_TM = "12602"
internal const val LOYALTY_ID_GLOBE_PREPAID = "12603"
internal const val LOYALTY_ID_GLOBE_POSTPAID = "12641"
internal const val LOYALTY_ID_HPW = "12895"
internal const val LOYALTY_ID_GAH = "12896"
internal const val LOYALTY_ID_NONE = "none"

internal fun String.toLoyaltyProgramIdEnum() =
    when (this) {
        LOYALTY_ID_GLOBE_PREPAID -> PREPAID
        LOYALTY_ID_GLOBE_POSTPAID -> POSTPAID
        LOYALTY_ID_HPW -> HPW
        LOYALTY_ID_TM -> TM
        LOYALTY_ID_GAH -> GAH
        else -> ALL
    }

internal fun LoyaltyProgramId.toLoyaltyId() = when (this) {
    TM -> LOYALTY_ID_TM
    PREPAID -> LOYALTY_ID_GLOBE_PREPAID
    POSTPAID -> LOYALTY_ID_GLOBE_POSTPAID
    HPW -> LOYALTY_ID_HPW
    GAH -> LOYALTY_ID_GAH
    ALL -> LOYALTY_ID_NONE
}

private fun NetworkError.toSpecific(): FetchRewardsCatalogError {
    return FetchRewardsCatalogError.General(GeneralError.Other(this))
}
