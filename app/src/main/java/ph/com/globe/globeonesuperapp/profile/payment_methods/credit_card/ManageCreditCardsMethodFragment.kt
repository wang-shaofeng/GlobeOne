/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile.payment_methods.credit_card

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ProfileManageCreditCardsFragmentBinding
import ph.com.globe.globeonesuperapp.profile.payment_methods.ManageCreditCardsResult
import ph.com.globe.globeonesuperapp.profile.payment_methods.PaymentMethodsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class ManageCreditCardsMethodFragment :
    NoBottomNavViewBindingFragment<ProfileManageCreditCardsFragmentBinding>(bindViewBy = {
        ProfileManageCreditCardsFragmentBinding.inflate(it)
    }) {

    private val paymentMethodsViewModel: PaymentMethodsViewModel by navGraphViewModels(R.id.payment_methods_subgraph) { defaultViewModelProviderFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshCreditCardList(false)
        with(viewBinding) {
            wfPaymentMethods.onBack {
                findNavController().navigateUp()
            }
            ibEdit.setOnClickListener {
                refreshCreditCardList(true)
            }
            btnCancel.setOnClickListener {
                refreshCreditCardList(false)
            }
        }

        paymentMethodsViewModel.manageCreditCardsResult.observe(viewLifecycleOwner, {
            when (it) {
                is ManageCreditCardsResult.CreditCardDeleted -> {
                    refreshCreditCardList(false)
                }
            }
        })
    }

    private fun refreshCreditCardList(swipeLeft: Boolean) {
        val creditCardsAdapter = CreditCardsAdapter(swipeLeft = swipeLeft) { creditCardItem ->
            paymentMethodsViewModel.removeCreditCard(creditCardItem)
        }
        viewBinding.rvCards.adapter = creditCardsAdapter
        creditCardsAdapter.submitList(paymentMethodsViewModel.linkedCreditCardAccounts.value?.map { card ->
            card.toCreditCardItem(requireContext())
        })
        if (swipeLeft) {
            viewBinding.ibEdit.visibility = View.GONE
            viewBinding.btnCancel.visibility = View.VISIBLE
        } else {
            viewBinding.btnCancel.visibility = View.GONE
            viewBinding.ibEdit.visibility = View.VISIBLE
        }
    }

    override val logTag = "ManageCreditCardsMethodFragment"
}
