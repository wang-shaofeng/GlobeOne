/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.util

import com.squareup.moshi.*
import ph.com.globe.model.banners.CtaModel
import ph.com.globe.model.banners.CtaObjectArray

class CtaObjectArrayCustomAdapter : JsonAdapter<CtaObjectArray>() {

    @FromJson
    override fun fromJson(reader: JsonReader): CtaObjectArray? {
        return if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
            val moshi = Moshi.Builder().build()
            val type = Types.newParameterizedType(MutableList::class.java, CtaModel::class.java)
            val adapter: JsonAdapter<List<CtaModel>> = moshi.adapter(type)
            return adapter.fromJson(reader)?.let { CtaObjectArray(it) }
        } else {
            // if it is object, consider empty
            reader.skipValue()
            null
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: CtaObjectArray?) {
        // noop, never used
    }
}
