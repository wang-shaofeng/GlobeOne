/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db

class DataWithFreshnessAndValidity<T> private constructor(
    val data: T?,
    val validityAndFreshness: DataValidityAndFreshness
) {

    fun <R> mapData(mapper: (T?) -> R): DataWithFreshnessAndValidity<R> =
        dataWithFreshnessAndValidity(mapper(data), validityAndFreshness)

    companion object {

        @JvmStatic
        fun <T> dataWithFreshnessAndValidity(
            data: T?,
            freshnessAndValidity: DataValidityAndFreshness
        ) =
            DataWithFreshnessAndValidity(data, freshnessAndValidity)

        @JvmStatic
        fun <T> freshData(data: T?): DataWithFreshnessAndValidity<T> {
            val freshnessAndValidity = DataValidityAndFreshness(isFresh = true, isValid = true)
            return dataWithFreshnessAndValidity(data, freshnessAndValidity)
        }

        @JvmStatic
        fun <T> staleData(data: T?): DataWithFreshnessAndValidity<T> {
            val freshnessAndValidity = DataValidityAndFreshness(isFresh = false, isValid = true)
            return dataWithFreshnessAndValidity(data, freshnessAndValidity)
        }

        @JvmStatic
        fun <T> invalidData(data: T?): DataWithFreshnessAndValidity<T> {
            val freshnessAndValidity = DataValidityAndFreshness(isFresh = true, isValid = false)
            return dataWithFreshnessAndValidity(data, freshnessAndValidity)
        }
    }

}

fun DataWithFreshnessAndValidity<*>.needsUpdate() = with(validityAndFreshness) {
    isFresh.not() || isValid.not()
}

fun DataWithFreshnessAndValidity<*>.needsForceUpdate() = validityAndFreshness.isValid.not()

data class DataValidityAndFreshness(
    val isFresh: Boolean,
    val isValid: Boolean
)
