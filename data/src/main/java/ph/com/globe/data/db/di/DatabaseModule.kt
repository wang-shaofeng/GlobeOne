/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import ph.com.globe.data.DataScope
import ph.com.globe.data.db.*
import ph.com.globe.data.db.enrolled_accounts.GlobeEnrolledAccountsDao
import ph.com.globe.data.db.groups.GlobeAccountGroupsDao
import ph.com.globe.data.db.prepaid_promo_subscription_usage.GlobeSubscriptionUsagesDao
import ph.com.globe.data.db.profile_info.GlobeRegisteredUserDao
import ph.com.globe.data.db.shop.GlobeShopItemsDao
import ph.com.globe.data.db.util.TimeWithUnitAmount
import java.util.concurrent.TimeUnit
import javax.inject.Named

@Module
object DatabaseProvidesModule {

    @JvmStatic
    @Provides
    @DataScope
    fun provideGlobeDatabase(context: Context): GlobeDatabase =
        Room.databaseBuilder(context, GlobeDatabase::class.java, GlobeDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @JvmStatic
    @Provides
    fun provideQueryFreshnessDao(globeDatabase: GlobeDatabase): RoomLastQueryWriteTimeDao =
        globeDatabase.queryFreshnessDao()

    @JvmStatic
    @Provides
    @Named(ONE_HOUR_FRESHNESS)
    fun provideQueryTimeFreshnessDaoOneHour(globeDatabase: GlobeDatabase): QueryTimeFreshnessDao =
        QueryTimeFreshnessDao(
            globeDatabase.queryFreshnessDao(),
            GlobeDatabase.defaultTimeInMsProvider,
            TimeWithUnitAmount(1, TimeUnit.HOURS)
        )

    @JvmStatic
    @Provides
    @Named(ONE_DAY_FRESHNESS)
    fun provideQueryTimeFreshnessDaoOneDay(globeDatabase: GlobeDatabase): QueryTimeFreshnessDao =
        QueryTimeFreshnessDao(
            globeDatabase.queryFreshnessDao(),
            GlobeDatabase.defaultTimeInMsProvider,
            TimeWithUnitAmount(1, TimeUnit.DAYS)
        )

    @JvmStatic
    @Provides
    fun provideEnrolledAccountsDao(globeDatabase: GlobeDatabase): GlobeEnrolledAccountsDao =
        globeDatabase.enrolledAccountsDao()

    @JvmStatic
    @Provides
    fun provideRegisteredUserDao(globeDatabase: GlobeDatabase): GlobeRegisteredUserDao =
        globeDatabase.registeredUserDao()

    @JvmStatic
    @Provides
    fun provideAccountGroupsDao(globeDatabase: GlobeDatabase): GlobeAccountGroupsDao =
        globeDatabase.accountGroupsDao()

    @JvmStatic
    @Provides
    fun provideShopItemsDao(globeDatabase: GlobeDatabase): GlobeShopItemsDao =
        globeDatabase.shopItemsDao()

    @JvmStatic
    @Provides
    fun provideAccountSubscriptionUsageDao(globeDatabase: GlobeDatabase): GlobeSubscriptionUsagesDao =
        globeDatabase.subscriptionUsagesDao()

}

@Module
abstract class DatabaseBindsModule {

    @Binds
    abstract fun bindTimeInMsProvider(defaultTimeInMsProvider: DefaultTimeInMsProvider): TimeInMsProvider
}

const val ONE_DAY_FRESHNESS = "OneDayFreshness"
const val ONE_HOUR_FRESHNESS = "OneHourFreshness"
