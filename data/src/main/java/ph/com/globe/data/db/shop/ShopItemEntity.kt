/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.shop

import androidx.room.Embedded
import androidx.room.Entity
import com.squareup.moshi.JsonClass
import ph.com.globe.data.db.shop.ShopItemEntity.Companion.TABLE_NAME
import ph.com.globe.model.shop.domain_models.SectionItem
import ph.com.globe.model.shop.domain_models.ShopItem
import ph.com.globe.model.shop.domain_models.Validity

@Entity(
    tableName = TABLE_NAME,
    primaryKeys = ["name", "chargePromoId", "nonChargePromoId", "chargeServiceParam", "nonChargeServiceParam"]
)
@JsonClass(generateAdapter = true)
data class ShopItemEntity(
    val chargePromoId: String,
    val nonChargePromoId: String,
    val chargeServiceParam: String,
    val nonChargeServiceParam: String,
    val name: String,
    val description: String,
    val displayColor: String,
    @Embedded val validity: Validity?,
    val price: String,
    val discount: String?,
    val fee: String,
    val promoType: List<String>,
    val use: String,
    val functions: List<String>,
    val popularity: Int,
    val loanable: Boolean,
    val isContent: Boolean,
    val isVoucher: Boolean,
    val shareable: Boolean,
    val isTM: Boolean,
    val isTMMyFi: Boolean,
    val isHomePrepaidWifi: Boolean,
    val isGlobePrepaid: Boolean,
    val isPrepaid: Boolean,
    val isPostpaid: Boolean,
    val apiSubscribe: String,
    val apiProvisioningKeyword: String,
    val mobileDataSize: List<Long>,
    val mobileDataDescription: List<String>,
    val homeDataSize: List<Long>,
    val homeDataDescription: List<String>,
    val appDataSize: List<Long>,
    val appDataDescription: List<String>,
    val boosterAllocation: List<Long>?,
    val maximumDataAllocation: Long,
    val smsSize: List<Long>,
    val smsDescription: List<String>,
    val callSize: List<Long>,
    val callDescription: List<String>,
    val loadAllocation: String,
    val types: String,
    val sections: List<SectionItemEntity>,
    val boosters: List<String>? = null,
    val applicationService: ApplicationServiceEntity? = null,
    val freebie: FreebieItemEntity?,
    val includedApps: List<AppItemEntity>,
    val shareKeyword: String? = null,
    val shareFee: String? = null,
    val skelligKeyword: String?,
    val skelligWallet: String?,
    val skelligCategory: String?,
    val visibleOnMainCatalog: Boolean,
    val asset: String?,
    val method: String?,
    val partnerName: String?,
    val partnerRedirectionLink: String?,
    val denomCategory: String?,
    val monitoredInApp: Boolean,
    val isBooster: Boolean,
    val isFreebie: Boolean,
    val isGoCreate: Boolean,
    val isAnyAppService: Boolean
) {
    companion object {
        const val TABLE_NAME = "shop_items"
    }
}

fun ShopItemEntity.toDomain() =
    ShopItem(
        chargePromoId,
        nonChargePromoId,
        chargeServiceParam,
        nonChargeServiceParam,
        name,
        description,
        displayColor,
        validity,
        price,
        discount,
        fee,
        promoType,
        use,
        functions,
        popularity,
        loanable,
        isContent,
        isVoucher,
        shareable,
        isTM,
        isTMMyFi,
        isHomePrepaidWifi,
        isGlobePrepaid,
        isPrepaid,
        isPostpaid,
        apiSubscribe,
        apiProvisioningKeyword,
        mobileDataSize,
        mobileDataDescription,
        homeDataSize,
        homeDataDescription,
        appDataSize,
        appDataDescription,
        boosterAllocation,
        maximumDataAllocation,
        smsSize,
        smsDescription,
        callSize,
        callDescription,
        loadAllocation,
        types,
        sections.map { SectionItem(it.id, it.name, it.sortPriority, it.booster, it.isSection) },
        boosters,
        applicationService?.toDomain(),
        freebie?.toDomain(),
        includedApps.toDomain(),
        shareKeyword,
        shareFee,
        skelligKeyword,
        skelligWallet,
        skelligCategory,
        visibleOnMainCatalog,
        asset,
        method,
        partnerName,
        partnerRedirectionLink,
        denomCategory,
        monitoredInApp,
        isBooster,
        isFreebie,
        isGoCreate,
        isAnyAppService
    )

@JsonClass(generateAdapter = true)
data class ApplicationServiceEntity(
    val description: String,
    val apps: List<AppItemEntity>?
)

@JsonClass(generateAdapter = true)
data class AppItemEntity(
    val appIcon: String,
    val appName: String
)

@JsonClass(generateAdapter = true)
data class FreebieItemEntity(
    val description: String,
    val icon: String?,
    val items: List<FreebieSingleSelectItemEntity>?
)

@JsonClass(generateAdapter = true)
data class FreebieSingleSelectItemEntity(
    val title: String,
    val serviceChargeParam: String,
    val serviceNonChargeParam: String,
    val serviceNoneChargeId: String,
    val apiProvisioningKeyword: String,
    val size: Long,
    val sizeUnit: String,
    val duration: Int,
    val icons: List<String>,
    val type: String = ""
)

@JsonClass(generateAdapter = true)
data class SectionItemEntity(
    val id: String,
    val name: String,
    val sortPriority: Int,
    val booster: String?,
    val isSection: Boolean
)
