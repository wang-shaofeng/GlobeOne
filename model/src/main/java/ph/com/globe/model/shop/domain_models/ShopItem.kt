/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.shop.domain_models

import ph.com.globe.model.SearchItem
import ph.com.globe.model.util.brand.AccountBrand
import java.io.Serializable

data class ShopItem(
    val chargePromoId: String,
    val nonChargePromoId: String,
    val chargeServiceParam: String,
    val nonChargeServiceParam: String,
    override val name: String,
    val description: String,
    val displayColor: String,
    val validity: Validity?,
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
    val sections: List<SectionItem>,
    val boosters: List<String>? = null,
    val applicationService: ApplicationService? = null,
    val freebie: FreebieItem?,
    val includedApps: List<AppItem>,
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
) : Serializable, SearchItem

data class ApplicationService(
    val description: String,
    val apps: List<AppItem>?
) : Serializable

data class AppItem(
    val appIcon: String,
    val appName: String
) : Serializable

data class FreebieItem(
    val description: String,
    val icon: String?,
    val items: List<FreebieSingleSelectItem>?
) : Serializable

data class FreebieSingleSelectItem(
    val title: String,
    val serviceChargeParam: String,
    val serviceNonChargeParam: String,
    val serviceNoneChargeId: String,
    val apiProvisioningKeyword: String,
    val size: Long,
    val sizeUnit: String,
    val duration: Int,
    val icons: List<String>,
    val type: String
) : Serializable

data class SectionItem(
    val id: String,
    val name: String,
    val sortPriority: Int,
    val booster: String?,
    val isSection: Boolean
) : Serializable

data class Validity(
    val days: Int,
    val hours: Int
) : Serializable

fun ShopItem.isBrandCorrect(brand: AccountBrand?) =
    brand == AccountBrand.GhpPrepaid && isGlobePrepaid || brand == AccountBrand.GhpPostpaid && isPostpaid
            || brand == AccountBrand.Tm && (isTM || isTMMyFi) || brand == AccountBrand.Hpw && isHomePrepaidWifi

// General types of promos
const val TYPE_LIMITED = "limited"
const val TYPE_NEW = "new"
const val TYPE_DISCOUNTED = "discounted"

// Types of content promos by method
const val CONTENT_PROMO_METHOD_DCB = "Direct Carrier Billing (DCB)"
const val CONTENT_PROMO_METHOD_RIPLEY = "RIPLEY"
const val CONTENT_PROMO_METHOD_BOURNE_1 = "BOURNE DRP 1"
const val CONTENT_PROMO_METHOD_BOURNE_2 = "BOURNE DRP 2"

// Types of API to subscribe
const val PROMO_API_SERVICE_PROVISION = "ServiceProvision"

const val PROMO_API_VOUCHER = "CreatePromoVoucher"

// Categories
const val CATEGORY_GO_CREATE = "GoCREATE"

// Service description
const val UNLIMITED_CALLS_DESCRIPTION = "Unli Allnet Calls"
