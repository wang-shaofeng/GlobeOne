# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}
# End Glide

# Adyen Checkout
-keep class com.adyen.checkout.core.** { *; }
-dontwarn com.adyen.checkout.nfc.**
-dontwarn com.adyen.checkout.googlepay.**
-dontwarn com.adyen.checkout.wechatpay.**
-dontwarn com.adyen.checkout.threeds.**
# End Adyen Checkout

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Keep custom exceptions.
# End Firebase Crashlytics

# G1 Superapp
-keep class ph.com.globe.model.** { *; }
-keep class ph.com.globe.data.network.payment.model.** { *; }
-keep class ph.com.globe.globeonesuperapp.addaccount.confirmaccount.ConfirmAccountArgs { *; }
-keep class ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel$SendOtpResult { *; }
-keep class ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel$SendOtpResult$SentOtpSuccess { *; }
-keep class ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel$ResendOtpResult$ResendOtpSuccess { *; }
-keep class ph.com.globe.globeonesuperapp.payment.PaymentParameters { *; }
-keep class ph.com.globe.globeonesuperapp.payment.payment_loading_session.PaymentMethodWrapper { *; }
-keep class ph.com.globe.globeonesuperapp.payment.payment_processing.ProcessingEntryPoint { *; }
-keep class ph.com.globe.globeonesuperapp.group.group_overview.GroupMemberItem { *; }
-keep class ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts.AddAccountMoreAccountsViewModel$EnrollAccountUI { *; }
-keep class ph.com.globe.globeonesuperapp.shop.promo.filter.BoosterDetailsItem { *; }
-keep class ph.com.globe.globeonesuperapp.shop.promo.filter.Applications { *; }
-keep class ph.com.globe.globeonesuperapp.shop.promo.filter.AppDetailsItem { *; }
-keep class ph.com.globe.globeonesuperapp.addaccount.broadband.choosemodem.ModemItem { *; }
-keep class  ph.com.globe.globeonesuperapp.addaccount.broadband.failurescreen.HpwBroadBandEnrollmentError  { *; }
-keep class ph.com.globe.globeonesuperapp.addaccount.broadband.verification.AddAccountProcessingFragmentEntryPoint { *; }
-keep class ph.com.globe.globeonesuperapp.addaccount.broadband.verification.SecurityQuestions { *; }
-keep enum ph.com.globe.globeonesuperapp.rewards.allrewards.enrolled_accounts.EntryPoint { *; }
-keep class ph.com.globe.globeonesuperapp.rewards.pos.EnrolledAccountWithPoints { *; }
-keep class ph.com.globe.globeonesuperapp.rewards.pos.EnrolledAccountWithBrandAndPointsUiModel { *; }
-keep enum ph.com.globe.globeonesuperapp.rewards.pos.State { *; }
-keep class ph.com.globe.globeonesuperapp.account.AvailableCampaignPromosModelParcelable { *; }
-keep class ph.com.globe.globeonesuperapp.rewards.AccountBrandParcelable { *; }
-keep enum ph.com.globe.globeonesuperapp.account.personalized_campaigns.OfferType { *; }
-keep class ph.com.globe.globeonesuperapp.gocreate.loading.LoadingEntryPoint { *; }
-keep class ph.com.globe.globeonesuperapp.rewards.data_as_currency.ConversionResult { *; }
-keep class ph.com.globe.model.group.domain_models.UsageItem { *; }
#End G1 Superapp
