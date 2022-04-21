/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.auth.LoginError
import ph.com.globe.globeonesuperapp.register.utils.EmailValidator
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.PasswordValidator
import ph.com.globe.globeonesuperapp.utils.social_sign_in_controller.SocialSignInController
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.globeonesuperapp.web_view_components.Provider
import ph.com.globe.model.auth.LoginEmailParams
import ph.com.globe.model.auth.LoginSocialParams
import ph.com.globe.model.auth.LoginSocialResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authDomainManager: AuthDomainManager,
    private val emailValidator: EmailValidator,
    private val passwordValidator: PasswordValidator
) : BaseViewModel() {

    private val _emailValid = MutableLiveData<EmailValidator.Status>()
    val emailValid: LiveData<EmailValidator.Status> = _emailValid

    private val _canClickOnLogin = MutableLiveData<Boolean>()
    val canClickOnLogin = _canClickOnLogin as LiveData<Boolean>

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _loginResult = MutableLiveData<OneTimeEvent<LoginResult>>()
    val loginResult: LiveData<OneTimeEvent<LoginResult>> = _loginResult

    var validEmail: String? = null

    fun saveEmailIfValid(email: String) {
        val emailStatus = emailValidator.isValid(email)
        validEmail = if (emailStatus == EmailValidator.Status.Ok) {
            email
        } else {
            null
        }
        _emailValid.value = emailStatus
    }

    /**
     * email login and support social login migration, do migration only the [merge] is true.
     * @param merge if [merge] is true, merge the social login with email login
     * @param socialToken initial received social token
     * @param provider initial social provider
     */
    fun login(
        email: String,
        password: String,
        merge: Boolean? = null,
        socialToken: String? = null,
        provider: String? = null
    ) {
        if (email.isEmpty() || password.isEmpty()) {
            _loginResult.value =
                OneTimeEvent(LoginResult.EmailOrPasswordAreNotEntered)
            return
        }

        val emailStatus = emailValidator.isValid(email)
        val passwordValid = passwordValidator.isValid(password)

        when {
            emailStatus != EmailValidator.Status.Ok || !passwordValid -> {
                _loginResult.value =
                    OneTimeEvent(LoginResult.EmailOrPasswordFormatsAreIncorrect)
                return
            }
        }

        viewModelScope.launchWithLoadingOverlay(handler) {
            _canClickOnLogin.value = false
            authDomainManager.loginEmail(
                LoginEmailParams(
                    email,
                    password,
                    socialToken,
                    provider,
                    merge = merge
                )
            ).fold(
                {
                    dLog("Login successful.")
                    _loginResult.value = OneTimeEvent(LoginResult.LoginSuccessful)
                },
                {
                    when (it) {
                        is LoginError.InvalidUsernameOrPassword -> {
                            dLog("Login: email or password is invalid.")
                            _loginResult.value = OneTimeEvent(LoginResult.EmailOrPasswordAreInvalid)
                        }
                        is LoginError.TooManyFailedLogins -> {
                            dLog("Login: too many failed logins.")
                            _loginResult.value =
                                OneTimeEvent(LoginResult.TooManyFailedLogins)
                        }
                        is LoginError.General -> {
                            handler.handleGeneralError(it.error)
                        }
                        is LoginError.UserEmailNotVerified ->
                            _loginResult.value = OneTimeEvent(LoginResult.LoginUnverified)
                        else -> {
                            dLog("Login: login failed.")
                            _loginResult.value = OneTimeEvent(LoginResult.LoginFailed)
                        }
                    }
                }
            )
            _canClickOnLogin.value = true
        }
    }

    /**
     * social login support social login migration, do migration only the [merge] is true.
     * @param socialToken recent received social token
     * @param provider if [merge] is true, initial social provider, otherwise recent social provider
     * @param merge whether is login migration
     * @param mergeToken initial received social token, only used when the [merge] is true
     */
    fun loginSocial(
        socialToken: String,
        provider: Provider,
        merge: Boolean? = null,
        mergeToken: String? = null
    ) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            authDomainManager.loginSocial(
                LoginSocialParams(
                    socialProvider = provider.getParam(),
                    socialToken = socialToken,
                    mergeToken = mergeToken,
                    merge = merge
                )
            ).fold(
                {
                    when (it) {
                        is LoginSocialResult.SocialRegisterSuccessful -> {
                            dLog("Register social successful.")
                            _loginResult.value = OneTimeEvent(LoginResult.RegisterSocialSuccessful)
                        }
                        is LoginSocialResult.SocialLoginSuccessful -> {
                            dLog("Login social successful.")
                            _loginResult.value = OneTimeEvent(LoginResult.SocialLoginSuccessful)
                        }
                        else -> Unit
                    }
                },
                {
                    when (it) {
                        is LoginError.LoginWithThisEmailAlreadyExists -> {
                            dLog("Social login: login with this email already exists.")
                            _loginResult.value =
                                OneTimeEvent(
                                    LoginResult.LoginWithThisEmailAlreadyExists(
                                        LoginSocialParams(provider.getParam(), socialToken),
                                        it.moreInfo
                                    )
                                )
                        }
                        is LoginError.General -> {
                            handler.handleGeneralError(it.error)
                        }
                        else -> {
                            dLog("Social login: social login failed.")
                            handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                            _loginResult.value =
                                OneTimeEvent(LoginResult.SocialLoginFailed)
                        }
                    }
                }
            )
        }
    }

    /**
     *  if [merge] is true, use the [initialProvider] to call [loginSocial]
     */
    fun exchangeSocialAccessTokenWithGlobeSocialTokenAndSocialLogin(
        token: String,
        method: SocialSignInController.SignInMethod,
        merge: Boolean? = null,
        initialProvider: Provider? = null,
        mergeToken: String? = null
    ) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            val accessToken = authDomainManager.exchangeSocialAccessTokenWithGlobeSocialToken(
                token,
                method.toString()
            ).successOrErrorAction {
                return@launchWithLoadingOverlay handler.handleGeneralError(GeneralError.General)
            }

            if (merge == true) {
                if (initialProvider == null) {
                    dLog("Merge social login: social login provider is null")
                } else {
                    loginSocial(
                        accessToken,
                        initialProvider,
                        merge,
                        mergeToken
                    )
                }
            } else {
                loginSocial(
                    accessToken,
                    method.toProvider()
                )
            }
        }
    }

    sealed class LoginResult {
        object LoginFailed : LoginResult()
        object EmailOrPasswordAreNotEntered : LoginResult()
        object EmailOrPasswordFormatsAreIncorrect : LoginResult()
        object EmailOrPasswordAreInvalid : LoginResult()
        object TooManyFailedLogins : LoginResult()
        object SocialLoginFailed : LoginResult()

        /**
         * update structure to support login migration, will do the login migration
         * when the [moreInfo] is one of LOGIN_MIGRATION_CASES
         */
        data class LoginWithThisEmailAlreadyExists(
            val loginSocialParams: LoginSocialParams,
            val moreInfo: String? = null
        ) : LoginResult()

        object LoginSuccessful : LoginResult()
        object SocialLoginSuccessful : LoginResult()
        object LoginUnverified : LoginResult()
        object RegisterSocialSuccessful : LoginResult()
    }

    override val logTag = "LoginViewModel"
}

private fun SocialSignInController.SignInMethod.toProvider() = when (this) {
    is SocialSignInController.SignInMethod.Facebook -> Provider.Facebook
    is SocialSignInController.SignInMethod.Google -> Provider.Google
}
