/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.coming_soon

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.content.res.AppCompatResources
import ph.com.globe.globeonesuperapp.R

fun Context.showComingSoonDialog() {
    val dialog = AlertDialog.Builder(this)
        .setView(LayoutInflater.from(this).inflate(R.layout.coming_soon_dialog, null))
        .setPositiveButton(R.string.button_ok){ _, _->  }
        .create()
    dialog.setOnShowListener {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(AppCompatResources.getColorStateList(this, R.color.primary))
    }

    dialog.show()
}
