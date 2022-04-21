/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.reset_password

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.errors.auth.RequestResetPasswordError
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.register.utils.EmailValidator
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.auth.RequestResetPasswordParams
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authDomainManager: AuthDomainManager,
    private val emailValidator: EmailValidator
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _resetPasswordResult = MutableLiveData<OneTimeEvent<ResetPasswordResult>>()
    val resetPasswordResult: LiveData<OneTimeEvent<ResetPasswordResult>> = _resetPasswordResult

    private val _resendResetPasswordResult = MutableLiveData<OneTimeEvent<ResetPasswordResult>>()
    val resendResetPasswordResult: LiveData<OneTimeEvent<ResetPasswordResult>> =
        _resendResetPasswordResult

    fun sendPasswordResetEmail(email: String) {
        if (emailValidator.isValid(email) != EmailValidator.Status.Ok) {
            _resetPasswordResult.value = OneTimeEvent(ResetPasswordResult.EmailFormatIsIncorrect)
        } else {
            viewModelScope.launchWithLoadingOverlay(handler) {
                authDomainManager.requestResetPassword(
                    RequestResetPasswordParams(email)
                ).fold(
                    {
                        dLog("Password reset email sent.")
                        _resetPasswordResult.value =
                            OneTimeEvent(ResetPasswordResult.EmailSentSuccessfully)
                    },
                    {
                        dLog("Failed to send password reset email.")
                        when (it) {
                            is RequestResetPasswordError.General -> handler.handleGeneralError(it.error)
                            RequestResetPasswordError.NoRegisteredUserWithThisEmail -> {
                                dLog("Failed to send password reset email.")
                                _resetPasswordResult.value =
                                    OneTimeEvent(ResetPasswordResult.NoUserWithThisEmailAddress)
                            }
                            is RequestResetPasswordError.EmailIsForSocialLogin -> {
                                dLog("Email is for social login.")
                                _resetPasswordResult.value =
                                    OneTimeEvent(ResetPasswordResult.EmailIsForSocialLogin)
                            }
                            else -> handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                        }
                    })
            }
        }
    }

    fun resendPasswordResetEmail(email: String) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            authDomainManager.requestResetPassword(
                RequestResetPasswordParams(email)
            ).fold(
                {
                    dLog("Password reset email resent.")
                    _resendResetPasswordResult.value =
                        OneTimeEvent(ResetPasswordResult.EmailSentSuccessfully)
                },
                {
                    dLog("Failed to resend password reset email.")
                    when (it) {
                        is RequestResetPasswordError.General -> handler.handleGeneralError(it.error)
                        else -> handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                    }
                })
        }
    }

    sealed class ResetPasswordResult {
        object NoUserWithThisEmailAddress : ResetPasswordResult()
        object EmailFormatIsIncorrect : ResetPasswordResult()
        object EmailSentSuccessfully : ResetPasswordResult()
        object EmailIsForSocialLogin : ResetPasswordResult()
    }

    override val logTag = "ResetPasswordViewModel"
}
