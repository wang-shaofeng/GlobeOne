/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.payment

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetPaymentMethodResponse(
    val result: CreditCards
)

@JsonClass(generateAdapter = true)
data class CreditCards(
    val creditCards: List<CreditCardModel>
)

@JsonClass(generateAdapter = true)
data class CreditCardModel(
    val expiryMonth: String,
    val expiryYear: String,
    val cardSummary: String,
    val cardReference: String
)

data class DeletePaymentMethodParams(
    val cardReference: String
)

data class LinkingGCashAccountParams(
    val accountAlias: String,
    val referenceId: String
)

@JsonClass(generateAdapter = true)
data class LinkingGCashAccountRequest(
    val accountAlias: String
)
