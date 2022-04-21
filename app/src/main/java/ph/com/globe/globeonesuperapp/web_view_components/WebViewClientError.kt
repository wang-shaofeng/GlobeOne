/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.web_view_components

sealed class WebViewClientError {

    abstract val additionalHandlingErrorAction: () -> Unit

    data class NoInternetError(
        override val additionalHandlingErrorAction: () -> Unit = {}
    ) : WebViewClientError()

    data class GenericError(
        override val additionalHandlingErrorAction: () -> Unit = {}
    ) : WebViewClientError()

    data class Forbidden(
        override val additionalHandlingErrorAction: () -> Unit = {}
    ) : WebViewClientError()

    data class TimeoutConnection(
        override val additionalHandlingErrorAction: () -> Unit = {}
    ) : WebViewClientError()

    data class InterceptError(
        override val additionalHandlingErrorAction: () -> Unit = {}
    ) : WebViewClientError()
}
