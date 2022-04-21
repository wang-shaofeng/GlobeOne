/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.auth

/**
 * Enum class representing possible values login status.
 */
enum class LoginStatus(private val code: Int) {
    /**
     * Login is done via verified email.
     */
    VERIFIED(0),

    /**
     * Login is done via unverified email.
     */
    UNVERIFIED(1),

    /**
     * User is not logged in.
     */
    NOT_LOGGED_IN(2)
}

fun Int.toEmailVerificationStatus(): LoginStatus =
    when (this) {

        0 -> LoginStatus.VERIFIED

        1 -> LoginStatus.UNVERIFIED

        else -> LoginStatus.NOT_LOGGED_IN
    }

fun LoginStatus.toInteger(): Int =
    when (this) {

        LoginStatus.VERIFIED -> 0

        LoginStatus.UNVERIFIED -> 1

        LoginStatus.NOT_LOGGED_IN -> 2
    }
