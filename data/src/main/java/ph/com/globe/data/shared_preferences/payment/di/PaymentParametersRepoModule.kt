package ph.com.globe.data.shared_preferences.payment.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import ph.com.globe.data.DataScope
import javax.inject.Named

@Module
internal object PaymentParametersSharedPrefModule {

    @Provides
    @DataScope
    @Named(SHARED_PREFS_PAYMENT_PARAMETERS_KEY)
    fun provideSharedPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_PAYMENT_PARAMETERS_KEY, Context.MODE_PRIVATE)
}

internal const val SHARED_PREFS_PAYMENT_PARAMETERS_KEY = "globe_payment_parameters_shared_prefs"
