/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.events.custom

import ph.com.globe.analytics.events.AnalyticsEvent

class AppRatingAnalytics(
    private val date: String,
    private val time: String,
//    Removed email encryption is added
//    private val user_email: String,
    private val stars: String,
    private val category_id: String,
    private val category_name: String,
    private val review: String,
    private val closing_action: String,
    private val os: String,
    private val app_version: String
): AnalyticsEvent {

    override val eventName = "app_rating"

    override fun prepareParamsBundle(): Map<String, String> = mapOf(
        "date" to date,
        "time" to time,
//        Removed email encryption is added
//        "user_email" to user_email,
        "stars" to stars,
        "category_id" to category_id,
        "category_name" to category_name,
        "review" to review,
        "closing_action" to closing_action,
        "os" to os,
        "app_version" to app_version
    )

}
