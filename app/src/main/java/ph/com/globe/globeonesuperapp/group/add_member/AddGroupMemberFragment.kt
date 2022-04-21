/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.group.add_member

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.AppDataViewModel
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.AddGroupMemberFragmentBinding
import ph.com.globe.globeonesuperapp.group.GroupViewModel
import ph.com.globe.globeonesuperapp.group.GroupViewModel.AddMemberResult.AddMemberSuccess
import ph.com.globe.globeonesuperapp.group.GroupViewModel.AddMemberResult.SubscriberAlreadyMember
import ph.com.globe.globeonesuperapp.shop.util.ContactsViewModel
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.shop.formattedForPhilippines
import javax.inject.Inject

@AndroidEntryPoint
class AddGroupMemberFragment : NoBottomNavViewBindingFragment<AddGroupMemberFragmentBinding>({
    AddGroupMemberFragmentBinding.inflate(it)
}), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val appDataViewModel: AppDataViewModel by activityViewModels()

    private val contactsViewModel: ContactsViewModel by viewModels()
    private val groupViewModel: GroupViewModel by navGraphViewModels(R.id.group_subgraph) { defaultViewModelProviderFactory }

    private lateinit var addEnrolledAccountRecyclerViewAdapter: AddEnrolledAccountRecyclerViewAdapter

    private var selectedNumber = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDarkStatusBar()

        with(viewBinding) {
            addEnrolledAccountRecyclerViewAdapter =
                AddEnrolledAccountRecyclerViewAdapter(
                    enablingViewsCallback = { enableButton ->
                        btnAddMember.isEnabled = enableButton
                        tilMobileNumber.isSelected = false
                    }, { account ->
                        selectedNumber = account.msisdn
                        closeKeyboard(etMobileNumber, requireContext())
                        tilMobileNumber.error = null
                        groupViewModel.selectAccount(account.msisdn)
                    })
            rvAccounts.adapter = addEnrolledAccountRecyclerViewAdapter
            rvAccounts.itemAnimator = null

            groupViewModel.enrolledAccounts.observe(viewLifecycleOwner, {
                addEnrolledAccountRecyclerViewAdapter.submitList(it)
            })

            with(etMobileNumber) {
                addTextChangedListener { editable ->
                    requireContext().hideError(tilMobileNumber, etMobileNumber)
                    btnAddMember.isEnabled = !editable.isNullOrBlank()
                    editable.formatCountryCodeIfExists()
                    tilMobileNumber.hint = contactsViewModel.getNumberOwnerOrPlaceholder(
                        editable.toString(),
                        getString(R.string.mobile_number)
                    )
                    selectedNumber = editable.toString().formattedForPhilippines()
                }

                setOnFocusChangeListener { v, hasFocus ->
                    if (v != null) {
                        if (hasFocus) {
                            groupViewModel.unselectAccounts()
                            selectedNumber = ""
                            tilMobileNumber.isSelected = true
                        }
                        btnAddMember.isEnabled = !text.isNullOrBlank()
                    }
                }

                setOnEditorActionListener { v, _, _ ->
                    selectedNumber = etMobileNumber.text.toString().formattedForPhilippines()
                    if (v.text.isNotEmpty())
                        contactsViewModel.selectAndValidateNumber(v.text.toString())
                    closeKeyboard(v, requireContext())
                    true
                }
            }

            contactsViewModel.lastCheckedNumberValidation.observe(
                viewLifecycleOwner,
                { validation ->
                    reflectValidationToErrorDisplaying(validation, etMobileNumber, tilMobileNumber)
                    if (validation?.number == selectedNumber && validation.isValid) {
                        groupViewModel.addGroupMember(selectedNumber, groupViewModel.ownerAccountAlias)
                    }
                })

            ivClose.setOnClickListener {
                findNavController().navigateUp()
            }

            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }

            btnCancel.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        GROUP_DATA_SCREEN, BUTTON, CANCEL
                    )
                )
                findNavController().navigateUp()
            }

            btnAddMember.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        GROUP_DATA_SCREEN, BUTTON, ADD_ACCOUNT
                    )
                )
                contactsViewModel.selectAndValidateNumber(selectedNumber)
            }

            groupViewModel.addGroupMemberResult.observe(viewLifecycleOwner, {
                it.handleEvent { result ->
                    when (result) {
                        is AddMemberSuccess -> {
                            appDataViewModel.refreshAccountDetailsData(selectedNumber)
                            findNavController().navigateUp()
                        }

                        is SubscriberAlreadyMember -> requireContext().showError(
                            tilMobileNumber,
                            etMobileNumber,
                            resources.getString(R.string.already_member_error)
                        )
                    }
                }
            })
        }
    }

    override val logTag = "AddGroupMemberFragment"

    override val analyticsScreenName = "group.add_member"
}
