/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.enterotp

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.databinding.WebViewFragmentBinding
import ph.com.globe.globeonesuperapp.utils.GatewayIpAddressProvider
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.web_view_components.toInteractiveWebView
import javax.inject.Inject

@AndroidEntryPoint
class BroadbandModemInboxWebViewFragment : NoBottomNavViewBindingFragment<WebViewFragmentBinding>(
    bindViewBy = {
        WebViewFragmentBinding.inflate(it)
    }
) {

    @Inject
    lateinit var gatewayIpAddressProvider: GatewayIpAddressProvider

//    private val handler = GeneralEventsHandlerProvider.generalEventsHandler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            clWebviewHeader.visibility = View.VISIBLE

            ivClose.setOnClickListener {
                findNavController().navigateUp()
            }

            // TODO: Temporarily
//            val interceptingGlobeWebViewClient = webView.interceptingWebComponent(
//                InterceptingGlobeWebViewClient(
//                    handleInterceptedUrl = { url -> false }),
//                isInteractiveWebView = true
//            ).apply {
//                show(gatewayIpAddressProvider.getGateway())
//            }

            webView.toInteractiveWebView()
            webView.webViewClient = WebViewClient()
            webView.loadUrl(gatewayIpAddressProvider.getGateway())

//            interceptingGlobeWebViewClient.webViewStateStream.observe(viewLifecycleOwner) {
//                when (it) {
//                    is WebViewState.WebViewErrorState -> {
//
//                        // Hide WebView
//                        webView.visibility = View.GONE
//
//                        handler.handleGeneralError(GeneralError.General)
//                    }
//                    else -> Unit
//                }
//            }
        }
    }

    override val logTag: String get() = "AddAccountEnterOtpWebViewFragment"
}
