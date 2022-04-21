/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils

import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.inSpans

inline fun SpannableStringBuilder.onClick(
    crossinline onClick: () -> Unit,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder = inSpans(object : ClickableSpan() {
    override fun onClick(widget: View) {
        onClick()
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.isUnderlineText = false
    }

}, builderAction = builderAction)

fun spannedLinkString(
    text: String,
    linkText: String,
    linkColor: Int,
    onClick: () -> Unit
): CharSequence =
    buildSpannedString {
        append(text)
        append(" ")
        bold {
            color(linkColor) {
                onClick({ onClick() }) {
                    append(linkText)
                }
            }
        }
    }
