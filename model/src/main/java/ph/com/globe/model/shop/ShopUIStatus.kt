/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.shop

sealed class CatalogStatus {
    object Success : CatalogStatus()
    object Loading : CatalogStatus()
    object Error : CatalogStatus()
}
