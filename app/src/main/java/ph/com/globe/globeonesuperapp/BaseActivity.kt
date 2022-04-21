/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.appdynamics.eumagent.runtime.AgentConfiguration
import com.appdynamics.eumagent.runtime.Instrumentation
import com.globe.inappupdate.remote_config.RemoteConfigManager
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.events.custom.AppOpen
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.globeonesuperapp.databinding.ActivityBaseBinding
import ph.com.globe.globeonesuperapp.utils.eventWithResultObserve
import ph.com.globe.globeonesuperapp.utils.navigation.*
import ph.com.globe.globeonesuperapp.utils.navigation.backstack.BackStack
import ph.com.globe.globeonesuperapp.utils.navigation.history.History
import ph.com.globe.globeonesuperapp.utils.social_sign_in_controller.SocialSignInController
import ph.com.globe.globeonesuperapp.utils.ui.DeepLinkAction
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.Activity
import javax.inject.Inject

@AndroidEntryPoint
class BaseActivity :
    Activity<ActivityBaseBinding>(bindViewBy = { ActivityBaseBinding.inflate(it) }),
    CrossBackstackNavigator, HasLogTag {

    @Inject
    lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var authDomainManager: AuthDomainManager

    @Inject
    lateinit var remoteConfigManager: RemoteConfigManager

    @Inject
    lateinit var socialSignInController: SocialSignInController

    private val generalEventsViewModel: GeneralEventsViewModel by viewModels()
    private val appDataViewModel: AppDataViewModel by viewModels()

    private var navigateAction: NavigateAction? = null

    private lateinit var backStack: BackStack<NavHostFragmentKey, NavHostFragment>
    private lateinit var bottomNavigationClickNavigator: GlobeBottomNavigationClickNavigator<NavHostFragmentKey>

    private val currentNavHostKey: NavHostFragmentKey get() = backStack.history().peek()
    private val currentNavController: NavController
        get() = backStack.getObjectForKey(currentNavHostKey).navController

    var onActivityResultCallback: OnActivityResultHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        startAppDynamics()
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        remoteConfigManager.initialize()
        analyticsLogger.logAnalyticsEvent(AppOpen)

        GeneralEventsHandlerProvider.setHandler(generalEventsViewModel)
        generalEventsViewModel.handleDialogsAndOverlaysInActivity(this)

        appDataViewModel.preloadLottie(this)

        val bottomNavigation = activityBinding.bottomNavigation
        val screenChanger =
            NavHostFragmentScreenChanger(supportFragmentManager, R.id.fl_content) {
                backStack.screenIsAttached()
                navigate()
            }

        backStack = BackStack(
            History.from(SPLASH_KEY),
            screenChanger,
            BackStack.HistoryMode.REORDER_TO_TOP
        )
        backStack.onCreate(savedInstanceState)

        bottomNavigationClickNavigator =
            GlobeBottomNavigationClickNavigator.Builder(
                backStack,
                bottomNavigation,
                lifecycle,
                beforeNavigatingOnItemSelectedCallback = ::popToStartDestination,
                navigationItemReselectedCallback = ::popToStartDestination
            )
                .mapItemIdToKey(
                    R.id.dashboard_subgraph,
                    DASHBOARD_KEY
                )
                .mapItemIdToKey(
                    R.id.help_subgraph,
                    HELP_KEY
                )
                .mapItemIdToKey(
                    R.id.shop_subgraph,
                    SHOP_KEY
                )
                .mapItemIdToKey(
                    R.id.rewards_subgraph,
                    REWARDS_KEY
                )
                .build()

        backStack.addNavigationListener(bottomNavigationClickNavigator)

        onBackPressedDispatcher.addCallback(this) {
            // If the back stack is empty and the user has pressed back, exit
            if (currentNavController.popBackStack().not() && backStack.onBackPressed().not()) {
                finish()
            }
        }

        generalEventsViewModel.showBottomNav.observe(this, { isVisible ->
            val visibility = if (isVisible) {
                View.VISIBLE
            } else {
                View.GONE
            }

            activityBinding.clBottomNav.visibility = visibility
        })

        generalEventsViewModel.logoutEvent.observe(this) {
            generalEventsViewModel.dismiss()
            socialSignInController.logOut()
            if (it) crossNavigateWithoutHistory(TOKEN_EXPIRED_KEY, R.id.tokenExpiredFragment)
            else crossNavigateWithoutHistory(AUTH_KEY, R.id.selectSignMethodFragment)
        }

        generalEventsViewModel.handleDeepLink.eventWithResultObserve(this) {
            if (!generalEventsViewModel.isLoggedIn()) {
                when (it.deepLinkAction) {
                    is DeepLinkAction.ResetPassword -> {
                        crossNavigate(AUTH_KEY, R.id.loginFragment)
                        true
                    }
                    is DeepLinkAction.ShopTab -> {
                        crossNavigate(SHOP_KEY, R.id.shopFragment)
                        true
                    }
                    else -> true
                }
            } else
                false
        }

        handleDynamicLink(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        handleDynamicLink(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        backStack.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    private fun popToStartDestination(itemId: Int = 0) {
        with(currentNavController) {
            currentDestination?.id?.let {
                if (it != graph.startDestination) {
                    popBackStack(graph.startDestination, false)
                }
            }
        }
    }

    override fun crossNavigate(
        toBackStackKey: NavHostFragmentKey,
        @IdRes endDestinationId: Int,
        args: Bundle?,
        shouldPopToStartDestinationFromCurrentGraph: Boolean,
        removeStartDestinationFromNextGraph: Boolean
    ) {
        if (backStack.isInTransaction) return

        navigateAction = NavigateAction.Navigate(endDestinationId, args, removeStartDestinationFromNextGraph)

        if (shouldPopToStartDestinationFromCurrentGraph) popToStartDestination()
        backStack.goTo(toBackStackKey)
    }

    override fun crossNavigateWithoutHistory(
        toBackStackKey: NavHostFragmentKey,
        @IdRes endDestinationId: Int,
        args: Bundle?,
        removeStartDestinationFromNextGraph: Boolean
    ) {
        if (backStack.isInTransaction) return

        navigateAction = NavigateAction.Navigate(endDestinationId, args, removeStartDestinationFromNextGraph)

        popToStartDestination()
        backStack.replace(History.from(toBackStackKey))
    }

    override fun navigateToPreviousBackstack(
        toBackStackKey: NavHostFragmentKey,
        @IdRes returnToDestination: Int?
    ) {
        backStack.goBackTo(toBackStackKey)

        if (returnToDestination != null) {
            navigateAction = NavigateAction.PopBackStack(returnToDestination)
        }
    }

    fun navigate() {
        if (backStack.currentKey == DASHBOARD_KEY && generalEventsViewModel.getLastFragmentId() != null && generalEventsViewModel.getLastNavHostFragmentKey() != null) {
            val lastFragmentId = generalEventsViewModel.getLastFragmentId()
            val lastNavHostFragmentKey = generalEventsViewModel.getLastNavHostFragmentKey()
            val bundle = generalEventsViewModel.getFragmentData()
            generalEventsViewModel.lastNavHostFragmentKey(null, null, null)

            navigateAction = NavigateAction.Navigate(lastFragmentId!!, bundle, false)

            backStack.goTo(lastNavHostFragmentKey)
        } else {
            when (val action = navigateAction) {
                is NavigateAction.Navigate -> {
                    if (action.args != null || action.endDestinationId != currentNavController.graph.startDestination) {
                        val options = navOptions {
                            launchSingleTop = true
                            if (action.clearStartDestination && action.endDestinationId != currentNavController.graph.startDestination)
                                popUpTo(currentNavController.graph.id) {
                                    inclusive = true
                                }
                        }
                        currentNavController.safeNavigate(
                            action.endDestinationId,
                            action.args,
                            options
                        )
                    }
                }
                is NavigateAction.PopBackStack -> {
                    if (!currentNavController.popBackStack(action.endDestinationId, false)) {
                        dLog("Stack was not pop")
                    }
                }
            }
            navigateAction = null
        }
    }

    companion object {
        val AUTH_KEY = NavHostFragmentKey(R.navigation.navigation_auth)

        val SPLASH_KEY = NavHostFragmentKey(R.navigation.navigation_splash)

        val DASHBOARD_KEY = NavHostFragmentKey(R.navigation.dashboard_subgraph)
        val HELP_KEY = NavHostFragmentKey(R.navigation.help_subgraph)
        val SHOP_KEY = NavHostFragmentKey(R.navigation.shop_subgraph)
        val REWARDS_KEY = NavHostFragmentKey(R.navigation.rewards_subgraph)
        val PAYMENT_KEY = NavHostFragmentKey(R.navigation.payment_subgraph)
        val ADD_ACCOUNT_KEY = NavHostFragmentKey(R.navigation.navigation_add_account)
        val TOKEN_EXPIRED_KEY = NavHostFragmentKey(R.navigation.navigation_token_expired)
        val RATING_KEY = NavHostFragmentKey(R.navigation.rating_subgraph)
        val RAFFLE_KEY = NavHostFragmentKey(R.navigation.raffle_subgraph)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultCallback?.invoke(requestCode, resultCode, data)
    }

    private fun startAppDynamics() {
        Instrumentation.start(
            AgentConfiguration.builder()
                .withAppKey("AD-AAB-ABD-HHT")
                .withContext(applicationContext)
                .withLoggingLevel(Instrumentation.LOGGING_LEVEL_VERBOSE)
                .withCollectorURL("https://col.eum-appdynamics.com")
                .withScreenshotURL("https://image.eum-appdynamics.com")
                .build()
        )
    }

    private fun handleDynamicLink(intent: Intent?) {
        if (intent != null) {
            val data = intent.data
            Firebase.dynamicLinks.getDynamicLink(intent)
                .addOnSuccessListener(this) { firebaseData: PendingDynamicLinkData? ->
                    val uri = firebaseData?.link ?: data
                    if (uri != null) generalEventsViewModel.handleDeepLink(uri)
                }.addOnFailureListener(this) { }
            this.intent = intent.also { it.data = null }
        }
    }

    override fun onResume() {
        super.onResume()
        generalEventsViewModel.startUserSession()
    }

    override fun onPause() {
        super.onPause()
        generalEventsViewModel.pauseUserSession()
    }

    // override resources, so the app font size won't change as system's font size
    override fun getResources(): Resources {
        val config = Configuration()
        config.setToDefaults()
        return applicationContext.createConfigurationContext(config).resources
    }

    override val logTag: String = "BaseActivity"
}

sealed class NavigateAction {
    data class Navigate(
        @IdRes
        val endDestinationId: Int,
        val args: Bundle?,
        val clearStartDestination: Boolean
    ) : NavigateAction()

    data class PopBackStack(
        @IdRes
        val endDestinationId: Int
    ) : NavigateAction()
}

typealias OnActivityResultHandler = (requestCode: Int, resultCode: Int, data: Intent?) -> Unit
