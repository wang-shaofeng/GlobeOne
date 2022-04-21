/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.splash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.globeonesuperapp.AppDataViewModel
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.BaseActivity.Companion.AUTH_KEY
import ph.com.globe.globeonesuperapp.BaseActivity.Companion.DASHBOARD_KEY
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.SplashFragmentBinding
import ph.com.globe.globeonesuperapp.email_verification.EmailVerificationFragment.Companion.WAIT_FOR_DEEP_LINK
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.openPlayStore
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.app_update.InAppUpdateResult
import ph.com.globe.model.auth.LoginStatus
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : NoBottomNavViewBindingFragment<SplashFragmentBinding>(bindViewBy = {
    SplashFragmentBinding.inflate(it)
}), InstallStateUpdatedListener {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val viewModel: SplashViewModel by viewModels()

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()
    private val appDataViewModel: AppDataViewModel by activityViewModels()

    private lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(requireContext())

        (requireActivity() as BaseActivity).onActivityResultCallback =
            { requestCode, resultCode, data ->
                if (requestCode == IN_APP_UPDATE_REQUEST_CODE)
                    handleInAppUpdate(
                        requestCode, resultCode, data
                    )
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appUpdateManager.registerListener(this)

        parentFragmentManager.setFragmentResultListener(
            IN_APP_UPDATE_RECOMMENDED_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val result =
                bundle.getString(IN_APP_UPDATE_RECOMMENDED_KEY_VALUE)
            if (result == CANCEL_KEY)
                viewModel.initLoginData()
            else {
                viewModel.pendingUpdate = InAppUpdateResult.RecommendedUpdate
                tryAskForUpdate(viewModel.pendingUpdate)
            }
        }
        parentFragmentManager.setFragmentResultListener(
            IN_APP_UPDATE_MANDATORY_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val result =
                bundle.getString(IN_APP_UPDATE_MANDATORY_KEY_VALUE)
            if (result == CANCEL_KEY)
                requireActivity().finish()
            else {
                viewModel.pendingUpdate = InAppUpdateResult.MandatoryUpdate
                tryAskForUpdate(viewModel.pendingUpdate)
            }
        }

        viewModel.appUpdateStatusEvent.observe(viewLifecycleOwner) {
            it.handleEvent { inAppUpdateResult ->
                when (inAppUpdateResult) {
                    is InAppUpdateResult.MandatoryUpdate -> findNavController().safeNavigate(R.id.action_splashFragment_to_mandatoryUpdatePromptFragment)
                    is InAppUpdateResult.RecommendedUpdate -> findNavController().safeNavigate(R.id.action_splashFragment_to_recommendedUpdatePromptFragment)
                    is InAppUpdateResult.NoUpdate -> Unit
                }
            }
        }

        viewModel.isLoggedIn.observe(viewLifecycleOwner) {
            when (it) {
                LoginStatus.VERIFIED -> {
                    appDataViewModel.fetchAllInfo()
                    crossBackstackNavigator.crossNavigateWithoutHistory(
                        DASHBOARD_KEY,
                        R.id.dashboardFragment
                    )
                }
                LoginStatus.NOT_LOGGED_IN -> crossBackstackNavigator.crossNavigateWithoutHistory(
                    AUTH_KEY,
                    R.id.selectSignMethodFragment
                )
                LoginStatus.UNVERIFIED -> {
                    appDataViewModel.fetchAllInfo()
                    crossBackstackNavigator.crossNavigateWithoutHistory(
                        AUTH_KEY, R.id.emailVerificationFragment, bundleOf(WAIT_FOR_DEEP_LINK to true)
                    )
                }
            }
        }

        viewModel.setRatingParams(
            requireContext()
                .packageManager
                .getPackageInfo(requireContext().packageName, 0)
                .firstInstallTime
        )
    }

    // Checks that the update is not stalled during 'onResume()'.
    // However, we should execute this check at all app entry points.
    override fun onResume() {
        super.onResume()
        if (viewModel.userSentToPlaystore) {
            // if user is sent to play store and he returns to the app we will let the user use the application
            // as this can only occur on Recommended update as on Mandatory update we previously finished the application.
            viewModel.initLoginData()
            return
        }
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                // If the update is downloaded but not installed,
                // notify the user to complete the update.
                when {
                    appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED -> {
                        // Here, we could have a UI to handle the decision of if we want to immediately install downloaded content or not
                        appUpdateManager.completeUpdate()
                    }
                    appUpdateInfo.updateAvailability()
                            == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        // If an in-app update is already running, resume the update.
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            IMMEDIATE,
                            requireActivity(),
                            IN_APP_UPDATE_REQUEST_CODE
                        )
                    }
                    else -> tryAskForUpdate(viewModel.pendingUpdate)
                }
            }
    }

    override fun onDestroy() {
        (requireActivity() as BaseActivity).onActivityResultCallback = null
        super.onDestroy()
        appUpdateManager.unregisterListener(this)
    }

    private fun handleInAppUpdate(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IN_APP_UPDATE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> Unit // already handled

                Activity.RESULT_CANCELED -> {
                    Log.d(logTag, "Update Cancelled")
                    when (viewModel.appUpdateStatusValue) {
                        is InAppUpdateResult.MandatoryUpdate -> requireActivity().finish()
                        else -> viewModel.initLoginData()
                    }
                }

                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    Log.d(logTag, "Update Failure")
                    when (viewModel.appUpdateStatusValue) {
                        is InAppUpdateResult.MandatoryUpdate -> requireActivity().finish()
                        else -> viewModel.initLoginData()
                    }
                }
            }
        }
        (requireActivity() as BaseActivity).onActivityResultCallback = null
    }

    override fun onStateUpdate(state: InstallState) {
        when (state.installStatus()) {
            InstallStatus.PENDING -> {
            }
            InstallStatus.DOWNLOADED -> {
                // If we are in this state and the app re-runs we will try to finish the update
                // What we could do is prompt the screen to the user deciding if they want to proceed with installation
                // but for now we immediately try complete the update
                appUpdateManager.completeUpdate()
            }
            InstallStatus.DOWNLOADING -> {
                // we could use this to display the download progress
            }
            InstallStatus.INSTALLING -> {
            }
            InstallStatus.INSTALLED, InstallStatus.UNKNOWN -> {
                // we try to proceed to the app but we actually expect the in-app update support to restart the app
                viewModel.initLoginData()
            }
            InstallStatus.FAILED, InstallStatus.CANCELED -> {
                // if the update fails we will decide upon the update type
                when (viewModel.appUpdateStatusValue) {
                    is InAppUpdateResult.MandatoryUpdate -> requireActivity().finish()
                    else -> {
                        viewModel.initLoginData()
                    }
                }
            }
            else -> Unit // the other behaviour is not expected
        }
    }

    private fun tryAskForUpdate(currentUpdate: InAppUpdateResult?) {
        if (currentUpdate != null && !viewModel.checkedForUpdate) {
            viewModel.checkedForUpdate = true
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(currentUpdate.toUpdateType())
                ) {
                    // when we reach this the update is no longer pending
                    viewModel.pendingUpdate = null
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        currentUpdate.toUpdateType(),
                        requireActivity(),
                        IN_APP_UPDATE_REQUEST_CODE
                    )
                } else {
                    handleUpdateUnavailableFromAPIs()
                }
                viewModel.checkedForUpdate = false
            }
            appUpdateInfoTask.addOnFailureListener {
                handleUpdateUnavailableFromAPIs()
                viewModel.checkedForUpdate = false
            }
        }
    }

    private fun handleUpdateUnavailableFromAPIs() {
        // this is the case when we pick up the remote config value and it says that we need the update
        // but it is not available due to the internet issues or update not available trough the play store APIs
        if (viewModel.noInternet()) {
            // we only display the 'no internet' error and block the user form proceeding
            generalEventsViewModel.handleGeneralError(
                GeneralError.Other(NetworkError.NoInternet)
            )
        } else {
            // if we are connected to the internet but the update is not available from the play store
            // APIs we should send the user to the play store New GlobeOne page
            viewModel.userSentToPlaystore = true
            context?.openPlayStore("ph.com.globe.globeonesuperapp")
            if (viewModel.pendingUpdate is InAppUpdateResult.MandatoryUpdate) {
                // we will finish the app in case of MandatoryUpdate since we want to prevent the user from using the app.
                requireActivity().finish()
            }
        }
    }

    companion object {
        const val IN_APP_UPDATE_RECOMMENDED_KEY = "InAppUpdateRecommended_key"
        const val IN_APP_UPDATE_RECOMMENDED_KEY_VALUE = "InAppUpdateRecommended_keyValue"
        const val IN_APP_UPDATE_MANDATORY_KEY = "InAppUpdateMandatory_keyValue"
        const val IN_APP_UPDATE_MANDATORY_KEY_VALUE = "InAppUpdateMandatory_keyValue"

        const val CANCEL_KEY = "Cancel_key"
        const val UPDATE_KEY = "Update_key"

        const val IN_APP_UPDATE_REQUEST_CODE = 101
    }

    override val logTag: String = "SplashFragment"

}

fun InAppUpdateResult?.toUpdateType(): Int =
    when (this) {
        InAppUpdateResult.RecommendedUpdate -> FLEXIBLE
        InAppUpdateResult.MandatoryUpdate -> IMMEDIATE
        else -> -1
    }
