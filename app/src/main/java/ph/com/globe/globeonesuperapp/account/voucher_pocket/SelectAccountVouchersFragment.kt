/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.voucher_pocket

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.SelectAccountVouchersFragmentBinding
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class SelectAccountVouchersFragment :
    NoBottomNavViewBindingFragment<SelectAccountVouchersFragmentBinding>({
        SelectAccountVouchersFragmentBinding.inflate(it)
    }) {

    private val vouchersViewModel: VouchersViewModel by hiltNavGraphViewModels(R.id.vouchers_subgraph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDarkStatusBar()

        vouchersViewModel.getEnrolledAccounts()

        with(viewBinding) {
            val selectAccountVouchersRecyclerViewAdapter =
                SelectAccountVouchersRecyclerViewAdapter {
                    vouchersViewModel.accountClick(it.primaryMsisdn)
                }
            rvAccounts.adapter = selectAccountVouchersRecyclerViewAdapter
            rvAccounts.itemAnimator = null

            vouchersViewModel.enrolledAccounts.observe(viewLifecycleOwner, {
                selectAccountVouchersRecyclerViewAdapter.submitList(it)
            })

            btnSelectAccount.setOnClickListener {
                vouchersViewModel.selectOtherAccount()
                findNavController().navigateUp()
            }

            ivClose.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    override val logTag = "SelectAccountVouchersFragment"
}
