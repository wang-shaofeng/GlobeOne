/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.events

interface AnalyticsEvent {

    val eventName: String

    fun prepareParamsBundle(): Map<String, String>
}
