package ph.com.globe.globeonesuperapp.account.plan_details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.AccountDetailsViewModel
import ph.com.globe.globeonesuperapp.databinding.AccountPlanDetailsFragmentBinding
import ph.com.globe.globeonesuperapp.utils.getCutOffDay
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.billings.domain_models.ExcessCharges
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.toDateOrNull
import ph.com.globe.util.toFormattedStringOrEmpty
import ph.com.globe.util.toOrdinal
import java.util.*

@AndroidEntryPoint
class AccountPlanDetailsFragment :
    NoBottomNavViewBindingFragment<AccountPlanDetailsFragmentBinding>(bindViewBy = {
        AccountPlanDetailsFragmentBinding.inflate(it)
    }) {

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    private val accountPlanDetailsViewModel: AccountPlanDetailsViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }

    private val accountDetailsViewModel: AccountDetailsViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setDarkStatusBar()
        with(viewBinding) {

            ivClose.setOnClickListener {
                findNavController().navigateUp()
            }

            with(accountDetailsViewModel) {
                with(accountPlanDetailsViewModel) {
                    with(selectedEnrolledAccount) {

                        clSpendingLimit.isVisible = segment == AccountSegment.Mobile
                        tvContractEndDate.isVisible = segment == AccountSegment.Mobile
                        tvLabelContractEndDate.isVisible = segment == AccountSegment.Mobile

                        fetchData(primaryMsisdn, segment)

                        ivArrowLearnMore.setOnClickListener {
                            handleUrl(SPENDING_LIMIT_URL)
                        }
                        tvLearnMore.setOnClickListener {
                            handleUrl(SPENDING_LIMIT_URL)
                        }

                        when (segment) {
                            AccountSegment.Mobile -> {
                                mobilePlanDetails.observe(viewLifecycleOwner, {
                                    tvPlan.text = it.plan.planName
                                    billingDetails.value?.excessCharges?.let { excessCharges ->
                                        tvSpendingLimit.text = formattedSpendingLimit(
                                            it.plan.spendingLimit,
                                            excessCharges
                                        )
                                    }
                                    tvContractEndDate.text =
                                        it.gadget?.lockInEndDate?.toDateOrNull()
                                            .toFormattedStringOrEmpty(GlobeDateFormat.Default)
                                })
                            }
                            AccountSegment.Broadband -> {
                                broadbandPlanDetails.observe(viewLifecycleOwner, {
                                    tvPlan.text = it.plan.planName
                                    it.plan.bandwidth?.let {
                                        tvInternetSpeed.text = getString(
                                            R.string.account_plan_details_template_up_to_speed,
                                            it
                                        )
                                        tvInternetSpeed.visibility = View.VISIBLE
                                        tvLabelInternetSpeed.visibility = View.VISIBLE
                                    }
                                })
                            }
                        }

                        billingDetails.observe(viewLifecycleOwner, {
                            tvBillDueDate.text = getString(
                                R.string.account_plan_details_template_every_x_of_the_month,
                                it.dueDate.toDateOrNull()?.let { date ->
                                    val calendar = Calendar.getInstance()
                                    calendar.time = date
                                    calendar.get(Calendar.DAY_OF_MONTH).toOrdinal()
                                } ?: it.dueDate.toIntOrNull()?.toOrdinal()
                            )

                            tvCutoffDate.text = getString(
                                R.string.account_plan_details_template_every_x_of_the_month,
                                it.getCutOffDay()
                            )

                            with(it.billingAddress) {
                                tvAddress.text = getString(
                                    R.string.account_plan_details_template_address,
                                    houseNumber.orEmpty(),
                                    building.orEmpty(),
                                    street.orEmpty(),
                                    subdivisionVillage.orEmpty(),
                                    barangay.orEmpty(),
                                    city.orEmpty(),
                                    postalCode.orEmpty()
                                ).trim()
                            }

                            mobilePlanDetails.value?.plan?.spendingLimit?.let { limit ->
                                it.excessCharges?.let { excessCharges ->
                                    tvSpendingLimit.text =
                                        formattedSpendingLimit(limit, excessCharges)
                                }
                            }
                        })

                        accountDetails.observe(viewLifecycleOwner, {
                            tvName.text = if (it.middleName.isNullOrEmpty())
                                getString(
                                    R.string.account_plan_details_name_without_middle,
                                    it.firstName,
                                    it.lastName
                                ) else
                                getString(
                                    R.string.account_plan_details_name_with_middle,
                                    it.firstName,
                                    it.middleName,
                                    it.lastName
                                )
                            tvBirthday.text = it.birthday?.toDateOrNull()
                                .toFormattedStringOrEmpty(GlobeDateFormat.MonthFullNameCustomDate)
                            tvEmail.text = it.email
                        })
                    }
                }
            }
        }
    }

    private fun formattedSpendingLimit(limit: String, excessCharges: ExcessCharges) =
        with(excessCharges) {
            getString(
                R.string.template_fraction,
                getString(
                    R.string.pezos_prefix,
                    (call.toFloat() + data.toFloat() + others.toFloat() + vas.toFloat() + text.toFloat()).toString()
                ),
                getString(R.string.pezos_prefix, limit)
            )
        }

    private fun handleUrl(url: String): Boolean {
        generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        return true
    }

    override val logTag = "AccountPlanDetailsFragment"

}

private const val SPENDING_LIMIT_URL =
    "https://www.globe.com.ph/help/postpaid/spending-limit.html#gref"
