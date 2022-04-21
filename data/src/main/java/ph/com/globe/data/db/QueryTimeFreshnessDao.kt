/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db

import android.os.SystemClock
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ph.com.globe.data.DataScope
import ph.com.globe.data.db.LastQueryWriteTimeEntity.Companion.ROW
import ph.com.globe.data.db.LastQueryWriteTimeEntity.Companion.TABLE
import ph.com.globe.data.db.LastQueryWriteTimeEntity.Companion.TABLE_NAME
import ph.com.globe.data.db.LastQueryWriteTimeEntity.Companion.invalidEntity
import ph.com.globe.data.db.LastQueryWriteTimeEntity.Companion.staleEntity
import ph.com.globe.data.db.util.TimeWithUnitAmount
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@DataScope
class QueryTimeFreshnessDao(
    private val lastQueryWriteTimeDao: RoomLastQueryWriteTimeDao,
    private val timeInMsProvider: DefaultTimeInMsProvider,
    private val freshnessPeriod: TimeWithUnitAmount
) {

    @Synchronized
    suspend fun markFreshRow(tableName: String, row: String = "") {
        lastQueryWriteTimeDao.insert(
            LastQueryWriteTimeEntity(
                tableName, row, timeInMsProvider.currentTimeInMs
            )
        )
    }

    @Synchronized
    suspend fun markStaleRow(tableName: String, row: String = "") {
        lastQueryWriteTimeDao.insert(staleEntity(tableName, row))
    }

    @Synchronized
    suspend fun markInvalidRow(tableName: String, row: String = "") {
        lastQueryWriteTimeDao.insert(invalidEntity(tableName, row))
    }

    fun getValidityAndFreshness(
        tableName: String,
        row: String = ""
    ): Flow<DataValidityAndFreshness> =
        lastQueryWriteTimeDao.getLastQueryWriteFor(tableName, row)
            .map { lastQueryWriteTimeEntity ->

                lastQueryWriteTimeEntity?.let {
                    when {
                        it.timeInMs == 0L -> {
                            DataValidityAndFreshness(isFresh = false, isValid = true)
                        }
                        it.timeInMs < 0L -> {
                            DataValidityAndFreshness(isFresh = true, isValid = false)
                        }
                        else -> {
                            val elapsedTime = (timeInMsProvider.currentTimeInMs - it.timeInMs)
                            val isFresh =
                                elapsedTime > 0L && elapsedTime < freshnessPeriod.getMilliseconds()
                            DataValidityAndFreshness(isFresh = isFresh, isValid = true)
                        }
                    }
                } ?: DataValidityAndFreshness(isFresh = false, isValid = false)
            }

    suspend fun deleteData(tableName: String) {
        lastQueryWriteTimeDao.delete(tableName)
    }
}

interface TimeInMsProvider {
    val currentTimeInMs: Long
}

class DefaultTimeInMsProvider @Inject constructor() : TimeInMsProvider {

    override val currentTimeInMs: Long
        get() = SystemClock.elapsedRealtime()
}

@Dao
abstract class RoomLastQueryWriteTimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entity: LastQueryWriteTimeEntity)

    @Query("SELECT * FROM last_query_write_time WHERE table_name=:tableName AND row_name=:row")
    abstract fun getLastQueryWriteFor(
        tableName: String,
        row: String = ""
    ): Flow<LastQueryWriteTimeEntity?>

    @Query("DELETE FROM last_query_write_time WHERE table_name=:tableName")
    abstract suspend fun delete(tableName: String)
}

/**
 * The entity that contains time when the certain info is saved in the database.
 * [tableName] - name of the table that is saved, for example registered_user or enrolled_accounts
 * [row] - name of the row that is saved, for example every account details has it's own freshness.
 *      Table name is the same and row is used to determinate which account is exactly, msisdn is used as the row value
 */
@Entity(
    tableName = TABLE_NAME,
    primaryKeys = [TABLE, ROW]
)
data class LastQueryWriteTimeEntity(

    @ColumnInfo(name = TABLE)
    val tableName: String,

    @ColumnInfo(name = ROW)
    val row: String,

    val timeInMs: Long
) {

    companion object {

        const val TABLE_NAME = "last_query_write_time"

        const val TABLE = "table_name"
        const val ROW = "row_name"

        @JvmStatic
        fun invalidEntity(tableName: String, row: String) =
            LastQueryWriteTimeEntity(tableName, row, -1)

        @JvmStatic
        fun staleEntity(tableName: String, row: String) =
            LastQueryWriteTimeEntity(tableName, row, 0)
    }

}
