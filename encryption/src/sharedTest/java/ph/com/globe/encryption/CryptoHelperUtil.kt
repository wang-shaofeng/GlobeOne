/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.encryption

import org.assertj.core.api.Assertions.assertThat
import ph.com.globe.encryption.Crypto

fun Crypto.encryptDecryptTestHelper(original: ByteArray) {
    val encrypted: ByteArray = encrypt(original)
    val decrypted: ByteArray = decrypt(encrypted)
    assertThat(encrypted).isNotSameAs(original)
    assertThat(encrypted).isNotEqualTo(original)
    assertThat(decrypted).isEqualTo(original)
}
