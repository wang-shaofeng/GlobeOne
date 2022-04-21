/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.encryption.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import ph.com.globe.encryption.StringCrypto
import ph.com.globe.encryption.SuspendableStringCrypto
import javax.inject.Inject

class DefaultSuspendableStringCrypto @Inject constructor(private val stringCrypto: StringCrypto) :
    SuspendableStringCrypto {
    override suspend fun encrypt(data: String): String = withContext(Dispatchers.Default) {
        runInterruptible { stringCrypto.encrypt(data) }
    }

    override suspend fun decrypt(encryptedData: String): String = withContext(Dispatchers.Default) {
        runInterruptible { stringCrypto.decrypt(encryptedData) }
    }
}
