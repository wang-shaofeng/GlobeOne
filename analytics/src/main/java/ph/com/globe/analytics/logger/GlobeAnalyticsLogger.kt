/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.logger

import ph.com.globe.analytics.events.AnalyticsEvent
import javax.inject.Inject

class GlobeAnalyticsLogger @Inject constructor(
    private val analyticsList: List<GlobeAnalytics>
) : GlobeAnalytics() {

    override fun logAnalyticsEvent(analyticsEvent: AnalyticsEvent) {
        analyticsList.forEach {
            it.logAnalyticsEvent(analyticsEvent)
        }
    }
}
