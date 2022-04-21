/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.rewards

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetDataConversionDetailsResponse(
    val result: GetDataConversionDetailsResult
)

@JsonClass(generateAdapter = true)
data class GetDataConversionDetailsResult(
    val status: String,
    val error: String = ""
) {

    /**
     * There is a possible case, when API returns [CONVERSION_STATUS_FAILED] for
     * data conversion, but depends on some error code values from response we should
     * display successful screen. That's why method naming is 'successful error code'.
     * */
    fun isSuccessfulErrorCode() = arrayOf("E0011", "E0012", "E0013").contains(error)

    fun isNotEnoughDataErrorCode() = arrayOf("E0006", "E0007").contains(error)
}

const val CONVERSION_STATUS_SUCCESS = "S"
const val CONVERSION_STATUS_FAILED = "F"
const val CONVERSION_STATUS_CREATED = "C"
const val CONVERSION_STATUS_QUALIFICATION = "Q"
const val CONVERSION_STATUS_INCREMENT = "I"
const val CONVERSION_STATUS_DECREMENT = "D"
