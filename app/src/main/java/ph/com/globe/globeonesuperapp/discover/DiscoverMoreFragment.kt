/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.discover

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.DiscoverMoreFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.web_view_components.InterceptingGlobeWebViewClient
import ph.com.globe.globeonesuperapp.web_view_components.WebViewState
import ph.com.globe.globeonesuperapp.web_view_components.interceptingWebComponent
import javax.inject.Inject

@AndroidEntryPoint
class DiscoverMoreFragment :
    NoBottomNavViewBindingFragment<DiscoverMoreFragmentBinding>(bindViewBy = {
        DiscoverMoreFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val discoverMoreArgs by navArgs<DiscoverMoreFragmentArgs>()

    private var isLoadingPageError = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:viewarticles screen"))
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setLightStatusBar()

        with(viewBinding) {

            ivClose.setOnClickListener {
                findNavController().popBackStack()
            }

            val interceptingGlobeWebViewClient = InterceptingGlobeWebViewClient(
                handleInterceptedUrl = { url -> handleUrl(url) })

            val entryPageUrl = DISCOVER_MORE_PAGES_DIRECTORY.plus(DISCOVER_MORE_ENTRY_PAGE)

            // Setup WebView with InterceptingWebComponent
            val interceptingWebComponent =
                wvDiscoverMore.apply { settings.javaScriptEnabled = true }
                    .interceptingWebComponent(
                        interceptingGlobeWebViewClient,
                        isInteractiveWebView = false
                    )

            // Observe WebViewState values and perform actions upon that
            interceptingWebComponent.webViewStateStream.observe(viewLifecycleOwner) {
                when (it) {
                    is WebViewState.Display -> {
                        clLoading.visibility = View.GONE
                    }
                    is WebViewState.WebViewErrorState -> {
                        isLoadingPageError = true

                        // Hide WebView
                        wvDiscoverMore.visibility = View.GONE

                        // Configure and show error layout
                        btnGoBack.apply {
                            text = getString(
                                R.string.go_back_to_screen,
                                discoverMoreArgs.previousScreenTitle
                            )
                            setOnClickListener {
                                findNavController().popBackStack()
                            }
                        }
                        flLoadingError.visibility = View.VISIBLE
                    }
                    else -> Unit
                }
            }

            interceptingWebComponent.show(
                url = entryPageUrl
            )

            // Handle back button for WebView
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (wvDiscoverMore.canGoBack()
                            && !isLoadingPageError
                            && wvDiscoverMore.url?.contains(DISCOVER_MORE_ENTRY_PAGE) == false
                        ) {
                            wvDiscoverMore.goBack()
                        } else {
                            findNavController().popBackStack()
                        }
                    }
                }
            )
        }
    }

    private fun handleUrl(url: String): Boolean {
        return if (url.contains(DISCOVER_MORE_PAGES_DIRECTORY)) {
            false
        } else {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            true
        }
    }

    override val logTag = "DiscoverMoreFragment"

    override val analyticsScreenName: String = "discover_more"
}

const val DISCOVER_MORE_ENTRY_PAGE = "landing-page.html"
const val DISCOVER_MORE_PAGES_DIRECTORY = "file:///android_asset/discover_more/pages/"
