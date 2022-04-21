/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.events

sealed class GAEventCategory {
    object PromoLoanTopUp : GAEventCategory()
    object Registration : GAEventCategory()
    object AccountDetails : GAEventCategory()
    object Rewards : GAEventCategory()
    object AccountSettings : GAEventCategory()
    object LoginLogout : GAEventCategory()
}