/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile.payment_methods.gcash

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ProfileGCashFragmentBinding
import ph.com.globe.globeonesuperapp.profile.payment_methods.PaymentMethodsViewModel
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class ManageGCashMethodFragment :
    NoBottomNavViewBindingFragment<ProfileGCashFragmentBinding>(bindViewBy = {
        ProfileGCashFragmentBinding.inflate(it)
    }) {

    private val paymentMethodsViewModel: PaymentMethodsViewModel by navGraphViewModels(R.id.payment_methods_subgraph) { defaultViewModelProviderFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshGCashItemsList(false)
        with(viewBinding) {
            wfPaymentMethods.onBack {
                findNavController().navigateUp()
            }
            ibEdit.setOnClickListener {
                refreshGCashItemsList(true)
            }
            btnCancel.setOnClickListener {
                refreshGCashItemsList(false)
            }
            btnAddGCashAccount.setOnClickListener {
                findNavController().safeNavigate(R.id.action_manageGCashMethodFragment_to_linkGCashFragment)
            }
            if (!paymentMethodsViewModel.linkedGCashAccounts.value.isNullOrEmpty() && paymentMethodsViewModel.notLinkedGCashAccounts.value.isNullOrEmpty()) {
                // case when all of the enrolled accounts are already linked.
                btnAddGCashAccount.isEnabled = false
                tvAllAccountsLinked.visibility = View.VISIBLE
            }
        }

        paymentMethodsViewModel.manageGCashLinkingResult.observe(viewLifecycleOwner, {
            it.handleEvent {
                refreshGCashItemsList(false)
                viewBinding.btnAddGCashAccount.isEnabled = true
                viewBinding.tvAllAccountsLinked.visibility = View.GONE
            }
        })
    }

    private fun refreshGCashItemsList(swipeLeft: Boolean) {
        val gCashAdapter =
            GCashAdapter(
                lifecycleOwner = viewLifecycleOwner,
                swipeLeft = swipeLeft,
                { gCashItem -> paymentMethodsViewModel.removeGCash(gCashItem) },
                { gCashItem -> paymentMethodsViewModel.getGCashBalanceAsLiveData(gCashItem) }
            )
        viewBinding.rvGCashAccounts.adapter = gCashAdapter
        gCashAdapter.submitList(paymentMethodsViewModel.linkedGCashAccounts.value?.map { gCashItem ->
            gCashItem.toGCashItem()
        })
        if (swipeLeft) {
            viewBinding.ibEdit.visibility = View.GONE
            viewBinding.btnCancel.visibility = View.VISIBLE
        } else {
            viewBinding.btnCancel.visibility = View.GONE
            viewBinding.ibEdit.visibility = View.VISIBLE
        }
    }

    override val logTag = "ManageGCashMethodFragment"
}
