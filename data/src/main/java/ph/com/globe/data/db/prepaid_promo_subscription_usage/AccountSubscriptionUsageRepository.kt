/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.prepaid_promo_subscription_usage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.DataScope
import ph.com.globe.data.db.needsUpdate
import ph.com.globe.data.db.util.ParameterizedRepoUpdater
import ph.com.globe.data.db.util.ParameterlessRepoUpdater
import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.domain.account.db.AccountSubscriptionUsagesRepo
import ph.com.globe.errors.account.GetOcsAccessTokenError
import ph.com.globe.errors.account.GetPrepaidPromoSubscriptionUsageError
import ph.com.globe.model.account.domain_models.PromoSubscriptionUsage
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageParams
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageRequest
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageResponse
import ph.com.globe.util.LfResult
import javax.inject.Inject

/**
 * Repository for [PromoSubscriptionUsage]. Exposes [getAccountSubscriptionUsages] function, which will try to fetch
 * account subscription usages from [accountSubscriptionUsagesQueryDao], and [checkFreshnessAndUpdate] which check if there are stored subscription usages or
 * the data that is not stale, if it is stale it will update the data via [fetchAccountSubscriptionUsages] function.
 */
@DataScope
class AccountSubscriptionUsageRepository @Inject constructor(
    private val accountSubscriptionUsagesQueryDao: GlobeSubscriptionUsagesQueryDao,
    private val accountDataManager: AccountDataManager,
    private val ocsTokenRepoUpdater: ParameterlessRepoUpdater<String, GetOcsAccessTokenError>,
    private val accountSubscriptionUsagesRepoUpdater: ParameterizedRepoUpdater<GetPrepaidPromoSubscriptionUsageParams, GetPrepaidPromoSubscriptionUsageResponse, GetPrepaidPromoSubscriptionUsageError>
) : AccountSubscriptionUsagesRepo, HasLogTag {

    override suspend fun fetchAccountSubscriptionUsages(request: GetPrepaidPromoSubscriptionUsageRequest): LfResult<Unit, GetPrepaidPromoSubscriptionUsageError> {
        var ocsToken: String? = null
        ocsTokenRepoUpdater.update(accountDataManager::fetchOcsAccessToken) {
            accountDataManager.setOcsToken(it)
            ocsToken = it
        }?.let {
            return LfResult.failure(GetPrepaidPromoSubscriptionUsageError.General((it as GetOcsAccessTokenError.General).error))
        }

        ocsToken?.let { token ->
            accountSubscriptionUsagesRepoUpdater.update(
                GetPrepaidPromoSubscriptionUsageParams(
                    token,
                    request
                ), accountDataManager::getPrepaidPromoSubscriptionUsage
            ) {
                accountSubscriptionUsagesQueryDao.clearInsert(it.toEntity(request.serviceNumber))
            }?.let {
                return LfResult.failure(it)
            }
        }

        return LfResult.success(Unit)
    }

    override suspend fun checkFreshnessAndUpdate(request: GetPrepaidPromoSubscriptionUsageRequest): LfResult<Unit, GetPrepaidPromoSubscriptionUsageError> {
        val accountSubscriptionUsageWithFreshness =
            accountSubscriptionUsagesQueryDao.getFreshness(request.serviceNumber).first()
        if (accountSubscriptionUsageWithFreshness.needsUpdate()) {
            return fetchAccountSubscriptionUsages(request)
        }

        return LfResult.success(Unit)
    }

    override suspend fun getAccountSubscriptionUsages(msisdn: String): Flow<PromoSubscriptionUsage?> =
        accountSubscriptionUsagesQueryDao.getAccountSubscriptionUsages(msisdn)
            .map { it?.toDomain() }

    override suspend fun refreshAccountSubscriptionUsages(msisdn: String) {
        accountSubscriptionUsagesQueryDao.staleRow(msisdn)
    }

    override suspend fun invalidateAccountSubscriptionUsages(msisdn: String) {
        accountSubscriptionUsagesQueryDao.invalidRow(msisdn)
    }

    override suspend fun deleteAllAccountsSubscriptionUsages() {
        accountSubscriptionUsagesQueryDao.deleteAllAccountsSubscriptionUsages()
    }

    override suspend fun deleteMetadata() {
        accountSubscriptionUsagesQueryDao.deleteMetadata()
    }

    override val logTag = "AccountSubscriptionUsageRepository"
}
