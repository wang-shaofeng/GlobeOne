/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun String.parseRushCampaignDate() =
    try {
        SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS", Locale.US).parse(this)?.time
    } catch (e: ParseException){
        null
    }
