/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.rush

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi

@JsonClass(generateAdapter = true)
data class RushRemoteConfigData(
    val campaignLabel: String,
    val isCampaignActive: Boolean,
    val micrositeURLGame: String,
    val micrositeURLVoucher: String,
    val startDate: String,
    val endDate: String
)

fun String.toRushRemoteConfigData(): RushRemoteConfigData? {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(RushRemoteConfigData::class.java)

    return adapter.fromJson(this)
}
