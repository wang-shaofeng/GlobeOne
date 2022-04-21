/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.profile.response_models

import com.squareup.moshi.JsonClass
import java.io.Serializable

data class GetCustomerDetailsParams(
    val mobileNumber: String,
    val otpReferenceId: String? = null
)

fun GetCustomerDetailsParams.toQueryMap(): Map<String, String> = mapOf(
    "mobileNumber" to mobileNumber
)

@JsonClass(generateAdapter = true)
data class GetCustomerDetailsResponse(
    val result: CustomerDetails
)

@JsonClass(generateAdapter = true)
data class CustomerDetails(
    val customerId: String,
    val customerType: String,
    val customerTypeDescription: String,
    val mobileNumberId: String,
    val customerSubType: String,
    val customerSubTypeDescription: String,
) : Serializable
