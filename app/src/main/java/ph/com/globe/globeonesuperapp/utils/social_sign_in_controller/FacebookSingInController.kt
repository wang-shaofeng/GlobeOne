/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.social_sign_in_controller

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.util.LfResult

class FacebookSingInController(private val context: Context) : SignInController, HasLogTag {
    private val callbackManager = CallbackManager.Factory.create()

    private val loginManager = LoginManager.getInstance()

    private var tokenCallback: ((LfResult<String, SignInException>) -> Unit)? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    init {
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                if (result?.accessToken?.token != null) {
                    tokenCallback?.invoke(LfResult.success(result.accessToken.token))
                } else {
                    tokenCallback?.invoke(LfResult.failure(SignInException.TokenIsEmptyException))
                }
            }

            override fun onCancel() {
                tokenCallback?.invoke(LfResult.failure(SignInException.CancelException))
            }

            override fun onError(error: FacebookException?) {
                tokenCallback?.invoke(LfResult.failure(SignInException.FacebookException(error)))
            }
        })
    }

    override fun signIn(fragment: Fragment) {
        val accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken?.token != null && (accessToken.isExpired || accessToken.token.isEmpty())) {
            tryToRetrieveLogin(fragment)
        } else if (accessToken == null) {
            signInInternal(fragment)
        } else {
            tokenCallback?.invoke(LfResult.success(accessToken.token))
        }
    }

    private fun tryToRetrieveLogin(fragment: Fragment) {
        loginManager.retrieveLoginStatus(
            context,
            object : LoginStatusCallback {
                override fun onCompleted(accessToken: AccessToken) {
                    tokenCallback?.invoke(LfResult.success(accessToken.token))
                }

                override fun onFailure() {
                    if (fragment.context != null) {
                        signInInternal(fragment)
                    }
                }

                override fun onError(exception: Exception?) {
                    if (fragment.context != null) {
                        signInInternal(fragment)
                    }
                }
            })
    }

    private fun signInInternal(fragment: Fragment) {
        loginManager.logIn(fragment, listOf(PERMISSION_EMAIL, PERMISSION_PUBLIC_PROFILE))
    }

    override fun logOut() {
        loginManager.logOut()
    }

    fun registerTokenCallback(tokenResult: (LfResult<String, SignInException>) -> Unit) {
        this.tokenCallback = tokenResult
    }

    override val logTag: String = "FacebookSingInController"

    companion object {
        private const val PERMISSION_EMAIL = "email"
        private const val PERMISSION_PUBLIC_PROFILE = "public_profile"
    }
}
