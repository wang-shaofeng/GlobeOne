/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.data_as_currency

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.DataAsCurrencyUnsuccessfulFragmentBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.rewards.data_as_currency.ConversionResult.*

class DataAsCurrencyUnsuccessfulFragment :
    NoBottomNavViewBindingFragment<DataAsCurrencyUnsuccessfulFragmentBinding>(bindViewBy = {
        DataAsCurrencyUnsuccessfulFragmentBinding.inflate(it)
    }) {

    private val dacUnsuccessfulArgs: DataAsCurrencyUnsuccessfulFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        with(viewBinding) {

            when (dacUnsuccessfulArgs.conversionResult) {
                is ConversionFailure -> {
                    tvTitle.text = getString(R.string.data_conversion_failure_title)
                    tvDescription.text = getString(R.string.data_conversion_failure_description)
                }
                is NotEnoughData -> {
                    tvTitle.text = getString(R.string.data_conversion_not_enough_title)
                    tvDescription.text = getString(R.string.data_conversion_not_enough_description)
                }
            }

            btnTryAgain.setOnClickListener {
                findNavController().popBackStack(R.id.dataAsCurrencyFragment, false)
            }
        }

        // Disable back button
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}
    }

    override val logTag = "DataAsCurrencyUnsuccessfulFragment"
}
