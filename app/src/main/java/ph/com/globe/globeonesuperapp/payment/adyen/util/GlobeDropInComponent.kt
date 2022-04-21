package ph.com.globe.globeonesuperapp.payment.adyen.util

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.DropInResult
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import ph.com.globe.globeonesuperapp.BuildConfig.ADYEN_DROPIN_CLIENT_KEY
import ph.com.globe.globeonesuperapp.BuildConfig.FLAVOR_servers
import ph.com.globe.model.payment.ThirdPartyPaymentResult
import ph.com.globe.model.payment.toAdyenResult
import javax.inject.Inject

class GlobeDropInComponent @Inject constructor(@ApplicationContext val context: Context) {
    fun startPayment(amount: Double, paymentMethodsJSON: JSONObject, callingFragment: Fragment) {

        val cardConfiguration = CardConfiguration.Builder(context, ADYEN_DROPIN_CLIENT_KEY)
            .setShowStorePaymentField(false)
            .setEnvironment(getEnvironment())
            .build()

        val dropInConfiguration = DropInConfiguration.Builder(
            context,
            GlobeDropInService::class.java,
            ADYEN_DROPIN_CLIENT_KEY
        )
            .setAmount(GlobeAmount(amount))
            .setEnvironment(getEnvironment())
            .addCardConfiguration(cardConfiguration)
            .build()

        val paymentMethodsApiResponse =
            PaymentMethodsApiResponse.SERIALIZER.deserialize(paymentMethodsJSON)

        DropIn.startPayment(
            callingFragment,
            paymentMethodsApiResponse,
            dropInConfiguration
        )
    }

    fun getReturnUrl(): String = RedirectComponent.getReturnUrl(context)

    fun handleOnActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        successAction: () -> Unit = {},
        errorAction: () -> Unit = {},
        noInternetAction: () -> Unit = {},
        cancelAction: () -> Unit = {},
        cardRefusedAction:() -> Unit = {}
    ) {
        val dropInResult = DropIn.handleActivityResult(requestCode, resultCode, data) ?: return
        when (dropInResult) {
            is DropInResult.Error -> errorAction.invoke()
            is DropInResult.CancelledByUser -> cancelAction.invoke()
            is DropInResult.Finished -> {
                when (dropInResult.result.toAdyenResult()) {
                    is ThirdPartyPaymentResult.AdyenResult.AdyenResultAuthorised -> successAction.invoke()
                    is ThirdPartyPaymentResult.AdyenResult.AdyenResultNoConnection -> noInternetAction.invoke()
                    is ThirdPartyPaymentResult.AdyenResult.AdyenResultRefused -> cardRefusedAction.invoke()
                    in ThirdPartyPaymentResult.AdyenResult.failedTransactionTypes -> errorAction.invoke()
                    else -> errorAction.invoke()
                }
            }
        }
    }

    private fun getEnvironment(): Environment =
        if (FLAVOR_servers == "staging")
            Environment.TEST
        else
            Environment.EUROPE
}
