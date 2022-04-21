/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.events.custom

import ph.com.globe.analytics.events.AnalyticsEvent

class ConversionEvent(
    val params: Map<String, String>
) : AnalyticsEvent {

    override val eventName = "conversion_click"

    override fun prepareParamsBundle(): Map<String, String> = params
}
