/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.util

internal fun String.extractVersionNameNumber(): String = """\d+\.\d+\.\d+""".toRegex().find(this)?.value!!
