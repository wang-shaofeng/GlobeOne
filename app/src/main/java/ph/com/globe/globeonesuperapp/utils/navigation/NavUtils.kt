/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.navigation

import androidx.annotation.IdRes
import androidx.navigation.NavController

fun NavController.isOnBackStack(@IdRes id: Int): Boolean = try {
    getBackStackEntry(id); true
} catch (e: Throwable) {
    false
}
