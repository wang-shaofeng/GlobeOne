/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.auth

import ph.com.globe.errors.GeneralError

sealed class ValidateSecurityAnswersError {
    data class SecurityAnswersInsufficient(val incorrectAnswersIds: List<String>) :
        ValidateSecurityAnswersError()
    object MaxAttemptsReached : ValidateSecurityAnswersError()
    data class General(val error: GeneralError) : ValidateSecurityAnswersError()
}
