/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.reset_password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.ResetPasswordEmailSentFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.isOnBackStack
import ph.com.globe.globeonesuperapp.utils.oneTimeEventObserve
import ph.com.globe.globeonesuperapp.utils.showSnackbar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class ResetPasswordEmailSentFragment :
    NoBottomNavViewBindingFragment<ResetPasswordEmailSentFragmentBinding>(bindViewBy = {
        ResetPasswordEmailSentFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    private val viewModel: ResetPasswordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {

            tvEmailAddress.text = requireArguments().getString(EMAIL_ADDRESS)

            btnResendLink.setOnClickListener {
                val email = viewBinding.tvEmailAddress.text.toString()
                logUiActionEvent("Send")
                viewModel.resendPasswordResetEmail(email)
            }

            clCloseWrapper.setOnClickListener {
                if (findNavController().isOnBackStack(R.id.loginFragment))
                    findNavController().popBackStack(R.id.loginFragment, false)
                else
                    findNavController().popBackStack(R.id.selectSignMethodFragment, false)
            }
            clNavigateUpWrapper.setOnClickListener {
                findNavController().navigateUp()
            }

            viewModel.resendResetPasswordResult.oneTimeEventObserve(viewLifecycleOwner) {
                if (it is ResetPasswordViewModel.ResetPasswordResult.EmailSentSuccessfully) {
                    val customSnackbarViewBinding =
                        GlobeSnackbarLayoutBinding.inflate(LayoutInflater.from(requireContext()))
                    customSnackbarViewBinding.tvGlobeSnackbarTitle.setText(R.string.reset_password_link_sent)
                    customSnackbarViewBinding.tvGlobeSnackbarDescription.setText(R.string.reset_password_link_sent_description)
                    showSnackbar(customSnackbarViewBinding)
                }
            }
        }
    }

    override val logTag = "ResetPasswordEmailSentFragment"

    override val analyticsScreenName: String = "enrollment.forgot_password"
}
