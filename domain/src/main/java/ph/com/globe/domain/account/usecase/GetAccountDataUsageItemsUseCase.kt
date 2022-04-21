/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import ph.com.globe.domain.ReposManager
import ph.com.globe.domain.utils.convertDateToGroupDataFormat
import ph.com.globe.errors.account.GetPrepaidPromoSubscriptionUsageError
import ph.com.globe.errors.group.AccountDetailsGroupsError
import ph.com.globe.errors.shop.GetAllOffersError
import ph.com.globe.model.account.domain_models.DataItem
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageRequest
import ph.com.globe.model.group.domain_models.*
import ph.com.globe.model.util.BOOSTER
import ph.com.globe.model.util.FREEBIE
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetAccountDataUsageItemsUseCase @Inject constructor(reposManager: ReposManager) {

    private val shopItemsRepo = reposManager.getShopItemsRepo()
    private val accountGroupsRepo = reposManager.getAccountGroupsRepo()
    private val accountSubscriptionUsagesRepo = reposManager.getSubscriptionUsagesRepo()

    suspend fun execute(params: AccountDetailsGroupsParams): Flow<LfResult<List<UsageItem>, AccountDetailsGroupsError>> =
        shopItemsRepo.checkFreshnessAndUpdate().fold(
            {
                val shopItems = shopItemsRepo.getAllOffers(false).first()
                val usageItems = mutableListOf<UsageItem>()
                accountSubscriptionUsagesRepo.checkFreshnessAndUpdate(
                    GetPrepaidPromoSubscriptionUsageRequest(params.primaryMsisdn)
                ).fold(
                    {
                        val subscriptionUsages =
                            accountSubscriptionUsagesRepo.getAccountSubscriptionUsages(params.primaryMsisdn)
                                .first()
                        val data = mutableListOf<DataItem>()
                        subscriptionUsages?.mainData?.let {
                            data.addAll(it)
                        }
                        subscriptionUsages?.appData?.let {
                            data.addAll(it)
                        }

                        usageItems.addAll(data.sortedBy { if (it.skelligWallet == PR_GOSURF_DVB) 0 else it.skelligWallet[0].code }
                            .map { bucket ->
                                val shopItem =
                                    shopItems.find { it.skelligWallet == bucket.skelligWallet }
                                UsageItem(
                                    if (bucket.skelligWallet == PR_GOSURF_DVB) BUCKET_NAME_ALL_ACCESS_DATA else bucket.skelligCategory
                                        ?: BUCKET_NAME_OTHERS,
                                    category = "",
                                    isUnlimited = with (bucket) { type == UNLIMITED_TYPE || dataRemaining == null || dataTotal == null },
                                    left = bucket.dataRemaining ?: -1,
                                    total = bucket.dataTotal ?: -1,
                                    expiration = bucket.endDate.convertDateToGroupDataFormat(),
                                    accountNumber = params.primaryMsisdn,
                                    accountName = "",
                                    accountRole = shopItem?.let {
                                        when {
                                            it.isFreebie -> FREEBIE
                                            it.isBooster -> BOOSTER
                                            else -> ""
                                        }
                                    } ?: "",
                                    skelligWallet = "",
                                    skelligCategory = "",
                                    groupOwnerMobileNumber = "",
                                    apps = shopItems.find { it.skelligWallet == bucket.skelligWallet }?.applicationService?.apps
                                )
                            })
                    }, {
                        flowOf(
                            LfResult.failure<List<UsageItem>, AccountDetailsGroupsError>(
                                AccountDetailsGroupsError.General((it as GetPrepaidPromoSubscriptionUsageError.General).error)
                            )
                        )
                    }
                )

                accountGroupsRepo.checkFreshnessAndUpdate(params).fold(
                    {
                        val accountDetailsGroups =
                            accountGroupsRepo.getAccountGroups(params.primaryMsisdn).first()
                        accountDetailsGroups?.groups?.let {
                            usageItems.addAll(it)
                        }
                    }, {
                        flowOf(LfResult.failure<List<UsageItem>, AccountDetailsGroupsError>(it))
                    }
                )

                flowOf(LfResult.success(usageItems))
            }, {
                flowOf(LfResult.failure(AccountDetailsGroupsError.General((it as GetAllOffersError.General).error)))
            }
        )
}

private const val UNLIMITED_TYPE = "Unli"
