/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics

import ph.com.globe.analytics.events.ACTION_CLICK
import ph.com.globe.analytics.events.AnalyticsEvent
import ph.com.globe.analytics.events.NON_EXISTANT
import ph.com.globe.analytics.events.UiAction
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger

/*
 * Interface defines the analytics screen that will have the analytics screen name as requested
 * to be used logging a [UiAction] with [analyticsLogger].
 */
interface AnalyticsScreen {
    val analyticsScreenName: String
    val analyticsLogger: GlobeAnalyticsLogger
}

fun AnalyticsScreen.logUiActionEvent(
    target: String,
    action: String = ACTION_CLICK,
    userEmail: String = NON_EXISTANT,
    additionalParams: Map<String, String> = emptyMap()
) =
    analyticsLogger.logAnalyticsEvent(
        UiAction(
            userEmail,
            analyticsScreenName,
            action,
            target,
            additionalParams
        )
    )

fun AnalyticsScreen.logCustomEvent(analyticsEvent: AnalyticsEvent) {
    analyticsLogger.logAnalyticsEvent(analyticsEvent)
}
