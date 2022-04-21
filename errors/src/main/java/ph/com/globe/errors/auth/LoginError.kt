/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.auth

import ph.com.globe.errors.GeneralError

sealed class LoginError {

    class LoginWithThisEmailAlreadyExists(val moreInfo: String? = null) : LoginError()

    class SocialRegisterFailedError(error: RegisterError) : LoginError()

    object InvalidUsernameOrPassword : LoginError()

    object TooManyFailedLogins : LoginError()

    class UserEmailNotVerified(val email: String) : LoginError()

    class UserNotYetRegistered(val email: String) : LoginError()

    data class General(val error: GeneralError) : LoginError()
}
