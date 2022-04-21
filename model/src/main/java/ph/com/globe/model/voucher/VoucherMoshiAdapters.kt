/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.voucher

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * [CouponStatus] adapter
 */
class CouponStatusTypeAdapter {
    @ToJson
    fun toJson(identifier: CouponStatus): String {
        return when (identifier) {
            CouponStatus.VISIBLE -> VISIBLE
            CouponStatus.HIDE -> HIDE
        }
    }

    @FromJson
    fun fromJson(esimIdentifierType: String): CouponStatus {
        return when (esimIdentifierType) {
            HIDE -> CouponStatus.HIDE
            else -> CouponStatus.VISIBLE
        }
    }
}

private const val VISIBLE = "I"

private const val HIDE = "E"
