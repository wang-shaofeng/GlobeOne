/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.personalized_campaign

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import ph.com.globe.model.convertDate
import java.util.*

@JsonClass(generateAdapter = true)
data class PersonalizedCampaignConfig(
    val campaign_label: String,
    val campaign_flow: String,
    val campaign_id: String,
    val brand: String,
    val segment: String,
    val applied_brand: List<String>,
    val get_mode: String,
    val avail_mode: String,
    val promo_type: String,
    val custom_attrib: String,
    val start_date: String,
    val end_date: String,
    val banner_URL: String,
    val primaryCTA: String,
    val primaryCTAtype: String,
    val secondaryCTAtype: String
)

fun String.toPersonalizedCampaignConfig(): List<PersonalizedCampaignConfig>? {
    val moshi = Moshi.Builder().build()
    val type =
        Types.newParameterizedType(MutableList::class.java, PersonalizedCampaignConfig::class.java)
    val adapter: JsonAdapter<List<PersonalizedCampaignConfig>> = moshi.adapter(type)

    return adapter.fromJson(this)
}

fun List<PersonalizedCampaignConfig>.filterDateValidity(): List<PersonalizedCampaignConfig> {
    val validCampaigns = mutableListOf<PersonalizedCampaignConfig>()
    forEach { campaign ->
        val startDate = campaign.start_date.convertDate()
        val endDate = campaign.end_date.convertDate()
        val currentDate = Date(System.currentTimeMillis())

        if (currentDate.after(startDate) && currentDate.before(endDate)) {
            validCampaigns.add(campaign)
        }
    }
    return validCampaigns
}
