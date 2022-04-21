/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils

import android.content.Context
import ph.com.globe.globeonesuperapp.R

// Possible bucket id values
internal const val SGU = "SGU"
internal const val SOB = "SOB"
internal const val SAU = "SAU"
internal const val SAB = "SAB"
internal const val SGB = "SGB"
internal const val VGU = "VGU"
internal const val VOB = "VOB"
internal const val VAU = "VAU"
internal const val VAB = "VAB"
internal const val VGB = "VGB"

fun getSubscriptionNetworkType(context: Context, bucketId: String): String {
    return when (bucketId) {
        SGU, SGB, VGU, VGB -> context.getString(R.string.account_details_network_type_globe_tm)
        SAU, SAB, VAU, VAB -> context.getString(R.string.account_details_network_type_all)
        SOB, VOB -> context.getString(R.string.account_details_network_type_other)
        else -> ""
    }
}
