/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.AppDataViewModel
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.BaseActivity.Companion.SHOP_KEY
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.content.ContentSubscriptionsViewModel
import ph.com.globe.globeonesuperapp.account.data.DataUsageViewModel
import ph.com.globe.globeonesuperapp.account.personalized_campaigns.OfferType
import ph.com.globe.globeonesuperapp.account.personalized_campaigns.PersonalizedCampaignsAdapter
import ph.com.globe.globeonesuperapp.databinding.AccountDetailsFragmentBinding
import ph.com.globe.globeonesuperapp.rewards.allrewards.SELECTED_ENROLLED_ACCOUNT_KEY
import ph.com.globe.globeonesuperapp.rewards.allrewards.TAB_KEY
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter.Companion.BORROW_ID
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter.Companion.LOAD_ID
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.ui.BalanceType.*
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel
import ph.com.globe.model.account.AvailableCampaignPromosModel
import ph.com.globe.model.account.AvailableCampaignPromosModel.PersonalizedCampaignsPromoType.*
import ph.com.globe.model.account.BrandStatus
import ph.com.globe.model.account.DataUsageStatus
import ph.com.globe.model.account.UIAccountBrand
import ph.com.globe.model.profile.domain_models.*
import ph.com.globe.model.rewards.RewardsCategory
import ph.com.globe.model.util.AccountStatus.*
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.brand.GLOBE_PLATINUM
import ph.com.globe.model.util.brand.toUserFriendlyBrandName
import ph.com.globe.util.nonEmptyOrNull
import javax.inject.Inject

@AndroidEntryPoint
class AccountDetailsFragment :
    NoBottomNavViewBindingFragment<AccountDetailsFragmentBinding>(bindViewBy = {
        AccountDetailsFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val appDataViewModel: AppDataViewModel by activityViewModels()

    private val accountDetailsViewModel: AccountDetailsViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }

    private val dataUsageViewModel: DataUsageViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }
    private val subscriptionsViewModel: ContentSubscriptionsViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }

    private val viewPagerHeightAnimator by lazy { ViewPager2ViewHeightAnimator() }

    private val verifyOtpViewModel: VerifyOtpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountDetailsViewModel.selectedEnrolledAccount =
            requireArguments().getSerializable(SELECTED_ENROLLED_ACCOUNT) as EnrolledAccount
        accountDetailsViewModel.setAccountAlias(accountDetailsViewModel.selectedEnrolledAccount.accountAlias)

        fetchData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setLightStatusBar()

        val personalizedCampaignsAdapter = PersonalizedCampaignsAdapter {
            when (it.availableCampaignPromosModel.promoType) {
                EXCLUSIVE_OFFERS -> viewOffers(
                    listOf(it.availableCampaignPromosModel),
                    OfferType.EXCLUSIVE_PROMOS
                )

                FREEBIE_OR_SURPRISE -> pullFreebie(it.availableCampaignPromosModel)

                SIM_SAMPLER ->
                    if (it.brand != AccountBrand.Tm) pullFreebie(it.availableCampaignPromosModel)
                    else viewOffers(listOf(it.availableCampaignPromosModel), OfferType.FREEBIE)

                NONE -> Unit
            }
        }

        with(viewBinding) {

            val balanceViewRewards =
                if (accountDetailsViewModel.selectedEnrolledAccount.isPostpaid()) {
                    balanceViewRewardsPostpaid
                } else {
                    balanceViewRewardsPrepaid
                }
            val balanceViewGCash =
                if (accountDetailsViewModel.selectedEnrolledAccount.isPostpaid()) {
                    balanceViewGCashPostpaid
                } else {
                    balanceViewGCashPrepaid
                }
            val clBalances =
                if (accountDetailsViewModel.selectedEnrolledAccount.isPostpaid()) {
                    llBalancesPostpaid
                } else {
                    clBalancesPrepaid
                }

            vp2PersonalizedCampaigns.adapter = personalizedCampaignsAdapter

            wfHome.onBack {
                findNavController().navigateUp()
            }

            btnPos.setOnClickListener {
                accountDetailsViewModel.tryToOpenPOS()
            }

            tvViewDetails.setOnClickListener {
                findNavController().safeNavigate(R.id.action_accountDetailsFragment_to_accountPlanDetailsFragment)
            }

            srlAccountDetails.setRefreshListener {
                appDataViewModel.refreshAccountDetailsData(accountDetailsViewModel.selectedEnrolledAccount.primaryMsisdn)
                fetchData()
            }

            with(accountDetailsViewModel) {
                accountAlias.observe(viewLifecycleOwner, { alias ->
                    tvAccountName.text = alias
                })
                with(selectedEnrolledAccount) {
                    mobileNumber?.let {
                        tvMobileNumber.text = it.toDisplayUINumberFormat()
                        tvMobileNumber.visibility = View.VISIBLE
                        ivMobileNumber.visibility = View.VISIBLE
                    }
                    landlineNumber?.nonEmptyOrNull()?.let {
                        val number =
                            it.formatLandlineNumber()
                        tvLandlineNumber.text = number

                        tvLandlineNumber.isVisible = number.isNotEmpty()
                        ivLandlineNumber.isVisible = number.isNotEmpty()
                    }
                    accountNumber?.let {
                        tvAccountNumber.text = it
                        tvAccountNumber.visibility = View.VISIBLE
                        tvAccountNumberLabel.visibility = View.VISIBLE
                    }

                    if (!isBroadband()) {
                        balanceViewGCash.visibility = View.VISIBLE
                    }
                    if (isPostpaid()) {
                        tvViewDetails.visibility = View.VISIBLE
                        if (isBroadband()) {
                            incPostpaidBroadband.root.visibility = View.VISIBLE
                        }
                        tvYourSubscriptions.text = getString(R.string.your_plan_usage)
                    }

                    vpAccountDetails.adapter =
                        AccountDetailsPagerAdapter(this@AccountDetailsFragment, isPostpaidMobile())

                    TabLayoutMediator(tlAccountDetails, vpAccountDetails) { tab, position ->
                        tab.text = when (position) {

                            TAB_POSITION_DATA -> resources.getString(R.string.account_details_data_tab)

                            TAB_POSITION_CONTENT -> resources.getString(R.string.account_details_content_tab)

                            TAB_POSITION_CALLS -> resources.getString(if (isPostpaidMobile()) R.string.account_details_calls_and_text_tab else R.string.account_details_calls_tab)

                            TAB_POSITION_TEXT -> resources.getString(R.string.account_details_text_tab)

                            else -> throw IllegalStateException()
                        }
                    }.attach()

                    viewPagerHeightAnimator.viewPager2 = vpAccountDetails
                }

                accountDetails.observe(viewLifecycleOwner, {
                    if (it.accountNumber.isNotEmpty()) {
                        tvAccountNumber.text = it.accountNumber
                        tvAccountNumber.visibility = View.VISIBLE
                        tvAccountNumberLabel.visibility = View.VISIBLE
                    }

                    it.landlineNumber?.nonEmptyOrNull()?.let {
                        val number =
                            it.formatLandlineNumber()
                        tvLandlineNumber.text = number

                        tvLandlineNumber.isVisible = number.isNotEmpty()
                        ivLandlineNumber.isVisible = number.isNotEmpty()
                    }
                })

                // Recalculate ViewPager height when subscriptions loaded into one of inner fragments
                subscriptionsDataLoadedEvent.observe(viewLifecycleOwner, {
                    it.handleEvent { tabPosition ->
                        viewPagerHeightAnimator.recalculate(tabPosition)
                    }
                })

                // Handle refreshing state
                isRefreshingData.observe(viewLifecycleOwner) { refreshing ->
                    srlAccountDetails.setRefreshing(refreshing)
                }

                // Brand
                brandStatus.observe(viewLifecycleOwner) { status ->
                    lavBrandLoading.apply {
                        isVisible = (status is BrandStatus.Loading).also { loading ->
                            if (loading) {
                                frame = 0
                                playAnimation()
                            }
                        }
                    }
                    tvBrandLabel.apply {
                        text = when (status) {
                            is BrandStatus.Success -> {
                                with(status.uiAccountBrand) {
                                    if (brand == AccountBrand.Hpw && tlAccountDetails.tabCount == 4) {
                                        tlAccountDetails.removeTabAt(TAB_POSITION_TEXT)
                                        tlAccountDetails.removeTabAt(TAB_POSITION_CALLS)
                                    }
                                    if (this is UIAccountBrand.PlatinumBrand)
                                        GLOBE_PLATINUM
                                    else
                                        brand.toUserFriendlyBrandName(selectedEnrolledAccount.segment)
                                }

                            }
                            else -> ""
                        }
                    }

                    // Set empty state for data usage if brand loading wasn't successful
                    if (status is BrandStatus.Empty)
                        dataUsageViewModel.setDataUsageStatus(DataUsageStatus.Empty)
                }

                // Retrieve data usage info only once after brand was loaded
                brandLoadedEvent.oneTimeEventObserve(viewLifecycleOwner, {
                    if (!selectedEnrolledAccount.isPostpaidBroadband()) {
                        dataUsageViewModel.fetchDataUsage(
                            selectedEnrolledAccount,
                            accountAlias.value ?: ""
                        )
                    }
                })

                postpaidBillStatus.observe(viewLifecycleOwner, { billStatus ->
                    accountStatus.value?.let { accountStatus ->
                        balancePostpaidView.setupWithStatuses(billStatus, accountStatus)
                    }
                })

                // Balances
                loadBalanceStatus.observe(viewLifecycleOwner) {
                    balanceViewLoad.updateBalanceStatus(it, LoadBalance)
                }

                rewardPointsStatus.observe(viewLifecycleOwner) {
                    balanceViewRewards.updateBalanceStatus(it, RewardPoints)
                }

                gCashBalanceStatus.observe(viewLifecycleOwner) {
                    balanceViewGCash.updateBalanceStatus(it, GCashBalance)
                }

                loanBalanceStatus.observe(viewLifecycleOwner) {
                    balanceViewLoan.updateBalanceStatus(it, LoanBalance)
                }

                customerCampaignPromo.observe(viewLifecycleOwner) {
                    personalizedCampaignsAdapter.submitList(it)
                    di2PersonalizedCampaigns.attachViewPager(vp2PersonalizedCampaigns)
                    vp2PersonalizedCampaigns.isVisible = it.isNotEmpty()
                    di2PersonalizedCampaigns.isVisible = it.size > 1
                }

                posAvailable.observe(viewLifecycleOwner) { isAvailable ->
                    btnPos.isVisible = isAvailable
                }

                openPOS.oneTimeEventObserve(viewLifecycleOwner) {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            HOME_SCREEN, CLICKABLE_ICON, CAMERA_SCANNING
                        )
                    )
                    findNavController().safeNavigate(
                        AccountDetailsFragmentDirections
                            .actionAccountDetailsFragmentToPosSubgraph(it)
                    )
                }

                accountStatus.observe(viewLifecycleOwner) { accountStatus ->
                    incInactiveAccountHeader.root.isVisible = accountStatus is Inactive
                    incInactiveAccount.root.isVisible = accountStatus is Inactive
                    balancePostpaidView.isVisible =
                        selectedEnrolledAccount.isPostpaid() && accountStatus !is Inactive

                    clBalances.isVisible = accountStatus !is Inactive

                    ivAccountStatus.setImageResource(
                        when (accountStatus) {
                            Active -> R.drawable.ic_status_active
                            Disconnected -> R.drawable.ic_status_disconnected
                            Inactive -> R.drawable.ic_status_inactive
                            else -> 0
                        }
                    )

                    postpaidBillStatus.value?.let { billStatus ->
                        accountStatus?.let { accountStatus ->
                            balancePostpaidView.setupWithStatuses(billStatus, accountStatus)
                        }
                    }
                }

                incInactiveAccount.btnRemoveAccount.setOnClickListener {
                    removeAccount(inactiveAccount = true)
                }

                accountDeleted.oneTimeEventObserve(viewLifecycleOwner) {
                    findNavController().popBackStack(R.id.dashboardFragment, false)
                }
            }

            ivEditAccount.setOnClickListener {
                findNavController().safeNavigate(R.id.action_accountDetailsFragment_to_accountDetailsEditFragment)
            }

            balanceViewLoad.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        HOME_SCREEN, CLICKABLE_TEXT, LOAD
                    )
                )
                findNavController().safeNavigate(
                    AccountDetailsFragmentDirections.actionAccountDetailsFragmentToShopSubgraph(
                        tabToSelect = LOAD_ID,
                        mobileNumber = accountDetailsViewModel.selectedEnrolledAccount.primaryMsisdn,
                        toolbarTab = getString(R.string.account_details)
                    )
                )
            }

            balanceViewGCash.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        HOME_SCREEN, CLICKABLE_TEXT, GCASH
                    )
                )
                if (accountDetailsViewModel.selectedEnrolledAccount.isGcashLinked)
                    context?.openPlayStore("com.globe.gcash.android")
                else {
                    verifyOtpViewModel.sendOtp(
                        accountDetailsViewModel.selectedEnrolledAccount.primaryMsisdn,
                        listOf(OTP_KEY_G_CASH_LINK)
                    )
                }
            }

            balanceViewLoan.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        HOME_SCREEN, CLICKABLE_TEXT, LOAN
                    )
                )
                findNavController().safeNavigate(
                    AccountDetailsFragmentDirections.actionAccountDetailsFragmentToShopSubgraph(
                        tabToSelect = BORROW_ID,
                        mobileNumber = accountDetailsViewModel.selectedEnrolledAccount.primaryMsisdn,
                        toolbarTab = getString(R.string.account_details)
                    )
                )
            }

            balanceViewRewards.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        HOME_SCREEN, CLICKABLE_TEXT, REWARDS
                    )
                )
                crossBackstackNavigator.crossNavigate(
                    BaseActivity.REWARDS_KEY,
                    R.id.allRewardsInnerFragment,
                    bundleOf(
                        TAB_KEY to RewardsCategory.NONE,
                        SELECTED_ENROLLED_ACCOUNT_KEY to accountDetailsViewModel.selectedEnrolledAccount
                    )
                )
            }

            verifyOtpViewModel.sendOtpResult.observe(viewLifecycleOwner) {
                it.handleEvent { result ->
                    when (result) {
                        is VerifyOtpViewModel.SendOtpResult.SentOtpSuccess -> {
                            findNavController().safeNavigate(
                                AccountDetailsFragmentDirections.actionAccountDetailsFragmentToLinkGCashOtpFragment(
                                    result,
                                    accountDetailsViewModel.selectedEnrolledAccount.accountAlias
                                )
                            )
                        }
                        else -> Unit
                    }
                }
            }

            btnActivity.setOnClickListener {
                findNavController().safeNavigate(
                    AccountDetailsFragmentDirections.actionAccountDetailsFragmentToRewardsPointsHistorySubgraph(
                        accountDetailsViewModel.selectedEnrolledAccount
                    )
                )
            }

            btnPocket.setOnClickListener {
                findNavController().safeNavigate(
                    AccountDetailsFragmentDirections.actionAccountDetailsFragmentToVouchersSubgraph(
                        accountDetailsViewModel.selectedEnrolledAccount
                    )
                )
            }
        }
    }

    private fun fetchData() {
        accountDetailsViewModel.fetchData(
            accountDetailsFailureCallback = { findNavController().navigateUp() }
        )
        dataUsageViewModel.setDataUsageStatus(DataUsageStatus.Loading)
        if (!accountDetailsViewModel.selectedEnrolledAccount.isPostpaidBroadband()) {
            subscriptionsViewModel.getContentSubscriptions(accountDetailsViewModel.selectedEnrolledAccount.primaryMsisdn)
        }
    }

    private fun pullFreebie(campaignPromos: AvailableCampaignPromosModel) {
        findNavController().safeNavigate(
            AccountDetailsFragmentDirections.actionAccountDetailsFragmentToPersonalizedCampaignsLoadingFragment(
                campaignPromos
            )
        )
    }

    private fun viewOffers(campaignPromos: List<AvailableCampaignPromosModel>, offer: OfferType) {
        findNavController().safeNavigate(
            AccountDetailsFragmentDirections.actionAccountDetailsFragmentToExclusivePromoFragment(
                campaignPromos.map { it.toParcelableModel() }.toTypedArray(), offer
            )
        )
    }

    override val logTag = "AccountDetailsEditFragment"

    override val analyticsScreenName = "account.details"
}

const val SELECTED_ENROLLED_ACCOUNT = "selected_enrolled_account"
