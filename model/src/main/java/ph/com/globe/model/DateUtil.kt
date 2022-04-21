/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun String.convertDate(): Date? {
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.US).parse(this)
    } catch (e: ParseException) {
        null
    }
}
