/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.navigation

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ph.com.globe.globeonesuperapp.utils.navigation.keys.ScreenKey

/**
 * The [ScreenKey] to be used with [NavHostFragmentScreenChanger] and [NavHostFragmentTransactionHelper].
 */
@Parcelize
data class NavHostFragmentKey(
    val graphId: Int
) : ScreenKey(graphId.toLong()), Parcelable {

    val fragmentTag get() = "navhost#$graphId"
}
