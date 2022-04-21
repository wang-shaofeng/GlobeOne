/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.borrow.payment

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ShopBorrowProcessingFragmentBinding
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.ShopItemDetailsViewModel
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.ShopItemDetailsViewModel.ProcessLoanResult.*
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class ShopBorrowProcessingFragment :
    NoBottomNavViewBindingFragment<ShopBorrowProcessingFragmentBinding>(
        bindViewBy = {
            ShopBorrowProcessingFragmentBinding.inflate(it)
        }
    ) {

    private val shopItemDetailsViewModel: ShopItemDetailsViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }
    private val shopBorrowProcessingFragmentArgs: ShopBorrowProcessingFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        shopItemDetailsViewModel.processLoan(shopBorrowProcessingFragmentArgs.otpReferenceId)

        shopItemDetailsViewModel.loanProcessResult.observe(viewLifecycleOwner, {
            it.handleEvent { result ->
                when (result) {
                    is SuccessfulLoan -> {
                        findNavController().safeNavigate(
                            ShopBorrowProcessingFragmentDirections.actionShopBorrowProcessingFragmentToShopBorrowSuccessfulFragment(
                                result.amount,
                                result.serviceFee,
                                result.validity,
                                result.loanName,
                                result.sentTo
                            )
                        )
                    }

                    is HasLoan -> {
                        findNavController().safeNavigate(
                            ShopBorrowProcessingFragmentDirections.actionShopBorrowProcessingFragmentToShopBorrowUnsuccessfulFragment(
                                getString(R.string.already_have_loan),
                                getString(R.string.try_again_with_another_number)
                            )
                        )
                    }

                    is UnsuccessfulLoan -> {
                        findNavController().safeNavigate(
                            ShopBorrowProcessingFragmentDirections.actionShopBorrowProcessingFragmentToShopBorrowUnsuccessfulFragment(
                                getString(R.string.borrow_request_declined_description),
                                getString(R.string.try_again)
                            )
                        )
                    }
                }
            }
        })

        // Adding a callback on back pressed to replace the standard up navigation with popBackStack
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // This is left empty because you need to wait for the processing to complete
        }
    }

    override val logTag = "ShopBorrowProcessingFragment"
}
