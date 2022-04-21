/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.BaseActivity.Companion.AUTH_KEY
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.dashboard.raffle.RaffleViewModel
import ph.com.globe.globeonesuperapp.databinding.RewardDetailsFragmentBinding
import ph.com.globe.globeonesuperapp.rewards.AccountValidation.*
import ph.com.globe.globeonesuperapp.rewards.allrewards.enrolled_accounts.EntryPoint
import ph.com.globe.globeonesuperapp.rewards.allrewards.enrolled_accounts.SelectEnrolledAccountFragment
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.toDisplayUINumberFormat
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.domain_models.isPostpaidBroadband
import ph.com.globe.model.rewards.LoyaltyProgramId
import ph.com.globe.model.rewards.RewardsCategory
import ph.com.globe.model.util.brand.AccountBrand
import javax.inject.Inject

@AndroidEntryPoint
class RewardDetailsFragment :
    NoBottomNavViewBindingFragment<RewardDetailsFragmentBinding>(bindViewBy = {
        RewardDetailsFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    private val rewardsViewModel: RewardsViewModel by navGraphViewModels(R.id.rewards_subgraph) { defaultViewModelProviderFactory }

    private val raffleViewModel: RaffleViewModel by navGraphViewModels(R.id.rewards_subgraph) { defaultViewModelProviderFactory }

    private val args by navArgs<RewardDetailsFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:rewardsredemption screen"))

        rewardsViewModel.clearAccountValidation()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setLightStatusBar()

        parentFragmentManager.setFragmentResultListener(
            SelectEnrolledAccountFragment.ENROLLED_ACCOUNT_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val result =
                bundle.getSerializable(SelectEnrolledAccountFragment.ENROLLED_ACCOUNT_NUMBER_KEY) as EnrolledAccount

            rewardsViewModel.setEnrolledAccount(result)
        }

        with(viewBinding) {

            wfRewardsCatalog.onBack {
                findNavController().navigateUp()
            }

            rewardsViewModel.isLoggedIn().let { loggedIn ->

                tvYouAreRedeemingFor.isVisible = loggedIn
                tilMobileNumber.isVisible = loggedIn
                clRedeem.isVisible = loggedIn
                btnSignUp.isVisible = !loggedIn
            }

            clRewardDetails.setBackgroundResource(
                when (args.rewardItem.category) {

                    RewardsCategory.OTHER -> {
                        if (args.rewardItem.pointsCost == "0") R.color.corporate_D_500
                        else R.color.corporate_A_400
                    }
                    RewardsCategory.DONATION -> R.color.corporate_C_400

                    RewardsCategory.PROMO -> R.color.prepaid_E_500

                    RewardsCategory.RAFFLE -> R.color.corporate_D_700

                    else -> R.color.corporate_D_500
                }
            )

            tvRewardTitle.text = args.rewardItem.name

            args.rewardItem.description.takeIf { it?.isNotBlank() ?: false }?.let {
                tvRewardDescription.text = it
                tvRewardDescription.isVisible = true
            }

            tvRewardCost.text = args.rewardItem.pointsCost.toInt().let { points ->
                if (points != 0) resources.getQuantityString(
                    R.plurals.reward_points_short,
                    points,
                    points
                )
                else getString(R.string.free)
            }

            vSelectAccount.setOnClickListener {
                findNavController().safeNavigate(
                    RewardDetailsFragmentDirections.actionRewardDetailsFragmentToSelectEnrolledAccountFragment(
                        EntryPoint.REWARDS,
                        getEligibleBrandForSelectedReward()
                    )
                )
            }
            btnSignUp.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        REWARDS_SCREEN, BUTTON, SIGN_UP_TO_REDEEM
                    )
                )
                generalEventsViewModel.lastNavHostFragmentKey(
                    BaseActivity.REWARDS_KEY,
                    R.id.rewardDetailsFragment,
                    args.toBundle()
                )
                crossBackstackNavigator.crossNavigate(
                    AUTH_KEY,
                    R.id.selectSignMethodFragment,
                    shouldPopToStartDestinationFromCurrentGraph = false
                )
            }

            btnNext.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Conversion,
                        REWARDS_SCREEN, BUTTON, REDEEM,
                        productName = args.rewardItem.name
                    )
                )
                findNavController().safeNavigate(
                    RewardDetailsFragmentDirections.actionRewardDetailsFragmentToRewardConfirmationFragment(
                        args.rewardItem
                    )
                )
            }

            raffleViewModel.kycCompleteLiveData.observe(viewLifecycleOwner, { complete ->
                if (args.rewardItem.category == RewardsCategory.RAFFLE) {
                    clKycStatus.visibility = View.VISIBLE

                    btnCompleteProfile.isVisible = !complete
                    btnCompleteProfile.setOnClickListener {
                        findNavController().safeNavigate(
                            RewardDetailsFragmentDirections.actionRewardDetailsFragmentToProfileSubgraph(
                                proceedForRaffle = true
                            )
                        )
                    }

                    tvKycStatusValue.apply {
                        text = getString(
                            if (complete)
                                R.string.raffle_profile_status_complete
                            else
                                R.string.raffle_profile_status_incomplete
                        )
                        setTextColor(
                            AppCompatResources.getColorStateList(
                                requireContext(),
                                if (complete)
                                    R.color.success
                                else
                                    R.color.caution
                            )
                        )
                    }
                    btnNext.isEnabled = complete
                }
            })

            rewardsViewModel.enrolledAccount.observe(viewLifecycleOwner) {
                if (it != null) {
                    etMobileNumber.setText(
                        it.enrolledAccount.primaryMsisdn.toDisplayUINumberFormat()
                    )
                    tilMobileNumber.hint = it.enrolledAccount.accountAlias

                    tvPts.isVisible = true
                    ivStarUser.isVisible = true
                    tvPts.text = "${it.points.toInt()} ${getString(R.string.pts)}"

                    // Validate account for selected reward
                    rewardsViewModel.validateReward(args.rewardItem)

                    tvInfo.isVisible = !it.enrolledAccount.isPostpaidBroadband()
                } else {
                    etMobileNumber.setText("")
                    tilMobileNumber.hint = getString(R.string.mobile_number)

                    tvPts.isVisible = false
                    ivStarUser.isVisible = false
                    tvInfo.isVisible = false
                }
            }

            rewardsViewModel.accountValidation.observe(viewLifecycleOwner) { validation ->
                when (validation) {
                    is AvailableToRedeem -> {
                        setNextButtonEnabled(true)
                    }
                    is NotEligibleNumber -> {
                        showErrorMessage(getString(R.string.cant_subscribe_to_this_reward))
                    }
                    is AlreadyRedeemed -> {
                        showErrorMessage(getString(R.string.you_can_redeem_this_once_per_account))
                    }
                    is BrandTypeGAH -> {
                        showErrorMessage(getString(R.string.this_reward_is_not_eligible_for_gah))
                        tvInfo.isVisible = false
                    }
                    is NoValidation -> {
                        setNextButtonEnabled(false)
                    }
                    is Failure -> {
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }

    private fun setNextButtonEnabled(enabled: Boolean) = with(viewBinding) {
        btnNext.isEnabled =
            enabled && (raffleViewModel.kycCompleteLiveData.value == true || args.rewardItem.category != RewardsCategory.RAFFLE)
        tilMobileNumber.error = null
        tilMobileNumber.isErrorEnabled = false
        tilMobileNumber.setBoxBackgroundColorResource(R.color.absolute_white)
        etMobileNumber.setTextColor(
            AppCompatResources.getColorStateList(
                requireContext(),
                R.color.accent_dark
            )
        )
    }

    private fun showErrorMessage(errorMessage: String) = with(viewBinding) {
        tilMobileNumber.isErrorEnabled = true
        tilMobileNumber.error = errorMessage
        tilMobileNumber.setBoxBackgroundColorResource(R.color.input_text_background_red)
        etMobileNumber.setTextColor(
            AppCompatResources.getColorStateList(
                requireContext(),
                R.color.error_text_red
            )
        )
        btnNext.isEnabled = false
    }

    private fun getEligibleBrandForSelectedReward(): Array<AccountBrandParcelable> {
        return args.rewardItem.loyaltyProgramIds.mapNotNull {
            when (it) {
                LoyaltyProgramId.PREPAID -> AccountBrand.GhpPrepaid
                LoyaltyProgramId.TM -> AccountBrand.Tm
                LoyaltyProgramId.HPW -> AccountBrand.Hpw
                LoyaltyProgramId.POSTPAID, LoyaltyProgramId.GAH -> AccountBrand.GhpPostpaid
                else -> null
            }?.toAccountBrandParcelable()
        }.toTypedArray()
    }

    override val logTag = "RewardDetailsFragment"

    override val analyticsScreenName = "reward.details"
}
