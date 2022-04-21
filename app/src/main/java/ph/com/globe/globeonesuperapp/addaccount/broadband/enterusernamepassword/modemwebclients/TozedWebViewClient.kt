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

class TozedWebViewClient(
    private val context: Context,
    private val username: String,
    private val password: String,
    modemWebAppInterface: ModemWebAppInterface
) : BaseModemWebViewClient(modemWebAppInterface) {

    override fun onPageFinished(view: WebView?, url: String?) {
        when {
            url?.equals(URL_LANDING) == true -> {
                var loginJS = loadAssetTextAsString(context, "hack_thebox_login.js")
                loginJS = loginJS?.replace("HACK_mUsername_HACK", username)
                loginJS = loginJS?.replace("HACK_mPassword_HACK", password)
                loginJS?.let {
                    view?.loadUrl(it)
                }
            }
            url?.equals(URL_HOME) == true -> {
                val homeJS = loadAssetTextAsString(context, "hack_tozed_homeredirect.js")
                homeJS?.let {
                    view?.loadUrl(it)
                }
            }
            url?.contains(URL_MSGS) == true -> {
                var getOtpJS = loadAssetTextAsString(context, "hack_thebox_getotp.js")
                getOtpJS =
                    getOtpJS?.replace("HACK_VERIF_RESULT_TIMEOUT_HACK", "$VERIFY_RESULT_TIMEOUT")
                getOtpJS?.let {
                    view?.loadUrl(it)
                }
            }
        }
    }
}

private const val URL_LANDING = "http://192.168.254.254/index.html#index_status"
private const val URL_HOME = "http://192.168.254.254/index.html#home"
private const val URL_MSGS = "#sms"

private const val VERIFY_RESULT_TIMEOUT = 60000
