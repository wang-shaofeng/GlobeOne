/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.date

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.toFormattedStringOrEmpty
import java.util.*

sealed class DateFilter() : Parcelable {
    abstract fun calculateDateRange(): List<DateRange>
    abstract fun getMonths(): Int
    abstract fun startDate(): String

    protected fun instantiateCalendarForCurrentDate(): Calendar = Calendar.getInstance().also {
        it.timeInMillis = System.currentTimeMillis()
        it.set(Calendar.HOUR, 0)
        it.set(Calendar.MINUTE, 0)
        it.set(Calendar.SECOND, 0)
        it.set(Calendar.MILLISECOND, 0)
    }

    @Parcelize
    data class Yesterday(private val buffer: Int) : DateFilter() {
        override fun calculateDateRange(): List<DateRange> {
            val dateTo = instantiateCalendarForCurrentDate().also {
                it.add(Calendar.DAY_OF_MONTH, buffer)
            }.timeInMillis

            val dateFrom = instantiateCalendarForCurrentDate().also {
                it.timeInMillis = System.currentTimeMillis()
                it.add(Calendar.DAY_OF_MONTH, -1)
            }.timeInMillis

            return listOf(
                DateRange(
                    dateFrom.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601),
                    dateTo.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
                )
            )
        }

        override fun getMonths() = 0

        override fun startDate() = instantiateCalendarForCurrentDate().also {
            it.timeInMillis = System.currentTimeMillis()
            it.add(Calendar.DAY_OF_MONTH, -1)
        }.timeInMillis.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
    }

    @Parcelize
    data class Last2Days(private val buffer: Int) : DateFilter() {
        override fun calculateDateRange(): List<DateRange> {
            val dateTo = instantiateCalendarForCurrentDate().also {
                it.add(Calendar.DAY_OF_MONTH, buffer)
            }.timeInMillis

            val dateFrom = instantiateCalendarForCurrentDate().also {
                it.timeInMillis = System.currentTimeMillis()
                it.add(Calendar.DAY_OF_MONTH, -2)
            }.timeInMillis

            return listOf(
                DateRange(
                    dateFrom.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601),
                    dateTo.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
                )
            )
        }

        override fun getMonths() = 0

        override fun startDate() = instantiateCalendarForCurrentDate().also {
            it.timeInMillis = System.currentTimeMillis()
            it.add(Calendar.DAY_OF_MONTH, -2)
        }.timeInMillis.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
    }

    @Parcelize
    data class Last3Days(private val buffer: Int) : DateFilter() {
        override fun calculateDateRange(): List<DateRange> {
            val dateTo = instantiateCalendarForCurrentDate().also {
                it.add(Calendar.DAY_OF_MONTH, buffer)
            }.timeInMillis

            val dateFrom = instantiateCalendarForCurrentDate().also {
                it.timeInMillis = System.currentTimeMillis()
                it.add(Calendar.DAY_OF_MONTH, -3)
            }.timeInMillis

            return listOf(
                DateRange(
                    dateFrom.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601),
                    dateTo.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
                )
            )
        }

        override fun getMonths() = 0

        override fun startDate() = instantiateCalendarForCurrentDate().also {
            it.timeInMillis = System.currentTimeMillis()
            it.add(Calendar.DAY_OF_MONTH, -3)
        }.timeInMillis.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
    }

    @Parcelize
    data class Last7Days(private val buffer: Int) : DateFilter() {
        override fun calculateDateRange(): List<DateRange> {
            val dateTo = instantiateCalendarForCurrentDate().also {
                it.add(Calendar.DAY_OF_MONTH, buffer)
            }.timeInMillis

            val dateFrom = instantiateCalendarForCurrentDate().also {
                it.timeInMillis = System.currentTimeMillis()
                it.add(Calendar.DAY_OF_MONTH, -7)
            }.timeInMillis

            return listOf(
                DateRange(
                    dateFrom.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601),
                    dateTo.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
                )
            )
        }

        override fun getMonths() = 0

        override fun startDate() = instantiateCalendarForCurrentDate().also {
            it.timeInMillis = System.currentTimeMillis()
            it.add(Calendar.DAY_OF_MONTH, -7)
        }.timeInMillis.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
    }

    @Parcelize
    data class Last30Days(private val buffer: Int) : DateFilter() {
        override fun calculateDateRange(): List<DateRange> {
            val dateTo1 = instantiateCalendarForCurrentDate().also {
                it.add(Calendar.DAY_OF_MONTH, buffer)
            }.timeInMillis

            val dateFrom1 = instantiateCalendarForCurrentDate().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_MONTH, -10)
            }.timeInMillis

            val dateFrom2 = instantiateCalendarForCurrentDate().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_MONTH, -20)
            }.timeInMillis

            val dateFrom3 = instantiateCalendarForCurrentDate().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_MONTH, -30)
            }.timeInMillis

            return listOf(
                DateRange(
                    dateFrom1.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601),
                    dateTo1.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
                ),
                DateRange(
                    dateFrom2.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601),
                    dateFrom1.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
                ),
                DateRange(
                    dateFrom3.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601),
                    dateFrom2.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
                ),
            )
        }

        override fun getMonths() = 1

        override fun startDate() = instantiateCalendarForCurrentDate().also {
            it.timeInMillis = System.currentTimeMillis()
            it.add(Calendar.DAY_OF_MONTH, -30)
        }.timeInMillis.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
    }

    @Parcelize
    object Last3Months : DateFilter() {
        override fun calculateDateRange(): List<DateRange> {

            val dateFrom = instantiateCalendarForCurrentDate().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.MONTH, -3)
            }.timeInMillis

            val dateTo = instantiateCalendarForCurrentDate().timeInMillis

            return listOf(
                DateRange(
                    dateFrom.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601),
                    dateTo.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
                )
            )
        }

        override fun getMonths() = 3

        override fun startDate() = instantiateCalendarForCurrentDate().also {
            it.timeInMillis = System.currentTimeMillis()
            it.add(Calendar.MONTH, -3)
        }.timeInMillis.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
    }

    @Parcelize
    object Last6Months : DateFilter() {
        override fun calculateDateRange(): List<DateRange> {

            val dateFrom = instantiateCalendarForCurrentDate().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.MONTH, -6)
            }.timeInMillis

            val dateTo = instantiateCalendarForCurrentDate().timeInMillis

            return listOf(
                DateRange(
                    dateFrom.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601),
                    dateTo.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
                )
            )
        }

        override fun getMonths() = 6

        override fun startDate() = instantiateCalendarForCurrentDate().also {
            it.timeInMillis = System.currentTimeMillis()
            it.add(Calendar.MONTH, -6)
        }.timeInMillis.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
    }

    @Parcelize
    object Last12Months : DateFilter() {
        override fun calculateDateRange(): List<DateRange> {

            val dateFrom = instantiateCalendarForCurrentDate().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.MONTH, -12)
            }.timeInMillis

            val dateTo = instantiateCalendarForCurrentDate().timeInMillis

            return listOf(
                DateRange(
                    dateFrom.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601),
                    dateTo.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
                )
            )
        }

        override fun getMonths() = 12

        override fun startDate() = instantiateCalendarForCurrentDate().also {
            it.timeInMillis = System.currentTimeMillis()
            it.add(Calendar.MONTH, -12)
        }.timeInMillis.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
    }

    @Parcelize
    object Last24Months : DateFilter() {
        override fun calculateDateRange(): List<DateRange> {

            val dateFrom = instantiateCalendarForCurrentDate().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.MONTH, -24)
            }.timeInMillis

            val dateTo = instantiateCalendarForCurrentDate().timeInMillis

            return listOf(
                DateRange(
                    dateFrom.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601),
                    dateTo.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
                )
            )
        }

        override fun getMonths() = 24

        override fun startDate() = instantiateCalendarForCurrentDate().also {
            it.timeInMillis = System.currentTimeMillis()
            it.add(Calendar.MONTH, -24)
        }.timeInMillis.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
    }

    data class DateRange(val dateFrom: String, val dateTo: String)
}

const val MINUS_ONE_DAY = -1
const val MINUS_TWO_DAY = -2
