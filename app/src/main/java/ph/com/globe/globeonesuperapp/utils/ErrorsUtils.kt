/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.color.MaterialColors
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import ph.com.globe.errors.account.GetAccountBrandError
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.shop.util.NumberValidation

/**
 * Shows a given error for a given [TextInputLayout] and [EditText].
 * [TextInputLayout]'s box background color defaults to [R.color.input_text_background_red].
 * The error text color defaults to [R.attr.colorError].
 */
fun Context.showError(
    textInputLayout: TextInputLayout,
    editText: EditText,
    errorText: String,
    @ColorRes textColor: Int? = null
): Context {
    textInputLayout.error = errorText
    textInputLayout.setBoxBackgroundColorResource(R.color.input_text_background_red)
    editText.setTextColor(
        textColor?.let {
            ResourcesCompat.getColor(
                resources,
                it,
                theme
            )
        } ?: MaterialColors.getColor(editText, R.attr.colorError)
    )
    return this
}

/**
 * Shows error on startIcon of [TextInputEditText]
 * [TextInputEditText]'s startIcon color defaults to [R.color.error_text_red].
 */
fun Context.showErrorOnStartIcon(textInputLayout: TextInputLayout): Context {
    textInputLayout.setStartIconTintList(
        ContextCompat.getColorStateList(
            this,
            R.color.error_text_red
        )
    )
    return this
}

/**
 * Hides error from given [TextInputLayout] and [EditText].
 * [TextInputLayout]'s box background color defaults to [R.color.absolute_white].
 * [TextInputEditText]'s text color defaults to [R.color.neutral_A_0].
 */
fun Context.hideError(
    textInputLayout: TextInputLayout,
    editText: EditText
): Context {
    textInputLayout.error = null
    textInputLayout.isErrorEnabled = false
    textInputLayout.setBoxBackgroundColorResource(R.color.absolute_white)
    editText.setTextColor(
        ResourcesCompat.getColor(
            resources,
            R.color.neutral_A_0,
            theme
        )
    )
    return this
}

/**
 * Hides error on startIcon of [TextInputEditText]
 * [TextInputEditText]'s startIcon color defaults to [R.color.corporate_A_600].
 */
fun Context.hideErrorOnStartIcon(textInputLayout: TextInputLayout): Context {
    textInputLayout.setStartIconTintList(
        ContextCompat.getColorStateList(
            this,
            R.color.corporate_A_600
        )
    )
    return this
}

/**
 * Shows a given OTP code error for a given [errorTextView], [digitsBackground] and [digits].
 * [digitsBackground]'s box background color defaults to [R.color.input_text_background_red].
 * The error text color defaults to [R.color.error_text_red].
 */
fun Context.showOtpError(
    digitsBackground: List<TextInputLayout>,
    digits: List<TextInputEditText>,
    errorTextView: TextView,
    errorText: String
): Context {
    errorTextView.text = errorText
    errorTextView.visibility = View.VISIBLE
    for (digitBackground in digitsBackground) digitBackground.error = " "
    for (digit in digits) digit.setTextColor(
        ResourcesCompat.getColor(
            resources,
            R.color.error_text_red,
            theme
        )
    )

    return this
}

/**
 * Hides OTP code error for a given [errorTextView], [digitsBackground] and [digits].
 * [digitsBackground]'s box background color defaults to [R.color.highlight].
 * [digits]'s text color defaults to [R.color.black_85].
 */
fun Context.hideOtpError(
    digitsBackground: List<TextInputLayout>,
    digits: List<TextInputEditText>,
    errorTextView: TextView
): Context {
    errorTextView.visibility = View.GONE
    for (digitBackground in digitsBackground) digitBackground.error = null
    for (digit in digits) digit.setTextColor(
        ResourcesCompat.getColor(
            resources,
            R.color.black_85,
            theme
        )
    )

    return this
}

/**
 * Displays error using [TextInputLayout] based on [NumberValidation] error
 */
fun Fragment.reflectValidationToErrorDisplaying(
    validation: NumberValidation,
    textInputEditText: TextInputEditText,
    textInputLayout: TextInputLayout,
    isInShopGraph: Boolean = false
) {
    if (isInShopGraph && validation.brand?.isPostpaid() == true) {
        requireContext().showError(
            textInputLayout,
            textInputEditText,
            getString(R.string.postpaid_number_is_not_supported),
            R.color.red
        )
        return
    }

    when (validation.error) {
        GetAccountBrandError.InvalidAccount -> requireContext().showError(
            textInputLayout,
            textInputEditText,
            getString(R.string.error_non_globe_number)
        )
        GetAccountBrandError.InvalidParameter -> requireContext().showError(
            textInputLayout,
            textInputEditText,
            getString(R.string.invalid_number)
        )
        else -> requireContext().hideError(
            textInputLayout,
            textInputEditText,
        )
    }
}

fun Fragment.showZipcodeError(
    zipcode: String,
    textInputEditText: TextInputEditText,
    textInputLayout: TextInputLayout
) {
    if (zipcode.length < 4)
        requireContext().showError(
            textInputLayout,
            textInputEditText,
            getString(R.string.profile_kyc_zipcode_error_message)
        )
    else
        requireContext().hideError(
            textInputLayout,
            textInputEditText,
        )
}

fun Fragment.showContactNumberError(
    number: String,
    textInputEditText: TextInputEditText,
    textInputLayout: TextInputLayout
) {
    if (number.length < 11)
        requireContext().showError(
            textInputLayout,
            textInputEditText,
            getString(R.string.profile_kyc_contact_error_message)
        )
    else
        requireContext().hideError(
            textInputLayout,
            textInputEditText,
        )

}

/**
 * Set error background for outlined [EditText]
 */
fun EditText.setOutlinedErrorBackground() {
    this.setBackgroundResource(R.drawable.et_outlined_error_background)
}

/**
 * Set regular background for outlined [EditText]
 */
fun EditText.setOutlinedBackground() {
    this.setBackgroundResource(R.drawable.et_outlined_background)
}
