/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.dashboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.AppDataViewModel
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.BaseActivity.Companion.SHOP_KEY
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.SELECTED_ENROLLED_ACCOUNT
import ph.com.globe.globeonesuperapp.addaccount.FROM_DASHBOARD
import ph.com.globe.globeonesuperapp.dashboard.banners.BannerAdapterCallback
import ph.com.globe.globeonesuperapp.dashboard.banners.BannerPagerAdapter
import ph.com.globe.globeonesuperapp.dashboard.banners.BannersViewModel
import ph.com.globe.globeonesuperapp.dashboard.maintenance.MaintenanceViewModel
import ph.com.globe.globeonesuperapp.dashboard.raffle.RaffleViewModel
import ph.com.globe.globeonesuperapp.databinding.DashboardFragmentBinding
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.globeonesuperapp.rating.RatingViewModel
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter.Companion.PROMO_ID
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.ui.BubbleView
import ph.com.globe.globeonesuperapp.utils.ui.DeepLinkAction
import ph.com.globe.globeonesuperapp.utils.ui.DeepLinkObject
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.view_binding.BottomNavViewBindingFragment
import ph.com.globe.model.banners.CTAType
import ph.com.globe.model.rewards.RewardsCategory
import java.util.*
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

@AndroidEntryPoint
class DashboardFragment :
    BottomNavViewBindingFragment<DashboardFragmentBinding>(bindViewBy = {
        DashboardFragmentBinding.inflate(
            it
        )
    }), AnalyticsScreen, BannerAdapterCallback {

    private val dashboardViewModel: DashboardViewModel by hiltNavGraphViewModels(R.id.dashboard_subgraph)

    private val ratingViewModel: RatingViewModel by viewModels()
    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()
    private val appDataViewModel: AppDataViewModel by activityViewModels()

    private val raffleViewModel: RaffleViewModel by hiltNavGraphViewModels(R.id.dashboard_subgraph)

    private val bannersViewModel: BannersViewModel by viewModels()

    private val maintenanceViewModel: MaintenanceViewModel by viewModels()

    private lateinit var bannersHandler: Handler
    private var currentBannerItem: Int = 0

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    lateinit var overlayAndDialogFactories: OverlayAndDialogFactories

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:home screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        with(ratingViewModel) {
            setCurrentTime(System.currentTimeMillis())
            evaluateRatingConditions()

            ratingRulesFulfilled.observe(viewLifecycleOwner, { event ->
                event.handleEvent { fulfilled ->
                    if (fulfilled)
                        findNavController().safeNavigate(R.id.action_dashboardFragment_to_rating_subgraph)
                }
            })
        }

        with(viewBinding) {

            appDataViewModel.lottie.value.let {
                it?.let {
                    lavName.setComposition(it)
                }
            }

            clProfile.doOnPreDraw {
                if (getView() != null) {
                    clProfile.setPadding(
                        clProfile.paddingLeft,
                        clProfile.paddingTop,
                        clProfile.paddingRight,
                        tbCollapsingToolbar.height
                    )
                }
            }

            with(raffleViewModel) {

                raffleInProgressLiveData.observe(viewLifecycleOwner, { (_, inProgress) ->
                    if (inProgress) {

                        bubbleVisibilityState.observe(viewLifecycleOwner, { visible ->
                            ivRaffleBubble.isVisible = visible
                        })

                        kycCompleteLiveData.observe(viewLifecycleOwner, { complete ->
                            ticketCount.observe(viewLifecycleOwner, { count ->
                                if (count > 0) {
                                    tvRaffleCount.text = count.toString()
                                    groupComplete.isVisible = complete
                                    groupIncomplete.isVisible = !complete

                                    cvRaffle.setOnClickListener {
                                        if (complete) {
                                            findNavController().safeNavigate(
                                                DashboardFragmentDirections.actionDashboardFragmentToRaffleFragment()
                                            )
                                        } else {
                                            findNavController().safeNavigate(
                                                DashboardFragmentDirections.actionDashboardFragmentToProfileSubgraph(
                                                    proceedForRaffle = true
                                                )
                                            )
                                        }
                                    }

                                    if (!raffleBubbleShown) showRaffleBubble()
                                }
                            })
                        })
                    }
                })
            }
            cvRaffle.setOnClickListener {
                findNavController().safeNavigate(
                    DashboardFragmentDirections.actionDashboardFragmentToProfileSubgraph(
                        proceedForRaffle = true
                    )
                )
            }

            with(dashboardViewModel) {

                // region Alternative accounts placeholders
                accountsStatus.observe(viewLifecycleOwner, {
                    with(it) {
                        incErrorState.root.isVisible = error
                        incEmptyState.root.isVisible = empty
                        lavLoading.isVisible = loading
                    }
                })

                incErrorState.btnReload.setOnClickListener {
                    dashboardViewModel.refreshAccounts()
                }

                incEmptyState.btnAddAccount.setOnClickListener {
                    crossBackstackNavigator.crossNavigate(
                        BaseActivity.ADD_ACCOUNT_KEY,
                        R.id.addAccountNumberFragment,
                        bundleOf(FROM_DASHBOARD to true)
                    )
                }
                // endregion

                // region Small UI components

                shouldUpdateGreeting.observe(
                    viewLifecycleOwner,
                    { shouldUpdateGreeting ->
                        if (shouldUpdateGreeting)
                            dashboardViewModel.setNewGreeting(
                                resources.getStringArray(R.array.dashboard_greetings)[Random.nextInt(
                                    0..14
                                )]
                            )
                    })

                bubbleVisibilityState.observe(viewLifecycleOwner, { isVisible ->
                    ivProfileBubble.isVisible = isVisible
                })

                greeting.observe(viewLifecycleOwner, { greeting ->
                    tvHello.text = greeting
                })

                nickname.observe(viewLifecycleOwner, {
                    if (it !is NicknameUiModel.Error) {
                        tvName.text = when (it) {
                            is NicknameUiModel.Data -> it.name
                            else -> getString(R.string.update_profile_placeholder)
                        }
                        tvName.visibility = View.VISIBLE
                        lavName.visibility = View.GONE
                    }
                })

                ivUser.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            HOME_SCREEN, CLICKABLE_ICON, PROFILE
                        )
                    )
                    findNavController().safeNavigate(R.id.action_dashboardFragment_to_profile_subgraph)
                }

                btnAddAccount.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            HOME_SCREEN, CLICKABLE_ICON, ADD
                        )
                    )
                    crossBackstackNavigator.crossNavigate(
                        BaseActivity.ADD_ACCOUNT_KEY,
                        R.id.addAccountNumberFragment,
                        bundleOf(FROM_DASHBOARD to true)
                    )
                }

                btnPos.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            HOME_SCREEN, CLICKABLE_ICON, CAMERA_SCANNING
                        )
                    )
                    findNavController().safeNavigate(R.id.action_dashboardFragment_to_pos_subgraph)
                }

                btnGoCreateNow.setOnClickListener {
                    findNavController().safeNavigate(
                        DashboardFragmentDirections.actionDashboardFragmentToGoCreateSubgraph(
                            entryPoint = getString(R.string.wayfinder_home)
                        )
                    )
                }

                ivDiscover.setOnClickListener {
                    findNavController().safeNavigate(
                        DashboardFragmentDirections.actionDashboardFragmentToDiscoverMoreFragment(
                            previousScreenTitle = getString(R.string.dashboard)
                        )
                    )
                }

                // endregion

                // region Accounts

                val consumptionRecyclerViewAdapter =
                    DashboardConsumptionRecyclerViewAdapter(
                        reloadCallback = { index, msisdn, accountAlias ->
                            reloadItem(index, msisdn, accountAlias)
                        },
                        promosCallback = {
                            crossBackstackNavigator.crossNavigate(
                                SHOP_KEY,
                                R.id.shopFragment,
                                bundleOf(
                                    "tabToSelect" to PROMO_ID,
                                    "mobileNumber" to it.primaryMsisdn
                                )
                            )
                        },
                        accountDetailsCallback = {
                            findNavController().safeNavigate(
                                R.id.action_dashboardFragment_to_account_subgraph,
                                bundleOf(
                                    SELECTED_ENROLLED_ACCOUNT to (it.enrolledAccount)
                                )
                            )
                        },
                        removeAccountCallback = {
                            removeAccount(it)
                        },
                        showBubbleCallback = { position, type ->
                            checkIfBubbleAvailable(position) {
                                BubbleView(requireContext()).show(clAccounts, position, type)
                            }
                        }
                    )

                val cardHeight = resources.getDimension(R.dimen.dashboard_account_card_height)
                svAccounts.setOnScrollChangeListener(
                    NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                        val visiblePosition = (scrollY / cardHeight).toInt()
                        consumptionRecyclerViewAdapter.onScroll(visiblePosition)
                    })

                rvConsumptionItems.adapter = consumptionRecyclerViewAdapter

                accountsUpdateTrigger.observe(viewLifecycleOwner, { trigger ->
                    with(consumptionRecyclerViewAdapter) {
                        submitList(currentUiModels.toList())
                        if (trigger.updateIndex >= 0)
                            notifyItemChanged(trigger.updateIndex)
                    }
                })

                accountRemovedEvent.oneTimeEventObserve(viewLifecycleOwner) { removedIndex ->
                    consumptionRecyclerViewAdapter.notifyItemRemoved(removedIndex)
                }

                // endregion

                // region Rush spinwheel
                shouldShowSpinwheel.observe(viewLifecycleOwner, { shouldShow ->
                    clSpinwheel.isVisible = shouldShow
                })

                spinwheelUrlResult.observe(viewLifecycleOwner, {
                    it.handleEvent { url ->
                        openSpinwheelUrl(url)
                    }
                })

                ivSpinwheel.setOnClickListener {
                    getSpinwheelUrl()
                }

                btnSpinwheel.setOnClickListener {
                    getSpinwheelUrl()
                }

                ivCloseSpinwheel.setOnClickListener {
                    generalEventsViewModel.handleDialog(overlayAndDialogFactories.createHideSpinwheelDialog {
                        hideSpinwheel()
                    })
                }

                // endregion

                // region Manual refreshing

                ablDashboard.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, offset ->
                    srlDashboard.isEnabled = offset == 0
                })

                srlDashboard.setRefreshListener {
                    refreshAccounts(manualRefresh = true)
                    bannersViewModel.fetchBanners()
                    maintenanceViewModel.getDashboardMaintenance()
                }

                isRefreshingData.observe(viewLifecycleOwner) { refreshing ->
                    srlDashboard.setRefreshing(refreshing)
                }

                // endregion
            }

            with(bannersViewModel) {
                banners.observe(viewLifecycleOwner, { bannerList ->
                    // TODO: Dashboard status bar should be transparent no matter this condition according to figma
                    if (bannerList.isNotEmpty()) {
                        if (noMaintenance()) {
                            setDarkStatusBar()
                        }
                        bannersHandler = Handler(Looper.getMainLooper())
                        vp2Banners.visibility = View.VISIBLE
                        di2Banners.isVisible = bannerList.size > 1
                        vp2Banners.registerOnPageChangeCallback(fragmentPageChangeListener)
                        val bannerAdapter = BannerPagerAdapter(this@DashboardFragment)
                        bannerAdapter.submitList(bannerList)
                        vp2Banners.adapter = bannerAdapter
                        di2Banners.attachViewPager(vp2Banners)
                        // have to update the raffle card view params here, if it's done in the layout
                        // itself, then it doesn't look good in case there are no banners
                        cvRaffle.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            topToBottom = R.id.di2_banners
                        }
                    } else {
                        vp2Banners.visibility = View.GONE
                        di2Banners.visibility = View.GONE
                        // unset the constraint in case it was previously set
                        // e.g. if the end date has passed while the app was open
                        cvRaffle.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            topToBottom = ConstraintLayout.LayoutParams.UNSET
                        }
                    }
                })
            }

            appDataViewModel.fetchRegisteredUserCompleted.observe(viewLifecycleOwner, {
                if (it) dashboardViewModel.getCustomerNickname()
            })

            with(maintenanceViewModel) {
                dashboardMaintenance.observe(viewLifecycleOwner, { maintenance ->
                    mvMaintenanceView.setMaintenance(maintenance)
                    if (maintenance.hasMaintenance) {
                        setStatusBarColor(R.color.neutral_B_2)
                    }
                })
            }
        }

        setWallpaper()

        generalEventsViewModel.handleDeepLink.eventWithResultObserve(viewLifecycleOwner) {
            handleDeepLink(it)
        }
    }

    override fun onResume() {
        super.onResume()
        if (this::bannersHandler.isInitialized) {
            postAutomaticBannerItemChange(currentBannerItem + 1)
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::bannersHandler.isInitialized) {
            bannersHandler.removeMessages(0)
        }
    }

    private fun noMaintenance(): Boolean =
        maintenanceViewModel.dashboardMaintenance.value?.hasMaintenance != true

    private fun setWallpaper() {
        // region Setting wallpaper & status bar colour
        when (Calendar.getInstance(TimeZone.getDefault())
            .get(Calendar.HOUR_OF_DAY)) {
            in (4..7) -> setMorning()
            in (8..14) -> setNoon()
            in (15..17) -> setSunset()
            in (18..23), in (0..3) -> setEvening()
        }
        // endregion
    }

    private fun setMorning() {
        viewBinding.ivBackground.setImageResource(R.drawable.morning)
        setMorningStatusBar()
    }

    private fun setNoon() {
        viewBinding.ivBackground.setImageResource(R.drawable.noon)
        setDaytimeStatusBar()
    }

    private fun setEvening() {
        viewBinding.ivBackground.setImageResource(R.drawable.evening)
        setNightStatusBar()
    }

    private fun setSunset() {
        viewBinding.ivBackground.setImageResource(R.drawable.sunset)
        setSunsetStatusBar()
    }

    private fun openSpinwheelUrl(url: String) =
        generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
            findNavController().safeNavigate(
                DashboardFragmentDirections.actionDashboardFragmentToSpinwheelWebViewFragment(
                    url
                )
            )
        }

    private fun handleDeepLink(deepLink: DeepLinkObject): Boolean =
        when (deepLink.deepLinkAction) {
            DeepLinkAction.ShopTab -> {
                crossBackstackNavigator.crossNavigate(
                    BaseActivity.SHOP_KEY,
                    R.id.shopFragment
                )
                true
            }
            DeepLinkAction.RewardCategoryRaffleTab -> {
                crossBackstackNavigator.crossNavigate(
                    BaseActivity.REWARDS_KEY,
                    R.id.allRewardsInnerFragment,
                    bundleOf("tab" to RewardsCategory.RAFFLE)
                )
                true
            }
            DeepLinkAction.RewardCategoryAllTab -> {
                crossBackstackNavigator.crossNavigate(
                    BaseActivity.REWARDS_KEY,
                    R.id.allRewardsInnerFragment,
                    bundleOf("tab" to RewardsCategory.NONE)
                )
                true
            }
            DeepLinkAction.RewardLanding -> {
                crossBackstackNavigator.crossNavigate(
                    BaseActivity.REWARDS_KEY,
                    R.id.rewardsFragment
                )
                true
            }
            DeepLinkAction.OpenSpinwheel -> {
                dashboardViewModel.getSpinwheelUrl()
                true
            }
            DeepLinkAction.ShopLoad -> {
                findNavController().safeNavigate(
                    R.id.action_dashboardFragment_to_shop_subgraph,
                    bundleOf(
                        "tabToSelect" to ShopPagerAdapter.LOAD_ID,
                        "toolbarTab" to getString(R.string.menu_home)
                    )
                )
                true
            }
            else -> {
                //no-op for now
                false
            }
        }

    override fun setBannerBackground(imageUrl: String?) {
        with(viewBinding) {
            if (imageUrl.isNullOrEmpty()) {
                ivBackground.setImageResource(R.drawable.banner_default_background)
            } else {
                GlobeGlide.with(ivBackground).load(imageUrl)
                    .fitCenter()
                    .placeholder(R.drawable.banner_default_background).into(ivBackground)
            }
        }
    }

    override fun buttonClicked(type: CTAType?, action: String?) {
        if (type != null && action != null)
            when (type) {
                CTAType.DEEPLINK -> generalEventsViewModel.handleDeepLink(Uri.parse(action))
                CTAType.EXTERNAL_LINK -> generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(action)
                        )
                    )
                }
                CTAType.OTHER -> {
                    //no-op for now
                }
            }
    }

    private fun postAutomaticBannerItemChange(position: Int) {
        bannersHandler.removeMessages(0)
        with(viewBinding) {
            val bannerAdapter = vp2Banners.adapter as BannerPagerAdapter
            val bannerCount = bannerAdapter.itemCount
            if (bannerCount > 1) {
                val actualPosition = position % bannerAdapter.itemCount
                bannersHandler.postDelayed({
                    vp2Banners.setCurrentItem(actualPosition, true)
                }, 10000)
            }
        }
    }

    private val fragmentPageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if (view != null) {
                with(viewBinding) {
                    val adapter = vp2Banners.adapter as BannerPagerAdapter
                    setBannerBackground(adapter.getItemAtPosition(position).bannerURL)
                    currentBannerItem = position
                    postAutomaticBannerItemChange(currentBannerItem + 1)
                }
            }
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) = Unit

        override fun onPageScrollStateChanged(state: Int) = Unit
    }

    override val logTag = "DashboardFragment"

    override val analyticsScreenName = "dashboard"
}
