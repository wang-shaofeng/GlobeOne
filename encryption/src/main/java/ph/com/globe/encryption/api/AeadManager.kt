/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.encryption.api

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.google.crypto.tink.Aead
import java.util.concurrent.TimeUnit
import javax.inject.Inject

//open so we can mock it in tests :(
@SuppressLint("CommitPrefEdits")
internal open class AeadManager @Inject constructor(
    context: Context
) {

    @VisibleForTesting
    internal var _sharedPrefsKeyStore: SharedPreferences

    @VisibleForTesting
    internal var _aeadFactory: Aes256GcmAeadFactory

    @VisibleForTesting
    internal var _crypto: Aead? = null

    init {
        val appContext = context.applicationContext
        _sharedPrefsKeyStore =
            appContext.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).apply {
                edit()
                    .putBoolean(TRY_USE_MASTER_KEY, TRY_USE_MASTER_KEY_DEFAULT_VAL)
                    .commitAndCheck()
            }
        _aeadFactory = Aes256GcmAeadFactory(appContext, CRYPTO_KEY, SHARED_PREFS_NAME)
    }

    @Throws(InterruptedException::class)
    open fun getOrGenerateAead(): Aead = _crypto ?: retryingCreateAead().also { _crypto = it }

    private fun retryingCreateAead(): Aead {
        val optionalMasterKeyPath = masterKeyUriOrNullIfWeShouldNotUseIt()
        // Retrying to create Aead if we are using master key (Android Keystore) since it can fail.
        // We will try few times (exponential backoff delayed) and rethrow exception if that fails.
        var maxRetryDelayFor =
            INITIAL_DELAY_IN_MS.takeIf { optionalMasterKeyPath != null } ?: -1L
        while (true) {
            maxRetryDelayFor = try {
                return _aeadFactory.createAead(optionalMasterKeyPath)
            } catch (throwable: Throwable) {
                when {
                    maxRetryDelayFor >= MAX_DELAY_IN_MS -> {
                        // Last try...
                        sleepForMaximum(MAX_DELAY_IN_MS)
                        -1
                    }
                    maxRetryDelayFor < 0 -> {
                        // No more retries, rethrow  to exit...
                        throw IllegalStateException(
                            "Retries failed, used AndroidKeystore=${optionalMasterKeyPath != null}.",
                            throwable
                        )
                    }
                    else -> {
                        sleepForMaximum(maxRetryDelayFor)
                        maxRetryDelayFor * 2
                    }
                }
            }
        }
    }

    private fun sleepForMaximum(maxMs: Long) {
        // Sleep somewhere in range
        Thread.sleep((maxMs * Math.random()).toLong())
    }

    private fun masterKeyUriOrNullIfWeShouldNotUseIt(): String? =
        MASTER_KEY_PATH.takeIf {
            //sanity check, since it should always have some value set
            check(_sharedPrefsKeyStore.contains(TRY_USE_MASTER_KEY))
            _sharedPrefsKeyStore.getBoolean(TRY_USE_MASTER_KEY, TRY_USE_MASTER_KEY_DEFAULT_VAL)
        }

    open fun clearAeadAndStopTryingToUseMasterKey() {
        _crypto = null
        _sharedPrefsKeyStore.edit()
            .putBoolean(TRY_USE_MASTER_KEY, false)
            .remove(CRYPTO_KEY)
            .commitAndCheck()
    }

    private fun SharedPreferences.Editor.commitAndCheck() {
        commit().also { check(it) { "Can't write to shared prefs?? Shouldn't happen!!" } }
    }

    private companion object {
        val MAX_DELAY_IN_MS: Long = TimeUnit.SECONDS.toMillis(2)
        const val INITIAL_DELAY_IN_MS: Long = 200

        const val SHARED_PREFS_NAME = "android_lib_encryption_shared_prefs"
        const val CRYPTO_KEY = "crypto_key"

        //We will try to use master key in the builder setup by default.
        //If there are some problems (CryptoOperationFailed is thrown, we will stop using the
        //Android Keystore since it's unreliable (by setting the value to false here).
        //Note that Google Tink does some validation of AndroidKeystore reliability and may
        //disable the usage of master key if the validation doesn't pass.
        //We are adding the layer of our own to be sure to stop using it when something goes wrong.
        const val TRY_USE_MASTER_KEY = "try_to_use_master_key"
        const val TRY_USE_MASTER_KEY_DEFAULT_VAL = true
        const val MASTER_KEY_PATH = "android-keystore://android_lib_master_key"
    }
}
