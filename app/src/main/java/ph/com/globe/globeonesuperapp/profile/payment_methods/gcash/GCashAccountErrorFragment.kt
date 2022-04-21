/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile.payment_methods.gcash

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.databinding.ProfileGcashAccountErrorFragmentBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class GCashAccountErrorFragment :
    NoBottomNavViewBindingFragment<ProfileGcashAccountErrorFragmentBinding>(
        bindViewBy = {
            ProfileGcashAccountErrorFragmentBinding.inflate(it)
        }
    ) {

    private val gCashAccountErrorArguments by navArgs<GCashAccountErrorFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gCashAccountErrorArguments.errorDescription.let { description ->

            // Setup error description text
            viewBinding.tvErrorDescription.text = description
        }

        viewBinding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override val logTag = "NoGCashAccountFoundFragment"
}
