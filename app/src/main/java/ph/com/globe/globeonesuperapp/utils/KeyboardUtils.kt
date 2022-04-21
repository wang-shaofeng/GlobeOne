/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager

fun closeKeyboard(view: View?, context: Context) {
    (view?.rootView as? ViewGroup)?.focusedChild?.let { focusedView ->
        // Clear focus to prevent keyboard from automatically reopening immediately
        focusedView.clearFocus()
        // And close keyboard
        val inputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            focusedView.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}
