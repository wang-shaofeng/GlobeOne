/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.events

class UiAction(
    private val loggedInEmail: String,
    private val page: String,
    private val action: String,
    private val target: String,
    private val additionalParams: Map<String, String> = emptyMap()
) : AnalyticsEvent {

    override val eventName = "ui_action"

    override fun prepareParamsBundle(): Map<String, String> =
        mapOf(
            PAGE_KEY to page,
            ACTION_KEY to action,
            TARGET_KEY to target,
            EMAIL_KEY to loggedInEmail
        ) + additionalParams
}
