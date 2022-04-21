/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import ph.com.globe.errors.GeneralError
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog.Dialog
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog.Overlay
import ph.com.globe.globeonesuperapp.utils.view_binding.BottomNavVisibility

interface GeneralEventsHandler {

    fun handleGeneralError(generalError: GeneralError)

    fun handleOverlay(overlay: Overlay)

    suspend fun startLoading()

    fun handleDialog(dialog: Dialog)

    fun dismiss()

    suspend fun endLoading()

    fun setBottomNavVisibility(bottomNavVisibility: BottomNavVisibility)
}

sealed class OverlayOrDialog {

    sealed class Overlay : OverlayOrDialog() {
        object Loading : Overlay()
    }

    sealed class Dialog : OverlayOrDialog() {
        object NoInternet : Dialog()
        object NetworkError : Dialog()
        object UnknownError : Dialog()
        object UserNotLoggedIn : Dialog()
        object UnrecoverableError : Dialog()
        object NumberNotActiveError : Dialog()
        object NumberChangedError : Dialog()
        object SubscriberDataMissingError : Dialog()

        data class CustomDialog(private val dialogFactory: Context.() -> AlertDialog) : Dialog() {

            fun createAndShow(context: Context): AlertDialog =
                dialogFactory(context).also { it.show() }
        }

        data class CustomFragmentDialog(
            private val dialogTag: String,
            private val dialogFactory: FragmentManager.() -> DialogFragment
        ) : Dialog() {

            fun createAndShow(fragmentManager: FragmentManager): DialogFragment =
                dialogFactory(fragmentManager).also {
                    it.show(fragmentManager, dialogTag)
                }
        }
    }

    object Dismiss : OverlayOrDialog()

    object DismissOverlay : OverlayOrDialog()
}
