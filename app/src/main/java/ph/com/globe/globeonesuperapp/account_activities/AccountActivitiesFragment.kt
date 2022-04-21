/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account_activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.AccountActivitiesFragmentBinding
import ph.com.globe.globeonesuperapp.rewards.allrewards.enrolled_accounts.EntryPoint
import ph.com.globe.globeonesuperapp.rewards.allrewards.enrolled_accounts.SelectEnrolledAccountFragment
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.oneTimeEventObserve
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.spannedLinkString
import ph.com.globe.globeonesuperapp.utils.toDisplayUINumberFormat
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.domain_models.isBroadband
import ph.com.globe.model.profile.domain_models.isPrepaid

@AndroidEntryPoint
class AccountActivitiesFragment : NoBottomNavViewBindingFragment<AccountActivitiesFragmentBinding>({
    AccountActivitiesFragmentBinding.inflate(it)
}) {

    private val accountActivityViewModel by hiltNavGraphViewModels<AccountActivitiesViewModel>(R.id.account_activities_subgraph)

    private val generalEventsViewModel by activityViewModels<GeneralEventsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLightStatusBar()

        parentFragmentManager.setFragmentResultListener(
            SelectEnrolledAccountFragment.ENROLLED_ACCOUNT_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val result =
                bundle.getSerializable(SelectEnrolledAccountFragment.ENROLLED_ACCOUNT_NUMBER_KEY) as EnrolledAccount

            accountActivityViewModel.setEnrolledAccount(result)
        }

        with(viewBinding) {
            accountActivityViewModel.scrollToTop.oneTimeEventObserve(viewLifecycleOwner) {
                ablActivities.setExpanded(true, true)
                ablInfo.setExpanded(true, true)
            }

            accountActivityViewModel.enrolledAccount.observe(viewLifecycleOwner) {
                if (it != null) {
                    tvAccountNumber.text =
                        it.primaryMsisdn.toDisplayUINumberFormat()
                    tvAccountName.hint = it.accountAlias
                }
                val isPrepaid = it.isPrepaid()
                val isMobile = !it.isBroadband()
                vpAccountActivities.adapter =
                    AccountPagerAdapter(this@AccountActivitiesFragment, isPrepaid, isMobile)
                vpAccountActivities.isUserInputEnabled = false

                val primaryColor = AppCompatResources.getColorStateList(
                    requireContext(),
                    R.color.primary
                ).defaultColor

                vpAccountActivities.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        tvNeedHelp.text = if (isPrepaid || !isMobile) {
                            tvNeedHelp.movementMethod = null
                            when(position) {
//                                TRANSACTION_TAB -> {
//                                    getString(R.string.transactions_note_prepaid_ledger)
//                                }
                                PREPAID_REWARDS_TAB -> {
                                    getString(R.string.transactions_note)
                                }
                                else -> getString(R.string.transactions_note)
                            }
                        } else {
                            when (position) {
                                BILL_STATEMENTS_TAB -> {
                                    tvNeedHelp.movementMethod = LinkMovementMethod.getInstance()
                                    spannedLinkString(
                                        getString(R.string.statements_trouble) + " ",
                                        getString(R.string.ask_for_help),
                                        primaryColor
                                    ) {
                                        generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                                            val intent =
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(STATEMENTS_HELP)
                                                )
                                            startActivity(intent)
                                        }
                                    }
                                }
                                BILL_PAYMENTS_TAB -> {
                                    tvNeedHelp.movementMethod = LinkMovementMethod.getInstance()
                                    spannedLinkString(
                                        getString(R.string.payments_trouble) + " ",
                                        getString(R.string.ask_for_help),
                                        primaryColor
                                    ) {
                                        generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                                            val intent =
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(PAYMENTS_HELP)
                                                )
                                            startActivity(intent)
                                        }
                                    }
                                }
                                POSTPAID_REWARDS_TAB -> {
                                    tvNeedHelp.movementMethod = null
                                    getString(R.string.transactions_note)
                                }
                                else -> throw IllegalStateException()
                            }
                        }
                    }
                })

                TabLayoutMediator(tlAccountActivities, vpAccountActivities)
                { tab, position ->
                    tab.text = if (isPrepaid) {
                        when (position) {
                            //TRANSACTION_TAB -> getString(R.string.transaction_title)
                            PREPAID_REWARDS_TAB -> getString(R.string.rewards)
                            else -> throw IllegalStateException()
                        }
                    } else if (isMobile) {
                        when (position) {
                            BILL_STATEMENTS_TAB -> getString(R.string.bill_statements_title)
                            BILL_PAYMENTS_TAB -> getString(R.string.bill_payments_title)
                            POSTPAID_REWARDS_TAB -> getString(R.string.rewards)
                            else -> throw IllegalStateException()
                        }
                    } else getString(R.string.rewards)
                }.attach()
            }

            ivEdit.setOnClickListener { chooseNumber() }
            wfRewardsHistory.onBack { findNavController().navigateUp() }
        }
    }

    private fun chooseNumber() {
        findNavController().safeNavigate(AccountActivitiesFragmentDirections
            .actionAccountActivityFragmentToSelectEnrolledAccountFragment2(EntryPoint.HISTORY))
    }

    override val logTag: String = "AccountActivitiesFragment"
}

private const val TRANSACTION_TAB = 0
private const val PREPAID_REWARDS_TAB = 0
private const val BILL_STATEMENTS_TAB = 0
private const val BILL_PAYMENTS_TAB = 1
private const val POSTPAID_REWARDS_TAB = 2

private const val PAYMENTS_HELP = "https://m.me/globeph?ref=NG1ReportPayment"
const val STATEMENTS_HELP = "https://m.me/globeph?ref=NG1BillDispute"
