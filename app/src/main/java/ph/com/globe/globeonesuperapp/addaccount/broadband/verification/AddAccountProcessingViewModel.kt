/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband.verification

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.errors.account.GetSecurityQuestionsError
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OTP_KEY_SET_ENROLL_ACCOUNT
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog
import ph.com.globe.model.auth.GetSecurityQuestionsParams
import ph.com.globe.model.util.brand.*
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class AddAccountProcessingViewModel @Inject constructor(
    private val authDomainManager: AuthDomainManager,
    private val overlayAndDialogFactories: OverlayAndDialogFactories
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _getSecurityQuestionsResult = MutableLiveData<SecurityQuestionsResult>()
    val getSecurityQuestionsResult: LiveData<SecurityQuestionsResult> = _getSecurityQuestionsResult

    fun getSecurityQuestions(msisdn: String) {
        viewModelScope.launch {
            authDomainManager.getSecurityQuestions(
                GetSecurityQuestionsParams(
                    msisdn = msisdn,
                    segment = AccountSegment.Broadband,
                    brand = AccountBrandType.Postpaid,
                    categoryIdentifier = OTP_KEY_SET_ENROLL_ACCOUNT.joinToString(separator = ",")
                )
            ).fold({
                if (it.securityQuestions.size == NUMBER_OF_SECURITY_QUESTIONS) {
                    _getSecurityQuestionsResult.value =
                        SecurityQuestionsResult.GetSecurityQuestionsSuccessful(
                            it.referenceId,
                            SecurityQuestions(
                                question1 = it.securityQuestions[0],
                                question2 = it.securityQuestions[1],
                                question3 = it.securityQuestions[2],
                                question4 = it.securityQuestions[3]
                            )
                        )
                } else {
                    // if the api call succeeds but the number of questions is not as expected
                    handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                }
            }, {
                _getSecurityQuestionsResult.value =
                    SecurityQuestionsResult.GetSecurityQuestionsFailed
                if (it is GetSecurityQuestionsError.General)
                    handler.handleGeneralError(it.error)
                else if (it is GetSecurityQuestionsError.CannotGetSecurityQuestions)
                    handler.handleDialog(overlayAndDialogFactories.createMaxAttemptsForSecurityQuestionsDialog())
            })
        }
    }

    override val logTag = "AddAccountProcessingViewModel"
}

sealed class SecurityQuestionsResult : Parcelable {
    @Parcelize
    object GetSecurityQuestionsFailed : SecurityQuestionsResult()

    @Parcelize
    data class GetSecurityQuestionsSuccessful(
        val referenceId: String,
        val securityQuestions: SecurityQuestions
    ) : Parcelable, SecurityQuestionsResult()
}

@Parcelize
open class AddAccountProcessingFragmentEntryPoint(
    val processingTitle: String? = null,
    val processingDescription: String? = null
) : Parcelable {
    class GetSecurityQuestionsEntryPoint(val msisdn: String, val brand: AccountBrand) :
        AddAccountProcessingFragmentEntryPoint()
}
