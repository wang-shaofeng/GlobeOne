/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.web_view_components

import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData

/**
 * Simple web view state observing component which handles and emits the current state of the provided web view.
 * Possible states [WebViewState]:
 * [LOADING]: representing state in which url is being loaded by either web client or web chrome client
 * [GENERIC_ERROR]: some kind of error occurred on web view client
 * [NO_INTERNET_ERROR]: error when there is no internet connection
 * [DISPLAYED]: state where web view has successfully loaded
 *
 */
class StateHandlerWebComponent(
    private val webView: WebView,
    private val globeWebViewClient: GlobeWebViewClient
) : WebComponent {

    private val _webViewStateStream = MediatorLiveData<WebViewState>().apply {
        addSource(
            globeWebViewClient.isLoading.asLiveData()
        ) { isLoading ->
            value = when (isLoading) {
                true -> WebViewState.Loading
                false -> WebViewState.Display
            }
        }

        addSource(globeWebViewClient.errorOccurred.asLiveData()) { errorOccurred ->
            value = when (errorOccurred) {
                is WebViewClientError.NoInternetError -> WebViewState.NoInternetError(
                    errorOccurred
                )
                is WebViewClientError.GenericError -> WebViewState.GenericError(errorOccurred)
                is WebViewClientError.Forbidden -> WebViewState.Forbidden(errorOccurred)
                is WebViewClientError.TimeoutConnection -> WebViewState.TimeoutError(errorOccurred)
                is WebViewClientError.InterceptError -> WebViewState.InterceptError(errorOccurred)
            }
        }

    }

    override val webViewStateStream: LiveData<WebViewState> = _webViewStateStream

    override fun show(url: String) = webView.also { it.clearHistory() }.loadUrl(url)
}
