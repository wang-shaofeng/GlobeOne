/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.content.unsubscribe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.catalog.CatalogDomainManager
import ph.com.globe.errors.catalog.UnsubscribeContentPromoError
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.catalog.UnsubscribeContentPromoParams
import ph.com.globe.util.onFailure
import ph.com.globe.util.onSuccess
import javax.inject.Inject

@HiltViewModel
class ContentUnsubscribeViewModel @Inject constructor(
    private val catalogDomainManager: CatalogDomainManager,
    private val overlayAndDialogFactories: OverlayAndDialogFactories,
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _unsubscribePromoResult = MutableLiveData<OneTimeEvent<Unit>>()
    val unsubscribePromoResult: LiveData<OneTimeEvent<Unit>> = _unsubscribePromoResult

    fun unsubscribeContentPromo(
        mobileNumber: String,
        serviceId: String,
        promoName: String,
        logAnalyticsAction: () -> Unit
    ) {

        handler.handleDialog(
            overlayAndDialogFactories.createUnsubscribeContentPromoDialog(promoName, {
                logAnalyticsAction.invoke()
                viewModelScope.launchWithLoadingOverlay(handler) {
                    catalogDomainManager.unsubscribeContentPromo(
                        UnsubscribeContentPromoParams(
                            mobileNumber,
                            serviceId
                        )
                    )
                        .onSuccess {
                            _unsubscribePromoResult.value = OneTimeEvent(it)
                            dLog("Unsubscribe content promo success")
                        }
                        .onFailure { error ->
                            handler.handleGeneralError(
                                (error as UnsubscribeContentPromoError.General).error
                            )
                            dLog("Unsubscribe content promo failure")
                        }
                }
            }, { logAnalyticsAction.invoke() })
        )
    }

    override val logTag = "ContentUnsubscribeViewModel"
}
