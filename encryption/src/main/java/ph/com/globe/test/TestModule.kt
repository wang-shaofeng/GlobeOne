/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.test

import dagger.Module
import dagger.Provides
import ph.com.globe.encryption.Crypto
import ph.com.globe.encryption.StringAesCrypto
import ph.com.globe.encryption.StringCrypto
import ph.com.globe.encryption.SuspendableStringCrypto

@Module
internal object TestModule {
    @Provides
    fun falltroughStringCrypto(): StringCrypto =
        object : StringCrypto {
            override fun encrypt(data: String): String = data
            override fun decrypt(encryptedData: String): String = encryptedData
        }

    @Provides
    fun falltroughCrypto(): Crypto =
        object : Crypto {
            override fun encrypt(data: ByteArray): ByteArray = data
            override fun decrypt(encryptedData: ByteArray): ByteArray = encryptedData
        }

    @Provides
    fun falltroughSuspendableStringCrypto(): SuspendableStringCrypto =
        object : SuspendableStringCrypto {
            override suspend fun encrypt(data: String): String = data
            override suspend fun decrypt(encryptedData: String): String = encryptedData
        }

    @Provides
    fun falltroughStringAesCrypto(): StringAesCrypto =
        object : StringAesCrypto {
            override fun encrypt(data: String, secretKey: String): String = data
        }
}
