/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.feature_activation

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

data class FeatureActivationModel(
    val name: String,
    val isEnabled: Boolean
)

@JsonClass(generateAdapter = true)
internal data class FeatureActivationJson(
    val is_enabled: Boolean
)

fun String.toFeatureActivationConfig(): List<FeatureActivationModel>? {
    val moshi = Moshi.Builder().build()
    val type = Types.newParameterizedType(
        Map::class.java,
        String::class.java,
        FeatureActivationJson::class.java
    )
    val adapter: JsonAdapter<Map<String, FeatureActivationJson>> = moshi.adapter(type)

    return adapter.fromJson(this)?.map { entry ->
        FeatureActivationModel(
            entry.key,
            entry.value.is_enabled
        )
    }
}

const val FEATURE_ACTIVATION_DAC_NAME = "dac"
const val FEATURE_ACTIVATION_CAMPAIGNS_BANNER_NAME = "campaigns_banner"
