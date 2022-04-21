/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.groups

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.DataScope
import ph.com.globe.data.db.needsUpdate
import ph.com.globe.data.db.shop.ShopItemsRepository
import ph.com.globe.data.db.util.ParameterizedRepoUpdater
import ph.com.globe.domain.account.db.AccountGroupsRepo
import ph.com.globe.domain.group.GroupDataManager
import ph.com.globe.domain.utils.getMemberRole
import ph.com.globe.errors.group.AccountDetailsGroupsError
import ph.com.globe.errors.group.RetrieveGroupServiceError
import ph.com.globe.errors.group.RetrieveGroupUsageError
import ph.com.globe.errors.group.RetrieveMemberUsageError
import ph.com.globe.errors.shop.GetAllOffersError
import ph.com.globe.model.group.*
import ph.com.globe.model.group.domain_models.AccountDetailsGroups
import ph.com.globe.model.group.domain_models.AccountDetailsGroupsParams
import ph.com.globe.model.shop.formattedForPhilippines
import ph.com.globe.util.*
import javax.inject.Inject

/**
 * Repository for [AccountDetailsGroups]. Exposes [getAccountGroups] function, which will try to fetch
 * account groups from [accountGroupsQueryDao], and [checkFreshnessAndUpdate] which check if there are stored groups or
 * the data that is not stale, if it is stale it will update the data via [fetchAccountGroups] function.
 */
@DataScope
class AccountGroupsRepository @Inject constructor(
    private val accountGroupsQueryDao: GlobeAccountGroupsQueryDao,
    private val groupDataManager: GroupDataManager,
    private val groupServiceRepoUpdater: ParameterizedRepoUpdater<RetrieveGroupServiceParams, RetrieveGroupServiceResponse, RetrieveGroupServiceError>,
    private val groupUsageRepoUpdater: ParameterizedRepoUpdater<RetrieveGroupUsageParams, RetrieveGroupUsageResponse, RetrieveGroupUsageError>,
    private val memberUsageRepoUpdater: ParameterizedRepoUpdater<RetrieveMemberUsageParams, RetrieveMemberUsageResponse, RetrieveMemberUsageError>,
    private val shopItemsRepository: ShopItemsRepository
) : AccountGroupsRepo, HasLogTag {

    override suspend fun fetchAccountGroups(params: AccountDetailsGroupsParams): LfResult<Unit, AccountDetailsGroupsError> =
        shopItemsRepository.checkFreshnessAndUpdate().fold(
            {
                val promosList = shopItemsRepository.getAllOffers(true).first()
                val usageItems = mutableListOf<UsageItemEntity>()
                val skelligWalletCategoryMap = mutableMapOf<String, String>()
                promosList.forEach {
                    if (it.skelligWallet != null && it.skelligCategory != null)
                        skelligWalletCategoryMap[it.skelligWallet ?: ""] = it.skelligCategory ?: ""
                }

                var retrieveGroupServiceResponse: RetrieveGroupServiceResponse? = null

                groupServiceRepoUpdater.update(
                    RetrieveGroupServiceParams(params.accountAlias),
                    groupDataManager::retrieveGroupService
                ) {
                    retrieveGroupServiceResponse = it
                }?.let {
                    return@fold if (it is RetrieveGroupServiceError.MobileNumberNotFound) {
                        LfResult.failure(AccountDetailsGroupsError.MobileNumberNotFound)
                    } else {
                        LfResult.failure(AccountDetailsGroupsError.General((it as RetrieveGroupServiceError.General).error))
                    }
                }

                retrieveGroupServiceResponse?.result?.wallets?.let { list ->
                    for (wallet in list) {
                        if (wallet.ownerMobileNumber.formattedForPhilippines() == params.primaryMsisdn.formattedForPhilippines()) {
                            // we are are retrieving the group usage for group owner
                            groupUsageRepoUpdater.update(
                                RetrieveGroupUsageParams(
                                    params.accountAlias,
                                    wallet.id
                                ), groupDataManager::retrieveGroupUsage
                            ) {
                                with(it.result) {
                                    usageItems.add(
                                        UsageItemEntity(
                                            title = skelligWalletCategoryMap[walletId]
                                                ?: "Group Data",
                                            category = "DATA LEFT",
                                            left = (volumeRemaining ?: "").toIntOrNull() ?: 0,
                                            total = (totalAllocated ?: "").toIntOrNull() ?: 0,
                                            expiration = endDate?.toDateWithTimeZoneOrNull().toFormattedStringOrEmpty(
                                                GlobeDateFormat.GroupDataFormat),
                                            accountNumber = params.primaryMsisdn,
                                            accountName = params.accountAlias,
                                            accountRole = params.primaryMsisdn.getMemberRole(wallet.ownerMobileNumber),
                                            skelligWallet = wallet.id,
                                            skelligCategory = skelligWalletCategoryMap[walletId]
                                                ?: "Group Data",
                                            groupOwnerMobileNumber = wallet.ownerMobileNumber,
                                            apps = null,
                                            used = volumeUsed?.toIntOrNull() ?: 0
                                        )
                                    )
                                }
                            }?.let {
                                return@fold when (it) {
                                    is RetrieveGroupUsageError.MobileNumberNotFound -> {
                                        LfResult.failure(AccountDetailsGroupsError.MobileNumberNotFound)
                                    }
                                    is RetrieveGroupUsageError.SubscriberNotBelongToAnyPool -> {
                                        LfResult.failure(AccountDetailsGroupsError.SubscriberNotBelongToAnyPool)
                                    }
                                    is RetrieveGroupUsageError.GroupNotExist -> {
                                        LfResult.failure(AccountDetailsGroupsError.GroupNotExist)
                                    }
                                    is RetrieveGroupUsageError.WalletNotFound -> {
                                        LfResult.failure(AccountDetailsGroupsError.WalletNotFound)
                                    }
                                    else -> {
                                        LfResult.failure(AccountDetailsGroupsError.General((it as RetrieveGroupUsageError.General).error))
                                    }
                                }
                            }
                        } else {
                            // we are are retrieving the group usage for group member
                            memberUsageRepoUpdater.update(
                                RetrieveMemberUsageParams(
                                    isGroupOwner =
                                    wallet.ownerMobileNumber.contains(
                                        params.primaryMsisdn.removePrefix("0")
                                    ),
                                    memberAccountAlias = params.accountAlias,
                                    keyword = wallet.id,
                                    memberMobileNumber = params.primaryMsisdn,
                                    ownerMobileNumber = wallet.ownerMobileNumber
                                ),
                                groupDataManager::retrieveMemberUsage
                            ) {
                                with(it.result) {
                                    usageItems.add(
                                        UsageItemEntity(
                                            title = skelligWalletCategoryMap[walletId]
                                                ?: "Group Data",
                                            category = "DATA LEFT",
                                            left = volumeRemaining.toIntOrNull() ?: 0,
                                            total = totalAllocated.toIntOrNull() ?: 0,
                                            expiration = endDate.toDateWithTimeZoneOrNull().toFormattedStringOrEmpty(
                                                GlobeDateFormat.GroupDataFormat),
                                            accountNumber = params.primaryMsisdn,
                                            accountName = params.accountAlias,
                                            accountRole = params.primaryMsisdn.getMemberRole(wallet.ownerMobileNumber),
                                            skelligWallet = wallet.id,
                                            skelligCategory = skelligWalletCategoryMap[walletId]
                                                ?: "Group Data",
                                            groupOwnerMobileNumber = wallet.ownerMobileNumber,
                                            apps = null,
                                            used = volumeUsed.toIntOrNull() ?: 0
                                        )
                                    )
                                }
                            }?.let {
                                return@fold LfResult.failure(AccountDetailsGroupsError.General((it as RetrieveMemberUsageError.General).error))
                            }
                        }
                    }
                }

                accountGroupsQueryDao.clearInsert(
                    AccountGroupsEntity(
                        params.primaryMsisdn,
                        usageItems
                    )
                )

                return@fold LfResult.success(Unit)
            }, {
                return@fold LfResult.failure(AccountDetailsGroupsError.General((it as GetAllOffersError.General).error))
            }
        )

    override suspend fun checkFreshnessAndUpdate(params: AccountDetailsGroupsParams): LfResult<Unit, AccountDetailsGroupsError> {
        val accountGroupsWithFreshness =
            accountGroupsQueryDao.getFreshness(params.primaryMsisdn).first()
        if (accountGroupsWithFreshness.needsUpdate()) {
            return fetchAccountGroups(params)
        }

        return LfResult.success(Unit)
    }

    override suspend fun getAccountGroups(primaryMsisdn: String): Flow<AccountDetailsGroups?> =
        accountGroupsQueryDao.getAccountGroups(primaryMsisdn).map { it?.toDomain() }

    override suspend fun refreshAccountGroups(primaryMsisdn: String) {
        accountGroupsQueryDao.staleRow(primaryMsisdn)
    }

    override suspend fun invalidateAccountGroups(primaryMsisdn: String) {
        accountGroupsQueryDao.invalidRow(primaryMsisdn)
    }

    override suspend fun deleteAllAccountsGroups() {
        accountGroupsQueryDao.deleteAllAccountsGroups()
    }

    override suspend fun deleteMetadata() {
        accountGroupsQueryDao.deleteMetadata()
    }

    override val logTag = "AccountGroupsRepository"
}
