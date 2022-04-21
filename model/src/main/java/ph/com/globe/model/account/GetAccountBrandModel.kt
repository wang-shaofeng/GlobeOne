/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account

import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.brand.StringAsAccountBrand

data class GetAccountBrandParams(
    val msisdn: String
)

fun GetAccountBrandParams.toQueryMap(): Map<String, String> = mapOf("msisdn" to msisdn)

@JsonClass(generateAdapter = true)
data class GetAccountBrandResponse(
    val result: GetAccountBrandResult
)

@JsonClass(generateAdapter = true)
data class GetAccountBrandResult(
    @StringAsAccountBrand
    val brand: AccountBrand
)
