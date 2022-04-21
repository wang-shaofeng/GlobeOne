/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rating

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.RatingFeedbackSuccessFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class RatingFeedbackSuccessFragment :
    NoBottomNavViewBindingFragment<RatingFeedbackSuccessFragmentBinding>(bindViewBy = {
        RatingFeedbackSuccessFragmentBinding.inflate(it)
    }) {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewBinding.btnDone.setOnClickListener {
            findNavController().popBackStack(R.id.ratingFragment, true)
        }
    }
    override val logTag = "RatingFeedbackSuccessFragment"
}
