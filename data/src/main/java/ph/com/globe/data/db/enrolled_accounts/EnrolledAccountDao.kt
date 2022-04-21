/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.enrolled_accounts

import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ph.com.globe.data.DataScope
import ph.com.globe.data.db.DataWithFreshnessAndValidity
import ph.com.globe.data.db.QueryTimeFreshnessDao
import ph.com.globe.data.db.di.ONE_DAY_FRESHNESS
import ph.com.globe.data.db.di.ONE_HOUR_FRESHNESS
import ph.com.globe.data.db.enrolled_accounts.EnrolledAccountEntity.Companion.TABLE_NAME
import javax.inject.Inject
import javax.inject.Named

@Dao
abstract class GlobeEnrolledAccountsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract suspend fun insertEnrolledAccounts(enrolledAccounts: List<EnrolledAccountEntity>)

    @Query("DELETE FROM enrolled_accounts")
    internal abstract suspend fun deleteEnrolledAccounts()

    @Query("DELETE FROM enrolled_accounts WHERE primaryMsisdn=:primaryMsisdn")
    internal abstract suspend fun deleteEnrolledAccount(primaryMsisdn: String)

    @Synchronized
    @Transaction
    open suspend fun clearInsert(enrolledAccounts: List<EnrolledAccountEntity>) {
        deleteEnrolledAccounts()
        insertEnrolledAccounts(enrolledAccounts)
    }

    @Query("SELECT * FROM enrolled_accounts")
    internal abstract fun getAllAccounts(): List<EnrolledAccountEntity>
}

@DataScope
class GlobeEnrolledAccountsQueryDao @Inject constructor(
    private val enrolledAccountsDao: GlobeEnrolledAccountsDao,
    @Named(ONE_HOUR_FRESHNESS) private val queryTimeFreshness: QueryTimeFreshnessDao
) {

    @Synchronized
    suspend fun clearInsert(enrolledAccounts: List<EnrolledAccountEntity>) {
        withContext(Dispatchers.IO) {
            enrolledAccountsDao.clearInsert(enrolledAccounts)
            queryTimeFreshness.markFreshRow(TABLE_NAME)
        }
    }

    fun getAllEnrolledAccounts(): Flow<DataWithFreshnessAndValidity<List<EnrolledAccountEntity>>> {
        val freshnessFlow = queryTimeFreshness.getValidityAndFreshness(TABLE_NAME)

        return freshnessFlow.map { validityAndFreshness ->
            DataWithFreshnessAndValidity.dataWithFreshnessAndValidity(
                enrolledAccountsDao.getAllAccounts(),
                validityAndFreshness
            )
        }.flowOn(Dispatchers.IO)
    }

    @Synchronized
    suspend fun staleRow() = queryTimeFreshness.markStaleRow(TABLE_NAME)

    @Synchronized
    suspend fun invalidRow() = queryTimeFreshness.markInvalidRow(TABLE_NAME)

    @Synchronized
    suspend fun deleteEnrolledAccounts() = enrolledAccountsDao.deleteEnrolledAccounts()

    @Synchronized
    suspend fun deleteEnrolledAccount(primaryMsisdn: String) =
        enrolledAccountsDao.deleteEnrolledAccount(primaryMsisdn)

    suspend fun deleteMetadata() = queryTimeFreshness.deleteData(TABLE_NAME)
}
