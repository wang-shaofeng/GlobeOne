/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.gocreate.loading

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GoCreateLoadingFragmentBinding
import ph.com.globe.globeonesuperapp.gocreate.GoCreateGeneralViewModel
import ph.com.globe.globeonesuperapp.gocreate.loading.LoadCatalogResult.LoadSuccessful
import ph.com.globe.globeonesuperapp.gocreate.loading.LoadingEntryPoint.LoadCatalog
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class GoCreateLoadingFragment :
    NoBottomNavViewBindingFragment<GoCreateLoadingFragmentBinding>(bindViewBy = {
        GoCreateLoadingFragmentBinding.inflate(it)
    }) {

    private val goCreateLoadingArgs: GoCreateLoadingFragmentArgs by navArgs()

    private val generalViewModel: GoCreateGeneralViewModel by hiltNavGraphViewModels(R.id.go_create_subgraph)

    private val loadingViewModel: GoCreateLoadingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        when (goCreateLoadingArgs.entryPoint) {
            is LoadCatalog -> {
                setupLoadingText(
                    R.string.taking_you_to_go_create,
                    R.string.have_fun_with_mixing_things
                )
                loadingViewModel.loadCatalog()
            }
        }

        // Load catalog result
        loadingViewModel.loadCatalogResult.observe(viewLifecycleOwner) {
            it.handleEvent { result ->
                when (result) {
                    is LoadSuccessful -> {
                        generalViewModel.updateCatalogOffers(result.catalogOffers)

                        if (result.catalogOffers.any { it.isGoCreate }) {
                            findNavController().safeNavigate(R.id.action_goCreateLoadingFragment_to_goCreateSelectionFragment)
                        } else {
                            onLoadingFailed()
                        }
                    }
                    else -> {
                        onLoadingFailed()
                    }
                }
            }
        }

        // Disable back button
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}
    }

    private fun setupLoadingText(@StringRes title: Int, @StringRes message: Int) {
        with(viewBinding) {
            tvLoadingTitle.text = getString(title)
            tvLoadingMessage.text = getString(message)
        }
    }

    private fun onLoadingFailed() {
        generalViewModel.showGeneralError()
        findNavController().navigateUp()
    }

    override val logTag = "GoCreateLoadingFragment"
}
