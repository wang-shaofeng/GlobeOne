/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.web_view_components

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import ph.com.globe.globeonesuperapp.BuildConfig
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.WebViewFragmentBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.util.fold

class SocialSingInWebViewFragment :
    NoBottomNavViewBindingFragment<WebViewFragmentBinding>(bindViewBy = {
        WebViewFragmentBinding.inflate(it)
    }) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            val loginOutcome = viewBinding.webView.interceptingWebComponent(
                InterceptingGlobeWebViewClient(urlInterceptor = GlobeSocialSignInUrlInterceptor())
            )
                .showAndWaitForResult(
                    url = "${BuildConfig.SOCIAL_LOGIN_URL}/${
                        arguments?.getParcelable<Provider>(PROVIDER).toString()
                    }/start?language_preference=en&token_url=${
                        getString(R.string.token_url)
                    }"
                )
            loginOutcome.fold({
                setFragmentResult(
                    SOCIAL_SIGN_IN_REQUEST_KEY,
                    bundleOf(
                        RESULT_TOKEN to it,
                        PROVIDER to arguments?.getParcelable<Provider>(PROVIDER)
                    )
                )
                findNavController().navigateUp()
            }, {
                findNavController().navigateUp()
            })
        }
    }

    override val logTag = "SocialSingInWebViewFragment"
}

const val RESULT_TOKEN: String = "resultToken"
const val PROVIDER: String = "provider"

const val SOCIAL_SIGN_IN_REQUEST_KEY = "SocialSingInWebViewFragment_requestKey"

sealed class Provider : Parcelable {

    open fun getParam() = toString()

    @Parcelize
    object Facebook : Provider() {
        override fun toString(): String = "facebook"
    }

    @Parcelize
    object Google : Provider() {
        override fun toString(): String = "googleplus"
    }

    @Parcelize
    object Yahoo : Provider() {
        override fun toString(): String = "yahoo-oauth2"
    }

    @Parcelize
    object Apple : Provider() {
        override fun getParam(): String = "apple-id"
        override fun toString(): String = "apple"
    }
}
