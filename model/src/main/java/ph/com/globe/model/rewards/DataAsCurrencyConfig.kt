/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.rewards

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi

data class DataAsCurrencyConfig(
    val expireDate: String,
)

@JsonClass(generateAdapter = true)
internal data class DataAsCurrencyConfigJson(
    val expire_date: String
)

fun String.toDataAsCurrencyConfig(): DataAsCurrencyConfig? {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(DataAsCurrencyConfigJson::class.java)

    val dataAsCurrencyConfigJson = adapter.fromJson(this)

    return dataAsCurrencyConfigJson?.let {
        return DataAsCurrencyConfig(
            it.expire_date
        )
    }
}
