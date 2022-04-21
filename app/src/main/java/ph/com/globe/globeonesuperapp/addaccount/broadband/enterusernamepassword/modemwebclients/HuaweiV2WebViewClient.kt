/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband.enterusernamepassword.modemwebclients

import android.content.Context
import android.webkit.WebView
import ph.com.globe.globeonesuperapp.addaccount.broadband.enterusernamepassword.AddAccountEnterUsernamePasswordViewModel.ModemWebAppInterface
import ph.com.globe.globeonesuperapp.utils.loadAssetTextAsString

class HuaweiV2WebViewClient(
    private val context: Context,
    private val username: String,
    private val password: String,
    modemWebAppInterface: ModemWebAppInterface
) : BaseModemWebViewClient(modemWebAppInterface) {

    override fun onPageFinished(view: WebView?, url: String?) {
        when {
            url?.contains("overview") == true -> {
                var loginJS = loadAssetTextAsString(context, "hack_huawei_login_v2_overview.js")
                loginJS = loginJS?.replace("HACK_userId_HACK", username)
                loginJS = loginJS?.replace("HACK_password_HACK", password)
                loginJS?.let {
                    view?.loadUrl(it)
                }
            }
            url?.contains("home") == true -> {
                view?.loadUrl(url.replace("home", "sms"))
            }
            url?.contains("/content.html#sms") == true -> {
                val getOtpJS = loadAssetTextAsString(context, "hack_huawei_get_otp_v2.js")
                getOtpJS?.let {
                    view?.loadUrl(it)
                }
            }
        }

        super.onPageFinished(view, url)
    }
}
