/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.OneButtonDialogBinding

/**
 * Pop up dialog builder that creates dialog with one button
 */
class OneButtonDialogBuilder(val context: Context) {
    private val customAlertDialogBuilder =
        CustomAlertDialogBuilder(context)

    fun createDialog(
        dialogTitle: Int,
        dialogDescription: Int,
        buttonText: Int = R.string.button_ok,
        callback: (() -> Unit)? = null
    ): AlertDialog = createDialog(
        context.getString(dialogTitle),
        context.getString(dialogDescription),
        context.getString(buttonText),
        callback
    )

    fun createDialog(
        dialogTitle: String,
        dialogDescription: String,
        buttonText: String = context.getString(R.string.button_ok),
        callback: (() -> Unit)? = null
    ): AlertDialog {
        val binding = OneButtonDialogBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog = customAlertDialogBuilder.setCancelable(false)
            .setView(binding.root)
            .create()

        with(binding) {
            tvOneButtonDialogTitle.text = dialogTitle
            tvOneButtonDialogDescription.text = dialogDescription
            btnAction.text = buttonText

            btnAction.setOnClickListener {
                dialog.dismiss()
                callback?.invoke()
            }
        }

        return dialog
    }
}
