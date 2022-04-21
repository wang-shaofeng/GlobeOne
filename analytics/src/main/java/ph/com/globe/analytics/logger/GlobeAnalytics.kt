/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.logger

import android.os.Bundle
import ph.com.globe.analytics.events.AnalyticsEvent
import java.util.*

abstract class GlobeAnalytics {

    abstract fun logAnalyticsEvent(analyticsEvent: AnalyticsEvent)
}

fun Map<String, String>.toBundle(): Bundle =
    Bundle().apply {
        entries.forEach { entry ->
            putString(entry.key, entry.value)
        }
    }
