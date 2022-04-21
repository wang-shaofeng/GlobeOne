/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.events.custom

import ph.com.globe.analytics.events.AnalyticsEvent
import ph.com.globe.analytics.events.TYPE

class AccountRegisteredBySocial(
    private val type: String
) : AnalyticsEvent {

    override val eventName = "account_registered_by_social"

    override fun prepareParamsBundle(): Map<String, String> =
        mapOf(
            TYPE to type
        )
}
