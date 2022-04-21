/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils

import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Validates if the provided password conforms to the established policy.
 */
class PasswordValidator @Inject constructor() {

    fun isValid(password: String): Boolean {
        val matcher = VALID_PASSWORD_REGEX.matcher(password)
        return matcher.matches()
    }

    companion object {
        private val VALID_PASSWORD_REGEX =
            Pattern.compile(
                "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%!&*^_?+='\"{}.,/`~():;\\-])(?=\\S+$).{9,130}"
            )
    }
}
