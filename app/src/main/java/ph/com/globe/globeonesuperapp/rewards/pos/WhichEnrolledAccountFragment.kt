/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.pos

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.WhichEnrolledAccountFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class WhichEnrolledAccountFragment :
    NoBottomNavViewBindingFragment<WhichEnrolledAccountFragmentBinding>({
        WhichEnrolledAccountFragmentBinding.inflate(it)
    }) {

    private val posViewModel by navGraphViewModels<POSViewModel>(R.id.pos_subgraph) { defaultViewModelProviderFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        posViewModel.getEnrolledAccounts()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = EnrolledAccountsWithPointsAdapter { posViewModel.select(it) }

        with(viewBinding) {
            ivBack.setOnClickListener { findNavController().navigateUp() }

            rvAccounts.adapter = adapter
            with(posViewModel) {
                enrolledAccountUiModel.observe(viewLifecycleOwner) {
                    it.find { it.isSelected }?.let {
                        btnNext.isEnabled = true
                    } ?: run { btnNext.isEnabled = false }
                    adapter.submitList(it)
                }

                merchantDetails?.let {
                    tvRewardCost.text = getString(R.string.mock_minimum_x_points, it.minimumPoints)
                    tvRewardTitle.text = it.merchantName
                }
            }

            btnNext.setOnClickListener {
                posViewModel.selectedAccount?.let {
                    findNavController().safeNavigate(
                        WhichEnrolledAccountFragmentDirections.actionWhichEnrolledAccountFragmentToPayPointsFragment()
                    )
                }
            }
        }
    }

    override val logTag: String = "WhichEnrolledAccountFragment"
}
