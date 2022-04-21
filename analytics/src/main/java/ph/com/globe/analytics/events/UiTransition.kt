/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.events

class UiTransition(
    private val sourcePage: String,
    private val destinationPage: String
) : AnalyticsEvent {

    override val eventName = "ui_transition"

    override fun prepareParamsBundle(): Map<String, String> =
        mapOf(
            SOURCE_PAGE to sourcePage,
            DESTINATION_PAGE to destinationPage
        )
}
