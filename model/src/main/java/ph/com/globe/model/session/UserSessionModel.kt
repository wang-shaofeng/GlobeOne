/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.session

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class UserSession(
    val sessionId: Long,
    val startTimeInMillis: Long,
    var pauseTimeInMillis: Long? = null
) : Serializable

fun UserSession.isExpired(): Boolean = when (val pauseTime = this.pauseTimeInMillis) {
    null -> false
    else -> pauseTime < System.currentTimeMillis() - SESSION_INACTIVITY_TIMEOUT_MILLIS
}

// Session inactivity timeout is 30 minutes as per Globe configuration
const val SESSION_INACTIVITY_TIMEOUT_MILLIS = 1000 * 60 * 30
const val SESSION_INACTIVITY_TIMEOUT_MILLIS_TEST = 1000 * 5
