/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband.enterusernamepassword

import android.webkit.JavascriptInterface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.errors.auth.VerifyOtpError
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OTP_KEY_SET_ENROLL_ACCOUNT
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.auth.VerifyOtpParams
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class AddAccountEnterUsernamePasswordViewModel @Inject constructor(
    private val authDomainManager: AuthDomainManager
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private lateinit var msisdn: String
    private lateinit var referenceId: String
    private lateinit var brand: AccountBrandType
    private lateinit var segment: AccountSegment

    private var usernameNotEmpty = false
    private var passwordNotEmpty = false

    private val _enableNextButton = MutableLiveData<OneTimeEvent<Boolean>>()
    val enableNextButton: LiveData<OneTimeEvent<Boolean>> = _enableNextButton

    private val _enrollHPWResult = MutableLiveData<OneTimeEvent<EnrollHPWAccount>>()
    val enrollHPWResult: LiveData<OneTimeEvent<EnrollHPWAccount>> = _enrollHPWResult

    private var modemWebAppInterface: ModemWebAppInterface? = null

    fun setInfo(
        msisdn: String,
        referenceId: String,
        brand: AccountBrandType,
        segment: AccountSegment
    ) {
        this.msisdn = msisdn
        this.referenceId = referenceId
        this.brand = brand
        this.segment = segment
    }

    fun usernameChanged(username: String?) {
        usernameNotEmpty = !username.isNullOrBlank()
        _enableNextButton.value = OneTimeEvent(usernameNotEmpty && passwordNotEmpty)
    }

    fun passwordChanged(password: String?) {
        passwordNotEmpty = !password.isNullOrBlank()
        _enableNextButton.value = OneTimeEvent(usernameNotEmpty && passwordNotEmpty)
    }

    fun getWebAppInterface(): ModemWebAppInterface {
        if (modemWebAppInterface == null) {
            modemWebAppInterface = ModemWebAppInterface()
        }
        return modemWebAppInterface!!
    }

    fun navigateToSomethingWentWrongScreen() {
        viewModelScope.launch {
            _enrollHPWResult.value = OneTimeEvent(EnrollHPWAccount.SomethingWentWrongError)
        }
    }

    fun navigateToWrongCredentialScreen() {
        viewModelScope.launch {
            _enrollHPWResult.value = OneTimeEvent(EnrollHPWAccount.BadModemInfoError)
        }
    }

    fun verifyOtp(otp: String) = viewModelScope.launchWithLoadingOverlay(handler) {
        authDomainManager.verifyOtp(
            VerifyOtpParams(
                msisdn = msisdn,
                referenceId = referenceId,
                code = otp,
                brandType = brand,
                segment = segment,
                categoryIdentifier = OTP_KEY_SET_ENROLL_ACCOUNT
            )
        ).fold({
            dLog("Otp confirmed")
            _enrollHPWResult.value = OneTimeEvent(EnrollHPWAccount.EnrollingSuccess)
        }, { error ->
            dLog("Failed to confirm otp")
            when (error) {
                is VerifyOtpError.OtpCodeIncorrect -> _enrollHPWResult.value = OneTimeEvent(
                    EnrollHPWAccount.OtpCodeIncorrect
                )
                is VerifyOtpError.OtpCodeExpired -> _enrollHPWResult.value =
                    OneTimeEvent(EnrollHPWAccount.OtpCodeExpired)

                is VerifyOtpError.OtpCodeAlreadyVerified -> _enrollHPWResult.value =
                    OneTimeEvent(EnrollHPWAccount.OtpCodeAlreadyVerified)

                is VerifyOtpError.OtpVerifyingMaxAttempt -> _enrollHPWResult.value =
                    OneTimeEvent(EnrollHPWAccount.OtpVerifyingMaxAttempt)

                is VerifyOtpError.General -> {
                    handler.handleGeneralError(error.error)
                    _enrollHPWResult.value =
                        OneTimeEvent(EnrollHPWAccount.SomethingWentWrongError)
                }

                else -> handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
            }
        })
    }

    inner class ModemWebAppInterface {

        @JavascriptInterface
        fun passOtp(otp: String?) {
            if (otp.isNullOrBlank()) navigateToSomethingWentWrongScreen()
            else verifyOtp(otp)
        }

        @JavascriptInterface
        fun failedToConnect() {
            navigateToSomethingWentWrongScreen()
        }

        @JavascriptInterface
        fun errorToDisplay() {
            navigateToWrongCredentialScreen()
        }
    }

    sealed class EnrollHPWAccount {

        object SomethingWentWrongError : EnrollHPWAccount()

        object BadModemInfoError : EnrollHPWAccount()

        object OtpCodeIncorrect : EnrollHPWAccount()

        object OtpCodeExpired : EnrollHPWAccount()

        object OtpCodeAlreadyVerified : EnrollHPWAccount()

        object OtpVerifyingMaxAttempt : EnrollHPWAccount()

        object EnrollingSuccess : EnrollHPWAccount()
    }

    override val logTag = "AddAccountEnterUsernamePasswordViewModel"
}

const val GLOBE_AT_HOME = "GlobeAtHome"
