/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors

import com.squareup.moshi.JsonClass
import okhttp3.Headers

sealed class NetworkError : Throwable() {

    data class Http(
        val httpStatusCode: Int,
        val errorResponse: ErrorResponse?,
        val rawBody: String,
        val headers: Headers
    ) : NetworkError()

    data class IOError(
        override val cause: Throwable
    ) : NetworkError()

    data class ParsingError(
        override val cause: Throwable
    ) : NetworkError()

    object UserNotLoggedInError : NetworkError()

    object NoAccessToken : NetworkError()

    object NoOcsToken : NetworkError()

    object InvalidParamsFormat : NetworkError()

    object NoInternet : NetworkError()
}

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    val error: ErrorInfo,
    val moreInfo: String
)

@JsonClass(generateAdapter = true)
data class ErrorInfo(
    val code: String,
    val message: String,
    val details: String,
    val displayMessage: String
)

@JsonClass(generateAdapter = true)
data class ErrorTokenResponse(
    val error: ErrorInfo?,
    val moreInfo: String?,
    val message: String?
)
