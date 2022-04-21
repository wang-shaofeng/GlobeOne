/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.profile.response_models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetERaffleEntriesResponse(
    val result: GetERaffleEntriesResult
)

@JsonClass(generateAdapter = true)
data class GetERaffleEntriesResult(
    val claimed: Raffle,
    val unclaimed: Raffle
)

@JsonClass(generateAdapter = true)
data class Raffle(
    val entries: List<Entry>,
    val sets: List<RaffleSet>,
    val total: Int
)

@JsonClass(generateAdapter = true)
data class Entry(
    val count: Int,
    val mobileNumber: String
)

@JsonClass(generateAdapter = true)
data class RaffleSet(
    val count: Int,
    val set: String
)
