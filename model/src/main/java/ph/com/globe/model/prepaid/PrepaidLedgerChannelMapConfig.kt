/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.prepaid

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

fun String.toChannelConfigMap(): Map<String, String>? {
    val moshi = Moshi.Builder().build()
    val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
    val adapter: JsonAdapter<Map<String, String>> = moshi.adapter(type)
    return adapter.fromJson(this)
}
