/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.events

class ApiFailureEvent(
    override val eventName: String,
    private val api: String,
    private val error: String
) : AnalyticsEvent {

    override fun prepareParamsBundle(): Map<String, String> =
        mapOf(
            API to api,
            ERROR to error
        )
}
