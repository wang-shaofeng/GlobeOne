/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.shop

import androidx.annotation.IntDef
import androidx.annotation.StringDef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ph.com.globe.model.shop.domain_models.*
import ph.com.globe.model.shop.network_models.*
import ph.com.globe.model.util.*
import ph.com.globe.util.SECONDS_IN_ONE_DAY
import ph.com.globe.util.SECONDS_IN_ONE_HOUR

/**
 * The function that converts OfferJson received by the catalog's GetAllOffers API in the offer_by_id map.
 * Here, we extract all relevant fields from the catalog response for the current offer.
 *
 * @return [ShopItemEntity] value used to store data in our DB
 */
fun OfferJson.toEntity(response: GetAllOffersResponse): ShopItemEntity {

    // All sections of the entire catalog, found under category_by_id map from the response
    val sections =
        response.category_by_id.values.filter { it.data.name?.lowercase() == "Section".lowercase() }

    // categories of a current offer
    val categories =
        response.category_by_id.filter { it.key in (data.categories ?: listOf()) }

    // products contained by a current offer
    val products = response.product_by_id.filter { it.key in data.products }

    // list of all services contained by a current offer
    val servicesList = mutableListOf<ServiceJson>()

    // mapping of product ids to list of services contained by each product
    val productServicesMap = mutableMapOf<String, MutableList<ServiceJson>>()
    products.values.forEach { product ->
        productServicesMap[product.id ?: ""] = mutableListOf()
        product.data.services?.forEach { service ->
            response.service_by_id[service]?.let {
                productServicesMap[product.id]?.add(it)
                servicesList.add(it)
            }
        }
    }

    return ShopItemEntity(
        chargePromoId = data.misc_params?.nf_serviceid_withcharging ?: "",
        nonChargePromoId = data.misc_params?.nf_serviceid_wocharging ?: "",
        chargeServiceParam = data.misc_params?.nf_param_withcharging ?: "",
        nonChargeServiceParam = data.misc_params?.nf_param_wocharging ?: "",
        name = data.name ?: "",
        description = data.description ?: "",
        displayColor = extractDisplayColor(),
        validity = extractValidity(products),
        price = data.amounts.primary ?: "",
        discount = data.misc_params?.discount.takeIf { d -> d != "0" && d != "" },
        fee = data.misc_params?.loan_servicefee ?: "0",
        promoType = categories.extractOfferTypes(),
        use = "",
        functions = servicesList.extractFunctions(data.misc_params?.type == BOOSTER),
        popularity = data.sort_priority ?: 0,
        loanable = data.misc_params?.type ?: "" == LOAN,
        isContent = data.misc_params?.type ?: "" == CONTENT,
        isVoucher = data.misc_params?.api_apisubscribe == PROMO_API_VOUCHER,
        shareable = !data.misc_params?.salsap_keyword.isNullOrBlank(),
        isTM = data.misc_params?.brand_mainaccounttype_tm.toBoolean(),
        isTMMyFi = data.misc_params?.brand_mainaccounttype_tmmyfi.toBoolean(),
        isHomePrepaidWifi = data.misc_params?.brand_mainaccounttype_hpw.toBoolean(),
        isGlobePrepaid = data.misc_params?.brand_mainaccounttype_gpmyfi.toBoolean(),
        isPrepaid = data.misc_params?.brand_mainaccounttype_prepaid.toBoolean(),
        isPostpaid = data.misc_params?.brand_mainaccounttype_tmpostpaid.toBoolean(),
        apiSubscribe = data.misc_params?.api_apisubscribe ?: "",
        apiProvisioningKeyword = data.misc_params?.keyword_keywordsubscribe ?: "",
        mobileDataSize = servicesList.extractServiceSize(DATA_SERVICE_VALUE, MOBILE_DATA_KEY),
        mobileDataDescription = servicesList.extractServiceDescription(
            DATA_SERVICE_VALUE,
            MOBILE_DATA_KEY
        ),
        homeDataSize = servicesList.extractServiceSize(DATA_SERVICE_VALUE, HOME_DATA_KEY),
        homeDataDescription = servicesList.extractServiceDescription(
            DATA_SERVICE_VALUE,
            HOME_DATA_KEY
        ),
        appDataSize = servicesList.extractServiceSize(DATA_SERVICE_VALUE, APP_DATA_KEY),
        appDataDescription = servicesList.extractServiceDescription(
            DATA_SERVICE_VALUE,
            APP_DATA_KEY
        ),
        boosterAllocation =
        if (data.misc_params?.type == BOOSTER)
            servicesList.extractServiceSize(DATA_SERVICE_VALUE, BOOSTER)
        else null,
        maximumDataAllocation = servicesList.extractTotalDataSize(
            products
        ),
        smsSize = servicesList.extractServiceSize(SMS_SERVICE_VALUE),
        smsDescription = servicesList.extractServiceDescription(SMS_SERVICE_VALUE),
        callSize = servicesList.extractServiceSize(CALLS_SERVICE_VALUE),
        callDescription = servicesList.extractServiceDescription(CALLS_SERVICE_VALUE),
        loadAllocation = data.misc_params?.load_allocation ?: "0",
        types = extractTypes(),
        sections = categories.extractSections(sections),
        boosters = categories.extractBoosterCategories(),
        applicationService = if (data.misc_params?.type == BOOSTER) ApplicationServiceEntity(
            data.description ?: "",
            if (servicesList.any { it.data.misc_params?.type == BOOSTER }) servicesList.find { it.data.misc_params?.type == BOOSTER }?.data?.misc_params?.booster_appname_appicon?.extractToBoosterAppIcon() else emptyList()
        ) else null,
        freebie = products
            .filter { productServicesMap[it.value.id]?.all { serviceJson -> serviceJson.data.misc_params?.type == FREEBIE || serviceJson.data.misc_params?.type == FREEBIE_VOUCHER } == true }
            .extractToFreebieItems(
                productServicesMap,
                data.misc_params?.nf_serviceid_wocharging,
                data.misc_params?.nf_param_withcharging,
                data.misc_params?.nf_param_wocharging,
                data.misc_params?.keyword_keywordsubscribe
            ),
        shareKeyword = data.misc_params?.salsap_keyword,
        shareFee = data.misc_params?.salsap_servicefee,
        skelligKeyword = data.misc_params?.skellig_keyword,
        skelligWallet = data.misc_params?.skellig_wallet,
        skelligCategory = data.misc_params?.skellig_category,
        includedApps = servicesList.find { it.data.misc_params?.type == APP_DATA_KEY }?.data?.misc_params?.appdata_appname_appicon?.extractToBoosterAppIcon()
            ?: emptyList(),
        visibleOnMainCatalog = data.static_assets?.display_visibleonappmaincatalog?.toBoolean()
            ?: false,
        asset = data.misc_params?.asset,
        method = data.misc_params?.method,
        partnerName = data.misc_params?.partner_partnername,
        partnerRedirectionLink = data.misc_params?.partner_redirectionlink,
        denomCategory = data.misc_params?.denom_category,
        monitoredInApp = data.static_assets?.display_monitoredinapp.toBoolean(),
        isBooster = data.misc_params?.type == BOOSTER,
        isFreebie = data.misc_params?.type == FREEBIE,
        isGoCreate = categories.containsGoCreateCategory(),
        isAnyAppService = servicesList.any { it.data.misc_params?.type == APP_DATA_KEY }
    )
}

/**
 * Extracts the types of a current offer from a map of offer's categories
 */
private fun Map<String, CategoryJson>.extractOfferTypes() =
    this.filter { it.value.data.parent == null }.map { it.value.data.name ?: "" }

/**
 * Given the list of all sections in the catalog, we are extracting this offer's section entities
 */
private fun Map<String, CategoryJson>.extractSections(allSections: List<CategoryJson>) =
    this.values.distinct().sortedBy { it.data.sort_priority }
        .map {
            SectionItemEntity(
                it.id ?: "",
                it.data.name ?: "",
                it.data.sort_priority ?: 0,
                it.data.misc_params?.booster,
                allSections.find { category -> category.id == it.data.parent } != null
            )
        }

private fun OfferJson.extractValidity(products: Map<String, ProductJson>): Validity? {

    fun getValidityFromTimestamp(timestamp: Long): Validity {
        val days = timestamp / SECONDS_IN_ONE_DAY
        val hours = (timestamp % SECONDS_IN_ONE_DAY) / SECONDS_IN_ONE_HOUR
        return Validity(days.toInt(), hours.toInt())
    }

    return if (products.values.any { it.isMainProduct() } && data.misc_params?.type != BOOSTER) {
        // if any of the products of the offer is of type "main" we include it's duration value as validity
        val timestamp = products.values.first { it.isMainProduct() }.getDuration()
        getValidityFromTimestamp(timestamp)
    } else if (products.isNotEmpty()) {
        // else if there are any products in the offer instance we use any of them for the validity
        val timestamp = products.values.first().getDuration()
        getValidityFromTimestamp(timestamp)
    } else if (data.misc_params?.type == CONTENT) {
        // in case of content offer type there are no products attached to the offer so we use it's own duration value
        val timestamp = data.misc_params?.duration ?: 0
        getValidityFromTimestamp(timestamp)
    } else
    // if no duration field is present there is no validity information
        null
}

private fun OfferJson.extractTypes(): String {
    var types = ""

    if (this.data.end_time != null) types += " $TYPE_LIMITED"
    if (!this.data.misc_params?.discount.isNullOrBlank() && this.data.misc_params?.discount != "0") types += " $TYPE_DISCOUNTED"

    this.data.static_assets?.display_tags?.let {
        if (it.isNotBlank()) for (tag in it.split(" ")) types += " ${tag.lowercase()}"
    }

    return types
}

/**
 * Extracts all products of a type freebie to a [FreebieItemEntity] instance
 *
 * @param services used to map each individual product to its' services
 *
 * @param serviceIdWoCharging &
 * @param paramWithCharging &
 * @param paramWithoutCharging &
 * @param apiProvisioningKeyword are used to map its' values to the respective freebie
 * (offers with freebies will have multiple values inside these strings ex. "Fun4ALL:FA, WatchAnime:WA ..."
 * and each value is be mapped to its' freebie)
 *
 * @return [FreebieItemEntity]
 */
private fun Map<String, ProductJson>.extractToFreebieItems(
    services: Map<String, List<ServiceJson>>,
    serviceIdWoCharging: String?,
    paramWithCharging: String?,
    paramWithoutCharging: String?,
    apiProvisioningKeyword: String?
): FreebieItemEntity? {
    val freebieSingleSelectItems = mutableListOf<FreebieSingleSelectItemEntity>()

    // we are extracting the values from provided fields if they contain multiple sections separated with a comma (,)
    // once separated, those values are of format 'freebie_name:value' where the 'value' is a param, serviceId of keyword (catalog values used for payment)
    var serviceWoChargingIds = emptyList<String>()
    serviceIdWoCharging?.let {
        if (it != "N/A" && it.contains(",") && it.contains(":"))
            serviceWoChargingIds = it.split(",")
    }
    var paramsCharge = emptyList<String>()
    paramWithCharging?.let {
        if (it != "N/A" && it.contains(",") && it.contains(":"))
            paramsCharge = it.split(",")
    }
    var paramsNonCharge = emptyList<String>()
    paramWithoutCharging?.let {
        if (it != "N/A" && it.contains(",") && it.contains(":"))
            paramsNonCharge = it.split(",")
    }
    var apiProvisioningKeywords = emptyList<String>()
    apiProvisioningKeyword?.let {
        if (it != "N/A" && it.contains(",") && it.contains(":"))
            apiProvisioningKeywords = it.split(",")
    }
    var size = 0L
    var sizeUnit = ""
    var duration: Int
    var oneFreebieDescription = ""

    if (!values.any { product -> services[product.id]?.all { it.isFreebie() } == true })
        return null

    values.forEach { product ->
        if (services[product.id]?.all { it.data.misc_params?.type == FREEBIE || it.data.misc_params?.type == FREEBIE_VOUCHER } == true) {
            services[product.id]?.forEach { service ->
                oneFreebieDescription = service.data.description ?: ""
                duration =
                    (service.data.misc_params?.duration?.toIntOrNull()
                        ?: 0) / SECONDS_IN_ONE_DAY
                service.data.unit_amount?.let {
                    size = when {
                        it >= GB -> it / GB
                        it >= MB -> it / MB
                        it >= KB -> it / KB
                        else -> it
                    }
                    sizeUnit = when {
                        it >= GB -> GB_STRING
                        it >= MB -> MB_STRING
                        it >= KB -> KB_STRING
                        else -> B_STRING
                    }
                }
                val icons = mutableListOf<String>()
                service.data.misc_params?.freebie_appname_appicon?.split(",")
                    ?.forEach { appNameAppIcon ->
                        if (appNameAppIcon.isNotEmpty() && appNameAppIcon.contains("~")) {
                            appNameAppIcon.split("~").getOrNull(1)?.let {
                                icons.add(it.trim())
                            }
                        }
                    }
                freebieSingleSelectItems.add(
                    FreebieSingleSelectItemEntity(
                        title = product.data.name ?: "",
                        serviceNoneChargeId = if (serviceWoChargingIds.isNotEmpty()) serviceWoChargingIds.find {
                            it.contains(
                                product.data.name ?: "-1"
                            )
                        }?.split(":")?.getOrNull(1) ?: "" else serviceIdWoCharging
                            ?: "",
                        serviceChargeParam = if (paramsCharge.isNotEmpty()) paramsCharge.find {
                            it.contains(
                                product.data.name ?: "-1"
                            )
                        }?.split(":")?.getOrNull(1) ?: "" else paramWithCharging
                            ?: "",
                        apiProvisioningKeyword = if (apiProvisioningKeywords.isNotEmpty()) apiProvisioningKeywords.find {
                            it.contains(
                                product.data.name ?: "-1"
                            )
                        }?.split(":")?.getOrNull(1) ?: "" else apiProvisioningKeyword
                            ?: "",
                        serviceNonChargeParam = if (paramsNonCharge.isNotEmpty()) paramsNonCharge.find {
                            it.contains(
                                product.data.name ?: "-1"
                            )
                        }?.split(":")?.getOrNull(1) ?: "" else paramWithoutCharging ?: "",
                        size = size,
                        sizeUnit = sizeUnit,
                        duration = duration,
                        icons = icons,
                        type = service.data.misc_params?.type ?: ""
                    )
                )
            }
        }
    }
    return if (freebieSingleSelectItems.size > 1)
    // if the offer contains more than one freebie then user can select only one of them
        FreebieItemEntity("Freebie of your choice (pick one):", null, freebieSingleSelectItems)
    else
        FreebieItemEntity(oneFreebieDescription, null, null)
}

/**
 * Extracts booster categories that this offer is a part of.
 * The function should only execute with a non-null result if the current offer is a booster.
 *
 * @return List<String> of booster categories this offer is a part of, else if the offer is not a booster return null
 */
private fun Map<String, CategoryJson>.extractBoosterCategories(): List<String>? {
    val boosterCategories = mutableListOf<String>()

    values.forEach { categoryJson ->
        categoryJson.data.misc_params?.let {
            it.booster?.let {
                boosterCategories.add(it)
            }
        }
    }

    return if (boosterCategories.isNotEmpty()) boosterCategories else null
}

private fun String.extractToBoosterAppIcon(): List<AppItemEntity> {
    val appItems = mutableListOf<AppItemEntity>()
    val apps = this.split(',')

    for (app in apps) {
        if (app != "" && app.contains("~"))
            appItems.add(
                AppItemEntity(
                    app.split("~").getOrNull(1)?.trim() ?: "",
                    app.split("~").getOrNull(0) ?: ""
                )
            )
        else
            appItems.add(AppItemEntity("", ""))
    }

    return appItems
}

/**
 * For the given list of services and provided [dataType] and [serviceType],
 * the function returns amount of the service for that [dataType].
 *
 * @param dataType, available values: "mobile", "home", "app" and "freebie"
 * ("freebie" is not a data type itself but is considered as such due to the catalog configuration)
 * @param serviceType, used to match the service type we want to take into consideration
 *
 * @return List<Long>, list of amounts for the service list for provided [serviceType] and [dataType]
 */
private fun List<ServiceJson>.extractServiceSize(
    @CatalogServiceAllocationType
    serviceType: Int,
    @CatalogDataType
    dataType: String? = null
): List<Long> =
    this.filter { it.data.service_type == serviceType && (serviceType != DATA_SERVICE_VALUE || it.data.misc_params?.type == dataType ?: "") }
        .map { service ->
            if (service.hasUnlimitedAmount()) UNLIMITED_VALUE_INDICATOR
            else service.getUnitAmount()
        }.filter { it != 0L }

/**
 * For the given list of services and provided [dataType], the function returns maximum of data that can be used
 * for that [dataType].
 *
 * The function does the same job as
 * @see extractServiceSize function
 * but it calculates the total available amount with calculating it's own amount and duration with the product's duration.
 * Logic: total = service::amount * product::duration / service::duration
 * This way we take into consideration the case when data amount resets in the midst of product's duration time.
 *
 * @param dataType, available values: "mobile", "home", "app" and "freebie"
 * ("freebie" is not a data type itself but is considered as such due to the catalog configuration)
 * @param products, used to fetch the parent product of the service for it's duration field
 */
private fun List<ServiceJson>.extractMaximumDataServiceSize(
    @CatalogDataType
    dataType: String,
    products: Map<String, ProductJson>
): List<Long> =
    this.filter { it.data.service_type == DATA_SERVICE_VALUE && it.data.misc_params?.type == dataType }
        .map { service ->
            val parentProduct =
                products.values.find { it.data.services?.contains(service.id) == true }
                    ?: return listOf()

            if (service.hasUnlimitedAmount()) UNLIMITED_VALUE_INDICATOR
            else if (service.hasDuration() && parentProduct.hasDuration()) {
                // if both duration values are present in the catalog we calculate maximum data that can be used
                service.getUnitAmount() *
                        (parentProduct.getDuration()) / service.getDuration()
            } else {
                // else, we only use it's value
                service.getUnitAmount()
            }
        }.filter { it != 0L }

/**
 * For the given list of services we extract the sole amount of data that can be used
 *
 * @param products, used to fetch the parent product of the service for it's duration field
 *
 * @return Long, the maximum data that can be allocates by the offer
 */
private fun List<ServiceJson>.extractTotalDataSize(
    products: Map<String, ProductJson>
): Long {

    return (extractMaximumDataServiceSize(
        MOBILE_DATA_KEY, products
    ).let { if (it.isEmpty()) 0L else if (it.first() == UNLIMITED_VALUE_INDICATOR) return UNLIMITED_VALUE_INDICATOR else it.first() }
            +
            extractMaximumDataServiceSize(
                HOME_DATA_KEY, products
            ).let { if (it.isEmpty()) 0L else if (it.first() == UNLIMITED_VALUE_INDICATOR) return UNLIMITED_VALUE_INDICATOR else it.first() }
            +
            extractMaximumDataServiceSize(
                APP_DATA_KEY, products
            ).let { if (it.isEmpty()) 0L else if (it.first() == UNLIMITED_VALUE_INDICATOR) return UNLIMITED_VALUE_INDICATOR else it.first() }
            + (extractMaximumDataServiceSize(FREEBIE, products).maxOrNull() ?: 0L))
}

/**
 * Due to catalog's configuration the description should be either in service::data::description or service::data::name
 */
private fun List<ServiceJson>.extractServiceDescription(
    @CatalogServiceAllocationType
    serviceType: Int,
    @CatalogDataType
    dataType: String? = null
): List<String> =
    this.filter { it.data.service_type == serviceType && (serviceType != DATA_SERVICE_VALUE || it.data.misc_params?.type == dataType ?: "") }
        .map { service ->
            if (service.hasUnlimitedAmount())
                service.data.description ?: ""
            else
                service.data.name ?: ""
        }

private fun List<ServiceJson>.extractFunctions(isBooster: Boolean): List<String> {
    val functions = mutableListOf<String>()

    forEach { service ->
        when (service.data.service_type) {
            DATA_SERVICE_VALUE -> {
                functions.add("${service.data.misc_params?.type}_data_allocation")
            }
            CALLS_SERVICE_VALUE -> {
                functions.add("voice_allocation")
            }
            SMS_SERVICE_VALUE -> {
                functions.add("sms_allocation")
            }
        }
    }

    if (isBooster) functions.add("booster_allocation")

    return functions
}

/**
 * By catalog's configuration we are extracting the colors with the logic
 *
 * @return String, the hex string of a color
 */
private fun OfferJson.extractDisplayColor(): String =
    when {
        data.misc_params?.type ?: "" == CONTENT -> {
            CONTENT_ACCENT_COLOR
        }
        data.static_assets?.display_color.isNullOrBlank() -> {
            DEFAULT_ACCENT_COLOR
        }
        else -> {
            data.static_assets?.display_color ?: DEFAULT_ACCENT_COLOR
        }
    }

private fun Map<String, CategoryJson>.containsGoCreateCategory(): Boolean {
    return values.any { category -> category.data.name == CATEGORY_GO_CREATE }
}

fun List<AppItemEntity>.toDomain() = this.map { AppItem(it.appIcon, it.appName) }

fun ApplicationServiceEntity.toDomain() =
    ApplicationService(
        description,
        apps?.toDomain()
    )

fun FreebieItemEntity.toDomain() =
    FreebieItem(
        description,
        icon,
        items?.map {
            FreebieSingleSelectItem(
                it.title,
                it.serviceChargeParam,
                it.serviceNonChargeParam,
                it.serviceNoneChargeId,
                it.apiProvisioningKeyword,
                it.size,
                it.sizeUnit,
                it.duration,
                it.icons,
                it.type
            )
        }
    )

fun Flow<List<ShopItemEntity>?>.toDomain() = this.map { it?.map { it.toDomain() } ?: emptyList() }

// we are handling unlimited values as -1L
const val UNLIMITED_VALUE_INDICATOR = -1L

const val CONTENT_ACCENT_COLOR = "#A569AD"
const val DEFAULT_ACCENT_COLOR = "#000000"

// service type values used by the catalog
private const val DATA_SERVICE_VALUE = 1
private const val CALLS_SERVICE_VALUE = 2
private const val SMS_SERVICE_VALUE = 3

@IntDef(
    DATA_SERVICE_VALUE,
    CALLS_SERVICE_VALUE,
    SMS_SERVICE_VALUE
)
private annotation class CatalogServiceAllocationType

@StringDef(
    APP_DATA_KEY,
    HOME_DATA_KEY,
    MOBILE_DATA_KEY,
    // due to catalog configuration, freebie is also considered as a data type in ServiceJson instances
    FREEBIE
)
private annotation class CatalogDataType
