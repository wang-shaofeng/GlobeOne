/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.events.custom

import ph.com.globe.analytics.events.AnalyticsEvent

object AccountRegisteredByEmail : AnalyticsEvent {

    override val eventName = "account_registered_by_email"

    override fun prepareParamsBundle(): Map<String, String> =
        mapOf()
}
