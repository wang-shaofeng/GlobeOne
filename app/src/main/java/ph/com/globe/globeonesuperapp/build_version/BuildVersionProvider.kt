/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.build_version

import ph.com.globe.globeonesuperapp.BuildConfig
import javax.inject.Inject

interface BuildVersionProvider {

    fun provideBuildVersionCode(): Int

    fun provideBuildVersionName(): String

}

class DefaultBuildVersionProvider @Inject constructor() : BuildVersionProvider {

    override fun provideBuildVersionCode() = BuildConfig.VERSION_CODE

    override fun provideBuildVersionName() = BuildConfig.VERSION_NAME

}
