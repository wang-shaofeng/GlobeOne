/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.dashboard

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.databinding.WebViewFragmentBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.web_view_components.toInteractiveWebView

@AndroidEntryPoint
class SpinwheelWebViewFragment : NoBottomNavViewBindingFragment<WebViewFragmentBinding>(
    bindViewBy = {
        WebViewFragmentBinding.inflate(it)
    }
){

    private val spinwheelFragmentArgs by navArgs<SpinwheelWebViewFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            clWebviewHeader.visibility = View.VISIBLE

            ivClose.setOnClickListener {
                findNavController().navigateUp()
            }

            webView.toInteractiveWebView()
            webView.webViewClient = WebViewClient()
            webView.loadUrl(spinwheelFragmentArgs.spinwheelUrl)
        }
    }

    override val logTag: String get() = "SpinwheelWebViewFragment"
}
