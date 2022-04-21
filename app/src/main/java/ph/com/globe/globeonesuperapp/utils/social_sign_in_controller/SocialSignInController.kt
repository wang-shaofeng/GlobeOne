/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.social_sign_in_controller

import android.content.Intent
import androidx.fragment.app.Fragment
import ph.com.globe.util.LfResult

interface SocialSignInController {

    fun signIn(fragment: Fragment, method: SignInMethod)

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun logOut()

    fun setSignInMethod(method: SignInMethod?)

    fun getSignInMethod(): SignInMethod?

    fun registerCallback(callback: (LfResult<SignInData, SignInException>) -> Unit)

    fun unregisterCallback()

    fun generateSocialAccessTokenFromEmail(
        fragment: Fragment,
        it: SignInData.Google.GoogleWithEmail
    )

    sealed class SignInMethod {

        object Facebook : SignInMethod() {
            override fun toString(): String = "facebook"
        }

        object Google : SignInMethod() {
            override fun toString(): String = "googleplus"
        }

        companion object {
            fun init(name: String?): SignInMethod? = when (name) {
                "facebook" -> Facebook
                "googleplus" -> Google
                else -> null
            }
        }
    }

    sealed class SignInData {
        data class Facebook(val token: String) : SignInData()
        sealed class Google : SignInData() {
            data class GoogleWithToken(val token: String) : Google()
            data class GoogleWithEmail(val email: String) : Google()
        }
    }
}
