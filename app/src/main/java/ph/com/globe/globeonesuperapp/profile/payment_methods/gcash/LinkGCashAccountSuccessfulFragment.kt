/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile.payment_methods.gcash

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ProfileLinkGCashSuccessfulFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.isOnBackStack
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class LinkGCashAccountSuccessfulFragment :
    NoBottomNavViewBindingFragment<ProfileLinkGCashSuccessfulFragmentBinding>(
        bindViewBy = {
            ProfileLinkGCashSuccessfulFragmentBinding.inflate(it)
        }
    ) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override val logTag = "LinkGCashAccountSuccessfulFragment"
}
