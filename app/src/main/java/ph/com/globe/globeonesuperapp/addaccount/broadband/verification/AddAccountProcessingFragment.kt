/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband.verification

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.databinding.AddAccountSecurityQuestionsProcessingFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class AddAccountProcessingFragment :
    NoBottomNavViewBindingFragment<AddAccountSecurityQuestionsProcessingFragmentBinding>(
        bindViewBy = {
            AddAccountSecurityQuestionsProcessingFragmentBinding.inflate(it)
        }
    ) {

    private val viewModel: AddAccountProcessingViewModel by viewModels()

    private val addAccountSecurityQuestionsArgs: AddAccountProcessingFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setLightStatusBar()

        with(viewBinding) {
            addAccountSecurityQuestionsArgs.entryPoint.processingDescription?.let {
                tvTitle.text = it
            }
            addAccountSecurityQuestionsArgs.entryPoint.processingTitle?.let {
                tvDescription.text = it
            }
        }
        when (val entryPoint = addAccountSecurityQuestionsArgs.entryPoint) {
            is AddAccountProcessingFragmentEntryPoint.GetSecurityQuestionsEntryPoint -> {
                viewModel.getSecurityQuestions(entryPoint.msisdn)

                viewModel.getSecurityQuestionsResult.observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is SecurityQuestionsResult.GetSecurityQuestionsSuccessful -> {
                            findNavController().safeNavigate(
                                AddAccountProcessingFragmentDirections.actionAddAccountProcessingFragmentToAddAccountSecurityQuestionsFragment(
                                    result.securityQuestions,
                                    result.referenceId,
                                    entryPoint.msisdn,
                                    entryPoint.brand
                                )
                            )
                        }
                        is SecurityQuestionsResult.GetSecurityQuestionsFailed -> {
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }
    }

    override val logTag = "AddAccountSecurityQuestionsProcessingFragment"
}
