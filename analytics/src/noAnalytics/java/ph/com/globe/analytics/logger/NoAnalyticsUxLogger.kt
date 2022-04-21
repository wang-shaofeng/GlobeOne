/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.logger

import javax.inject.Inject

/**
 * [UxLogger] for noAnalytics flavor, doesn't log anything.
 */
class NoAnalyticsUxLogger @Inject constructor() : UxLogger {

    override fun dLog(message: String) = Unit

    override fun eLog(exception: Throwable) = Unit
}
