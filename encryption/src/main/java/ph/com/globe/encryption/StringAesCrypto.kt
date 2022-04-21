/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.encryption

interface StringAesCrypto {

    @Throws(InterruptedException::class)
    fun encrypt(data: String, secretKey: String): String
}
