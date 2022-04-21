/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.payment_successful

import android.content.SharedPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.domain.session.SessionDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import javax.inject.Inject

@HiltViewModel
class PaymentSuccessfulViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val sessionDomainManager: SessionDomainManager,
    private val overlayAndDialogFactories: OverlayAndDialogFactories
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    fun shouldShowSingUpPrompt(): Boolean {
        // the function checks if the sign up prompt fragment should be shown and if so
        // it stores the session id of the session it is shown in
        val currentSessionId = sessionDomainManager.getCurrentUserSessionId()
        val lastPromptedSessionId = (sharedPreferences.getLong(
            LAST_PAYMENT_PROMPT_SESSION_ID_KEY,
            -1
        ))
        return (lastPromptedSessionId != currentSessionId).also {
            if (it) sharedPreferences.edit()
                .putLong(LAST_PAYMENT_PROMPT_SESSION_ID_KEY, currentSessionId).apply()
        }
    }

    fun shouldShowSingUpPromptHpw(): Boolean {
        // the function checks if the sign up prompt fragment should be shown and if so
        // it stores the session id of the session it is shown in
        val currentSessionId = sessionDomainManager.getCurrentUserSessionId()
        val lastPromptedSessionId = (sharedPreferences.getLong(
            LAST_PAYMENT_HPW_PROMPT_SESSION_ID_KEY,
            -1
        ))
        return (lastPromptedSessionId != currentSessionId).also {
            if (it) sharedPreferences.edit()
                .putLong(LAST_PAYMENT_HPW_PROMPT_SESSION_ID_KEY, currentSessionId).apply()
        }
    }

    fun showVoucherActivationInfoDialog(voucherCode: String?, yesCallback: () -> Unit, noCallback: () -> Unit) {
        handler.handleDialog(
            if (voucherCode != null) {
                overlayAndDialogFactories.createVoucherCodeDialog(yesCallback, noCallback)
            } else {
                overlayAndDialogFactories.createVoucherActivationLinkDialog(yesCallback, noCallback)
            }
        )
    }

    override val logTag = "PaymentSuccessfulViewModel"
}

private const val LAST_PAYMENT_PROMPT_SESSION_ID_KEY = "LastPaymentPrompt_sessionId_key"
private const val LAST_PAYMENT_HPW_PROMPT_SESSION_ID_KEY = "LastPaymentHpwPrompt_sessionId_key"
