/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.db

import kotlinx.coroutines.flow.Flow
import ph.com.globe.errors.account.GetPrepaidPromoSubscriptionUsageError
import ph.com.globe.model.account.domain_models.PromoSubscriptionUsage
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageParams
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageRequest
import ph.com.globe.util.LfResult

interface AccountSubscriptionUsagesRepo {

    suspend fun fetchAccountSubscriptionUsages(request: GetPrepaidPromoSubscriptionUsageRequest): LfResult<Unit, GetPrepaidPromoSubscriptionUsageError>

    suspend fun checkFreshnessAndUpdate(request: GetPrepaidPromoSubscriptionUsageRequest): LfResult<Unit, GetPrepaidPromoSubscriptionUsageError>

    suspend fun getAccountSubscriptionUsages(msisdn: String): Flow<PromoSubscriptionUsage?>

    suspend fun refreshAccountSubscriptionUsages(msisdn: String)

    suspend fun invalidateAccountSubscriptionUsages(msisdn: String)

    suspend fun deleteAllAccountsSubscriptionUsages()

    suspend fun deleteMetadata()
}
