package ph.com.globe.globeonesuperapp.payment.adyen.util

import com.adyen.checkout.components.model.payments.Amount

class GlobeAmount(value: Double) : Amount() {
    init {
        this.currency = "PHP"
        // DropIn will count the amount value with the two decimal points
        // so we need to multiply user's inputted amount by 100
        this.value = (value * TWO_DECIMALS_ADDITION).toInt()
    }

    companion object {
        const val TWO_DECIMALS_ADDITION = 100
    }
}
