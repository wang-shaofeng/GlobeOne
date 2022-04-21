/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.verify_otp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ph.com.globe.analytics.events.NO_EMAIL_STORED
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.domain.user_details.UserDetailsDomainManager
import ph.com.globe.errors.account.GetAccountBrandError
import ph.com.globe.errors.account.GetAccountStatusError
import ph.com.globe.errors.auth.SendOtpError
import ph.com.globe.errors.auth.VerifyOtpError
import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog.Dialog.UnknownError
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.account.GetAccountBrandParams
import ph.com.globe.model.account.GetAccountStatusParams
import ph.com.globe.model.auth.GetOtpParams
import ph.com.globe.model.auth.OtpType
import ph.com.globe.model.auth.SendOtpParams
import ph.com.globe.model.auth.VerifyOtpParams
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.model.util.brand.toSegment
import ph.com.globe.util.fold
import ph.com.globe.util.onSuccess
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class VerifyOtpViewModel @Inject constructor(
    private val authDomainManager: AuthDomainManager,
    private val accountDomainManager: AccountDomainManager,
    private val requestOtpTimer: RequestOtpTimer,
    private val overlayAndDialogFactories: OverlayAndDialogFactories,
    savedStateHandle: SavedStateHandle,
    private val userDetailsDomainManager: UserDetailsDomainManager
) : BaseViewModel(), RequestOtpTimerReceiver {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _sendOtpResult = MutableLiveData<OneTimeEvent<SendOtpResult>>()
    val sendOtpResult: LiveData<OneTimeEvent<SendOtpResult>> = _sendOtpResult

    private val _closeKeyboardSignal = MutableLiveData<OneTimeEvent<Unit>>()
    val closeKeyboardSignal: LiveData<OneTimeEvent<Unit>> = _closeKeyboardSignal

    private val _resendOtpStatus = MutableLiveData(true)
    val resendOtpStatus: LiveData<Boolean> = _resendOtpStatus

    private val _verifyOtpResult = MutableLiveData<OneTimeEvent<VerifyOtpResult>>()
    val verifyOtpResult: LiveData<OneTimeEvent<VerifyOtpResult>> = _verifyOtpResult

    private val _resendOtpResult = MutableLiveData<OneTimeEvent<ResendOtpResult>>()
    val resendOtpResult: LiveData<OneTimeEvent<ResendOtpResult>> = _resendOtpResult

    private var confirmationJob: Job? = null

    lateinit var referenceId: String

    lateinit var encryptedMsisdn: String

    val encryptedUserEmail by lazy {
        userDetailsDomainManager.getEmail().fold({ email ->
            encryptData(email)
        }, {
            NO_EMAIL_STORED
        })
    }

    // Only for test purposes
    // ========================================================================
    private val _getOtpLiveData = MutableLiveData<String>()
    val getOtpLiveData: LiveData<String> = _getOtpLiveData

    fun getOtp(msisdn: String, referenceId: String, categoryIdentifiers: List<String>) {
        viewModelScope.launch {
            authDomainManager.getOtp(
                GetOtpParams(
                    msisdn,
                    referenceId,
                    categoryIdentifiers.joinToString(separator = ",")
                )
            ).onSuccess { _getOtpLiveData.value = it.otp }
        }
    }
    // ========================================================================

    init {
        savedStateHandle.get<String>("referenceId")?.let {
            referenceId = it
        }

        savedStateHandle.get<String>("msisdn")?.let {
            encryptedMsisdn = encryptData(it)
        }
    }

    private fun encryptData(data: String): String = userDetailsDomainManager.encryptData(data = data)

    fun maxOtpAttemptsReached(onBackCallback: () -> Unit) {
        handler.handleDialog(overlayAndDialogFactories.createMaxAttemptsReachedDialog(onBackCallback))
    }

    fun sendOtp(
        msisdn: String,
        categoryIdentifiers: List<String>,
        outsideUIProcessing: Boolean = false
    ) {
        val sendOtpJob: suspend () -> Unit = {
            val numberBrand: AccountBrand? = getBrandFromGetAccountBrand(msisdn = msisdn)

            numberBrand?.let { brand ->
                authDomainManager.sendOtp(
                    SendOtpParams(
                        type = OtpType.SMS,
                        // we should only convertToPrefixNumberFormat when the type is 'sms'
                        msisdn = msisdn.convertToPrefixNumberFormat(),
                        categoryIdentifier = categoryIdentifiers,
                        brandType = brand.brandType,
                        // TODO the 'GHP' brand can be both the 'broadband' and the 'mobile'
                        segment = brand.toSegment()
                    )
                ).fold({
                    dLog("Otp sent successfully.")
                    _sendOtpResult.value =
                        OneTimeEvent(
                            SendOtpResult.SentOtpSuccess(
                                msisdn = msisdn.convertToPrefixNumberFormat(),
                                brand = brand,
                                brandType = brand.brandType,
                                referenceId = it.referenceId
                            )
                        )
                }, {
                    dLog("Otp sending failed.")

                    if (outsideUIProcessing) {
                        _sendOtpResult.value = OneTimeEvent(SendOtpResult.SentOtpFailure)
                    } else {
                        when (it) {
                            is SendOtpError.General -> handler.handleGeneralError(it.error)
                            else -> handler.handleDialog(UnknownError)
                        }
                    }
                })
            }
        }

        if (outsideUIProcessing) {
            viewModelScope.launch {
                sendOtpJob.invoke()
            }
        } else {
            viewModelScope.launchWithLoadingOverlay(handler) {
                sendOtpJob.invoke()
            }
        }
    }

    // different function than the one above sendOtp() since here we are using a different API to get the brand
    fun addAccountSendOtp(
        msisdn: String,
        segment: AccountSegment,
        sendOtpType: OtpType = OtpType.SMS,
        rawBrand: AccountBrand? = null,
        addBroadbandManually: Boolean? = null
    ) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            var numberBrand: AccountBrand? = rawBrand
            if (numberBrand == null) {
                // we call get account status only if we don't have the brand
                numberBrand = getBrandFromAccountStatus(msisdn, segment)
            }
            numberBrand?.let { brand ->
                authDomainManager.sendOtp(
                    SendOtpParams(
                        type = sendOtpType,
                        msisdn = msisdn,
                        categoryIdentifier = OTP_KEY_SET_ENROLL_ACCOUNT,
                        brandType = brand.brandType,
                        segment = segment
                    )
                ).fold({
                    dLog("Otp sent successfully.")
                    _sendOtpResult.value =
                        OneTimeEvent(
                            SendOtpResult.SentOtpSuccess(
                                msisdn = msisdn,
                                brand = brand,
                                brandType = brand.brandType,
                                referenceId = it.referenceId,
                                addBroadbandManually = addBroadbandManually ?: false,
                                sendOtpType = sendOtpType
                            )
                        )
                }, {
                    dLog("Otp sending failed.")
                    when (it) {
                        is SendOtpError.MaxAttemptsReached -> {
                            if (segment == AccountSegment.Broadband && numberBrand == AccountBrand.GhpPostpaid)
                                handler.handleDialog(overlayAndDialogFactories.createMaxAttemptsReachedDialog {})
                            else
                                handler.handleDialog(UnknownError)
                        }
                        is SendOtpError.General -> handler.handleGeneralError(it.error)
                        else -> handler.handleDialog(UnknownError)
                    }
                })
            }
        }
    }

    private suspend fun getBrandFromAccountStatus(
        msisdn: String,
        segment: AccountSegment
    ): AccountBrand? {
        accountDomainManager.getAccountStatus(GetAccountStatusParams(msisdn, segment))
            .fold({ response ->
                dLog("Add account OTP sent, brand: ${response.brand}")
                return response.brand
            }, {
                dLog("Failed to sent otp $it")
                when (it) {
                    is GetAccountStatusError.InvalidAccount -> {
                        _sendOtpResult.value = OneTimeEvent(SendOtpResult.NotGlobeNumber)
                    }
                    is GetAccountStatusError.InactiveAccount -> {
                        _sendOtpResult.value = OneTimeEvent(SendOtpResult.InactiveAccount)
                    }
                    is GetAccountStatusError.NoLongerInSystemAccount -> {
                        _sendOtpResult.value = OneTimeEvent(SendOtpResult.NoLongerInSystemAccount)
                    }
                    is GetAccountStatusError.General -> handler.handleGeneralError(it.error)
                    else -> handler.handleDialog(UnknownError)
                }
                return null
            })
    }

    private suspend fun getBrandFromGetAccountBrand(msisdn: String): AccountBrand? {
        accountDomainManager.getAccountBrand(GetAccountBrandParams(msisdn))
            .fold({ response ->
                dLog("Fetch account brand successful, brand: ${response.result.brand}")
                return response.result.brand
            }, {
                dLog("Fetch account brand failed $it")
                when (it) {
                    is GetAccountBrandError.InvalidParameter, GetAccountBrandError.InvalidAccount -> {
                        _sendOtpResult.value = OneTimeEvent(SendOtpResult.NotGlobeNumber)
                    }
                    is GetAccountBrandError.General -> handler.handleGeneralError(it.error)
                    else -> handler.handleDialog(UnknownError)
                }
                return null
            })
    }

    fun resendOtpCode(
        phoneNumber: String,
        brandType: AccountBrandType,
        segment: AccountSegment = AccountSegment.Mobile,
        categoryIdentifiers: List<String>,
        sendOtpType: OtpType = OtpType.SMS
    ) = viewModelScope.launchWithLoadingOverlay(handler) {
        authDomainManager.sendOtp(
            SendOtpParams(
                type = sendOtpType,
                msisdn = phoneNumber,
                categoryIdentifier = categoryIdentifiers,
                brandType = brandType,
                segment = segment
            )
        ).fold({
            referenceId = it.referenceId
            _resendOtpResult.value =
                OneTimeEvent(ResendOtpResult.ResendOtpSuccess(it.referenceId))
            if (BuildConfig.FLAVOR_servers == "staging")
                getOtp(phoneNumber, it.referenceId, categoryIdentifiers)
            dLog("Resend otp successful.")
        }, {
            dLog("Resend otp failed.")
            when (it) {
                is SendOtpError.MaxAttemptsReached -> {
                    if (segment == AccountSegment.Broadband && brandType == AccountBrandType.Postpaid)
                        _resendOtpResult.value = OneTimeEvent(ResendOtpResult.MaxOtpResendReached)
                    else
                        handler.handleDialog(UnknownError)
                }
                is SendOtpError.General -> handler.handleGeneralError(it.error)
                else -> handler.handleDialog(UnknownError)
            }
        })
        startOtpTimer()
    }

    fun confirmOtp(
        msisdn: String,
        referenceId: String,
        brandType: AccountBrandType,
        otpCode: String,
        segment: AccountSegment = AccountSegment.Mobile,
        categoryIdentifiers: List<String>
    ) {
        if (confirmationJob?.isActive == true) return
        confirmationJob = viewModelScope.launchWithLoadingOverlay(handler) {
            authDomainManager.verifyOtp(
                VerifyOtpParams(
                    msisdn = msisdn,
                    referenceId = referenceId,
                    code = otpCode,
                    brandType = brandType,
                    segment = segment,
                    categoryIdentifier = categoryIdentifiers
                )
            ).fold({
                dLog("Otp confirmed")
                _verifyOtpResult.value =
                    OneTimeEvent(VerifyOtpResult.VerifyOtpSuccess(it.cxsMessageId))
            }, { error ->
                dLog("Failed to confirm otp")
                when (error) {
                    is VerifyOtpError.OtpCodeIncorrect -> _verifyOtpResult.value =
                        OneTimeEvent(VerifyOtpResult.OtpCodeIncorrect(error.cxsMessageId))
                    is VerifyOtpError.OtpCodeExpired -> _verifyOtpResult.value =
                        OneTimeEvent(VerifyOtpResult.OtpCodeExpired(error.cxsMessageId))
                    is VerifyOtpError.OtpCodeAlreadyVerified -> _verifyOtpResult.value =
                        OneTimeEvent(VerifyOtpResult.OtpCodeAlreadyVerified(error.cxsMessageId))
                    is VerifyOtpError.OtpVerifyingMaxAttempt -> _verifyOtpResult.value =
                        OneTimeEvent(VerifyOtpResult.OtpVerifyingMaxAttempt(error.cxsMessageId))
                    is VerifyOtpError.General -> {
                        handler.handleGeneralError(error.error)
                        OneTimeEvent(VerifyOtpResult.GeneralError(error.cxsMessageId))
                    }
                    else -> handler.handleDialog(UnknownError)
                }
            })
        }
    }

    fun startOtpTimer() {
        _resendOtpStatus.value = false
        requestOtpTimer.startCountDown(this)
    }

    override fun countDownFinished() {
        _resendOtpStatus.value = true
    }

    sealed class SendOtpResult : Serializable {
        object NotAMobileNumber : SendOtpResult()

        object NotABroadbandNumber : SendOtpResult()

        object NotGlobeNumber : SendOtpResult()

        object InactiveAccount : SendOtpResult()

        object NoLongerInSystemAccount : SendOtpResult()

        data class SentOtpSuccess(
            val msisdn: String,
            val brand: AccountBrand,
            val brandType: AccountBrandType,
            val referenceId: String,
            val addBroadbandManually: Boolean = false,
            val sendOtpType: OtpType = OtpType.SMS
        ) : SendOtpResult()

        object SentOtpFailure : SendOtpResult()
    }

    sealed class VerifyOtpResult(val cxsMessageId: String) {
        data class OtpCodeIncorrect(val cxsMsgId: String) : VerifyOtpResult(cxsMsgId)
        data class OtpCodeExpired(val cxsMsgId: String) : VerifyOtpResult(cxsMsgId)
        data class OtpCodeAlreadyVerified(val cxsMsgId: String) : VerifyOtpResult(cxsMsgId)
        data class OtpVerifyingMaxAttempt(val cxsMsgId: String) : VerifyOtpResult(cxsMsgId)
        data class GeneralError(val cxsMsgId: String) : VerifyOtpResult(cxsMsgId)
        data class VerifyOtpSuccess(val cxsMsgId: String) : VerifyOtpResult(cxsMsgId)
    }

    sealed class ResendOtpResult {
        object MaxOtpResendReached : ResendOtpResult()
        data class ResendOtpSuccess(val referenceId: String) : ResendOtpResult()
    }

    override val logTag = "VerifyOTPViewModel"

}
