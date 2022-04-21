/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils

import android.text.Editable

fun Editable?.formatCountryCodeIfExists(){
    if (!isNullOrBlank()) {
        if (startsWith("+63"))
            replace(0, 3, "0")
        else if ((startsWith("63")))
            replace(0, 2, "0")
        else if(!startsWith("0") && !startsWith("6") && !startsWith("+"))
            insert(0, "0")
    }
}

fun Editable?.formatCountryCodeForBroadband(){
    if (!isNullOrBlank()) {
        if (startsWith("+63"))
            replace(0, 3, "0")
    }
}

fun Editable?.getStringOrNull(): String? {
    return if (!isNullOrEmpty()) {
        this.toString()
    } else null
}
