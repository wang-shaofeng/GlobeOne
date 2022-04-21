/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband.enterusernamepassword.modemwebclients

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import ph.com.globe.globeonesuperapp.addaccount.broadband.enterusernamepassword.AddAccountEnterUsernamePasswordViewModel
import ph.com.globe.globeonesuperapp.addaccount.broadband.enterusernamepassword.AddAccountEnterUsernamePasswordViewModel.ModemWebAppInterface
import ph.com.globe.globeonesuperapp.utils.loadAssetTextAsString

class ShanghaiWebViewClient(
    private val context: Context,
    modemWebAppInterface: ModemWebAppInterface
) : BaseModemWebViewClient(modemWebAppInterface) {

    override fun onPageFinished(view: WebView?, url: String?) {
        val getOtpJS = loadAssetTextAsString(context, "hack_shanghai_getotp.js")
        getOtpJS?.let {
            view?.loadUrl(it)
        }
    }
}
