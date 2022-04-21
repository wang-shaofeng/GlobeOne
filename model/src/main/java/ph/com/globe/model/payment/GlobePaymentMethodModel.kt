package ph.com.globe.model.payment

import java.io.Serializable

abstract class GlobePaymentMethod : Serializable {
    abstract val name: String

    sealed class ThirdPartyPaymentMethod : GlobePaymentMethod(), Serializable {
        object Adyen : ThirdPartyPaymentMethod(), Serializable {
            override val name: String = "Adyen"
        }

        object GCash : ThirdPartyPaymentMethod(), Serializable {
            override val name: String = "GCash"
        }
    }

    object ChargeToLoad : GlobePaymentMethod(), Serializable {
        override val name: String = "ChargeToLoad"
    }
}

const val GCASH = "GCASH"
const val ADYEN_DROPIN = "DROPIN"
