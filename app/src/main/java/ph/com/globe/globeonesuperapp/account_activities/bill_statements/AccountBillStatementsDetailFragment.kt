/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account_activities.bill_statements

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account_activities.AccountActivitiesViewModel
import ph.com.globe.globeonesuperapp.account_activities.STATEMENTS_HELP
import ph.com.globe.globeonesuperapp.databinding.AccountBillStatementsDetailFragmentBinding
import ph.com.globe.globeonesuperapp.utils.balance.toPezosFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.spannedLinkString
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.toDateOrNull
import ph.com.globe.util.toFormattedStringOrNull

@AndroidEntryPoint
class AccountBillStatementsDetailFragment :
    NoBottomNavViewBindingFragment<AccountBillStatementsDetailFragmentBinding>({
        AccountBillStatementsDetailFragmentBinding.inflate(it)
    }) {

    private val accountActivityViewModel by hiltNavGraphViewModels<AccountActivitiesViewModel>(R.id.account_activities_subgraph)

    private val generalEventsViewModel by activityViewModels<GeneralEventsViewModel>()

    private val args by navArgs<AccountBillStatementsDetailFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        with(viewBinding) {
            wfHistory.onBack { findNavController().navigateUp() }

            tvNeedHelp.movementMethod = LinkMovementMethod.getInstance()
            tvNeedHelp.text = spannedLinkString(
                getString(R.string.bill_statements_details) + " ",
                getString(R.string.ask_for_help),
                AppCompatResources.getColorStateList(requireContext(), R.color.primary).defaultColor
            ) {
                generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(STATEMENTS_HELP))
                    startActivity(intent)
                }
            }

            with(incBillInfo) {
                accountActivityViewModel.enrolledAccount.observe(viewLifecycleOwner) {
                    tvAccountAlias.text = it.accountAlias
                    val startDate = args.billingStatement.billStartDate?.toDateOrNull()
                        .toFormattedStringOrNull(GlobeDateFormat.Default)
                    val endDate = args.billingStatement.billEndDate?.toDateOrNull()
                        .toFormattedStringOrNull(GlobeDateFormat.Default)

                    tvDates.text =
                        getString(
                            R.string.account_details_template_bill_time_span,
                            startDate,
                            endDate
                        )

                    tvBillAmount.text =
                        args.billingStatement.totalAmount?.toPezosFormattedDisplayBalance()
                }

                btnViewBill.setOnClickListener {
                    accountActivityViewModel.enrolledAccount.value?.let {
                        findNavController().safeNavigate(
                            AccountBillStatementsDetailFragmentDirections.actionAccountBillStatementsDetailFragmentToORStatementFragment(
                                it,
                                args.billingStatement.id ?: "",
                                args.billingStatement.verificationToken
                            )
                        )
                    }
                }
            }
        }
    }

    override val logTag: String = "BIllStatementsDetailFragment"
}
