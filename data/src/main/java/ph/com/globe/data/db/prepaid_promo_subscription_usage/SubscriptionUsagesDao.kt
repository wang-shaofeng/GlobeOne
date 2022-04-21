/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.prepaid_promo_subscription_usage

import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ph.com.globe.data.DataScope
import ph.com.globe.data.db.DataWithFreshnessAndValidity
import ph.com.globe.data.db.QueryTimeFreshnessDao
import ph.com.globe.data.db.di.ONE_HOUR_FRESHNESS
import ph.com.globe.data.db.prepaid_promo_subscription_usage.PromoSubscriptionUsageEntity.Companion.TABLE_NAME
import javax.inject.Inject
import javax.inject.Named

@Dao
abstract class GlobeSubscriptionUsagesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract suspend fun insertAccountSubscriptionUsages(promoSubscriptionUsageEntity: PromoSubscriptionUsageEntity)

    @Query("DELETE FROM subscription_usages WHERE msisdn=:msisdn")
    internal abstract suspend fun deleteAccountSubscriptionUsages(msisdn: String)

    @Query("DELETE FROM subscription_usages")
    abstract suspend fun clearAllSubscriptionUsages()

    @Synchronized
    @Transaction
    open suspend fun clearInsert(promoSubscriptionUsageEntity: PromoSubscriptionUsageEntity) {
        deleteAccountSubscriptionUsages(promoSubscriptionUsageEntity.msisdn)
        insertAccountSubscriptionUsages(promoSubscriptionUsageEntity)
    }

    @Query("SELECT * FROM subscription_usages WHERE msisdn=:msisdn")
    internal abstract fun getAccountSubscriptionUsages(msisdn: String): Flow<PromoSubscriptionUsageEntity?>
}

@DataScope
class GlobeSubscriptionUsagesQueryDao @Inject constructor(
    private val subscriptionUsagesDao: GlobeSubscriptionUsagesDao,
    @Named(ONE_HOUR_FRESHNESS) private val queryTimeFreshness: QueryTimeFreshnessDao
) {

    @Synchronized
    suspend fun clearInsert(promoSubscriptionUsageEntity: PromoSubscriptionUsageEntity) {
        withContext(Dispatchers.IO) {
            subscriptionUsagesDao.clearInsert(promoSubscriptionUsageEntity)
            queryTimeFreshness.markFreshRow(TABLE_NAME, promoSubscriptionUsageEntity.msisdn)
        }
    }

    fun getFreshness(primaryMsisdn: String): Flow<DataWithFreshnessAndValidity<PromoSubscriptionUsageEntity>> {
        val freshnessFlow = queryTimeFreshness.getValidityAndFreshness(TABLE_NAME, primaryMsisdn)

        return freshnessFlow.combine(
            subscriptionUsagesDao.getAccountSubscriptionUsages(
                primaryMsisdn
            )
        ) { validityAndFreshness, accountGroups ->
            DataWithFreshnessAndValidity.dataWithFreshnessAndValidity(
                accountGroups,
                validityAndFreshness
            )
        }.flowOn(Dispatchers.IO)
    }

    fun getAccountSubscriptionUsages(msisdn: String): Flow<PromoSubscriptionUsageEntity?> =
        subscriptionUsagesDao.getAccountSubscriptionUsages(msisdn)

    @Synchronized
    suspend fun staleRow(msisdn: String) =
        queryTimeFreshness.markStaleRow(TABLE_NAME, msisdn)

    @Synchronized
    suspend fun invalidRow(msisdn: String) =
        queryTimeFreshness.markInvalidRow(TABLE_NAME, msisdn)

    @Synchronized
    suspend fun deleteAllAccountsSubscriptionUsages() =
        subscriptionUsagesDao.clearAllSubscriptionUsages()

    suspend fun deleteMetadata() = queryTimeFreshness.deleteData(TABLE_NAME)
}
