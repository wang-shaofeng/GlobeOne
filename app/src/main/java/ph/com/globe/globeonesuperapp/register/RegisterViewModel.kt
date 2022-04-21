/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.errors.auth.RegisterError
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.register.utils.EmailValidator
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog.Dialog.UnknownError
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.auth.RegisterEmailParams
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authDomainManager: AuthDomainManager,
    private val emailValidator: EmailValidator
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _registerResult = MutableLiveData<OneTimeEvent<RegisterResult>>()
    val registerResult: LiveData<OneTimeEvent<RegisterResult>> = _registerResult

    private val _passwordContains9Characters = MutableLiveData<Boolean>()
    val passwordContains9Characters: LiveData<Boolean> = _passwordContains9Characters

    private val _passwordContainsSpecialCharacters = MutableLiveData<Boolean>()
    val passwordContainsSpecialCharacters: LiveData<Boolean> = _passwordContainsSpecialCharacters

    private val _passwordContainsUpperCharacters = MutableLiveData<Boolean>()
    val passwordContainsUpperCharacters: LiveData<Boolean> = _passwordContainsUpperCharacters

    private val _passwordContainsLowerCharacters = MutableLiveData<Boolean>()
    val passwordContainsLowerCharacters: LiveData<Boolean> = _passwordContainsLowerCharacters

    private val _passwordContainsNumber = MutableLiveData<Boolean>()
    val passwordContainsNumber: LiveData<Boolean> = _passwordContainsNumber

    private val _passwordsMatch = MutableLiveData<Boolean>()
    val passwordsMatch: LiveData<Boolean> = _passwordsMatch

    private val _progressBarValue = MutableLiveData<Int>()
    val progressBarValue: LiveData<Int> = _progressBarValue

    private val _passwordStrength = MutableLiveData<PasswordStrength>()
    val passwordStrength: LiveData<PasswordStrength> = _passwordStrength

    private val _emailValid = MutableLiveData<EmailValidator.Status>()
    val emailValid: LiveData<EmailValidator.Status> = _emailValid

    private val _privacyPolicyIsChecked = MutableLiveData<Boolean>()

    private val passwordConditionValues: Array<Boolean> =
        arrayOf(false, false, false, false, false, false)

    private val passwordMediatorLiveData = MediatorLiveData<Boolean>().also {
        fun changeValue(value: Boolean, position: Int) {
            if (value != passwordConditionValues[position]) updateValue(value, position)
        }

        it.addSource(passwordContains9Characters) { value ->
            changeValue(value, 0)
        }
        it.addSource(passwordContainsSpecialCharacters) { value ->
            changeValue(value, 1)
        }
        it.addSource(passwordContainsUpperCharacters) { value ->
            changeValue(value, 2)
        }
        it.addSource(passwordContainsLowerCharacters) { value ->
            changeValue(value, 3)
        }
        it.addSource(passwordContainsNumber) { value ->
            changeValue(value, 4)
        }
        it.addSource(passwordsMatch) { value ->
            changeValue(value, 5)
        }
        it.addSource(emailValid) { value ->
            if ((value == EmailValidator.Status.Ok) != it.value) {
                it.value =
                    !passwordConditionValues.contains(false) && (value == EmailValidator.Status.Ok)
            }
        }
    }

    val canProceed = MediatorLiveData<Boolean>().also { liveData ->
        var privacy = false
        var password = false

        fun update() {
            liveData.value = privacy && password
        }

        liveData.addSource(_privacyPolicyIsChecked) {
            privacy = it
            update()
        }
        liveData.addSource(passwordMediatorLiveData) {
            password = it
            update()
        }
    }

    private fun updateValue(value: Boolean, condition: Int) {
        passwordConditionValues[condition] = value

        passwordMediatorLiveData.value =
            !passwordConditionValues.contains(false) && (_emailValid.value == EmailValidator.Status.Ok)

        val conditionsFulfilled = passwordConditionValues.filter { it }.count()

        _progressBarValue.value = conditionsFulfilled * 100 / passwordConditionValues.count()

        when (conditionsFulfilled) {
            0 -> _passwordStrength.value = PasswordStrength.NoPassword
            in 1..2 -> _passwordStrength.value = PasswordStrength.KeepGoingStrength
            in 3..4 -> _passwordStrength.value = PasswordStrength.AlmostThereStrength
            in 5..6 -> _passwordStrength.value = PasswordStrength.GoodJobStrength
        }
    }

    fun register(email: String, password: String, passwordConfirm: String) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            authDomainManager.registerEmail(
                RegisterEmailParams(
                    email,
                    password,
                    passwordConfirm
                )
            ).fold(
                {
                    dLog("Registration successful.")
                    _registerResult.value = OneTimeEvent(RegisterResult.RegisterSuccessful)
                },
                { registrationError ->
                    dLog("Registration failed.")
                    when (registrationError) {
                        is RegisterError.EmailAddressAlreadyInUse -> {
                            dLog("Registration failed: email address already in use.")
                            _registerResult.value =
                                OneTimeEvent(RegisterResult.EmailAddressAlreadyInUse)
                        }
                        is RegisterError.General -> handler.handleGeneralError(registrationError.error)
                        else -> handler.handleDialog(UnknownError)
                    }
                }
            )
        }
    }

    fun emailIsValid(emailAddress: String) {
        _emailValid.value = emailValidator.isValid(emailAddress)
    }

    fun validatePassword(password: String, confirmPassword: String) {

        _passwordContainsUpperCharacters.value = password.any { it.isUpperCase() }

        _passwordContainsLowerCharacters.value = password.any { it.isLowerCase() }

        _passwordContainsNumber.value = password.any { it.isDigit() }

        _passwordContains9Characters.value = password.length >= 9

        _passwordContainsSpecialCharacters.value = password.any { it.isLetterOrDigit().not() }

        validatePasswordsMatch(password, confirmPassword)
    }

    fun validatePasswordsMatch(password: String, confirmPassword: String) {
        _passwordsMatch.value =
            (password == confirmPassword && password.isNotBlank())
    }

    fun privacyPolicyIsChecked(checked: Boolean) {
        _privacyPolicyIsChecked.value = checked
    }

    sealed class RegisterResult {
        object EmailAddressAlreadyInUse : RegisterResult()
        object RegisterSuccessful : RegisterResult()
    }

    sealed class PasswordStrength {
        object KeepGoingStrength : PasswordStrength()
        object AlmostThereStrength : PasswordStrength()
        object GoodJobStrength : PasswordStrength()
        object NoPassword : PasswordStrength()
    }

    override val logTag = "RegisterViewModel"
}
