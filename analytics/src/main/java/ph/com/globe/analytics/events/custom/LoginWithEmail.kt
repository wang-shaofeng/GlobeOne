/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.events.custom

import ph.com.globe.analytics.events.AnalyticsEvent

object LoginWithEmail : AnalyticsEvent {

    override val eventName = "login_with_email"

    override fun prepareParamsBundle(): Map<String, String> =
        mapOf()
}
