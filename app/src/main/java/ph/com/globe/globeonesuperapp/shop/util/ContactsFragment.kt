/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.util

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.payment.CURRENT_NAV_GRAPH
import ph.com.globe.globeonesuperapp.utils.PHILIPPINES_COUNTRY_CODE_NUMBER_INT
import ph.com.globe.globeonesuperapp.utils.PHILIPPINES_COUNTRY_CODE_NUMBER_STRING
import ph.com.globe.globeonesuperapp.utils.PHILIPPINES_LOCAL_NUMBER_SIZE
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.justPhoneNumber
import ph.com.globe.globeonesuperapp.utils.permissions.requestPermission
import ph.com.globe.model.shop.ContactData
import ph.com.globe.model.shop.PhoneNumber
import ph.com.globe.model.shop.formattedForPhilippines
import ph.com.globe.util.subStringWithChecks
import javax.inject.Inject

@AndroidEntryPoint
class ContactsFragment : Fragment(), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private lateinit var contactsViewModel: ContactsViewModel

    private val belongsToNavigationId: Int
        get() = arguments?.getString(CURRENT_NAV_GRAPH)?.toInt() ?: R.id.shop_subgraph

    private var numberSelected = false

    private val onContactSelected =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.data?.let { uri ->
                    val cursor = context?.contentResolver?.query(uri, null, null, null, null)
                    cursor?.takeIf { cursor.count > 0 }?.let { cursor ->

                        cursor.moveToFirst()

                        val number = cursor.getString(
                            cursor.getColumnIndex(
                                CommonDataKinds.Phone.NUMBER
                            )
                        )

                        val name = cursor.getString(
                            cursor.getColumnIndex(
                                CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY
                            )
                        )

                        contactsViewModel.numberSelectedFromContacts =
                            ContactData(name, number.justPhoneNumber().formattedForPhilippines())

                        chooseNumber(
                            parseNumber(number.justPhoneNumber())?.formattedForPhilippines() ?: ""
                        )

                        cursor.close()
                    }
                }
            } else {
                findNavController().navigateUp()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermission(
            permissionType = Manifest.permission.READ_CONTACTS,
            rationaleTitle = R.string.rationale_title,
            rationaleMessage = R.string.contacts_rationale_message,
            whenGranted = {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        PERMISSIONS_SCREEN, BUTTON, ALLOW_SERVICES
                    )
                )
                launchContactPickingIntent()
            },
            whenDenied = {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        PERMISSIONS_SCREEN, BUTTON, DENY_SERVICES
                    )
                )
                findNavController().navigateUp()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        contactsViewModel =
            navGraphViewModels<ContactsViewModel>(belongsToNavigationId) { defaultViewModelProviderFactory }.value
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        // ensuring number is updated as a result of contact picking
        if (!numberSelected)
            contactsViewModel.selectAndValidateNumber(contactsViewModel.selectedNumber.value)
        super.onDestroyView()
    }

    private fun chooseNumber(number: String) {
        numberSelected = true
        contactsViewModel.selectAndValidateNumber(number)
        findNavController().navigateUp()
    }

    private fun launchContactPickingIntent() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = CommonDataKinds.Phone.CONTENT_TYPE
        onContactSelected.launch(intent)
    }

    private fun parseNumber(number: String?) = if (number != null && number.isNotEmpty()) {
        when (number.length) {
            // check if the number's length is 9. If true then create the object using 63.
            PHILIPPINES_LOCAL_NUMBER_SIZE ->
                PhoneNumber(PHILIPPINES_COUNTRY_CODE_NUMBER_INT, number)
            else -> PhoneNumber(
                number.subStringWithChecks(0, PHILIPPINES_COUNTRY_CODE_NUMBER_STRING.length)
                    .toInt(),
                number.subStringWithChecks(
                    PHILIPPINES_COUNTRY_CODE_NUMBER_STRING.length,
                    number.length
                )
            )
        }
    } else null

    override val analyticsScreenName = "contacts"

}
