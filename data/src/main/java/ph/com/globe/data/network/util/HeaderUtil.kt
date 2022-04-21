/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.util

import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.NetworkError.UserNotLoggedInError
import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold

internal fun TokenRepository.createAuthenticatedHeader(): LfResult<Map<String, String>, NetworkError> {
    val userToken = getUserToken().fold(
        {
            it
        },
        {
            return LfResult.failure(UserNotLoggedInError)
        }
    )

    return LfResult.success(
        mapOf(
            USER_TOKEN to "Bearer $userToken"
        )
    )
}

internal fun TokenRepository.createHeaderWithSessionCredentials(): Map<String, String> =
    mapOf(
        "Session-Credentials" to "Basic ${BuildConfig.SESSION_CREDENTIALS}"
    )

internal fun TokenRepository.createHeaderWithContentType(): Map<String, String> =
    mapOf(
        "Content-Type" to "application/json"
    )

internal suspend fun TokenRepository.createHeaderWithReferenceId(referenceId: String?, verificationType: String? = null): LfResult<Map<String, String>, NetworkError> {
    if (referenceId.isNullOrEmpty()) {
        return getUserToken().fold(
            {
                LfResult.success(
                    // if the referenceId is not existent we are trying to use a userToken instead (Globe API spec logic)
                    mapOf("User-Token" to "Bearer $it")
                )
            },
            {
                return LfResult.failure(UserNotLoggedInError)
            }
        )
    } else {
        val referenceIdKey: String = when(verificationType) {
            VERIFICATION_TYPE_OTP -> REFERENCE_ID_HEADER_KEY_OTP
            VERIFICATION_TYPE_SECURITY_QUESTIONS -> REFERENCE_ID_HEADER_KEY_SECURITY_QUESTIONS
            VERIFICATION_TYPE_SIM_SERIAL -> REFERENCE_ID_HEADER_KEY_SIM_SERIAL
            else -> REFERENCE_ID_HEADER_KEY_OTP
        }
        return LfResult.success(
            // if the referenceId is existent we don't need a userToken
            mapOf(referenceIdKey to referenceId)
        )
    }
}

internal suspend fun TokenRepository.createAuthenticatedHeaderWithReferenceId(referenceId: String, verificationType: String? = null): LfResult<Map<String, String>, NetworkError> {
        return getUserToken().fold(
            {
                val referenceIdKey: String = when(verificationType) {
                    VERIFICATION_TYPE_OTP -> REFERENCE_ID_HEADER_KEY_OTP
                    VERIFICATION_TYPE_SECURITY_QUESTIONS -> REFERENCE_ID_HEADER_KEY_SECURITY_QUESTIONS
                    VERIFICATION_TYPE_SIM_SERIAL -> REFERENCE_ID_HEADER_KEY_SIM_SERIAL
                    else -> REFERENCE_ID_HEADER_KEY_OTP
                }
                return LfResult.success(
                    // if the referenceId is not existent we are trying to use a userToken instead (Globe API spec logic)
                    mapOf("User-Token" to "Bearer $it", referenceIdKey to referenceId)
                )
            },
            {
                return LfResult.failure(UserNotLoggedInError)
            }
        )
}

internal fun TokenRepository.createHeaderForPaymentSession(): Map<String, String> =
    this.createHeaderWithContentType().let {
        if (BuildConfig.FLAVOR_servers == "prod")
            return@let it.plus("Payment-Authorization" to "Basic ${BuildConfig.PAYMENT_AUTHORIZATION}")
        return@let it
    }

internal fun TokenRepository.createHeaderForAuth(): Map<String, String> =
    mapOf(
        "Content-Type" to "application/json",
        "Channel" to "superapp",
        "Platform" to "app"
    )

internal fun createHeaderForOcsAuth(): Map<String, String> =
    mapOf(
        "Authorization" to BuildConfig.OCS_AUTHORIZATION,
        "Content-Type" to "application/json",
        "g-channel" to BuildConfig.OCS_G_CHANNEL
    )

internal fun TokenRepository.createAuthenticatedHeaderForGroup(): LfResult<Map<String, String>, NetworkError> {
    val userToken = getUserToken().fold(
        {
            it
        },
        {
            return LfResult.failure(UserNotLoggedInError)
        }
    )

    return LfResult.success(
        mapOf(
            USER_TOKEN to "Bearer $userToken",
            "Source" to "CXS"
        )
    )
}

internal const val USER_TOKEN = "User-Token"

// verification types
const val VERIFICATION_TYPE_OTP = "OTP"
const val VERIFICATION_TYPE_SECURITY_QUESTIONS = "SecurityQuestions"
const val VERIFICATION_TYPE_SIM_SERIAL = "SimSerialPairing"

// referenceId header field name-
const val REFERENCE_ID_HEADER_KEY_OTP = "OTPReferenceId"
const val REFERENCE_ID_HEADER_KEY_SECURITY_QUESTIONS = "SQReferenceId"
const val REFERENCE_ID_HEADER_KEY_SIM_SERIAL= "SimReferenceId"
