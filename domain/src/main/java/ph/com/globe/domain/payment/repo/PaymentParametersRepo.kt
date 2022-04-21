package ph.com.globe.domain.payment.repo

interface PaymentParametersRepo {
    fun getMerchantAccount(): String
    fun getTokenPaymentId(): String
    fun setMerchantAccount(merchantAccount: String)
    fun setTokenPaymentId(tokenPaymentId: String)
}
