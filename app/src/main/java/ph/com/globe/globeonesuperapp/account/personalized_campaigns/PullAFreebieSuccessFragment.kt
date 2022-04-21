/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.personalized_campaigns

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.PullAFreebieSuccessFragmentBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class PullAFreebieSuccessFragment :
    NoBottomNavViewBindingFragment<PullAFreebieSuccessFragmentBinding>({
        PullAFreebieSuccessFragmentBinding.inflate(it)
    }) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {} // block on back press

        viewBinding.btnDone.setOnClickListener {
            findNavController().popBackStack(
                R.id.accountDetailsFragment,
                false
            )
        }
    }

    override val logTag: String = "PullAFreebieSuccessFragment"
}
