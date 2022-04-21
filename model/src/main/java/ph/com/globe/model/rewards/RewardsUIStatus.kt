/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.rewards

sealed class RewardsCatalogStatus {
    object Success : RewardsCatalogStatus()
    object Loading : RewardsCatalogStatus()
    object Error : RewardsCatalogStatus()
}
