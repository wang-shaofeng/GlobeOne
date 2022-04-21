/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.smart_rating

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi

@JsonClass(generateAdapter = true)
data class RatingCondition<T>(
    val enable: Boolean,
    val value: T
)

@JsonClass(generateAdapter = true)
data class PlatformCondition(
    val low_version: RatingCondition<String>,
    val high_version: RatingCondition<String>
)

@JsonClass(generateAdapter = true)
data class ImprovementOptionsInLanguages(
    val en: List<ImprovementOption>
)

@JsonClass(generateAdapter = true)
data class ImprovementOption(
    val id: Int,
    val value: String
)

data class ImprovementOptionUI(
    val id: Int,
    val title: String,
    var applied: Boolean
)

@JsonClass(generateAdapter = true)
data class SmartRatingConditions(
    val android: PlatformCondition,
    val interval: RatingCondition<Int>,
    val onceeachversion: RatingCondition<Boolean>,
    val enrolledaccounts: RatingCondition<Boolean>,
    val firstshown: RatingCondition<Int>,
    val waittime: RatingCondition<Int>,
    val optionID: RatingCondition<Int>,
    val omitted: RatingCondition<Boolean>
)

@JsonClass(generateAdapter = true)
data class SmartRatingConfig(
    val enabled: Boolean,
    val conditions: SmartRatingConditions,
    val options: ImprovementOptionsInLanguages
)

fun String.toSmartRatingConfig(): SmartRatingConfig? {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(SmartRatingConfig::class.java)

    return adapter.fromJson(this)
}

fun ImprovementOption.toImprovementOptionUI() = ImprovementOptionUI(id, value, false)
