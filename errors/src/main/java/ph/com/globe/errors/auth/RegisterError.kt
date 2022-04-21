/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.auth

import ph.com.globe.errors.GeneralError

sealed class RegisterError {

    object EmailAddressAlreadyInUse : RegisterError()

    object InvalidParameters : RegisterError()

    data class AcceptTermsError(val error: AcceptUserAgreementError) : RegisterError()

    data class LoginFailedError(val error: LoginError) : RegisterError()

    data class General(val error: GeneralError) : RegisterError()
}
