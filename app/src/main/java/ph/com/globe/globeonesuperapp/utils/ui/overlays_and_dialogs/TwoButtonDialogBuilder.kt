/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.TwoButtonDialogBinding

/**
 * Pop up dialog builder that creates dialog with two buttons
 */
class TwoButtonDialogBuilder(val context: Context) {

    private val customAlertDialogBuilder = CustomAlertDialogBuilder(context)

    fun createDialog(
        dialogTitle: Int,
        dialogDescription: Int,
        rightButtonCallback: (() -> Unit)? = null,
        leftButtonCallback: (() -> Unit)? = null,
        rightButtonText: Int = R.string.no,
        leftButtonText: Int = R.string.yes,
        showCloseButton: Boolean = false,
        titleArgs: Array<String>? = null,
        showIconDrawable: Boolean = true
    ): AlertDialog {
        val binding = TwoButtonDialogBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog =
            customAlertDialogBuilder.setCancelable(false)
                .setView(binding.root)
                .create()

        with(binding) {
            tvTwoButtonDialogTitle.apply {
                if (titleArgs != null) text = context.getString(dialogTitle, *titleArgs)
                else setText(dialogTitle)
            }
            tvTwoButtonDialogDescription.setText(dialogDescription)
            btnRight.setText(rightButtonText)
            btnLeft.setText(leftButtonText)

            btnRight.setOnClickListener {
                dialog.dismiss()
                rightButtonCallback?.invoke()
            }

            btnLeft.setOnClickListener {
                dialog.dismiss()
                leftButtonCallback?.invoke()
            }

            if (showCloseButton) {
                ivTwoButtonDialogDrawable.apply {
                    setImageResource(R.drawable.ic_close)
                    setOnClickListener {
                        dialog.dismiss()
                    }
                }
            }
            ivTwoButtonDialogDrawable.isVisible = showIconDrawable
        }
        return dialog
    }
}
