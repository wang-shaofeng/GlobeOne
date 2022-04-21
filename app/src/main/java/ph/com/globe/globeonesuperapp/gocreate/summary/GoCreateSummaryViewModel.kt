/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.gocreate.summary

import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import javax.inject.Inject

@HiltViewModel
class GoCreateSummaryViewModel @Inject constructor(
    private val overlayAndDialogFactories: OverlayAndDialogFactories
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    fun showTryAgainNextTimeDialog(yesCallback: () -> Unit) {
        handler.handleDialog(
            overlayAndDialogFactories.createGoCreateTryAgainNextTimeDialog(
                yesCallback
            )
        )
    }

    override val logTag = "GoCreateSummaryViewModel"
}
