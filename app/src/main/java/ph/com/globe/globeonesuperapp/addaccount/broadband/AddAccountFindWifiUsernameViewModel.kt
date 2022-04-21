/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband

import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import javax.inject.Inject

@HiltViewModel
class AddAccountFindWifiUsernameViewModel @Inject constructor(
    private val overlayAndDialogFactories: OverlayAndDialogFactories
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    fun cancelAddingAccount(yesCallback: () -> Unit, noCallback: () -> Unit) {
        handler.handleDialog(
            overlayAndDialogFactories.createAddAccountMobileNumberCancelDialog(
                yesCallback,
                noCallback
            )
        )
    }

    override val logTag = "AddAccountFindWifiBroadbandViewModel"
}
