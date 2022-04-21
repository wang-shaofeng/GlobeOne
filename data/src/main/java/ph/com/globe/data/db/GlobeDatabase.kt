/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import ph.com.globe.data.db.enrolled_accounts.EnrolledAccountEntity
import ph.com.globe.data.db.enrolled_accounts.GlobeEnrolledAccountsDao
import ph.com.globe.data.db.groups.AccountGroupsEntity
import ph.com.globe.data.db.groups.GlobeAccountGroupsDao
import ph.com.globe.data.db.groups.UsageItemEntity
import ph.com.globe.data.db.prepaid_promo_subscription_usage.DataItemEntity
import ph.com.globe.data.db.prepaid_promo_subscription_usage.GlobeSubscriptionUsagesDao
import ph.com.globe.data.db.prepaid_promo_subscription_usage.PromoSubscriptionUsageEntity
import ph.com.globe.data.db.profile_info.GlobeRegisteredUserDao
import ph.com.globe.data.db.profile_info.RegisteredUserAddressEntity
import ph.com.globe.data.db.profile_info.RegisteredUserEntity
import ph.com.globe.data.db.shop.*
import ph.com.globe.data.network.sharedMoshi
import ph.com.globe.model.util.brand.*

@Database(
    entities = [
        LastQueryWriteTimeEntity::class,
        EnrolledAccountEntity::class,
        RegisteredUserEntity::class,
        AccountGroupsEntity::class,
        ShopItemEntity::class,
        PromoSubscriptionUsageEntity::class
    ],
    version = 9
)
@TypeConverters(
    GlobeTypeConverters::class
)
abstract class GlobeDatabase : RoomDatabase() {

    abstract fun enrolledAccountsDao(): GlobeEnrolledAccountsDao

    abstract fun queryFreshnessDao(): RoomLastQueryWriteTimeDao

    abstract fun registeredUserDao(): GlobeRegisteredUserDao

    abstract fun accountGroupsDao(): GlobeAccountGroupsDao

    abstract fun shopItemsDao(): GlobeShopItemsDao

    abstract fun subscriptionUsagesDao(): GlobeSubscriptionUsagesDao

    companion object {
        const val NAME = "Globe.db"

        val defaultTimeInMsProvider = DefaultTimeInMsProvider()
    }

}

class GlobeTypeConverters {

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter: JsonAdapter<List<String>> = sharedMoshi.adapter(stringListType)

    private val stringRegisteredUserAddressEntityAdapter =
        sharedMoshi.adapter(RegisteredUserAddressEntity::class.java)

    private val stringListUsageItemType =
        Types.newParameterizedType(List::class.java, UsageItemEntity::class.java)
    private val stringListUsageItemAdapter: JsonAdapter<List<UsageItemEntity>> =
        sharedMoshi.adapter(stringListUsageItemType)

    private val longListType =
        Types.newParameterizedType(List::class.java, Long::class.javaObjectType)
    private val longListAdapter: JsonAdapter<List<Long>> = sharedMoshi.adapter(longListType)

    private val stringListSectionItemType =
        Types.newParameterizedType(List::class.java, SectionItemEntity::class.java)
    private val stringListSectionItemAdapter: JsonAdapter<List<SectionItemEntity>> =
        sharedMoshi.adapter(stringListSectionItemType)

    private val stringApplicationServiceAdapter =
        sharedMoshi.adapter(ApplicationServiceEntity::class.java)

    private val stringFreebieItemAdapter = sharedMoshi.adapter(FreebieItemEntity::class.java)

    private val stringListAppItemType =
        Types.newParameterizedType(List::class.java, AppItemEntity::class.java)
    private val stringListAppItemAdapter: JsonAdapter<List<AppItemEntity>> =
        sharedMoshi.adapter(stringListAppItemType)

    private val stringListDataItemType =
        Types.newParameterizedType(List::class.java, DataItemEntity::class.java)
    private val stringListDataItemAdapter: JsonAdapter<List<DataItemEntity>> =
        sharedMoshi.adapter(stringListDataItemType)

    @TypeConverter
    fun fromStringList(stringList: List<String>?): String =
        stringListAdapter.toJson(stringList)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        stringListAdapter.fromJson(value) ?: listOf()

    @TypeConverter
    fun fromRegisteredUserEntityToString(entity: RegisteredUserAddressEntity): String =
        stringRegisteredUserAddressEntityAdapter.toJson(entity)

    @TypeConverter
    fun fromStringToRegisteredUserEntity(value: String): RegisteredUserAddressEntity? =
        stringRegisteredUserAddressEntityAdapter.fromJson(value)

    @TypeConverter
    fun fromListUsageItemToString(list: List<UsageItemEntity>): String =
        stringListUsageItemAdapter.toJson(list)

    @TypeConverter
    fun fromStringToListUsageItem(value: String): List<UsageItemEntity> =
        stringListUsageItemAdapter.fromJson(value) ?: listOf()

    @TypeConverter
    fun fromLongList(list: List<Long>?): String =
        longListAdapter.toJson(list)

    @TypeConverter
    fun toLongList(value: String): List<Long> =
        longListAdapter.fromJson(value) ?: listOf()

    @TypeConverter
    fun fromListSectionItemToString(list: List<SectionItemEntity>?): String =
        stringListSectionItemAdapter.toJson(list)

    @TypeConverter
    fun fromStringToListSectionItem(value: String): List<SectionItemEntity> =
        stringListSectionItemAdapter.fromJson(value) ?: listOf()

    @TypeConverter
    fun fromApplicationServiceEntityToString(entity: ApplicationServiceEntity?): String =
        stringApplicationServiceAdapter.toJson(entity)

    @TypeConverter
    fun fromStringToApplicationServiceEntity(value: String): ApplicationServiceEntity? =
        stringApplicationServiceAdapter.fromJson(value)

    @TypeConverter
    fun fromFreebieItemEntityToString(entity: FreebieItemEntity?): String =
        stringFreebieItemAdapter.toJson(entity)

    @TypeConverter
    fun fromStringToFreebieItemEntity(value: String): FreebieItemEntity? =
        stringFreebieItemAdapter.fromJson(value)

    @TypeConverter
    fun fromListAppItemToString(list: List<AppItemEntity>?): String =
        stringListAppItemAdapter.toJson(list)

    @TypeConverter
    fun fromStringToListAppItem(value: String): List<AppItemEntity> =
        stringListAppItemAdapter.fromJson(value) ?: listOf()

    @TypeConverter
    fun fromListDataItemToString(list: List<DataItemEntity>?): String =
        stringListDataItemAdapter.toJson(list)

    @TypeConverter
    fun fromStringToListDataItem(value: String): List<DataItemEntity> =
        stringListDataItemAdapter.fromJson(value) ?: listOf()

    @TypeConverter
    fun fromAccountBrandToString(value: AccountBrand): String =
        value.name

    @TypeConverter
    fun fromStringToAccountBrand(value: String): AccountBrand =
        value.toAccountBrand()

    @TypeConverter
    fun fromAccountBrandTypeToString(value: AccountBrandType): String =
        value.toString()

    @TypeConverter
    fun fromStringToAccountBrandType(value: String): AccountBrandType =
        value.toAccountBrandType()

    @TypeConverter
    fun fromAccountSegmentToString(value: AccountSegment): String =
        value.toString()

    @TypeConverter
    fun fromStringToAccountSegment(value: String): AccountSegment =
        value.toAccountSegment()
}
