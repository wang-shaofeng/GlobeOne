/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.payment

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class GetPaymentParams(
    val mobileNumber: String,
    val startDate: String,
    val endDate: String
)

@JsonClass(generateAdapter = true)
data class GetPaymentsResponse(
    val result: GetPaymentsResult
)

@JsonClass(generateAdapter = true)
data class GetPaymentsResult(
    val payments: List<Payment>,
    val token: String
)

@JsonClass(generateAdapter = true)
data class Payment(
    val amount: String,
    val date: String,
    val receiptId: String,
    val loadTime: String,
    val sourceId: String
) : Serializable
