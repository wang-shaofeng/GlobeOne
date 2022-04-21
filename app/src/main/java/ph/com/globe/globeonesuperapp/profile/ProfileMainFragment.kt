/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BuildConfig
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ProfileFragmentBinding
import ph.com.globe.globeonesuperapp.termsandprivacypolicy.TERMS_AND_CONDITIONS_URL
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.oneTimeEventObserve
import ph.com.globe.globeonesuperapp.utils.setWhiteStatusBar
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.BottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class ProfileMainFragment :
    BottomNavViewBindingFragment<ProfileFragmentBinding>(bindViewBy = {
        ProfileFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    private val profileFragmentArgs by navArgs<ProfileMainFragmentArgs>()

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val profileViewModel: ProfileViewModel by navGraphViewModels(R.id.profile_subgraph) { defaultViewModelProviderFactory }

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:my profile screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if (profileFragmentArgs.proceedForRaffle)
            findNavController().safeNavigate(
                ProfileMainFragmentDirections.profileToProfileDetails(
                    true
                )
            )

        setWhiteStatusBar()

        with(viewBinding) {

            with(profileViewModel) {

                nickname.observe(viewLifecycleOwner, { nickname ->
                    tvProfileUserNickname.text = nickname
                })

                shouldShowGameVouchers.observe(viewLifecycleOwner, { shouldShowGameVouchers ->
                    psivGameVouchers.isVisible = shouldShowGameVouchers
                })

                gameVouchersUrlResult.observe(viewLifecycleOwner, {
                    it.handleEvent { url ->
                        generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                            findNavController().safeNavigate(
                                ProfileMainFragmentDirections.actionProfileFragmentToGameVouchersWebViewFragment(
                                    url
                                )
                            )
                        }
                    }
                })

                psivGameVouchers.setOnClickListener {
                    getGameVouchersUrl()
                }

                logoutConfirmed.oneTimeEventObserve(viewLifecycleOwner, {
                    logCustomEvent(
                        analyticsEventsProvider.provideCustomGAEvent(
                            GAEventCategory.LoginLogout,
                            USER_LOGOUT,
                            profileViewModel.encryptedUserEmail
                        )
                    )
                })
            }

            psivProfileDetails.setOnClickListener {
                findNavController().safeNavigate(R.id.profile_to_profile_details)
            }

            psivPaymentMethods.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        MY_PROFILE_SCREEN, CLICKABLE_TEXT, PAYMENT_METHODS
                    )
                )

                logCustomEvent(
                    analyticsEventsProvider.provideCustomGAEvent(
                        GAEventCategory.AccountSettings,
                        GET_PAYMENT_METHOD,
                        profileViewModel.encryptedUserEmail
                    )
                )

                findNavController().safeNavigate(R.id.action_profile_fragment_to_payment_methods_subgraph)
            }

            psivPrivacyPolicy.setOnClickListener {
                generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_AND_CONDITIONS_URL))
                    startActivity(intent)
                }
            }

            psivLogout.setOnClickListener {
                profileViewModel.logout()
            }
            tvVersion.text = getString(R.string.version_code, BuildConfig.VERSION_NAME)
        }
    }

    override val logTag = "ProfileMainFragment"

    override val analyticsScreenName = "profile.main"
}
