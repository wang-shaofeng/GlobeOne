/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile.details.kyc

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.dashboard.CREDITED_TO_KEY
import ph.com.globe.globeonesuperapp.dashboard.EarnedTicketsResult.EarnedTicketsSuccessfully
import ph.com.globe.globeonesuperapp.dashboard.TICKETS_EARNED_KEY
import ph.com.globe.globeonesuperapp.dashboard.TITLE_KEY
import ph.com.globe.globeonesuperapp.databinding.ProfileAuthenticityConfirmationFragmentBinding
import ph.com.globe.globeonesuperapp.profile.ProfileViewModel
import ph.com.globe.globeonesuperapp.termsandprivacypolicy.TERMS_AND_CONDITIONS_URL
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class AuthenticityConfirmationFragment :
    NoBottomNavViewBindingFragment<ProfileAuthenticityConfirmationFragmentBinding>(
        bindViewBy = { ProfileAuthenticityConfirmationFragmentBinding.inflate(it) }
    ) {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val profileViewModel: ProfileViewModel by hiltNavGraphViewModels(R.id.profile_subgraph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDarkStatusBar()

        with(viewBinding) {
            profileViewModel.shouldRepopulateUI = false

            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            icClose.setOnClickListener {
                findNavController().navigateUp()
            }

            btnConfirm.setOnClickListener {
                profileViewModel.dataCertified = true
                profileViewModel.updateUserProfile()
            }

            val spannableStringLink =
                SpannableString(getString(R.string.profile_kyc_certify_authenticity_description))
            val spanStart = 262
            val spanEnd = 266

            spannableStringLink.apply {
                setSpan(
                    URLSpan(CERTIFY_URL),
                    spanStart,
                    spanEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.primary
                        )
                    ), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(StyleSpan(Typeface.BOLD), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(object : UnderlineSpan() {
                    override fun updateDrawState(tp: TextPaint) {
                        tp.isUnderlineText = false
                    }
                }, spanStart, spanEnd, 0)
            }
            tvCertifyAuthenticityDescription.text = spannableStringLink
            tvCertifyAuthenticityDescription.movementMethod = LinkMovementMethod.getInstance()

            profileViewModel.earnedTickets.observe(viewLifecycleOwner, {
                it.handleEvent { earnedTicketsResult ->
                    if (earnedTicketsResult is EarnedTicketsSuccessfully) {
                        crossBackstackNavigator.crossNavigateWithoutHistory(
                            BaseActivity.RAFFLE_KEY,
                            R.id.earnedRaffleTicketsFragment,
                            bundleOf(
                                TITLE_KEY to getString(R.string.earned_tickets),
                                CREDITED_TO_KEY to earnedTicketsResult.profileName,
                                TICKETS_EARNED_KEY to earnedTicketsResult.numOfTickets
                            )
                        )
                    } else {
                        crossBackstackNavigator.crossNavigateWithoutHistory(
                            BaseActivity.DASHBOARD_KEY,
                            R.id.dashboardFragment
                        )
                    }
                }
            })
        }
    }

    override val logTag = "AuthenticityConfirmationFragment"

}

private val CERTIFY_URL = "https://help.gcash.com/hc/en-us/articles/360017722393-How-do-I-get-Fully-Verified-"