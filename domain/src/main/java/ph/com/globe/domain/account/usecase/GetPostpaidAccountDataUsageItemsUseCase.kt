/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import ph.com.globe.analytics.logger.CompositeUxLogger.dLog
import ph.com.globe.domain.ReposManager
import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.domain.utils.convertDateToGroupDataFormat
import ph.com.globe.errors.group.AccountDetailsGroupsError
import ph.com.globe.model.account.GetPostpaidPromoSubscriptionUsageRequest
import ph.com.globe.model.account.network_models.DataItemJson
import ph.com.globe.model.group.domain_models.*
import ph.com.globe.model.util.BOOSTER
import ph.com.globe.model.util.FREEBIE
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetPostpaidAccountDataUsageItemsUseCase @Inject constructor(
    reposManager: ReposManager,
    private val accountManager: AccountDataManager
) {

    private val shopItemsRepo = reposManager.getShopItemsRepo()
    private val accountGroupsRepo = reposManager.getAccountGroupsRepo()

    suspend fun execute(params: AccountDetailsGroupsParams): Flow<LfResult<List<UsageItem>, AccountDetailsGroupsError>> {
        val usageItems = mutableListOf<UsageItem>()

        accountManager.fetchOcsAccessToken().fold({ token ->
            accountManager.getPostpaidPromoSubscriptionUsage(
                token,
                GetPostpaidPromoSubscriptionUsageRequest(params.primaryMsisdn)
            ).fold({

                // Main data
                it.promoSubscriptionUsage.mainData?.takeIf { it.isNotEmpty() }?.let { buckets ->

                    // Check for unlimited data
                    if (buckets.first().isUnlimitedData()) {
                        usageItems.add(
                            UsageItem(
                                BUCKET_NAME_MAIN_ALL_ACCESS_DATA, isUnlimited = true
                            )
                        )

                        // Add only one unlimited bucket and then return
                        // Note: Group data and app data still possible
                        return@let

                    } else {

                        // Main all access data
                        buckets.filter { it.isMainAllAccessData() }.let { mainData ->
                            if (mainData.isNotEmpty()) {
                                var remaining = 0
                                var total = 0
                                mainData.forEach {
                                    remaining += it.dataRemaining ?: 0
                                    total += it.dataTotal ?: 0
                                }
                                usageItems.add(
                                    UsageItem(
                                        BUCKET_NAME_MAIN_ALL_ACCESS_DATA,
                                        left = remaining,
                                        total = total,
                                        accountNumber = params.primaryMsisdn
                                    )
                                )
                            }
                        }

                        // One-time all access data
                        buckets.filter { it.isOneTimeAllAccessData() }.let { oneTimeData ->
                            if (oneTimeData.isNotEmpty()) {
                                var remaining = 0
                                var total = 0
                                oneTimeData.forEach {
                                    remaining += it.dataRemaining ?: 0
                                    total += it.dataTotal ?: 0
                                }
                                usageItems.add(
                                    UsageItem(
                                        BUCKET_NAME_ONE_TIME_ALL_ACCESS,
                                        left = remaining,
                                        total = total,
                                        accountNumber = params.primaryMsisdn,
                                        addOnData = true,
                                        addOnDataType = DATA_TYPE_ONE_TIME_ACCESS,
                                        includedPromos = oneTimeData.getIncludedPromos()
                                    )
                                )
                            }
                        }

                        // Recurring all-access data
                        buckets.filter { it.isRecurringAllAccessData() }.let { recurringData ->
                            if (recurringData.isNotEmpty()) {
                                var remaining = 0
                                var total = 0
                                recurringData.forEach {
                                    remaining += it.dataRemaining ?: 0
                                    total += it.dataTotal ?: 0
                                }
                                usageItems.add(
                                    UsageItem(
                                        BUCKET_NAME_RECURRING_ALL_ACCESS,
                                        left = remaining,
                                        total = total,
                                        accountNumber = params.primaryMsisdn,
                                        addOnData = true,
                                        addOnDataType = DATA_TYPE_RECURRING_ACCESS,
                                        includedPromos = recurringData.getIncludedPromos()
                                    )
                                )
                            }
                        }
                    }
                }

                // Group data
                accountGroupsRepo.checkFreshnessAndUpdate(params).fold(
                    {
                        val accountDetailsGroups =
                            accountGroupsRepo.getAccountGroups(params.primaryMsisdn).first()
                        accountDetailsGroups?.groups?.let {
                            usageItems.addAll(it)
                        }
                    }, {
                        dLog("Fetching postpaid group data failure")
                    }
                )

                // App data
                it.promoSubscriptionUsage.appData?.let { appDataItems ->
                    shopItemsRepo.checkFreshnessAndUpdate().fold({
                        val shopItems = shopItemsRepo.getAllOffers(false).first()

                        usageItems.addAll(appDataItems.sortedBy { if (it.skelligWallet == PR_GOSURF_DVB) 0 else it.skelligWallet[0].code }
                            .map { bucket ->
                                val shopItem =
                                    shopItems.find { it.skelligWallet == bucket.skelligWallet }
                                UsageItem(
                                    if (bucket.skelligWallet == PR_GOSURF_DVB) BUCKET_NAME_ALL_ACCESS_DATA else bucket.skelligCategory
                                        ?: BUCKET_NAME_OTHERS,
                                    category = "",
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
                        dLog("Fetching catalog offers failure")
                    })
                }
            }, {
                dLog("Fetching postpaid promo subscription usage failure")
            })
        }, {
            dLog("Fetching OSC access token failure")
        })

        return flowOf(LfResult.success(usageItems))
    }
}

private fun DataItemJson.isUnlimitedData() =
    type == UNLIMITED_TYPE && skelligCategory?.contains(ADD_ON_SKELLIG_CATEGORY) == false

private fun DataItemJson.isMainAllAccessData() =
    !(isOneTimeAllAccessData() || isRecurringAllAccessData())

private fun DataItemJson.isOneTimeAllAccessData() =
    skelligCategory == ADD_ON_SKELLIG_CATEGORY && skelligWallet.endsWith(ONE_TIME_CHARGE_SUFFIX)

private fun DataItemJson.isRecurringAllAccessData() =
    skelligCategory == ADD_ON_SKELLIG_CATEGORY && !skelligWallet.endsWith(ONE_TIME_CHARGE_SUFFIX)

private fun List<DataItemJson>.getIncludedPromos(): List<UsagePromo> {
    return map { dataItem ->
        UsagePromo(
            dataItem.details.firstOrNull()?.promoName ?: "",
            dataItem.dataRemaining ?: 0,
            dataItem.dataTotal ?: 0,
            dataItem.endDate.convertDateToGroupDataFormat()
        )
    }.filter { it.dataTotal > 0 }
}

private const val UNLIMITED_TYPE = "Unli"
private const val ADD_ON_SKELLIG_CATEGORY = "Add-on Data"
private const val ONE_TIME_CHARGE_SUFFIX = "_OC"
