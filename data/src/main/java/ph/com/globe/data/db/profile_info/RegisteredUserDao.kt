/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.profile_info

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
import ph.com.globe.data.db.profile_info.RegisteredUserEntity.Companion.TABLE_NAME
import javax.inject.Inject
import javax.inject.Named

@Dao
abstract class GlobeRegisteredUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract suspend fun insertRegisteredUser(user: RegisteredUserEntity)

    @Query("DELETE FROM registered_user")
    internal abstract suspend fun deleteRegisteredUser()

    @Synchronized
    @Transaction
    open suspend fun clearInsert(user: RegisteredUserEntity) {
        deleteRegisteredUser()
        insertRegisteredUser(user)
    }

    @Query("SELECT * FROM registered_user")
    internal abstract fun getRegisteredUser(): Flow<RegisteredUserEntity?>

    @Query("SELECT nickname FROM registered_user")
    internal abstract fun getNickname(): Flow<String?>

    @Query("SELECT firstName FROM registered_user")
    internal abstract fun getFirstName(): Flow<String?>
}

@DataScope
class GlobeRegisteredUserQueryDao @Inject constructor(
    private val registeredUserDao: GlobeRegisteredUserDao,
    @Named(ONE_HOUR_FRESHNESS) private val queryTimeFreshness: QueryTimeFreshnessDao
) {

    @Synchronized
    suspend fun clearInsert(user: RegisteredUserEntity) {
        withContext(Dispatchers.IO) {
            registeredUserDao.clearInsert(user)
            queryTimeFreshness.markFreshRow(TABLE_NAME)
        }
    }

    fun getFreshness(): Flow<DataWithFreshnessAndValidity<RegisteredUserEntity>> {
        val freshnessFlow = queryTimeFreshness.getValidityAndFreshness(TABLE_NAME)

        return freshnessFlow.combine(registeredUserDao.getRegisteredUser()) { validityAndFreshness, registeredUser ->
            DataWithFreshnessAndValidity.dataWithFreshnessAndValidity(
                registeredUser,
                validityAndFreshness
            )
        }.flowOn(Dispatchers.IO)
    }

    fun getRegisteredUser(): Flow<RegisteredUserEntity?> = registeredUserDao.getRegisteredUser()

    fun getNickname(): Flow<String?> = registeredUserDao.getNickname()

    fun getFirstName(): Flow<String?> = registeredUserDao.getFirstName()

    @Synchronized
    suspend fun staleRow() = queryTimeFreshness.markStaleRow(TABLE_NAME)

    @Synchronized
    suspend fun invalidRow() = queryTimeFreshness.markInvalidRow(TABLE_NAME)

    @Synchronized
    suspend fun deleteRegisteredUser() = registeredUserDao.deleteRegisteredUser()

    suspend fun deleteMetadata() = queryTimeFreshness.deleteData(TABLE_NAME)
}
