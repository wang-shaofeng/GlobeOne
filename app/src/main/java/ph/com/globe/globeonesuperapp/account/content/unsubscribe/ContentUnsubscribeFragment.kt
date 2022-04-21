/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.content.unsubscribe

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.CLICKABLE_TEXT
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.events.SUBSCRIPTION_SCREEN
import ph.com.globe.analytics.events.UNSUBSCRIBE
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.analytics.logger.eLog
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.content.ContentSubscriptionsViewModel
import ph.com.globe.globeonesuperapp.databinding.AccountDetailsContentUnsubscribeFragmentBinding
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.toDateWithTimeZoneOrNull
import ph.com.globe.util.toFormattedStringOrEmpty
import javax.inject.Inject

@AndroidEntryPoint
class ContentUnsubscribeFragment :
    NoBottomNavViewBindingFragment<AccountDetailsContentUnsubscribeFragmentBinding>(bindViewBy = {
        AccountDetailsContentUnsubscribeFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val subscriptionsViewModel: ContentSubscriptionsViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }
    private val unsubscribeViewModel: ContentUnsubscribeViewModel by viewModels()

    private val unsubscribeArguments: ContentUnsubscribeFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        with(viewBinding) {

            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }

            with(unsubscribeArguments) {

                try {
                    vPromoInfoBackground.setBackgroundColor(Color.parseColor(contentSubscription.displayColor))
                } catch (e: Exception) {
                    vPromoInfoBackground.setBackgroundColor(Color.parseColor("#000000"))
                    eLog(Exception("displayColor has a bad format: ${contentSubscription.displayColor}"))
                }

                GlobeGlide.with(ivPromoIcon).load(contentSubscription.asset).into(ivPromoIcon)

                tvPromoName.text = contentSubscription.promoName

                val expirationDate = contentSubscription.expiryDate
                    .toDateWithTimeZoneOrNull()
                    .toFormattedStringOrEmpty(GlobeDateFormat.Default)
                tvExpirationDate.text = getString(R.string.expires, expirationDate)

                tvPromoDescription.text = contentSubscription.description

                tvUnsubscribe.setOnClickListener {
                    val unsubscribeEvent = analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        SUBSCRIPTION_SCREEN, CLICKABLE_TEXT, UNSUBSCRIBE,
                        productName = contentSubscription.promoName
                    )
                    logCustomEvent(unsubscribeEvent)
                    unsubscribeViewModel.unsubscribeContentPromo(
                        mobileNumber,
                        contentSubscription.serviceId,
                        contentSubscription.promoName
                    ) { logCustomEvent(unsubscribeEvent) }
                }

                unsubscribeViewModel.unsubscribePromoResult.observe(viewLifecycleOwner, {
                    it.handleEvent {
                        subscriptionsViewModel.removeContentSubscription(
                            contentSubscription.serviceId
                        )
                    }
                })
            }
        }

        subscriptionsViewModel.subscriptionRemovedEvent.observe(viewLifecycleOwner, {
            it.handleEvent {
                findNavController().navigateUp()
            }
        })
    }

    override val logTag = "ContentUnsubscribeFragment"

    override val analyticsScreenName = "content.unsubscribe"
}
