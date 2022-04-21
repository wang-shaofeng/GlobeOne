/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.events.custom

import ph.com.globe.analytics.events.AnalyticsEvent
import ph.com.globe.analytics.events.GAEventCategory

class GACustomEvent(
    val eventCategory: GAEventCategory,
    val params: Map<String, String>
) : AnalyticsEvent {

    override val eventName = when (eventCategory) {
        GAEventCategory.PromoLoanTopUp -> "promo_loan_topup"
        GAEventCategory.Registration -> "registration"
        GAEventCategory.AccountDetails -> "account_details"
        GAEventCategory.AccountSettings -> "account_settings"
        GAEventCategory.LoginLogout -> "log_in_log_out"
        GAEventCategory.Rewards -> "rewards"
    }

    override fun prepareParamsBundle(): Map<String, String> = params
}
