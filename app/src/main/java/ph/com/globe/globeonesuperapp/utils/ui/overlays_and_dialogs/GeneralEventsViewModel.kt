/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.domain.connectivity.ConnectivityDomainManager
import ph.com.globe.domain.session.SessionDomainManager
import ph.com.globe.errors.GeneralError
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.navigation.NavHostFragmentKey
import ph.com.globe.globeonesuperapp.utils.ui.DeepLinkHandler
import ph.com.globe.globeonesuperapp.utils.ui.DeepLinkObject
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog.*
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog.Dialog.*
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog.Overlay.*
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.overlays.AutoDismissDialogFragment
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.overlays.LoadingOverlay
import ph.com.globe.globeonesuperapp.utils.view_binding.BottomNavVisibility
import ph.com.globe.model.auth.LoginStatus
import ph.com.globe.model.network.NetworkConnectionStatus
import ph.com.globe.util.exhaustive
import javax.inject.Inject
import ph.com.globe.errors.NetworkError as ApiNetworkError

@HiltViewModel
class GeneralEventsViewModel @Inject constructor(
    private val authDomainManager: AuthDomainManager,
    private val sessionDomainManager: SessionDomainManager,
    private val overlayAndDialogFactories: OverlayAndDialogFactories,
    private val connectivityDomainManager: ConnectivityDomainManager
) : ViewModel(), GeneralEventsHandler, HasLogTag {

    override val logTag = "generalEventsHandlingViewModel"

    private val _overlaysAndDialogs = MutableLiveData<OneTimeEvent<OverlayOrDialog>>()
    private val overlaysAndDialogs: LiveData<OneTimeEvent<OverlayOrDialog>> get() = _overlaysAndDialogs

    private var displayedOverlay: AutoDismissDialogFragment? = null
    private var displayedDialog: AlertDialog? = null
    private var displayedFragmentDialog: DialogFragment? = null

    private val _showBottomNav = MutableLiveData<Boolean>()
    val showBottomNav: LiveData<Boolean> = _showBottomNav

    val logoutEvent = authDomainManager.logoutEvent().asLiveData(Dispatchers.Default)

    fun isLoggedIn(): Boolean = authDomainManager.getLoginStatus() != LoginStatus.NOT_LOGGED_IN

    private val _handleDeepLink = MutableLiveData<EventWithResult<DeepLinkObject, Boolean>>()
    val handleDeepLink: MutableLiveData<EventWithResult<DeepLinkObject, Boolean>> = _handleDeepLink

    private val deepLinkHandler = DeepLinkHandler {
        // if result is true, then deeplink is handled, so we can clear event.
        _handleDeepLink.postEventWithResult(it) { param -> if (param == true) clearEvent() }
    }

    @Volatile
    private var numOfLoading = 0

    private val loadingMutex = Mutex()

    init {
        viewModelScope.launch {
            authDomainManager.setSymmetricKey()
        }
    }

    override fun handleGeneralError(generalError: GeneralError) {
        when (generalError) {
            GeneralError.NotLoggedIn -> handleDialog(NetworkError)
            GeneralError.General -> handleDialog(NetworkError)
            is GeneralError.Other -> {
                if (generalError.error == ApiNetworkError.NoInternet)
                    handleDialog(NoInternet)
                else
                    handleDialog(NetworkError)
            }
        }
    }

    override fun handleOverlay(overlay: Overlay) {
        _overlaysAndDialogs.postOneTimeEvent(overlay)
    }

    override fun handleDialog(dialog: Dialog) {
        _overlaysAndDialogs.value = OneTimeEvent(dialog)
    }

    override fun dismiss() {
        _overlaysAndDialogs.postOneTimeEvent(Dismiss)
    }

    override fun setBottomNavVisibility(bottomNavVisibility: BottomNavVisibility) {

        _showBottomNav.value = when (bottomNavVisibility) {

            BottomNavVisibility.NO_BOTTOM_NAV -> false

            BottomNavVisibility.AUTHENTICATED_BOTTOM_NAV -> {
                // TODO this is temporary and not completely valid; isLoggedIn should be used
                isLoggedIn()
            }

            BottomNavVisibility.VISIBLE_BOTTOM_NAV -> true
        }
    }

    fun handleDialogsAndOverlaysInActivity(
        activity: FragmentActivity
    ) {
        overlaysAndDialogs.observe(activity, {
            it.handleEvent { overlayOrDialog ->
                viewModelScope.launch(Dispatchers.Main + NonCancellable) {
                    if (activity.isFinishing ||
                        activity.supportFragmentManager.isStateSaved ||
                        activity.supportFragmentManager.isDestroyed
                    ) return@launch

                    if (displayedOverlay != null || overlayOrDialog != DismissOverlay)
                        tryDismissDisplayedOverlayAndDialog()

                    when (overlayOrDialog) {

                        is Overlay -> {
                            when (overlayOrDialog) {
                                is Loading -> displayLoadingOverlay(activity)
                            }
                        }

                        is Dialog -> {
                            when (overlayOrDialog) {

                                is NetworkError -> displayGeneralError(activity)

                                is UnknownError -> displayGeneralError(activity)

                                is CustomDialog -> displayedDialog =
                                    overlayOrDialog.createAndShow(activity)

                                is CustomFragmentDialog -> displayedFragmentDialog =
                                    overlayOrDialog.createAndShow(activity.supportFragmentManager)

                                is NoInternet -> displayedFragmentDialog =
                                    displayNoInternetDialog(activity)

                                else -> {
                                }
                            }
                        }

                        is Dismiss -> loadingMutex.withLock { numOfLoading = 0 }

                        is DismissOverlay -> loadingMutex.withLock { numOfLoading = 0 }
                    }.exhaustive

                }
            }
        })

    }

    private suspend fun tryDismissDisplayedOverlayAndDialog() {
        loadingMutex.withLock {
            displayedOverlay?.dismiss()
            displayedOverlay = null

            displayedFragmentDialog?.dismiss()
            displayedFragmentDialog = null

            displayedDialog?.dismiss()
            displayedDialog = null
        }
    }

    fun leaveGlobeOneAppNonZeroRated(callback: () -> Unit) {
        viewModelScope.launch {
            when (connectivityDomainManager.getNetworkStatus()) {
                is NetworkConnectionStatus.NotConnectedToInternet -> {
                    handleGeneralError(GeneralError.Other(ApiNetworkError.NoInternet))
                }
                is NetworkConnectionStatus.ConnectedToWifi, NetworkConnectionStatus.Other -> {
                    callback.invoke()
                }
                is NetworkConnectionStatus.ConnectedToMobileData -> {
                    handleDialog(
                        overlayAndDialogFactories.createLeaveGlobeOneAppNonZeroRatedDialog(
                            callback
                        )
                    )
                }
            }
        }
    }

    override suspend fun startLoading() {
        loadingMutex.withLock {
            numOfLoading++
            if (numOfLoading == 1) {
                _overlaysAndDialogs.postOneTimeEvent(Loading)
            }
        }
    }

    override suspend fun endLoading() {
        loadingMutex.withLock {
            numOfLoading--
            numOfLoading = if (numOfLoading < 0) 0 else numOfLoading
            if (numOfLoading == 0) {
                _overlaysAndDialogs.postOneTimeEvent(DismissOverlay)
            }
        }
    }

    private fun displayLoadingOverlay(activity: FragmentActivity) {
        displayedOverlay = LoadingOverlay().also {
            it.showDialog(activity.supportFragmentManager)
        }
    }

    private fun displayGeneralError(activity: ComponentActivity) {
        OneButtonDialogBuilder(
            activity
        ).createDialog(
            R.string.were_sorry_about_that,
            R.string.something_went_wrong,
            R.string.go_back
        ).also { it.show() }
    }

    private fun displayNoInternetDialog(activity: FragmentActivity) =
        NoInternetDialog().also {
            it.showDialog(activity.supportFragmentManager)
        }

    private var lastNavHostFragmentKey: NavHostFragmentKey? = null
    private var lastFragmentId: Int? = null
    private var fragmentData: Bundle? = null

    fun lastNavHostFragmentKey(key: NavHostFragmentKey?, fragmentId: Int?, bundle: Bundle? = null) {
        lastNavHostFragmentKey = key
        lastFragmentId = fragmentId
        fragmentData = bundle
    }

    fun getLastNavHostFragmentKey() = lastNavHostFragmentKey
    fun getLastFragmentId() = lastFragmentId
    fun getFragmentData() = fragmentData

    fun handleDeepLink(uri: Uri) {
        deepLinkHandler.setDeepLink(uri)
    }

    fun startUserSession() {
        sessionDomainManager.startUserSession()
    }

    fun pauseUserSession() {
        sessionDomainManager.pauseUserSession()
    }
}
