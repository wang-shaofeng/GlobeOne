/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.termsandprivacypolicy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ph.com.globe.globeonesuperapp.databinding.TermsAndPrivacyPolicyFragmentBinding
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.web_view_components.InterceptingGlobeWebViewClient
import ph.com.globe.globeonesuperapp.web_view_components.interceptingWebComponent

@AndroidEntryPoint
class TermsAndPrivacyPolicyFragment :
    NoBottomNavViewBindingFragment<TermsAndPrivacyPolicyFragmentBinding>({
        TermsAndPrivacyPolicyFragmentBinding.inflate(it)
    }) {

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDarkStatusBar()

        with(viewBinding) {

            lifecycleScope.launch { // Setup WebView
                wvTermsAndConditions.interceptingWebComponent(
                    InterceptingGlobeWebViewClient(
                        handleInterceptedUrl = { url -> handleUrl(url) }),
                    isInteractiveWebView = false
                ).show(
                    url = PRIVACY_POLICY_ASSETS_PAGE
                )
            }

            btnGotIt.setOnClickListener { findNavController().navigateUp() }
        }
    }

    private fun handleUrl(url: String): Boolean {
        generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        return true
    }

    override val logTag = "TermsAndPrivacyPolicyFragment"
}

const val PRIVACY_POLICY_ASSETS_PAGE = "file:///android_asset/privacy_policy/privacy-policy.html"

const val TERMS_AND_CONDITIONS_URL = "https://www.globe.com.ph/apps-content/globeone/terms.html"
