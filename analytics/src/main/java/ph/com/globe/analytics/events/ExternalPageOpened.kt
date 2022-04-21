/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.events

class ExternalPageOpened(
    private val link: String
) : AnalyticsEvent {
    override val eventName: String
        get() = "external_open_page"

    override fun prepareParamsBundle(): Map<String, String> =
        mapOf(
            LINK to link
        )
}
