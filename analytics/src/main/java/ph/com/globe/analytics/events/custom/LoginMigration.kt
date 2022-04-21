/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.events.custom

import ph.com.globe.analytics.events.AnalyticsEvent
import ph.com.globe.analytics.events.TYPE

object LoginMigration : AnalyticsEvent {

    override val eventName = "login_migration"

    override fun prepareParamsBundle(): Map<String, String> =
        mapOf()
}
