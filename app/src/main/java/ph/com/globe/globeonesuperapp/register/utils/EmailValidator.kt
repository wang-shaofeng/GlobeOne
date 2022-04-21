/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.register.utils

import android.text.InputFilter
import android.widget.EditText
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Validates if the provided email conforms to the established policy.
 */
class EmailValidator @Inject constructor() {

    fun isValid(email: String?): Status {
        val matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email)

        if (email.isNullOrBlank()) return Status.EmailIsInvalid
        else if (email.length > EMAIL_MAX_LENGTH) return Status.MoreThen128Chars
        else if (!matcher.matches()) return Status.EmailIsInvalid
        return Status.Ok
    }

    companion object {
        private const val EMAIL_REGEXP =
            "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"

        private val VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile(
                EMAIL_REGEXP,
                Pattern.CASE_INSENSITIVE
            )
    }

    sealed class Status {
        object Ok : Status()
        object MoreThen128Chars : Status()
        object EmailIsInvalid : Status()
    }
}

/**
 * Extension for email input [EditText] class.
 *
 * With this function two [InputFilter]s will be applied:
 * 1. The first filter handles max email length reached event and can be used to display corresponding UI error.
 * 2. The second filter limits max email input by user.
 *
 * Note: with this function you need to remove android:maxLength attribute from [EditText] in layout file.
 *
 * @param lengthChangedListener is Unit, returns true in case if last input's length greater than [EMAIL_MAX_LENGTH].
 * */
fun EditText.setupEmailInputFilter(lengthChangedListener: (maxLengthReached: Boolean) -> Unit) {
    filters = arrayOf(
        InputFilter { _, start, end, dest, dstart, dend ->

            val newLength = dest.length + (end - start) - (dend - dstart)
            val maxLengthReached = newLength > EMAIL_MAX_LENGTH

            lengthChangedListener.invoke(maxLengthReached)
            return@InputFilter null
        },
        InputFilter.LengthFilter(EMAIL_MAX_LENGTH)
    )
}

const val EMAIL_MAX_LENGTH = 128
