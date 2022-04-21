/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.raffle

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types


@JsonClass(generateAdapter = true)
data class RaffleRemoteConfigModel(
    val name: String,
    val startDate: String,
    val endDate: String,
    val drawDate: String
)

fun String.raffleRemoteConfigModelsFromJson(): List<RaffleRemoteConfigModel>? {
    val moshi = Moshi.Builder().build()
    val type =
        Types.newParameterizedType(MutableList::class.java, RaffleRemoteConfigModel::class.java)
    val adapter: JsonAdapter<List<RaffleRemoteConfigModel>> = moshi.adapter(type)
    return adapter.fromJson(this)
}

const val SET_AB = "Set A and Set B"
