/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.web_view_components

import android.graphics.Bitmap
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.globeonesuperapp.web_view_components.WebViewClientError.GenericError
import ph.com.globe.globeonesuperapp.web_view_components.WebViewClientError.InterceptError
import ph.com.globe.util.LfResult

abstract class GlobeWebViewClient : WebViewClient(), HasLogTag {

    override val logTag: String = "GlobeWebViewClient"

    private val _isLoading = MutableStateFlow(true)
    val isLoading: Flow<Boolean> = _isLoading

    private val _errorOccurred =
        MutableSharedFlow<WebViewClientError>(
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
            extraBufferCapacity = 1
        )
    val errorOccurred: Flow<WebViewClientError> = _errorOccurred.asSharedFlow()

    private fun notifyLoadingFinished() {
        _isLoading.value = false
    }

    private fun notifyLoadingStarted() {
        _isLoading.value = true
    }

    private fun notifyErrorOccurred(error: WebViewClientError) =
        _errorOccurred.tryEmit(error)

    private fun errorFromErrorCode(
        errorCode: Int?,
        webView: WebView?
    ): WebViewClientError {
        val handlingAction = {
            webView?.stopLoading()
            webView?.loadUrl(BLANK_PAGE_URL)
            Unit
        } // TODO Handle errors when we know what they are
        return GenericError(additionalHandlingErrorAction = handlingAction) // have generic error be propagated for every type of webview client error
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        notifyLoadingStarted()
        super.onPageStarted(view, url, favicon)
    }

    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            notifyErrorOccurred(
                error = errorFromErrorCode(errorCode, view)
            )

            notifyLoadingFinished()
            super.onReceivedError(view, errorCode, description, failingUrl)
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (request?.isForMainFrame == true)
                notifyErrorOccurred(
                    error = errorFromErrorCode(error?.errorCode, view)
                )

            notifyLoadingFinished()
            super.onReceivedError(view, request, error)
        }
    }

    override fun onPageCommitVisible(view: WebView?, url: String?) {
        notifyLoadingFinished()
        super.onPageCommitVisible(view, url)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        notifyLoadingFinished()
        super.onPageFinished(view, url)
    }

}

class InterceptingGlobeWebViewClient(
    private val urlInterceptor: GlobeInterceptor? = null,
    private val handleInterceptedUrl: HandleInterceptedUrl? = null
) : GlobeWebViewClient() {

    private val completableDeferred =
        CompletableDeferred<LfResult<String, InterceptError>>()

    suspend fun interceptUrl() = completableDeferred.await()

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) processUrl(request?.url.toString()) else false

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) processUrl(url.toString()) else false

    private fun processUrl(url: String?): Boolean = when (url) {
        null -> false
        else -> {
            handleInterceptedUrl.let {
                val shouldOverride = it?.invoke(url)
                val interceptedOutcome = urlInterceptor?.intercept(url)
                if (interceptedOutcome != null) {
                    completableDeferred.complete(interceptedOutcome)
                    true
                } else shouldOverride == true
            }
        }
    }
}

const val BLANK_PAGE_URL = "about:blank"

typealias HandleInterceptedUrl = (String) -> Boolean
