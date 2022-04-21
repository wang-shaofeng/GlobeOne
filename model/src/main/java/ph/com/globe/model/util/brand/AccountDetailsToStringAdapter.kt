/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.util.brand

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
annotation class StringAsAccountBrand

class StringAsAccountBrandAdapter {

    @ToJson
    fun toJson(@StringAsAccountBrand accountBrand: AccountBrand): String {
        return accountBrand.name
    }

    @FromJson
    @StringAsAccountBrand
    fun fromJson(brand: String): AccountBrand {
        return brand.toAccountBrand()
    }
}

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
annotation class StringAsAccountBrandType

class StringAsAccountBrandTypeAdapter {

    @ToJson
    fun toJson(@StringAsAccountBrandType accountBrandType: AccountBrandType): String {
        return accountBrandType.toString()
    }

    @FromJson
    @StringAsAccountBrandType
    fun fromJson(brandType: String): AccountBrandType {
        return brandType.toAccountBrandType()
    }
}

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
annotation class StringAsAccountSegment

class StringAsAccountSegmentAdapter {

    @ToJson
    fun toJson(@StringAsAccountSegment accountSegment: AccountSegment): String {
        return accountSegment.toString()
    }

    @FromJson
    @StringAsAccountSegment
    fun fromJson(accountSegment: String): AccountSegment {
        return accountSegment.toAccountSegment()
    }
}
