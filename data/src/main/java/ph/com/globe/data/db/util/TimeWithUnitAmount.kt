/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.util

import java.util.concurrent.TimeUnit

/**
 * Class that represents time with its unit amount. The constructor takes time in [TimeUnit.MILLISECONDS]
 * by default.
 */
class TimeWithUnitAmount(
    private val timeAmount: Long,
    private val timeUnit: TimeUnit = TimeUnit.MILLISECONDS
) {

    fun getMilliseconds() =
        timeUnit.toMillis(timeAmount)

    fun getDays() =
        timeUnit.toDays(timeAmount)
}
