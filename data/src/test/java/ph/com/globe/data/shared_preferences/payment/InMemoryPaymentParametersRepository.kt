package ph.com.globe.data.shared_preferences.payment

import ph.com.globe.domain.payment.repo.PaymentParametersRepo
import javax.inject.Inject

class InMemoryPaymentParametersRepository @Inject constructor() :
    PaymentParametersRepo {
    private var merchantAccount: String = ""
    private var tokenPaymentId: String = ""
    override fun getMerchantAccount(): String {
        return merchantAccount
    }

    override fun getTokenPaymentId(): String {
        return tokenPaymentId
    }

    override fun setMerchantAccount(merchantAccount: String) {
        this.merchantAccount = merchantAccount
    }

    override fun setTokenPaymentId(tokenPaymentId: String) {
        this.tokenPaymentId = tokenPaymentId
    }
}
