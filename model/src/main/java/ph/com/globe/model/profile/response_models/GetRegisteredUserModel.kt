/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.profile.response_models

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@JsonClass(generateAdapter = true)
data class GetRegisteredUserResponse(
    val result: GetRegisteredUserResponseResult
)

@JsonClass(generateAdapter = true)
data class GetRegisteredUserResponseResult(
    val emailVerificationDate: String? = null,
    val email: String,
    val firstName: String? = null,
    val middleName: String? = null,
    val lastName: String? = null,
    val nickname: String? = null,
    val suffix: String? = null,
    val birthdate: String? = null,
    val salutation: String? = null,
    val contactNumber: String? = null,
    val address: RegisteredUserAddress? = null
)

@JsonClass(generateAdapter = true)
data class RegisteredUserAddress(
    val province: String? = null,
    val city: String? = null,
    val barangay: String? = null,
    val street: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val postal: String? = null
)

@JsonClass(generateAdapter = true)
data class Province(
    val id: String,
    val prov_code: String,
    val prov_name: String,
    var cities: List<City>? = null
) : Comparable<Province> {
    override fun compareTo(other: Province): Int {
        return this.prov_name.compareTo(other.prov_name)
    }
}

@JsonClass(generateAdapter = true)
data class City(
    val id: Long,
    val prov_code: String,
    val mun_code: String,
    val city_municipality_name: String,
    var barangays: List<Barangay>? = null
) : Comparable<City> {
    override fun compareTo(other: City): Int {
        return this.city_municipality_name.compareTo(other.city_municipality_name)
    }
}

@JsonClass(generateAdapter = true)
data class Barangay(
    val id: Long,
    val mun_code: String,
    val brgy_name: String
) : Comparable<Barangay> {
    override fun compareTo(other: Barangay): Int {
        return this.brgy_name.compareTo(other.brgy_name)
    }
}

fun String.provincesFromJson(): List<Province>? {
    val moshi = Moshi.Builder().build()
    val type = Types.newParameterizedType(MutableList::class.java, Province::class.java)
    val adapter: JsonAdapter<List<Province>> = moshi.adapter(type)
    return adapter.fromJson(this)
}

fun String.citiesFromJson(): List<City>? {
    val moshi = Moshi.Builder().build()
    val type = Types.newParameterizedType(MutableList::class.java, City::class.java)
    val adapter: JsonAdapter<List<City>> = moshi.adapter(type)
    return adapter.fromJson(this)
}

fun String.barangaysFromJson(): List<Barangay>? {
    val moshi = Moshi.Builder().build()
    val type = Types.newParameterizedType(MutableList::class.java, Barangay::class.java)
    val adapter: JsonAdapter<List<Barangay>> = moshi.adapter(type)
    return adapter.fromJson(this)
}

typealias RegisterUserParams = GetRegisteredUserResponseResult

