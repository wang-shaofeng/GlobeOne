package ph.com.globe.data.shared_preferences.payment

import android.content.SharedPreferences
import ph.com.globe.data.shared_preferences.payment.di.SHARED_PREFS_PAYMENT_PARAMETERS_KEY
import ph.com.globe.domain.payment.repo.PaymentParametersRepo
import javax.inject.Inject
import javax.inject.Named

class DefaultPaymentParametersRepository @Inject constructor(
    @Named(
        SHARED_PREFS_PAYMENT_PARAMETERS_KEY
    ) private val sharedPreferences: SharedPreferences
) :
    PaymentParametersRepo {
    override fun getMerchantAccount(): String {
        return sharedPreferences.getString(MERCHANT_ACCOUNT_KEY, "") ?: ""
    }

    override fun getTokenPaymentId(): String {
        return sharedPreferences.getString(TOKEN_PAYMENT_ID_KEY, "") ?: ""
    }

    override fun setMerchantAccount(merchantAccount: String) {
        sharedPreferences.edit().putString(MERCHANT_ACCOUNT_KEY, merchantAccount).apply()
    }

    override fun setTokenPaymentId(tokenPaymentId: String) {
        sharedPreferences.edit().putString(TOKEN_PAYMENT_ID_KEY, tokenPaymentId).apply()
    }

    companion object {
        private const val MERCHANT_ACCOUNT_KEY = "merchant_account_key"
        private const val TOKEN_PAYMENT_ID_KEY = "token_payment_id_key"
    }
}
