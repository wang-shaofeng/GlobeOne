/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.util.brand

import ph.com.globe.model.util.brand.AccountBrand.*
import ph.com.globe.model.util.brand.AccountBrandType.Postpaid
import ph.com.globe.model.util.brand.AccountBrandType.Prepaid
import ph.com.globe.model.util.brand.AccountSegment.Broadband
import ph.com.globe.model.util.brand.AccountSegment.Mobile
import java.io.Serializable

/**
 * Sealed class representing the account brands supported by the App.
 *
 * Along with the actual brand information, the object instances of this classes subtypes will provide methods for
 * API/UseCase handling and UI display of each individual brand in every situation.
 *
 * New GlobeOne SuperApp supports following brands: [GhpPrepaid] as "GHP-Prepaid", [GhpPostpaid] as "GHP" (postpaid), [Hpw] as "PW", [Tm] as "TM"
 */
sealed class AccountBrand : Serializable {

    // The field representing a raw string value of a brand. It is used for APIs and for general mapping of the brand
    // (brand string to object instance and vice versa conversion is performed by the value of this field).
    abstract val name: String

    // Field used for UI display, a user friendly brand name string
    abstract val uiName: String

    // The type of the brand, can be either "prepaid" or "postpaid"
    abstract val brandType: AccountBrandType

    fun isPostpaid(): Boolean = brandType == Postpaid
    fun isPrepaid(): Boolean = brandType == Prepaid

    override fun toString(): String = name

    object GhpPrepaid : AccountBrand(), Serializable {
        override val name get() = GHP_PREPAID_BRAND_NAME
        override val uiName get() = GHP_PREPAID_USER_FRIENDLY_NAME
        override val brandType get() = Prepaid
    }

    object GhpPostpaid : AccountBrand(), Serializable {
        override val name: String get() = GHP_POSTPAID_BRAND_NAME
        override val uiName: String get() = GHP_POSTPAID_USER_FRIENDLY_NAME
        override val brandType get() = Postpaid
    }

    object Hpw : AccountBrand(), Serializable {
        override val name get() = HPW_BRAND_NAME
        override val uiName get() = HPW_USER_FRIENDLY_NAME
        override val brandType get() = Prepaid
    }

    object Tm : AccountBrand(), Serializable {
        override val name get() = TM_BRAND_NAME
        override val uiName get() = TM_USER_FRIENDLY_NAME
        override val brandType get() = Prepaid
    }
}

/**
 * This sealed class represents the type of an account.
 *
 * It is replacing the raw string occurrences in the app. Available values: "postpaid" as [Postpaid] and "prepaid" as [Prepaid]
 */
sealed class AccountBrandType : Serializable {
    object Postpaid : AccountBrandType(), Serializable {
        override fun toString(): String = POSTPAID_BRAND_TYPE
    }

    object Prepaid : AccountBrandType(), Serializable {
        override fun toString(): String = PREPAID_BRAND_TYPE
    }
}

/**
 * This sealed class represents the segment of an account.
 *
 * It is replacing the raw string occurrences in the app. Available values: "mobile" as [Mobile] and "broadband" as [Broadband]
 */
sealed class AccountSegment : Serializable {
    object Mobile : AccountSegment(), Serializable {
        override fun toString(): String = MOBILE_SEGMENT
    }

    object Broadband : AccountSegment(), Serializable {
        override fun toString(): String = BROADBAND_SEGMENT
    }
}
