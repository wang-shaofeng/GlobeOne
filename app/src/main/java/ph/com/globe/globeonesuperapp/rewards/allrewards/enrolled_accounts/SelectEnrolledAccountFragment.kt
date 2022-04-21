/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.allrewards.enrolled_accounts

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.SelectEnrolledAccountFragmentBinding
import ph.com.globe.globeonesuperapp.rewards.RewardsViewModel
import ph.com.globe.globeonesuperapp.rewards.toAccountBrand
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class SelectEnrolledAccountFragment :
    NoBottomNavViewBindingFragment<SelectEnrolledAccountFragmentBinding>({
        SelectEnrolledAccountFragmentBinding.inflate(it)
    }) {

    private val enrolledAccountsViewModel: EnrolledAccountsViewModel by viewModels()

    private lateinit var rewardsViewModel: RewardsViewModel

    private val args: SelectEnrolledAccountFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If brand value passed through the arguments, try to initialize
        // ViewModel with this brand as eligible for selection and cached accounts
        args.eligibleBrands.let { eligibleBrands ->
            if (eligibleBrands != null) {
                rewardsViewModel =
                    navGraphViewModels<RewardsViewModel>(R.id.rewards_subgraph) { defaultViewModelProviderFactory }.value

                enrolledAccountsViewModel.initializeWithEligibleBrand(
                    eligibleBrands.map { it.toAccountBrand() }.toTypedArray(),
                    rewardsViewModel.cachedAccountsForSelection
                )
            } else {
                enrolledAccountsViewModel.initializeEnrolledAccount()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDarkStatusBar()

        with(viewBinding) {
            rvEnrolledAccounts.itemAnimator = null

            val enrolledAccountsAdapter = EnrolledAccountsAdapter {
                enrolledAccountsViewModel.selectMobileNumber(it)
            }

            ivClose.setOnClickListener { findNavController().navigateUp() }

            rvEnrolledAccounts.adapter = enrolledAccountsAdapter

            enrolledAccountsViewModel.accountsUIModels.observe(viewLifecycleOwner) {
                btnSelectAccount.isEnabled = it.any { it.selected }
                enrolledAccountsAdapter.submitList(it)

                // Save accounts for selection with loaded brands (flow with eligible brand)
                if (args.eligibleBrands != null) {
                    rewardsViewModel.cachedAccountsForSelection = it
                }
            }


            if (args.entryPoint == EntryPoint.HISTORY) {
                tvChooseAccountTitle.text = getString(R.string.choose_an_account)
                tvHeaderTitle.text = getString(R.string.wayfinder_history)
            }


            enrolledAccountsViewModel.navigateUp.observe(viewLifecycleOwner) {
                it.handleEvent {
                    setFragmentResult(
                        ENROLLED_ACCOUNT_REQUEST_KEY,
                        bundleOf(ENROLLED_ACCOUNT_NUMBER_KEY to it)
                    )
                    findNavController().navigateUp()
                }
            }

            btnSelectAccount.setOnClickListener {
                btnSelectAccount.isEnabled = false
                enrolledAccountsViewModel.done()
            }
        }
    }

    override val logTag: String = "SelectEnrolledAccountFragment"

    companion object {
        const val ENROLLED_ACCOUNT_REQUEST_KEY = "SelectEnrolledAccountFragment_requestKey"
        const val ENROLLED_ACCOUNT_NUMBER_KEY = "SelectEnrolledAccountFragment_number"
    }
}

enum class EntryPoint {
    REWARDS, HISTORY;
}
