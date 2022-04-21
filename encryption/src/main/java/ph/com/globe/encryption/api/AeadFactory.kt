/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.encryption.api

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AesGcmKeyManager
import com.google.crypto.tink.integration.android.AndroidKeysetManager

internal open class Aes256GcmAeadFactory(
    context: Context,
    private val preferenceCryptoKey: String,
    private val preferenceFileName: String
) {
    private val appContext = context.applicationContext

    open fun createAead(optMasterKeyUri: String?): Aead = AndroidKeysetManager.Builder().apply {
        withSharedPref(appContext, preferenceCryptoKey, preferenceFileName)
        withKeyTemplate(AesGcmKeyManager.aes256GcmTemplate())
        if (optMasterKeyUri != null) {
            withMasterKeyUri(optMasterKeyUri)
        }
    }.build().run {
        keysetHandle.getPrimitive(Aead::class.java)
    }

    companion object {
        init {
            AeadConfig.register()
        }
    }
}
