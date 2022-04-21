/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.web_view_components

import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.lifecycle.LiveData
import ph.com.globe.util.LfResult

class InterceptingWebComponent(
    webView: WebView,
    private val globeWebViewClient: InterceptingGlobeWebViewClient
) : InterceptWebComponent<LfResult<String, WebViewClientError.InterceptError>> {

    private val stateHandlerWebComponent =
        StateHandlerWebComponent(
            webView,
            globeWebViewClient
        )

    // the value to be observed and used to manipulate the loading overlay and screen display
    override val webViewStateStream: LiveData<WebViewState> =
        stateHandlerWebComponent.webViewStateStream

    override fun show(url: String) = stateHandlerWebComponent.show(url)

    override suspend fun showAndWaitForResult(url: String): LfResult<String, WebViewClientError.InterceptError> {
        show(url)
        return globeWebViewClient.interceptUrl()
    }
}

fun WebView.interceptingWebComponent(
    globeWebViewClient: InterceptingGlobeWebViewClient,
    isInteractiveWebView: Boolean = true
) = let { webView ->
    webView.webChromeClient = WebChromeClient()
    webView.webViewClient = globeWebViewClient
    if (isInteractiveWebView)
        webView.toInteractiveWebView()
    InterceptingWebComponent(this, globeWebViewClient)
}
