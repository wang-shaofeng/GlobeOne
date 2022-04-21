/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.logger

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import ph.com.globe.analytics.events.AnalyticsEvent

internal class GlobeFirebaseAnalytics(
    context: Context
) : GlobeAnalytics() {

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun logAnalyticsEvent(analyticsEvent: AnalyticsEvent) {
        val params = analyticsEvent.prepareParamsBundle()
        firebaseAnalytics.logEvent(analyticsEvent.eventName, params.toBundle())
    }

}
