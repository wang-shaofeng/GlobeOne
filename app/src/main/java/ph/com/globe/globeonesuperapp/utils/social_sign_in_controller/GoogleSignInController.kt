/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.social_sign_in_controller

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.util.LfResult


class GoogleSignInController(private val context: Context) : SignInController, HasLogTag {

    private val signInClient: SignInClient = Identity.getSignInClient(context)
    private val request = GetSignInIntentRequest.builder()
        .setServerClientId(WEB_CLIENT_ID)
        .build()

    private var tokenCallback: ((LfResult<String, SignInException>) -> Unit)? = null
    private var emailCallback: ((LfResult<String, SignInException>) -> Unit)? = null

    override fun signIn(fragment: Fragment) {
        signInClient.getSignInIntent(request)
            .addOnSuccessListener(fragment.requireActivity()) { result ->
                try {
                    if (fragment.isAdded)
                        fragment.startIntentSenderForResult(
                            result.intentSender, REQ_GOOGLE_SIGN_IN,
                            null, 0, 0, 0, null
                        )
                } catch (e: IntentSender.SendIntentException) {
                    tokenCallback?.invoke(LfResult.failure(SignInException.CancelException))
                }
            }
            .addOnFailureListener(fragment.requireActivity()) {
                tokenCallback?.invoke(LfResult.failure(SignInException.GoogleException(it)))
            }
            .addOnCanceledListener(fragment.requireActivity()) {
                tokenCallback?.invoke(LfResult.failure(SignInException.CancelException))
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQ_GOOGLE_SIGN_IN, REQUEST_AUTHORIZATION -> try {
                val credential = signInClient.getSignInCredentialFromIntent(data)
                val email = credential.id

                emailCallback?.invoke(LfResult.success(email))
            } catch (e: ApiException) {
                if (e.status == Status.RESULT_CANCELED) {
                    tokenCallback?.invoke(LfResult.failure(SignInException.CancelException))
                } else {
                    tokenCallback?.invoke(LfResult.failure(SignInException.GoogleException(e)))
                }
            }
        }
    }

    fun generateTokenFromEmail(fragment: Fragment, email: String) {
        try {
            val accessToken = GoogleAuthUtil.getToken(
                fragment.requireContext(),
                Account(email, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE),
                "oauth2:https://www.googleapis.com/auth/plus.login"
            )

            tokenCallback?.invoke(LfResult.success(accessToken))
        } catch (e: Exception) {
            if (e is UserRecoverableAuthException) {
                fragment.startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
            } else {
                tokenCallback?.invoke(LfResult.failure(SignInException.GoogleException(e)))
            }
        }
    }

    override fun logOut() {
        signInClient.signOut()
    }

    fun registerTokenCallback(tokenResult: (LfResult<String, SignInException>) -> Unit) {
        tokenCallback = tokenResult
    }

    fun registerEmailCallback(emailResult: (LfResult<String, SignInException>) -> Unit) {
        emailCallback = emailResult
    }

    companion object {
        const val REQ_GOOGLE_SIGN_IN = 1111

        const val REQUEST_AUTHORIZATION = 1112

        private const val WEB_CLIENT_ID =
            "442918590457-v623g362350tsvhot15906fcf32j3m3h.apps.googleusercontent.com"

    }

    override val logTag: String = "GoogleSignInController"
}
