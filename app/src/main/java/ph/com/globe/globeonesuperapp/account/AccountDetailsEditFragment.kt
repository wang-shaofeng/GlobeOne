/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.AccountDetailsEditFragmentBinding
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.account.getBrandSafely
import ph.com.globe.model.account.toNumberType
import ph.com.globe.model.profile.domain_models.isBroadband
import ph.com.globe.model.shop.formattedForPhilippines
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.util.nonEmptyOrNull
import ph.com.globe.model.util.brand.toUserFriendlyBrandName

class AccountDetailsEditFragment :
    NoBottomNavViewBindingFragment<AccountDetailsEditFragmentBinding>(bindViewBy = {
        AccountDetailsEditFragmentBinding.inflate(it)
    }) {

    private val accountDetailsViewModel: AccountDetailsViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setDarkStatusBar()

        with(viewBinding) {
            with(accountDetailsViewModel) {

                dataIsChanged.oneTimeEventObserve(viewLifecycleOwner) {
                    val customSnackbarViewBinding =
                        GlobeSnackbarLayoutBinding.inflate(LayoutInflater.from(requireContext()))
                    customSnackbarViewBinding.tvGlobeSnackbarTitle.text =
                        getString(R.string.account_changes_saved)
                    customSnackbarViewBinding.tvGlobeSnackbarDescription.text =
                        getString(R.string.account_changes_saved_description)
                    showSnackbar(customSnackbarViewBinding)
                }

                accountAlias.observe(viewLifecycleOwner, { alias ->
                    tvAccountAlias.text = alias
                    tietAccountAlias.setText(alias)
                })

                if (selectedEnrolledAccount.brandType == AccountBrandType.Postpaid) {
                    accountDetails.observe(viewLifecycleOwner, {
                        with(it) {
                            mobileNumber?.nonEmptyOrNull()?.let {
                                tvMobileNumber.isVisible =
                                    !selectedEnrolledAccount.isBroadband()
                                tvMobileNumberTitle.isVisible =
                                    !selectedEnrolledAccount.isBroadband()
                                tvMobileNumber.text =
                                    it.formattedForPhilippines().toDisplayUINumberFormat()
                            }

                            landlineNumber?.nonEmptyOrNull()?.let {
                                val number = it.formatLandlineNumber()
                                tvLandlineNumber.text = number
                                tvLandlineNumber.isVisible = number.isNotEmpty()
                                tvLandlineNumberTitle.isVisible = number.isNotEmpty()
                            }

                            accountNumber.nonEmptyOrNull()?.let {
                                tvAccountNumber.visibility = View.VISIBLE
                                tvAccountNumberTitle.visibility = View.VISIBLE
                                tvAccountNumber.text = it
                            }
                        }
                    })
                } else {
                    tvMobileNumber.isVisible = !selectedEnrolledAccount.isBroadband()
                    tvMobileNumberTitle.isVisible = !selectedEnrolledAccount.isBroadband()
                    tvMobileNumber.text = selectedEnrolledAccount.primaryMsisdn
                    tvMobileNumberTitle.text =
                        selectedEnrolledAccount.primaryMsisdn.toNumberType().toString()
                }

                brandStatus.observe(viewLifecycleOwner) { status ->
                    tvBrandLabel.text = status.getBrandSafely()
                        ?.toUserFriendlyBrandName(selectedEnrolledAccount.segment)
                }

                tietAccountAlias.doOnTextChanged {s:CharSequence?, _, _, _ ->
                    btnSaveChanges.isEnabled =
                        s.toString().trim() != tvAccountAlias.text.toString() && s?.isNotBlank() ?: false
                }

                ivClose.setOnClickListener {
                    findNavController().navigateUp()
                }

                btnSaveChanges.setOnClickListener {
                    modifyAccount(tietAccountAlias.text.toString())
                }

                btnRemoveAccount.setOnClickListener {
                    removeAccount()
                }

                accountDeleted.oneTimeEventObserve(viewLifecycleOwner) {
                    findNavController().popBackStack(R.id.dashboardFragment, false)
                }
            }
        }
    }

    override val logTag = "AccountDetailsEditFragment"
}
