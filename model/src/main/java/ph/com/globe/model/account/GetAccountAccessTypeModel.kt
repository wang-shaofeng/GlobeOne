package ph.com.globe.model.account

import com.squareup.moshi.JsonClass

data class GetAccountAccessTypeParams(
    val serviceNumber: String
)

fun GetAccountAccessTypeParams.toQueryMap() =
    mapOf("serviceNumber" to serviceNumber)

@JsonClass(generateAdapter = true)
data class GetAccountAccessTypeResponse(
    val result: GetAccountAccessTypeResult
)

@JsonClass(generateAdapter = true)
data class GetAccountAccessTypeResult(
    val username: String,
    val accessType: String,
    val accessSubType: String,
    val dataPolicyCode: String
)

fun GetAccountAccessTypeResult.isUnlimitedData() =
    dataPolicyCode.startsWith(UNLIMITED_PREFIX, ignoreCase = true)

private const val UNLIMITED_PREFIX = "unli"
