/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.web_view_components

import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView

fun WebView.toInteractiveWebView() = apply {
    CookieManager.getInstance().removeAllCookies(null)
    settings.run {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        javaScriptEnabled = true
        domStorageEnabled = true
        javaScriptCanOpenWindowsAutomatically = true
        supportMultipleWindows()
        builtInZoomControls = true
        displayZoomControls = false
        useWideViewPort = true
        loadWithOverviewMode = true
        cacheMode = WebSettings.LOAD_NO_CACHE
        userAgentString = userAgentString.replace("; wv", "")
    }

    setOnKeyListener { view, i, keyEvent ->
        if (keyEvent.action == KeyEvent.ACTION_DOWN) {
            val webView = view as WebView
            when (i) {
                KeyEvent.KEYCODE_BACK -> {
                    if (webView.canGoBack()) {
                        webView.goBack()
                        return@setOnKeyListener true
                    }
                }
            }
        }
        return@setOnKeyListener false
    }
}
