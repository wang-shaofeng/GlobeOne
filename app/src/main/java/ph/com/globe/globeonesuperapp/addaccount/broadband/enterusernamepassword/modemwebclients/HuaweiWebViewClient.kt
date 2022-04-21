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

class HuaweiWebViewClient(
    private val context: Context,
    private val username: String,
    private val password: String,
    private val url: String,
    modemWebAppInterface: ModemWebAppInterface
) : BaseModemWebViewClient(modemWebAppInterface) {

    override fun onPageFinished(view: WebView?, url: String?) {
        if (url?.contains("overview") == true) {
            var loginJS = loadAssetTextAsString(context, "hack_huawei_login.js")
            loginJS = loginJS?.replace("HACK_userId_HACK", username)
            loginJS = loginJS?.replace("HACK_password_HACK", password)
            loginJS?.let {
                view?.loadUrl(it)
            }
        } else if (url?.contains("home") == true || url?.contains("modifypassword") == true) {
            view?.loadUrl(this.url)
        } else if (url?.contains("smsinbox") == true) {
            val getOtpJS = loadAssetTextAsString(context, "hack_huawei_get_otp.js")
            getOtpJS?.let {
                view?.loadUrl(it)
            }
        }
    }
}
