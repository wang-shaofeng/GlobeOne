/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.or

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.domain.payment.PaymentDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.payment.GetPaymentReceiptParams
import ph.com.globe.model.payment.Payment
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class ORPaymentViewModel @Inject constructor(
    private val paymentDomainManager: PaymentDomainManager,
    private val overlayAndDialogFactories: OverlayAndDialogFactories,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    lateinit var receiptId: String
    lateinit var token: String

    private val _orPaymentStatus = MutableLiveData<OneTimeEvent<ORPaymentStatus>>()
    val orPaymentStatus: LiveData<OneTimeEvent<ORPaymentStatus>> = _orPaymentStatus

    init {
        savedStateHandle.get<Payment>("payment")?.receiptId?.let { receiptId = it }
        savedStateHandle.get<String>("token")?.let { token = it }
    }

    fun getPaymentOR(failureCallback: () -> Unit) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            paymentDomainManager.getPaymentReceipt(
                GetPaymentReceiptParams(
                    receiptId,
                    token
                )
            ).fold({
                _orPaymentStatus.value = OneTimeEvent(ORPaymentStatus.Success(it))
            }, {
                _orPaymentStatus.value = OneTimeEvent(ORPaymentStatus.Error)
                handler.handleDialog(
                    overlayAndDialogFactories.createAccountDetailsFailedDialog(
                        failureCallback
                    )
                )
            })
        }
    }

    sealed class ORPaymentStatus {
        data class Success(
            val html: String
        ) : ORPaymentStatus()

        object Error : ORPaymentStatus()
    }

    override val logTag = "ORPaymentViewModel"
}
