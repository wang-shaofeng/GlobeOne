/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account_activities.rewards

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.domain.utils.convertDateToGroupDataFormat
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account_activities.AccountActivitiesViewModel
import ph.com.globe.globeonesuperapp.databinding.AccountActivityDetailsFragmentBinding
import ph.com.globe.globeonesuperapp.utils.toDisplayUINumberFormat
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.account_activities.RewardsTransactionType

@AndroidEntryPoint
class AccountRewardDetailsFragment :
    NoBottomNavViewBindingFragment<AccountActivityDetailsFragmentBinding>({
        AccountActivityDetailsFragmentBinding.inflate(it)
    }) {

    private val accountActivityViewModel by hiltNavGraphViewModels<AccountActivitiesViewModel>(R.id.account_activities_subgraph)

    private val args by navArgs<AccountRewardDetailsFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(viewBinding) {
            accountActivityViewModel.enrolledAccount.observe(viewLifecycleOwner) {
                tvAccount.text = it.accountAlias
                tvNumber.text = it.primaryMsisdn.toDisplayUINumberFormat()
            }

            with(args.transaction) {
                tvDate.text = date.convertDateToGroupDataFormat()
                tvPoints.text = (if (totalPoints > 0f) "+" else "") +
                        tvPoints.resources.getQuantityString(
                            R.plurals.reward_points_short_decimal,
                            if (totalPoints == 1.0) 1 else 2,
                            totalPoints
                        )

                when (val type = rewardsTransactionType) {
                    RewardsTransactionType.EarnedPoints -> {
                        ivTransactionType.setImageResource(R.drawable.ic_earned_points)

                        tvTransactionType.text = getString(R.string.earned_points_title)
                        tvTransactionDetails.text = getString(R.string.earned_points_details)

                        tvBy.text = getString(R.string.earned_by)
                        tvOn.text = getString(R.string.earned_on)

                        cvTransactionDetails.isVisible = true
                    }
                    RewardsTransactionType.RedeemedReward -> {
                        ivTransactionType.setImageResource(R.drawable.ic_redeemed_reward)

                        tvTransactionType.text = getString(R.string.redeemed_reward_title)

                        tvBy.text = getString(R.string.redeemed_by)
                        tvOn.text = getString(R.string.redeemed_on)
                    }
                    is RewardsTransactionType.PaidWithPoints -> {
                        ivTransactionType.setImageResource(R.drawable.ic_paid_with_points)

                        tvTransactionType.text = getString(R.string.paid_with_points_title)
                        gStore.isVisible = true
                        tvStoreName.text = type.partner

                        tvBy.text = getString(R.string.redeemed_by)
                        tvOn.text = getString(R.string.redeemed_on)
                    }
                    RewardsTransactionType.ExpiredPoints -> {
                        ivTransactionType.setImageResource(R.drawable.ic_expired_points)

                        tvTransactionType.text = getString(R.string.expired_points_title)

                        tvBy.text = getString(R.string.earned_by)
                        tvOn.text = getString(R.string.earned_on)
                    }
                    RewardsTransactionType.RefundedPoints -> {
                        ivTransactionType.setImageResource(R.drawable.ic_refunded_points)

                        tvTransactionType.text = getString(R.string.refunded_points_title)
                        tvTransactionDetails.text = getString(R.string.refunded_points_details)

                        tvBy.text = getString(R.string.earned_by)
                        tvOn.text = getString(R.string.earned_on)

                        cvTransactionDetails.isVisible = true
                    }
                    RewardsTransactionType.GiftedReward -> {
                        ivTransactionType.setImageResource(R.drawable.ic_gifted_reward)

                        tvTransactionType.text = getString(R.string.gifted_reward_title)

                        tvBy.text = getString(R.string.sent_by)
                        tvOn.text = getString(R.string.sent_to)
                    }
                    RewardsTransactionType.DeductedPoints -> {
                        ivTransactionType.setImageResource(R.drawable.ic_deducted_points)

                        tvTransactionType.text = getString(R.string.deducted_points_title)
                        tvTransactionDetails.text = getString(R.string.deducted_points_details)

                        tvBy.text = getString(R.string.deducted_from)
                        tvOn.text = getString(R.string.deducted_on)

                        cvTransactionDetails.isVisible = true
                    }
                }

                wfTransactionDetails.onBack { findNavController().navigateUp() }
            }
        }
    }

    override val logTag: String = "AccountActivityDetailsFragment"
}
