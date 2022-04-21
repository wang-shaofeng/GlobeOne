/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.data_as_currency

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.domain.utils.convertDateToGroupDataFormat
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.SELECTED_ENROLLED_ACCOUNT
import ph.com.globe.globeonesuperapp.databinding.DataAsCurrencySuccessfulFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.rewards.RewardsCategory
import javax.inject.Inject

@AndroidEntryPoint
class DataAsCurrencySuccessfulFragment :
    NoBottomNavViewBindingFragment<DataAsCurrencySuccessfulFragmentBinding>(bindViewBy = {
        DataAsCurrencySuccessfulFragmentBinding.inflate(it)
    }) {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val dataAsCurrencyViewModel: DataAsCurrencyViewModel by navGraphViewModels(R.id.rewards_subgraph) { defaultViewModelProviderFactory }

    private val dacSuccessfulArgs: DataAsCurrencySuccessfulFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        with(viewBinding) {
            with(dataAsCurrencyViewModel) {

                tvTitle.text = getString(
                    if (dacSuccessfulArgs.conversionDelay)
                        R.string.data_conversion_success_with_delay_title
                    else
                        R.string.data_conversion_success_title
                )

                tvSuccessDescription.text = getString(
                    if (dacSuccessfulArgs.conversionDelay)
                        R.string.data_conversion_success_with_delay_description
                    else
                        R.string.data_conversion_success_description
                )

                rewardPoints.observe(viewLifecycleOwner) {
                    tvRewardPoints.text = resources.getQuantityString(
                        R.plurals.reward_points_short_decimal,
                        if (it == 1f) 1 else 2,
                        it
                    )
                }

                selectedQualification.observe(viewLifecycleOwner, { qualification ->
                    tvAccountName.text = qualification.accountName
                    tvAccountNumber.text = qualification.number

                    selectedAmount.value?.let { amount ->
                        val points = amount * qualification.exchangeRate
                        tvRewardPointsHighlighted.text = resources.getQuantityString(
                            R.plurals.reward_points_short,
                            points,
                            points
                        )

//                      Temporary commented due to API issue with data remaining from qualification API
//                        val dataRemaining = (qualification.dataRemaining - amount)
//                            .takeIf { remaining -> remaining > 0 } ?: 0
//                        tvRemainingDataValue.text = getString(R.string.remaining_gb, dataRemaining)
                    }

                    llRemainingData.setOnClickListener {
                        crossBackstackNavigator.crossNavigateWithoutHistory(
                            BaseActivity.DASHBOARD_KEY,
                            R.id.account_subgraph,
                            bundleOf(
                                SELECTED_ENROLLED_ACCOUNT to (qualification.enrolledAccount)
                            )
                        )
                    }
                })

                expireDate.observe(viewLifecycleOwner) { date ->
                    tvValidUntil.text = getString(
                        R.string.valid_until,
                        date.convertDateToGroupDataFormat()
                    )
                }

                llRewardPoints.setOnClickListener {
                    findNavController().popBackStack(R.id.rewardsFragment, false)
                }

                btnViewAll.setOnClickListener {
                    findNavController().safeNavigate(DataAsCurrencySuccessfulFragmentDirections.actionDataAsCurrencySuccessfulFragmentToAllRewardsInnerFragment(
                        tab = RewardsCategory.NONE
                    ))
                }

                btnDonate.setOnClickListener {
                    findNavController().safeNavigate(DataAsCurrencySuccessfulFragmentDirections.actionDataAsCurrencySuccessfulFragmentToAllRewardsInnerFragment(
                        tab = RewardsCategory.DONATION
                    ))
                }

                btnGoToHome.setOnClickListener {
                    crossBackstackNavigator.crossNavigateWithoutHistory(
                        BaseActivity.DASHBOARD_KEY,
                        R.id.dashboardFragment
                    )
                }

                // Disable back button
                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}
            }
        }
    }

    override val logTag = "DataAsCurrencySuccessfulFragment"
}
