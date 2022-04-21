/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.or

import android.os.Bundle
import android.print.saveHtmlAsPdf
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.OrPaymentFragmentBinding
import ph.com.globe.globeonesuperapp.utils.permissions.registerActivityResultForStoragePermission
import ph.com.globe.globeonesuperapp.utils.permissions.requestStoragePermissionsIfNeededAndPerformSuccessAction
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.showSnackbar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.toDateOrNull
import ph.com.globe.util.toFormattedStringOrEmpty
import java.nio.charset.Charset

@AndroidEntryPoint
class ORPaymentFragment :
    NoBottomNavViewBindingFragment<OrPaymentFragmentBinding>(
        bindViewBy = { OrPaymentFragmentBinding.inflate(it) }
    ) {

    private val ORPaymentViewModel: ORPaymentViewModel by viewModels()

    private val args by navArgs<ORPaymentFragmentArgs>()

    var requestStorageActivityLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestStorageActivityLauncher = registerActivityResultForStoragePermission {
            viewBinding.handleDownload()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setDarkStatusBar()

        with(viewBinding) {

            ivClose.setOnClickListener {
                findNavController().navigateUp()
            }

            with(ORPaymentViewModel) {

                getPaymentOR(failureCallback = { findNavController().navigateUp() })

                wvReceipt.isVerticalScrollBarEnabled = false
                wvReceipt.isHorizontalScrollBarEnabled = false

                orPaymentStatus.observe(viewLifecycleOwner, { orPaymentStatusEvent ->

                    orPaymentStatusEvent.handleEvent { orPaymentStatus ->

                        if (orPaymentStatus is ORPaymentViewModel.ORPaymentStatus.Success) {

                            val encodedHtml = Base64.encodeToString(
                                orPaymentStatus.html.toByteArray(Charset.defaultCharset()),
                                Base64.NO_PADDING
                            )

                            // disable download button before wv is loaded, since we need it loaded for saving pdf
                            wvReceipt.webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    btnDownload.isEnabled = true
                                }
                            }

                            wvReceipt.loadData(encodedHtml, "text/html", "base64")

                            btnDownload.setOnClickListener {

                                if (requestStorageActivityLauncher != null) {
                                    requestStoragePermissionsIfNeededAndPerformSuccessAction(
                                        requestStorageActivityLauncher!!
                                    )
                                } else {
                                    handleDownload()
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    private fun OrPaymentFragmentBinding.handleDownload() {
        wvReceipt.saveHtmlAsPdf(
            "${args.payment.receiptId}_" +
                    "${
                        args.payment.date.toDateOrNull()
                            .toFormattedStringOrEmpty(GlobeDateFormat.ORDownload)
                    }.pdf"
        )

        GlobeSnackbarLayoutBinding.inflate(
            LayoutInflater.from(
                requireContext()
            )
        ).apply {
            tvGlobeSnackbarTitle.setText(R.string.payment_download_title)
            tvGlobeSnackbarDescription.setText(R.string.payment_download_description)
            showSnackbar(this)
        }
    }

    override val logTag = "ORPaymentFragment"
}
