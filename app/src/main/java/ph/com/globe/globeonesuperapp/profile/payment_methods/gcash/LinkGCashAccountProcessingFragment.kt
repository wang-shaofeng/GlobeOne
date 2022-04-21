/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile.payment_methods.gcash

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.errors.payment.LinkingGCashError
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ProfileGCashLinkingProcessingFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class LinkGCashAccountProcessingFragment :
    NoBottomNavViewBindingFragment<ProfileGCashLinkingProcessingFragmentBinding>(
        bindViewBy = {
            ProfileGCashLinkingProcessingFragmentBinding.inflate(it)
        }
    ) {

    private val viewModel: LinkGCashAccountProcessingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.linkGCashAccount(
            requireArguments().getString(ACCOUNT_ALIAS)!!,
            requireArguments().getString(REFERENCE_ID)!!
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLightStatusBar()

        viewModel.manageGCashLinkingResult.observe(viewLifecycleOwner) {
            it.handleEvent { result ->
                when (result) {
                    is GCashLinkingResult.GCashLinkedSuccessfully -> {
                        viewModel.refreshEnrolledAccounts()
                        findNavController().safeNavigate(R.id.action_linkGCashAccountProcessingFragment_to_linkGCashAccountSuccessfulFragment)
                    }
                    is GCashLinkingResult.GCashLinkingFailed -> {
                        findNavController().safeNavigate(
                            LinkGCashAccountProcessingFragmentDirections.actionLinkGCashAccountProcessingFragmentToGCashAccountErrorFragment(
                                when (result.error) {
                                    is LinkingGCashError.NoGCashAccountLinkedError -> {
                                        getString(R.string.gcash_error_no_gcash_account)
                                    }
                                    is LinkingGCashError.SuspendedOrInactiveError -> {
                                        getString(R.string.gcash_error_suspended_or_inactive)
                                    }
                                    else -> {
                                        getString(R.string.try_again_later)
                                    }
                                }
                            )
                        )
                    }
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = Unit
            })
    }

    override val logTag = "LinkGCashAccountProcessingFragment"
}

const val ACCOUNT_ALIAS = "AccountAlias"
const val REFERENCE_ID = "ReferenceId"
