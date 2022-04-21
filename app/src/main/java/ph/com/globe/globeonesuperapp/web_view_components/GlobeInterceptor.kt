/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.web_view_components

import android.util.Log
import ph.com.globe.globeonesuperapp.web_view_components.GlobeSocialSignInUrlInterceptor.Companion.TOKEN_URL_HOST
import ph.com.globe.globeonesuperapp.web_view_components.GlobeSocialSignInUrlInterceptor.Companion.TOKEN_URL_SCHEME
import ph.com.globe.model.payment.CANCEL_URL
import ph.com.globe.model.payment.SUCCESS_URL
import ph.com.globe.util.LfResult
import java.net.URI

interface GlobeInterceptor {
    /**
     * Attempts to intercept given URL.
     *
     * @return Parsed outcome with URL itself or error, if URL was intercepted. Will return null if URL was not intercepted.
     */
    fun intercept(url: String): LfResult<String, WebViewClientError.InterceptError>?
}

class GlobeSocialSignInUrlInterceptor : GlobeInterceptor {

    override fun intercept(url: String): LfResult<String, WebViewClientError.InterceptError>? {
        kotlin.runCatching {
            with(URI(url)) {
                Log.d("intercept scheme", this.scheme + "  host:  " + this.host)
                if (scheme == TOKEN_URL_SCHEME && host == TOKEN_URL_HOST) {
                    return if (url.socialSingInSuccessfulRedirectUrlValid()) {
                        LfResult.success(url.extractSocialSignInToken())
                    } else LfResult.failure(WebViewClientError.InterceptError())
                }
            }
        }
        return null
    }

    companion object {
        const val TOKEN_URL_SCHEME = "ph.com.globe.globeone.prod"
        const val TOKEN_URL_HOST = "oauth2redirect"
    }
}

fun String.socialSingInSuccessfulRedirectUrlValid() =
    contains(TOKEN_URL_SCHEME) && contains(TOKEN_URL_HOST) && contains("token")

fun String.extractSocialSignInToken(): String =
    split("token=")[1]

class GlobeGCashInterceptor : GlobeInterceptor {

    override fun intercept(url: String): LfResult<String, WebViewClientError.InterceptError>? {
        kotlin.runCatching {
            with(URI(url)) {
                Log.d("intercept URI: ", this.toString())
                if (this.toString().checkIfSuccessfulPayment()) {
                    return LfResult.success("success")
                } else if (this.toString().checkIfUnsuccessfulPayment()) {
                    return LfResult.failure(WebViewClientError.InterceptError())
                }
            }
        }
        return null
    }
}

fun String.checkIfSuccessfulPayment(): Boolean =
    contains(SUCCESS_URL)

fun String.checkIfUnsuccessfulPayment(): Boolean =
    contains(CANCEL_URL)
