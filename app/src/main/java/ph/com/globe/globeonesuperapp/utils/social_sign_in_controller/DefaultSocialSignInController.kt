/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.social_sign_in_controller

import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold

class DefaultSocialSignInController(
    private val facebookSingInController: FacebookSingInController,
    private val googleSignInController: GoogleSignInController,
    private val sharedPreferences: SharedPreferences
) : SocialSignInController {

    private var callback: ((LfResult<SocialSignInController.SignInData, SignInException>) -> Unit)? =
        null

    init {
        facebookSingInController.registerTokenCallback {
            response(it, SocialSignInController.SignInMethod.Facebook)
        }

        googleSignInController.registerTokenCallback {
            response(it, SocialSignInController.SignInMethod.Google)
        }

        googleSignInController.registerEmailCallback {
            callback?.invoke(it.fold({
                LfResult.success(SocialSignInController.SignInData.Google.GoogleWithEmail(it))
            }, {
                setSignInMethod(null)
                LfResult.failure(it)
            }))
        }
    }

    override fun signIn(fragment: Fragment, method: SocialSignInController.SignInMethod) {
        setSignInMethod(method)
        currentSocialSignInMethod { signIn(fragment) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        currentSocialSignInMethod { onActivityResult(requestCode, resultCode, data) }
    }

    override fun logOut() {
        currentSocialSignInMethod { logOut() }
    }

    private fun currentSocialSignInMethod(method: SignInController.() -> Unit) {
        when (getSignInMethod()) {
            SocialSignInController.SignInMethod.Facebook -> method(facebookSingInController)
            SocialSignInController.SignInMethod.Google -> method(googleSignInController)
        }
    }

    override fun setSignInMethod(method: SocialSignInController.SignInMethod?) {
        sharedPreferences.edit(true) {
            if (method == null) {
                remove(CURRENT_SIGN_IN_METHOD_KEY)
            } else {
                putString(CURRENT_SIGN_IN_METHOD_KEY, method.toString())
            }
        }
    }

    override fun getSignInMethod(): SocialSignInController.SignInMethod? =
        SocialSignInController.SignInMethod.init(
            sharedPreferences.getString(CURRENT_SIGN_IN_METHOD_KEY, null)
        )

    override fun registerCallback(callback: (LfResult<SocialSignInController.SignInData, SignInException>) -> Unit) {
        this.callback = callback
    }

    override fun unregisterCallback() {
        callback = null
    }

    override fun generateSocialAccessTokenFromEmail(
        fragment: Fragment,
        it: SocialSignInController.SignInData.Google.GoogleWithEmail
    ) {
        if (getSignInMethod() == SocialSignInController.SignInMethod.Google) {
            googleSignInController.generateTokenFromEmail(fragment, it.email)
        }
    }

    private fun response(
        result: LfResult<String, SignInException>,
        method: SocialSignInController.SignInMethod
    ) {
        callback?.invoke(result.fold({
            val signInData = when (method) {
                SocialSignInController.SignInMethod.Facebook ->
                    SocialSignInController.SignInData.Facebook(it)
                SocialSignInController.SignInMethod.Google ->
                    SocialSignInController.SignInData.Google.GoogleWithToken(it)
            }
            LfResult.success(signInData)
        }, {
            setSignInMethod(null)
            LfResult.failure(it)
        }))
    }

    companion object {
        private const val CURRENT_SIGN_IN_METHOD_KEY = "current_sign_in_method_key"
    }
}

sealed class SignInException : Exception() {
    object CancelException : SignInException()
    object TokenIsEmptyException : SignInException()
    data class FacebookException(val e: com.facebook.FacebookException?) : SignInException()
    data class GoogleException(val e: Exception?) : SignInException()
}

interface SignInController {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun logOut()

    fun signIn(fragment: Fragment)
}
