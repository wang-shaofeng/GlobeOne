/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.web_view_components

import androidx.lifecycle.LiveData

/**
 * Interface used for implementation of specific web component
 */
interface WebComponent {

    val webViewStateStream: LiveData<WebViewState>

    /**
     * Shows the url
     */
    fun show(url: String)
}

/**
 * Interface used for implementation of specific intercepting web component
 */
interface InterceptWebComponent<T> : WebComponent {
    /**
     * Shows the url, and waits for the specific redirect url to occur, intercepting it, and extracting data
     */
    suspend fun showAndWaitForResult(url: String): T
}

/**
 * WebView state indicating whether the webView is currently loading, some error has occurred, auth cookie doesn't exists or displayed.
 */
sealed class WebViewState {

    object Loading : WebViewState()

    object Display : WebViewState()

    open class WebViewErrorState (val webViewClientError: WebViewClientError) : WebViewState()
    class GenericError(webViewClientError: WebViewClientError) : WebViewErrorState(webViewClientError)
    class NoInternetError(webViewClientError: WebViewClientError) : WebViewErrorState(webViewClientError)
    class TimeoutError(webViewClientError: WebViewClientError) : WebViewErrorState(webViewClientError)
    class Forbidden(webViewClientError: WebViewClientError) : WebViewErrorState(webViewClientError)
    class InterceptError(webViewClientError: WebViewClientError) : WebViewErrorState(webViewClientError)

    fun WebViewErrorState.handleWebViewClientErrorIfNeeded() {
        webViewClientError.additionalHandlingErrorAction.invoke()
    }
}
