/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.util

import com.squareup.moshi.*

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
annotation class JsonObjectAsString

class JsonObjectToStringJsonAdapter {

    @ToJson
    fun toJson(@JsonObjectAsString s: String): String {
        return s
    }

    @FromJson
    @JsonObjectAsString
    fun fromJson(reader: JsonReader, adapter: JsonAdapter<Any>): String {
        val jsonObject: Any = reader.readJsonValue()!!
        return adapter.toJson(jsonObject)
    }
}
