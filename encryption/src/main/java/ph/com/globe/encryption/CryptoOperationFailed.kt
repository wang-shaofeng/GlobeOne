/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.encryption

/**
 * The exception thrown when all the retries are exausted and the Crypto operation is aborted.
 * The last exception is rethrown a cause wrapped in [CryptoOperationFailed].
 */
class CryptoOperationFailed : RuntimeException {
    constructor(cause: Throwable?) : super(cause)
    constructor(msg: String?, cause: Throwable?) : super(msg, cause)
}
