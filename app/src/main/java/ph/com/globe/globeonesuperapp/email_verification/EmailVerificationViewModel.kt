/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.email_verification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.domain.user_details.UserDetailsDomainManager
import ph.com.globe.errors.profile.SendVerificationEmailError
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.postOneTimeEvent
import ph.com.globe.globeonesuperapp.utils.setOneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.util.fold
import ph.com.globe.util.onFailure
import ph.com.globe.util.onSuccess
import javax.inject.Inject

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val profileDomainManager: ProfileDomainManager,
    private val authDomainManager: AuthDomainManager,
    userDetailsDomainManager: UserDetailsDomainManager
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _timer = MutableLiveData<TimerState>()
    val timer = _timer as LiveData<TimerState>
    private val _canResend = MutableLiveData<Boolean>()
    val canResend = _canResend as LiveData<Boolean>

    private val _email = MutableLiveData<String>()
    val email = _email as LiveData<String>

    private val _isVerificationSuccess = MutableLiveData<OneTimeEvent<Boolean>>()
    val isVerificationSuccess = _isVerificationSuccess as LiveData<OneTimeEvent<Boolean>>

    private val _emailIsAlreadyVerified = MutableLiveData<OneTimeEvent<Boolean>>()
    val emailIsAlreadyVerified = _emailIsAlreadyVerified as LiveData<OneTimeEvent<Boolean>>

    private val _verificationEmailIsSent = MutableLiveData<OneTimeEvent<Boolean>>()
    val verificationEmailIsSent = _verificationEmailIsSent as LiveData<OneTimeEvent<Boolean>>

    private val waitForDeepLink = CompletableDeferred<Boolean>()
    private var doesDeepLinkHandle = false

    init {
        tryToSendEmail()
        userDetailsDomainManager.getEmail().fold({
            _email.value = it
        }, {})
    }

    fun tryToSendEmail(showInfo: Boolean = false) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            val wait = waitForDeepLink.await()
            waitForDeepLink.complete(false)
            if (wait) {
                waitForDeepLink.complete(false)
                delay(WAIT_DEEP_LINK)
                if (doesDeepLinkHandle) {
                    doesDeepLinkHandle = false
                    return@launchWithLoadingOverlay
                }
            }

            val verifyEmailResult = profileDomainManager.sendVerificationEmail()
            verifyEmailResult
                .onSuccess {
                    startTimer()
                    if (showInfo) _verificationEmailIsSent.postOneTimeEvent(true)
                }
                .onFailure {
                    if (it is SendVerificationEmailError.General)
                        handler.handleGeneralError(it.error)
                    else if (it is SendVerificationEmailError.EmailIsAlreadyVerified)
                        _emailIsAlreadyVerified.value = OneTimeEvent(true)

                    _canResend.postValue(true)
                    _timer.postValue(TimerState(0, 0))
                }
        }
    }

    private fun startTimer() = viewModelScope.launch(Dispatchers.Default) {
        initTimer(TIMER_2_MINUTES)
            .onStart { _canResend.postValue(false) }
            .onCompletion { _canResend.postValue(true) }
            .collect { _timer.postValue(it) }
    }

    fun forceLogout() {
        viewModelScope.launch {
            authDomainManager.forceLogout()
        }
    }

    fun verifyEmail(verificationCode: String) {
        doesDeepLinkHandle = true
        viewModelScope.launchWithLoadingOverlay(handler) {
            profileDomainManager.verifyEmail(verificationCode)
                .onSuccess { _isVerificationSuccess.setOneTimeEvent(true) }
                .onFailure {
                    _canResend.postValue(true)
                    _timer.postValue(TimerState(0, 0))
                    _isVerificationSuccess.setOneTimeEvent(false)
                }
        }
    }

    fun waitForDeepLink(checkDeepLink: Boolean) {
        waitForDeepLink.complete(checkDeepLink)
    }

    override val logTag: String = "EmailVerificationViewModel"
}

private const val TIMER_THICKNESS_SECOND = 1000L
private const val TIMER_2_MINUTES = 60L * 2L - 1L

private const val WAIT_DEEP_LINK = 1000L

data class TimerState(
    val secondsRemaining: Long,
    val totalSeconds: Long
)

fun initTimer(totalSeconds: Long): Flow<TimerState> =
    (totalSeconds - 1 downTo 0).asFlow()
        .onEach { delay(TIMER_THICKNESS_SECOND) }
        .onStart { emit(totalSeconds) }
        .conflate()
        .transform { remainingSeconds ->
            emit(TimerState(remainingSeconds, totalSeconds))
        }
