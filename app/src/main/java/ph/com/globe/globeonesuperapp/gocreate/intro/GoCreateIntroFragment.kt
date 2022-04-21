/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.gocreate.intro

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GoCreateIntroFragmentBinding
import ph.com.globe.globeonesuperapp.gocreate.GoCreateGeneralViewModel
import ph.com.globe.globeonesuperapp.gocreate.loading.LoadingEntryPoint
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class GoCreateIntroFragment :
    NoBottomNavViewBindingFragment<GoCreateIntroFragmentBinding>(bindViewBy = {
        GoCreateIntroFragmentBinding.inflate(it)
    }) {

    private val goCreateIntroArgs: GoCreateIntroFragmentArgs by navArgs()

    private val generalViewModel: GoCreateGeneralViewModel by hiltNavGraphViewModels(R.id.go_create_subgraph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setLightStatusBar()

        with(viewBinding) {
            with(generalViewModel) {

                goCreateIntroArgs.entryPoint.let { title ->
                    setEntryPointTitle(title)
                    wfGoCreate.setLabel(title)
                }

                goCreateIntroArgs.mobileNumber?.let { number ->
                    if (!isMobileNumberSelected) {
                        setMobileNumber(number)
                    }
                }

                wfGoCreate.onBack {
                    findNavController().navigateUp()
                }

                vSelectAccount.setOnClickListener {
                    findNavController().safeNavigate(R.id.action_goCreateIntroFragment_to_goCreateSelectAccountFragment)
                }

                mobileNumber.observe(viewLifecycleOwner) { number ->
                    etMobileNumber.setText(number)
                }

                brandEligibleStatus.observe(viewLifecycleOwner) { isBrandEligible ->
                    tilMobileNumber.error =
                        if (!isBrandEligible) getString(R.string.cant_subscribe_error) else null
                    btnGoCreate.isEnabled = isBrandEligible
                }

                accountName.observe(viewLifecycleOwner) { name ->
                    tilMobileNumber.hint = name ?: getString(R.string.mobile_number)
                }

                btnGoCreate.setOnClickListener {
                    // Clear previously selected configuration
                    generalViewModel.clearSelectedConfiguration()
                    when {
                        goCreateOffers.any() -> {
                            findNavController().safeNavigate(R.id.action_goCreateIntroFragment_to_goCreateSelectionFragment)
                        }
                        !isCatalogCached -> {
                            findNavController().safeNavigate(
                                GoCreateIntroFragmentDirections.actionGoCreateIntroFragmentToGoCreateLoadingFragment(
                                    LoadingEntryPoint.LoadCatalog
                                )
                            )
                        }
                        else -> showGeneralError()
                    }
                }
            }
        }
    }

    override val logTag = "GoCreateIntroFragment"
}
