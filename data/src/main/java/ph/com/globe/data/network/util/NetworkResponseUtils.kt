/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.util

import com.squareup.moshi.JsonDataException
import okhttp3.Headers
import okhttp3.ResponseBody
import ph.com.globe.data.network.NoInternet
import ph.com.globe.data.network.sharedMoshi
import ph.com.globe.errors.ErrorResponseJsonAdapter
import ph.com.globe.errors.NetworkError
import ph.com.globe.util.LfResult
import retrofit2.Response

private val errorJsonJsonAdapter = ErrorResponseJsonAdapter(sharedMoshi)

internal fun <T> Response<T>.toLfSdkResult(): LfResult<T, NetworkError> =
    if (isSuccessful) {
        checkNotNull(body()) { "Use toEmptyLfSdkResult where empty body is expected" }.let { successBody ->
            LfResult.success<T, NetworkError>(successBody)
        }
    } else {
        checkNotNull(errorBody()) { "If request is not successful than error body can't be null ?!" }.let { errorRawBody ->

            return extractError(errorRawBody, code(), headers())
        }
    }

internal fun <T> Response<T?>.toNullableLfSdkResult(): LfResult<T?, NetworkError> =
    if (isSuccessful) {
        body()?.let {
            LfResult.success(it)
        } ?: LfResult.success<T?, NetworkError>(null)
    } else {
        checkNotNull(errorBody()) { "If request is not successful than error body can't be null ?!" }.let { errorRawBody ->

            return extractError(errorRawBody, code(), headers())
        }
    }

internal fun Response<Unit?>.toEmptyLfSdkResult(): LfResult<Unit, NetworkError> =
    if (isSuccessful) {
        check(body() == null || body() == Unit) { "Use toLfSdkResult where non empty body is expected" }.let {
            LfResult.success(Unit)
        }
    } else {
        checkNotNull(errorBody()) { "If request is not successful than error body can't be null ?!" }.let { errorRawBody ->

            return extractError(errorRawBody, code(), headers())
        }
    }

internal fun <T> extractError(
    errorRawBody: ResponseBody,
    httpStatusCode: Int,
    headers: Headers
): LfResult<T, NetworkError> {

    val rawBodyString = errorRawBody.string()
    val errorJson = runCatching { errorJsonJsonAdapter.fromJson(rawBodyString) }.getOrNull()
    val httpError = NetworkError.Http(httpStatusCode, errorJson, rawBodyString, headers)

    return LfResult.failure(httpError)
}

internal fun <T> Throwable.toLFSdkResult(): LfResult<T, NetworkError> = when (this) {
    is JsonDataException -> LfResult.failure(NetworkError.ParsingError(this))
    is NoInternet -> LfResult.failure(NetworkError.NoInternet)
    else -> LfResult.failure(NetworkError.IOError(this))
}
