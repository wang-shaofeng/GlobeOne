/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.gocreate.select_account

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GoCreateSelectAccountFragmentBinding
import ph.com.globe.globeonesuperapp.gocreate.GoCreateGeneralViewModel
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class GoCreateSelectAccountFragment :
    NoBottomNavViewBindingFragment<GoCreateSelectAccountFragmentBinding>(bindViewBy = {
        GoCreateSelectAccountFragmentBinding.inflate(it)
    }) {

    private val generalViewModel: GoCreateGeneralViewModel by hiltNavGraphViewModels(R.id.go_create_subgraph)

    private val selectAccountViewModel: GoCreateSelectAccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectAccountViewModel.initEnrolledAccounts(generalViewModel.mobileNumber.value)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setDarkStatusBar()

        with(viewBinding) {

            generalViewModel.entryPointTitle.observe(viewLifecycleOwner) { title ->
                tvHeaderTitle.text = title
            }

            val accountsAdapter = GoCreateSelectAccountAdapter { selectedAccount ->
                selectAccountViewModel.selectMobileNumber(selectedAccount.msisdn)
                btnDone.isEnabled = true
            }

            rvAccounts.apply {
                adapter = accountsAdapter
                itemAnimator = null
            }

            selectAccountViewModel.enrolledAccountItems.observe(viewLifecycleOwner) { items ->
                accountsAdapter.submitList(items)
                btnDone.isEnabled = items.any { it.selected }
            }

            ivClose.setOnClickListener {
                findNavController().navigateUp()
            }

            btnDone.setOnClickListener {
                generalViewModel.setMobileNumber(selectAccountViewModel.getSelectedMobileNumber())
                findNavController().navigateUp()
            }
        }
    }

    override val logTag = "GoCreateSelectAccountFragment"
}
