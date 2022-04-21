/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.coming_soon

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import ph.com.globe.globeonesuperapp.databinding.ComingSoonFragmentBinding
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

class ComingSoonFragment : NoBottomNavViewBindingFragment<ComingSoonFragmentBinding>(bindViewBy = {
    ComingSoonFragmentBinding.inflate(it)
}) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDarkStatusBar()

        viewBinding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override val logTag = "ComingSoonFragment"

}
