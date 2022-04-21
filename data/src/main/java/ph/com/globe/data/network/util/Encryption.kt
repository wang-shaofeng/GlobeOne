/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.util

import com.google.common.io.BaseEncoding
import org.bouncycastle.jce.provider.BouncyCastleProvider
import ph.com.globe.analytics.logger.CompositeUxLogger.dLog
import ph.com.globe.globeonesuperapp.data.BuildConfig
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.Provider
import java.security.PublicKey
import java.security.Security
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

internal fun encryptCredentials(credentials: String): String {

    val plaintext = credentials.toByteArray(StandardCharsets.UTF_8)
    dLog("credentials to byte array")

    val publicKey =
        loadPublicKey(BuildConfig.CIAM_PUBLIC_KEY)

    val cipher = getCipherInstance("RSA/None/PKCS1Padding")
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    val cipherText: ByteArray = cipher.doFinal(plaintext)

    return BaseEncoding.base64().encode(cipherText)
}

/*
 * Generate a PublicKey object from a string
 * @ key64 : public key in string format (BASE 64)
 */
private fun loadPublicKey(key64: String): PublicKey? {
    val data: ByteArray = BaseEncoding.base64().decode(key64)
    val spec = X509EncodedKeySpec(data)
    val fact = KeyFactory.getInstance("RSA")
    return fact.generatePublic(spec)
}

/**
 * Trying a set of [Provider]s that are preferable before falling back to default [Provider].
 */
private fun getCipherInstance(algorithm: String): Cipher {
    providersSortedByPriority.forEach { provider ->
        runCatching {
            Cipher.getInstance(algorithm, provider)
        }.getOrNull()?.let { cipherInstance -> return cipherInstance }
    }

    Security.addProvider(BouncyCastleProvider())

    // fallbacks to default Provider
    return Cipher.getInstance(algorithm)
}

private val providersSortedByPriority: List<Provider> = listOf(
    /* Conscrypt in GmsCore, updatable thus preferable */
    "GmsCore_OpenSSL",
    /* Conscrypt in AOSP, not updatable but still better than BC */
    "AndroidOpenSSL",
    /* JVM provider (used in unit tests) */
    "SunJCE"
).mapNotNull { Security.getProvider(it) }
