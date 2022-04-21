/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.util.brand

/**
 * The extension function used to convert raw account brand string to a proper object instance.
 *
 * IMPORTANT!!! The purpose of the extension function is only to be used by retrofit adapters and room type converters
 */
fun String.toAccountBrand(): AccountBrand =
    when (this) {
        GHP_POSTPAID_BRAND_NAME -> AccountBrand.GhpPostpaid
        GHP_PREPAID_BRAND_NAME -> AccountBrand.GhpPrepaid
        HPW_BRAND_NAME -> AccountBrand.Hpw
        TM_BRAND_NAME -> AccountBrand.Tm
        else -> throw IllegalStateException("There is no account brand: \"$this\"")
    }

/**
 * The extension function used to convert raw account brand type string to a proper object instance.
 *
 * IMPORTANT!!! The purpose of the extension function is only to be used by retrofit adapters and room type converters
 */
fun String.toAccountBrandType(): AccountBrandType =
    when (this) {
        POSTPAID_BRAND_TYPE -> AccountBrandType.Postpaid
        PREPAID_BRAND_TYPE -> AccountBrandType.Prepaid
        else -> throw IllegalStateException("There is no account brand type: \"$this\"")
    }
/**
 * The extension function used to convert raw account segment string to a proper object instance.
 *
 * IMPORTANT!!! The purpose of the extension function is only to be used by retrofit adapters and room type converters
 */
fun String.toAccountSegment(): AccountSegment =
    when (this) {
        BROADBAND_SEGMENT -> AccountSegment.Broadband
        MOBILE_SEGMENT -> AccountSegment.Mobile
        else -> throw IllegalStateException("There is no account segment: \"$this\"")
    }

fun AccountBrand.toSegment(): AccountSegment =
    when (this) {
        // TODO Important!
        // The 'GHP' brand can be both the 'broadband' and the 'mobile'
        // Here, we are considering it 'mobile' for GhpPostpaid brands
        // (as this ext function is only used on non postpaid flows)
        is AccountBrand.Hpw -> AccountSegment.Broadband
        else -> AccountSegment.Mobile
    }

fun AccountBrand.toUserFriendlyBrandName(segment: AccountSegment? = null): String =
    when {
        segment == AccountSegment.Broadband && this is AccountBrand.GhpPostpaid -> GHP_POSTPAID_BROADBAND_USER_FRIENDLY_NAME
        else -> this.uiName
    }
