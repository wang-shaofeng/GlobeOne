/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import java.util.concurrent.TimeUnit
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ph.com.globe.data.network.initChuckerInterceptor
import ph.com.globe.data.network.setCypherSpecIfNeeded
import java.io.InputStream


@GlideModule(glideName = "GlobeGlide")
class GlobeGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(initChuckerInterceptor(context))
            .readTimeout(90, TimeUnit.SECONDS)
            .connectTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .addNetworkInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .also { setCypherSpecIfNeeded(it) }
            .build()

        val factory = OkHttpUrlLoader.Factory(client)

        glide.registry.replace(
            GlideUrl::class.java,
            InputStream::class.java, factory
        )
    }
}
