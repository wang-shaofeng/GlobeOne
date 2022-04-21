/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account.domain_models

data class PromoSubscriptionUsage(
    val mainData: List<DataItem>?,
    val appData: List<DataItem>?
)

data class DataItem(
    val skelligCategory: String?,
    val skelligWallet: String,
    val dataRemaining: Int?,
    val dataTotal: Int?,
    val endDate: String,
    val type: String?,
)
