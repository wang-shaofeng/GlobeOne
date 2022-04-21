/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband.enterusernamepassword.modemwebclients

import android.content.Context
import android.webkit.WebView
import ph.com.globe.globeonesuperapp.addaccount.broadband.enterusernamepassword.AddAccountEnterUsernamePasswordViewModel
import ph.com.globe.globeonesuperapp.addaccount.broadband.enterusernamepassword.AddAccountEnterUsernamePasswordViewModel.ModemWebAppInterface
import ph.com.globe.globeonesuperapp.utils.loadAssetTextAsString

class NotionWebViewClient(
    private val context: Context,
    private val username: String,
    private val password: String,
    modemWebAppInterface: ModemWebAppInterface
) : BaseModemWebViewClient(modemWebAppInterface) {

    override fun onPageFinished(view: WebView?, url: String?) {
        when {
            url?.equals(URL_HOME) == true -> {
                val loginJS = loadAssetTextAsString(context, "hack_notion_start.js")
                loginJS?.let {
                    view?.loadUrl(it)
                }
            }
            url?.equals(URL_LOGIN) == true -> {
                var loginJS = loadAssetTextAsString(context, "hack_notion_login.js")
                loginJS = loginJS?.replace("HACK_mUsername_HACK", username)
                loginJS = loginJS?.replace("HACK_mPassword_HACK", password)
                loginJS?.let {
                    view?.loadUrl(it)
                }
            }
            url?.equals(URL_OTP) == true -> {
                var getOtpJS = loadAssetTextAsString(context, "hack_notion_getotp.js")
                getOtpJS =
                    getOtpJS?.replace("HACK_VERIF_RESULT_TIMEOUT_HACK", "$VERIFY_RESULT_TIMEOUT")
                getOtpJS?.let {
                    view?.loadUrl(it)
                }
            }
        }
    }
}

private const val URL_HOME = "http://192.168.254.254/"
private const val URL_LOGIN = "http://192.168.254.254/#."
private const val URL_OTP = "http://192.168.254.254/#otp"

private const val VERIFY_RESULT_TIMEOUT = 60000
