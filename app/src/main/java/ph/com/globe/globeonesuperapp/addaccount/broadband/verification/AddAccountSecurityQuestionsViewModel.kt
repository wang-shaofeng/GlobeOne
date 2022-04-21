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
import ph.com.globe.data.network.util.VERIFICATION_TYPE_SECURITY_QUESTIONS
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.errors.account.GetAccountDetailsError
import ph.com.globe.errors.auth.ValidateSecurityAnswersError
import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OTP_KEY_SET_ENROLL_ACCOUNT
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.account.GetAccountDetailsParams
import ph.com.globe.model.auth.*
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.model.util.convertToAccountStatusString
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class AddAccountSecurityQuestionsViewModel @Inject constructor(
    private val authDomainManager: AuthDomainManager,
    private val accountDomainManager: AccountDomainManager,
    private val overlayAndDialogFactories: OverlayAndDialogFactories
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _validateSecurityAnswersResult =
        MutableLiveData<OneTimeEvent<ValidateSecurityAnswersResult>>()
    val validateSecurityAnswersResult: LiveData<OneTimeEvent<ValidateSecurityAnswersResult>> =
        _validateSecurityAnswersResult

    private val _nextButtonEnabled = MutableLiveData<OneTimeEvent<Boolean>>()
    val nextButtonEnabled: LiveData<OneTimeEvent<Boolean>> = _nextButtonEnabled

    private val _loadAccountDetailsResult = MutableLiveData<OneTimeEvent<Boolean>>()
    val loadAccountDetailsResult: LiveData<OneTimeEvent<Boolean>> = _loadAccountDetailsResult

    private val _getSecurityAnswersResult = MutableLiveData<List<SecurityAnswer>>()
    val getSecurityAnswersResult: LiveData<List<SecurityAnswer>> = _getSecurityAnswersResult

    private val securityAnswers: MutableList<String?> = arrayListOf(null, null, null, null)
    private val securityQuestionIds: MutableList<String?> = arrayListOf(null, null, null, null)

    var accountStatus: String? = null
    var accountName: String? = null
    var accountNumber: String? = null
    var landlineNumber: String? = null
    var alternativeMobileNumber: String? = null

    fun getSecurityAnswers(referenceId: String) {
        if (BuildConfig.FLAVOR_servers == "staging") {
            viewModelScope.launch {
                authDomainManager.getSecurityAnswers(GetSecurityAnswersParams(referenceId)).fold({
                    _getSecurityAnswersResult.value = it
                }, {})
            }
        }
    }

    fun validateSecurityAnswers(referenceId: String, msisdn: String) {
        // first we disable the button
        _nextButtonEnabled.value = OneTimeEvent(false)

        viewModelScope.launchWithLoadingOverlay(handler) {
            authDomainManager.validateSecurityAnswers(
                ValidateSecurityAnswersParams(
                    referenceId = referenceId,
                    categoryIdentifier = OTP_KEY_SET_ENROLL_ACCOUNT,
                    brandType = AccountBrandType.Postpaid,
                    segment = AccountSegment.Broadband,
                    msisdn = msisdn,
                    createSecurityAnswers(securityAnswers, securityQuestionIds)
                )
            ).fold(
                {
                    _validateSecurityAnswersResult.value =
                        OneTimeEvent(ValidateSecurityAnswersResult.ValidateSecurityAnswersSuccess)
                }, {
                    if (it is ValidateSecurityAnswersError.General)
                        handler.handleGeneralError(it.error)
                    else if (it is ValidateSecurityAnswersError.MaxAttemptsReached) {
                        handler.handleDialog(overlayAndDialogFactories.createMaxAttemptsForSecurityQuestionsDialog())
                        _validateSecurityAnswersResult.value =
                            OneTimeEvent(ValidateSecurityAnswersResult.ValidateSecurityAnswersFailed)
                    } else if (it is ValidateSecurityAnswersError.SecurityAnswersInsufficient)
                        _validateSecurityAnswersResult.value = OneTimeEvent(
                            ValidateSecurityAnswersResult.ValidateSecurityAnswersInsufficient(
                                indexesOfWrongAnswers = it.incorrectAnswersIds.map {
                                    securityQuestionIds.indexOf(
                                        it
                                    )
                                }
                            )
                        )
                }
            )
        }
    }

    fun loadAccountDetails(msisdn: String, referenceId: String) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            accountDomainManager.getAccountDetails(
                GetAccountDetailsParams(
                    msisdn = msisdn,
                    segment = AccountSegment.Broadband,
                    referenceId,
                    VERIFICATION_TYPE_SECURITY_QUESTIONS
                )
            ).fold({
                accountStatus = it.statusDescription?.convertToAccountStatusString()
                accountName = "${it.firstName ?: ""} ${it.lastName ?: ""}"
                accountNumber = it.accountNumber
                landlineNumber = it.landlineNumber
                _loadAccountDetailsResult.value = OneTimeEvent(true)
            }, {
                if (it is GetAccountDetailsError.General)
                    handler.handleGeneralError(it.error)
                else
                    handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                _loadAccountDetailsResult.value = OneTimeEvent(false)
            })
        }
    }

    fun inputAnswer(answer: String, questionId: String, index: Int) {
        securityAnswers[index] = answer
        securityQuestionIds[index] = questionId
        _nextButtonEnabled.value =
            OneTimeEvent(securityAnswers.count { !it.isNullOrEmpty() } >= NUMBER_OF_REQUIRED_SECURITY_ANSWERS)
    }

    override val logTag = "AddAccountSecurityQuestionsViewModel"
}

sealed class ValidateSecurityAnswersResult {
    object ValidateSecurityAnswersSuccess : ValidateSecurityAnswersResult()
    data class ValidateSecurityAnswersInsufficient(val indexesOfWrongAnswers: List<Int>) :
        ValidateSecurityAnswersResult()

    object ValidateSecurityAnswersFailed : ValidateSecurityAnswersResult()
}

@Parcelize
data class SecurityQuestions(
    val question1: SecurityQuestion,
    val question2: SecurityQuestion,
    val question3: SecurityQuestion,
    val question4: SecurityQuestion
) : Parcelable

const val NUMBER_OF_SECURITY_QUESTIONS = 4
const val NUMBER_OF_REQUIRED_SECURITY_ANSWERS = 3
