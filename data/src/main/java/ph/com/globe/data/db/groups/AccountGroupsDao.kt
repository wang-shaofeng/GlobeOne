/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.groups

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
import ph.com.globe.data.db.groups.AccountGroupsEntity.Companion.TABLE_NAME
import javax.inject.Inject
import javax.inject.Named

@Dao
abstract class GlobeAccountGroupsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract suspend fun insertAccountGroups(accountGroupsEntity: AccountGroupsEntity)

    @Query("DELETE FROM account_groups WHERE primaryMsisdn=:primaryMsisdn")
    internal abstract suspend fun deleteAccountGroups(primaryMsisdn: String)

    @Query("DELETE FROM account_groups")
    abstract suspend fun clearAllAccountGroups()

    @Synchronized
    @Transaction
    open suspend fun clearInsert(accountGroupsEntity: AccountGroupsEntity) {
        deleteAccountGroups(accountGroupsEntity.primaryMsisdn)
        insertAccountGroups(accountGroupsEntity)
    }

    @Query("SELECT * FROM account_groups WHERE primaryMsisdn=:primaryMsisdn")
    internal abstract fun getAccountGroups(primaryMsisdn: String): Flow<AccountGroupsEntity?>
}

@DataScope
class GlobeAccountGroupsQueryDao @Inject constructor(
    private val accountGroupsDao: GlobeAccountGroupsDao,
    @Named(ONE_HOUR_FRESHNESS) private val queryTimeFreshness: QueryTimeFreshnessDao
) {

    @Synchronized
    suspend fun clearInsert(accountGroupsEntity: AccountGroupsEntity) {
        withContext(Dispatchers.IO) {
            accountGroupsDao.clearInsert(accountGroupsEntity)
            queryTimeFreshness.markFreshRow(TABLE_NAME, accountGroupsEntity.primaryMsisdn)
        }
    }

    fun getFreshness(primaryMsisdn: String): Flow<DataWithFreshnessAndValidity<AccountGroupsEntity>> {
        val freshnessFlow = queryTimeFreshness.getValidityAndFreshness(TABLE_NAME, primaryMsisdn)

        return freshnessFlow.combine(accountGroupsDao.getAccountGroups(primaryMsisdn)) { validityAndFreshness, accountGroups ->
            DataWithFreshnessAndValidity.dataWithFreshnessAndValidity(
                accountGroups,
                validityAndFreshness
            )
        }.flowOn(Dispatchers.IO)
    }

    fun getAccountGroups(primaryMsisdn: String): Flow<AccountGroupsEntity?> =
        accountGroupsDao.getAccountGroups(primaryMsisdn)

    @Synchronized
    suspend fun staleRow(primaryMsisdn: String) = queryTimeFreshness.markStaleRow(TABLE_NAME, primaryMsisdn)

    @Synchronized
    suspend fun invalidRow(primaryMsisdn: String) = queryTimeFreshness.markInvalidRow(TABLE_NAME, primaryMsisdn)

    @Synchronized
    suspend fun deleteAllAccountsGroups() = accountGroupsDao.clearAllAccountGroups()

    suspend fun deleteMetadata() = queryTimeFreshness.deleteData(TABLE_NAME)
}
