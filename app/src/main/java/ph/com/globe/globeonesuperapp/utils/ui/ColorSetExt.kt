/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

fun Context.setTextViewColor(
    textView: TextView,
    color: Int
) {
    textView.setTextColor(
        ContextCompat.getColor(
            this,
            color
        )
    )
}

fun Context.setImageViewColorFilter(
    imageView: ImageView,
    color: Int
) {
    imageView.setColorFilter(
        ContextCompat.getColor(
            this,
            color
        )
    )
}
