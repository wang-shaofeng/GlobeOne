/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.balance

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CheckBalanceSufficiencyResponse(
    val result: CheckBalanceSufficiencyResult
)

@JsonClass(generateAdapter = true)
data class CheckBalanceSufficiencyResult(
    val sufficient: Boolean
)

data class CheckBalanceSufficiencyParams(
    val msisdn: String,
    val amount: String
)

data class CheckAmaxWalletBalanceSufficiencyParams(
    val amount: String
)
