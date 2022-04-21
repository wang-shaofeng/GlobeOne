/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.data_as_currency

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.DataAsCurrencyProcessingFragmentBinding
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.rewards.data_as_currency.ConversionResult.*
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate

@AndroidEntryPoint
class DataAsCurrencyProcessingFragment :
    NoBottomNavViewBindingFragment<DataAsCurrencyProcessingFragmentBinding>(bindViewBy = {
        DataAsCurrencyProcessingFragmentBinding.inflate(it)
    }) {

    private val dataAsCurrencyViewModel: DataAsCurrencyViewModel by navGraphViewModels(R.id.rewards_subgraph) { defaultViewModelProviderFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataAsCurrencyViewModel.convertData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setLightStatusBar()

        dataAsCurrencyViewModel.conversionResult.observe(viewLifecycleOwner) {
            it.handleEvent { result ->
                when (result) {
                    is ConversionSuccess -> {
                        findNavController().safeNavigate(
                            DataAsCurrencyProcessingFragmentDirections.actionDataAsCurrencyProcessingFragmentToDataAsCurrencySuccessfulFragment(
                                result.conversionDelay
                            )
                        )
                    }
                    else -> {
                        findNavController().safeNavigate(
                            DataAsCurrencyProcessingFragmentDirections.actionDataAsCurrencyProcessingFragmentToDataAsCurrencyUnsuccessfulFragment(
                                result
                            )
                        )
                    }
                }
            }
        }
    }

    override val logTag = "DataAsCurrencyProcessingFragment"
}
