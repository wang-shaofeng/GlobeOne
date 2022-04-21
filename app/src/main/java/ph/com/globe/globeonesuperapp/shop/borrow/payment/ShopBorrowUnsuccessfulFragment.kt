/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.borrow.payment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ShopBorrowUnsuccessfulFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

class ShopBorrowUnsuccessfulFragment :
    NoBottomNavViewBindingFragment<ShopBorrowUnsuccessfulFragmentBinding>(
        bindViewBy = {
            ShopBorrowUnsuccessfulFragmentBinding.inflate(it)
        }
    ) {

    private val shopBorrowUnsuccessfulFragmentArgs by navArgs<ShopBorrowUnsuccessfulFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            tvInfoMessage.text = shopBorrowUnsuccessfulFragmentArgs.errorMessage

            btnTryAgain.setOnClickListener {
                findNavController().popBackStack(R.id.shopItemDetailsFragment, false)
            }

            btnCancel.setOnClickListener {
                findNavController().popBackStack(R.id.shopFragment, false)
            }

            // Adding a callback on back pressed to replace the standard up navigation with popBackStack
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        findNavController().popBackStack(R.id.shopItemDetailsFragment, false)
                    }
                }
            )
        }
    }

    override val logTag = "ShopBorrowUnsuccessfulFragment"
}
