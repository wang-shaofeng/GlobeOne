/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.auth

import ph.com.globe.errors.GeneralError

sealed class VerifyOtpError {

    data class OtpCodeIncorrect(val cxsMessageId: String) : VerifyOtpError()

    data class OtpCodeExpired(val cxsMessageId: String) : VerifyOtpError()

    data class OtpCodeAlreadyVerified(val cxsMessageId: String) : VerifyOtpError()

    data class OtpVerifyingMaxAttempt(val cxsMessageId: String) : VerifyOtpError()

    data class General(val error: GeneralError, val cxsMessageId: String) : VerifyOtpError()
}
