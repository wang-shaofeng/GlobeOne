/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spanned.SPAN_INCLUSIVE_EXCLUSIVE
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.addCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import okio.IOException
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ProfileDetailsFragmentBinding
import ph.com.globe.globeonesuperapp.profile.ProfileViewModel
import ph.com.globe.globeonesuperapp.termsandprivacypolicy.TERMS_AND_CONDITIONS_URL
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.date.pickDate
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.profile.response_models.*
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.nonEmptyOrNull
import ph.com.globe.util.toDateOrNull
import ph.com.globe.util.toFormattedStringOrEmpty
import javax.inject.Inject

@AndroidEntryPoint
class ProfileDetailsFragment :
    NoBottomNavViewBindingFragment<ProfileDetailsFragmentBinding>(bindViewBy = {
        ProfileDetailsFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    private val profileDetailsArgs by navArgs<ProfileDetailsFragmentArgs>()

    private var provinces: List<Province>? = null

    private var cities: List<City>? = null

    private var barangays: List<Barangay>? = null

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val profileViewModel: ProfileViewModel by hiltNavGraphViewModels(R.id.profile_subgraph)

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setLightStatusBar()

        setHints()

        provinces = loadJSONFile("Province.json")?.provincesFromJson()
        cities = loadJSONFile("City.json")?.citiesFromJson()
        barangays = loadJSONFile("Barangay.json")?.barangaysFromJson()

        barangays
            ?.groupBy { it.mun_code }
            ?.forEach { barangayGroup ->
                cities?.find { it.mun_code == barangayGroup.key }?.barangays =
                    barangayGroup.value.sortedBy { it }
            }

        cities
            ?.groupBy { it.prov_code }
            ?.forEach { cityGroup ->
                provinces?.find { it.prov_code == cityGroup.key }?.cities =
                    cityGroup.value.sortedBy { it }
            }

        provinces = provinces?.sortedBy { it }

        with(viewBinding) {

            val suffixAdapter = ArrayAdapter(
                requireContext(),
                R.layout.default_simple_list_item_holder_layout,
                resources.getStringArray(R.array.profile_details_suffix_options)
            )
            (tilMenuSuffix.editText as? AutoCompleteTextView)?.setAdapter(suffixAdapter)

            with(profileViewModel) {

                showKYCBanner.observe(viewLifecycleOwner, {
                    clCompleteKycBanner.isVisible = it ?: false
                })

                registeredUser.observe(viewLifecycleOwner, { registeredUser ->
                    if (profileViewModel.shouldRepopulateUI)
                        with(viewBinding) {
                            tietEmail.setCompoundDrawablesWithIntrinsicBounds(null,  null, AppCompatResources.getDrawable(requireContext(), R.drawable.ic_successfully_completed_circle), null)
                            tvFullName.text =
                                (if (registeredUser.firstName.isNullOrBlank() && registeredUser.lastName.isNullOrBlank())
                                    getString(R.string.profile_your_name_placeholder)
                                else
                                    "${registeredUser.firstName} ${registeredUser.lastName}").trim()
                            tietEmail.setText(registeredUser.email)
                            tietFirstName.setText(registeredUser.firstName)
                            tietMiddleName.setText(registeredUser.middleName)
                            tietLastName.setText(registeredUser.lastName)
                            tietNickname.setText(registeredUser.nickname)
                            tietPrimaryContactNumber.setText(registeredUser.contactNumber)
                            registeredUser.address?.let {
                                tietAddress1.setText(it.addressLine1)
                                tietAddress2.setText(it.addressLine2)
                                tietStreet.setText(it.street)

                                actvProvince.setText(it.province)
                                actvProvince.setAdapter(provincesAdapter())
                                actvProvince.setOnItemClickListener { _, _, provPosition, _ ->
                                    updateButtonEnabledStatus()
                                    actvCity.setText("")
                                    actvBarangay.setText("")
                                    actvBarangay.setAdapter(null)
                                    provinces?.get(provPosition)?.let { province ->
                                        setupCityActv(province)
                                    }
                                }

                                actvCity.setText(it.city)
                                if (!it.province.isNullOrEmpty()) {
                                    provinces?.find { province -> province.prov_name == it.province }
                                        ?.let { province ->
                                            setupCityActv(province)
                                        }
                                } else {
                                    actvCity.isEnabled = false
                                }

                                actvBarangay.setText(it.barangay)
                                if (!it.city.isNullOrEmpty()) {
                                    cities?.find { city -> city.city_municipality_name == it.city }
                                        ?.let { city ->
                                            actvBarangay.isEnabled = true
                                            actvBarangay.setAdapter(barangaysAdapter(city))
                                        }
                                } else {
                                    actvBarangay.isEnabled = false
                                }
                                actvBarangay.setOnItemClickListener { _, _, _, _ ->
                                    updateButtonEnabledStatus()
                                }

                                tietZipcode.setText(it.postal)
                            }

                            if (registeredUser.suffix?.isNotEmpty() == true)
                                (tilMenuSuffix.editText as? AutoCompleteTextView)?.setText(
                                    registeredUser.suffix, false
                                )

                            (tilMenuSuffix.editText as? AutoCompleteTextView)?.doOnTextChanged { _, _, _, _ ->
                                updateButtonEnabledStatus()
                            }

                            selectSalutation(registeredUser.salutation)

                            actvBirthday.setText(
                                registeredUser.birthdate?.toDateOrNull()
                                    .toFormattedStringOrEmpty(GlobeDateFormat.Default)
                            )
                            birthdateForApi =
                                registeredUser.birthdate?.toDateOrNull()
                                    .toFormattedStringOrEmpty(GlobeDateFormat.ProfileApi)
                            updateButtonEnabledStatus()
                        }
                })

                tietNickname.doOnTextChanged { _, _, _, _ -> updateButtonEnabledStatus() }
                tietFirstName.doOnTextChanged { _, _, _, _ -> updateButtonEnabledStatus() }
                tietMiddleName.doOnTextChanged { _, _, _, _ -> updateButtonEnabledStatus() }
                tietLastName.doOnTextChanged { _, _, _, _ -> updateButtonEnabledStatus() }
                tietPrimaryContactNumber.doOnTextChanged { _, _, _, _ -> updateButtonEnabledStatus() }
                tietAddress1.doOnTextChanged { _, _, _, _ -> updateButtonEnabledStatus() }
                tietAddress2.doOnTextChanged { _, _, _, _ -> updateButtonEnabledStatus() }
                tietStreet.doOnTextChanged { _, _, _, _ -> updateButtonEnabledStatus() }
                tietZipcode.doOnTextChanged { _, _, _, _ -> updateButtonEnabledStatus() }

                cgSalutations.setOnCheckedChangeListener { _, _ ->
                    updateButtonEnabledStatus()
                }

                btnSaveProfileDetailsChanges.setOnClickListener {
                    if (!(tietZipcode.text.toString().length != 4 && tietZipcode.text.toString()
                            .isNotEmpty()
                                || tietPrimaryContactNumber.text.toString().length != 11 && tietPrimaryContactNumber.text.toString()
                            .isNotEmpty())
                    ) {
                        prepareDataForUpdate()

                        if (!isKYCComplete())
                            if (raffleInProggress) {
                                showIncompleteKYCConfirmationDialog({
                                    updateUserProfile()
                                    if (profileDetailsArgs.proceedForRaffle)
                                        findNavController().popBackStack(
                                            R.id.profile_fragment,
                                            true
                                        )
                                }, {})
                            } else {
                                updateUserProfile()
                            }
                        else
                            findNavController().safeNavigate(R.id.action_profileDetailsFragment_to_authenticityConfirmationFragment)
                    } else {
                        showContactNumberError(
                            tietPrimaryContactNumber.text.toString(),
                            tietPrimaryContactNumber,
                            tilPrimaryContactNumber
                        )
                        showZipcodeError(
                            tietZipcode.text.toString(),
                            tietZipcode,
                            tilZipcode
                        )
                    }
                }

                if (dataCertified) {
                    updateUserProfile()
                    findNavController().popBackStack(R.id.profile_fragment, true)
                }

                actvBirthday.setAdapter(null)

                actvBirthday.setOnClickListener {
                    pickDate(
                        requireContext(),
                        action = { birthday ->
                            actvBirthday.setText(birthday.toFormattedStringOrEmpty(GlobeDateFormat.Default))
                            birthdateForApi =
                                birthday.toFormattedStringOrEmpty(GlobeDateFormat.ProfileApi)
                            updateButtonEnabledStatus()
                        })
                }

                tietPrimaryContactNumber.setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus)
                        showContactNumberError(
                            tietPrimaryContactNumber.text.toString(),
                            tietPrimaryContactNumber,
                            tilPrimaryContactNumber
                        )
                    else
                        requireContext().hideError(
                            tilPrimaryContactNumber,
                            tietPrimaryContactNumber
                        )
                }

                tietZipcode.setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus)
                        showZipcodeError(
                            tietZipcode.text.toString(),
                            tietZipcode,
                            tilZipcode
                        )
                    else
                        requireContext().hideError(
                            tilZipcode,
                            tietZipcode
                        )
                }

                tvDataPrivacy.movementMethod = LinkMovementMethod.getInstance()
                tvDataPrivacy.text = buildSpannedString {
                    append(getString(R.string.profile_kyc_data_privacy_policy))
                    append("  ")
                    bold {
                        color(
                            AppCompatResources.getColorStateList(
                                requireContext(),
                                R.color.primary
                            ).defaultColor
                        ) {
                            onClick({
                                generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                                    val intent =
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(TERMS_AND_CONDITIONS_URL)
                                        )
                                    startActivity(intent)
                                }
                            }) {
                                append(getString(R.string.profile_kyc_data_policy_url))
                            }
                        }
                    }
                    append(".")
                }
            }

            wfProfile.onBack {
                decideOnBackPressedAction()
            }

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                decideOnBackPressedAction()
            }
        }
    }

    private fun setHints() {
        with(viewBinding) {
            tilPrimaryContactNumber.makeHintAsteriskRed()
            tilFirstName.makeHintAsteriskRed()
            tilLastName.makeHintAsteriskRed()
            tilAddress1.makeHintAsteriskRed()
            tilStreet.makeHintAsteriskRed()
            tilProvince.makeHintAsteriskRed()
            tilCity.makeHintAsteriskRed()
            tilBarangay.makeHintAsteriskRed()
            tilZipcode.makeHintAsteriskRed()
            tilBirthday.makeHintAsteriskRed()
        }
    }

    private fun TextInputLayout.makeHintAsteriskRed() {
        val currentHint: String = hint?.toString() ?: ""
        val starPosition = currentHint.lastIndexOf("*")
        if (starPosition != -1) {
            hint = "" // clear hint, because previous and new string will be identical
            hint = buildSpannedString {
                append(currentHint)
                setSpan(
                    ForegroundColorSpan(
                        AppCompatResources.getColorStateList(
                            requireContext(),
                            R.color.red
                        ).defaultColor
                    ), starPosition, currentHint.length, SPAN_INCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    private fun ProfileDetailsFragmentBinding.setupCityActv(
        province: Province
    ) {
        actvCity.isEnabled = true
        actvCity.setAdapter(citiesAdapter(province))
        actvCity.setOnItemClickListener { _, _, cityPosition, _ ->
            updateButtonEnabledStatus()
            actvBarangay.setText("")
            province.cities?.get(cityPosition)?.let { city ->
                actvBarangay.isEnabled = true
                actvBarangay.setAdapter(
                    barangaysAdapter(city)
                )
            }
        }
    }

    private fun decideOnBackPressedAction() {
        if (profileDetailsArgs.proceedForRaffle)
            findNavController().popBackStack(R.id.profile_fragment, true)
        else
            findNavController().navigateUp()
        logCustomEvent(
            analyticsEventsProvider.provideEvent(
                EventCategory.Engagement,
                MY_PROFILE_SCREEN, CLICKABLE_TEXT, BACK
            )
        )
    }

    private fun prepareDataForUpdate() = with(viewBinding) {
        profileViewModel.updateUserProfileParams =
            UpdateUserProfileRequestParams(
                tietFirstName.text.toString().trim().nonEmptyOrNull(),
                tietMiddleName.text.toString().trim().nonEmptyOrNull(),
                tietLastName.text.toString().trim().nonEmptyOrNull(),
                tietNickname.text.toString().trim().nonEmptyOrNull(),
//                            (tilMenuSuffix.editText as? AutoCompleteTextView)?.text.toString().nonEmptyOrNull(),
                null,
                profileViewModel.birthdateForApi?.nonEmptyOrNull(),
                when {
                    chipMr.isChecked -> getString(R.string.profile_details_salutation_mr)
                    chipMrs.isChecked -> getString(R.string.profile_details_salutation_mrs)
                    chipMs.isChecked -> getString(R.string.profile_details_salutation_ms)
                    else -> null
                },
                tietPrimaryContactNumber.text.toString().trim().nonEmptyOrNull(),
                RegisteredUserAddress(
                    (tilProvince.editText as? AutoCompleteTextView)?.text.toString()
                        .nonEmptyOrNull(),
                    (tilCity.editText as? AutoCompleteTextView)?.text.toString()
                        .nonEmptyOrNull(),
                    (tilBarangay.editText as? AutoCompleteTextView)?.text.toString()
                        .nonEmptyOrNull(),
                    tietStreet.text.toString().trim().nonEmptyOrNull(),
                    tietAddress1.text.toString().trim().nonEmptyOrNull(),
                    tietAddress2.text.toString().trim().nonEmptyOrNull(),
                    tietZipcode.text.toString().trim().nonEmptyOrNull(),
                )
            )
        logCustomEvent(
            analyticsEventsProvider.provideEvent(
                EventCategory.Engagement,
                MY_PROFILE_SCREEN, BUTTON, SAVE_CHANGES
            )
        )
    }

    private fun selectSalutation(salutation: String?) {
        with(viewBinding) {
            chipMr.isChecked = salutation == getString(R.string.profile_details_salutation_mr)
            chipMs.isChecked = salutation == getString(R.string.profile_details_salutation_ms)
            chipMrs.isChecked = salutation == getString(R.string.profile_details_salutation_mrs)
        }
    }

    private fun updateButtonEnabledStatus() {
        with(viewBinding) {
            btnSaveProfileDetailsChanges.isEnabled =
                profileViewModel.registeredUser.value?.let { user ->
                    tietFirstName.text.toString() != user.firstName
                            || tietMiddleName.text.toString() != user.middleName
                            || tietLastName.text.toString() != user.lastName
                            || tietNickname.text.toString() != user.nickname
                            || actvBirthday.text.toString() != user.birthdate?.toDateOrNull()
                        .toFormattedStringOrEmpty(GlobeDateFormat.Default)
                            || (tilMenuSuffix.editText as? AutoCompleteTextView)?.text.toString() != user.suffix
                            || tietPrimaryContactNumber.text.toString() != user.contactNumber
                            || tietAddress1.text.toString() != user.address?.addressLine1
                            || tietAddress2.text.toString() != user.address?.addressLine2
                            || tietStreet.text.toString() != user.address?.street
                            || (tilProvince.editText as? AutoCompleteTextView)?.text.toString() != user.address?.province
                            || (tilCity.editText as? AutoCompleteTextView)?.text.toString() != user.address?.city
                            || (tilBarangay.editText as? AutoCompleteTextView)?.text.toString() != user.address?.barangay
                            || tietZipcode.text.toString() != user.address?.postal
                            || when {
                        chipMr.isChecked -> getString(R.string.profile_details_salutation_mr)
                        chipMrs.isChecked -> getString(R.string.profile_details_salutation_mrs)
                        chipMs.isChecked -> getString(R.string.profile_details_salutation_ms)
                        else -> ""
                    } != user.salutation
                } ?: false
        }
    }

    private fun isKYCComplete() = with(viewBinding) {
        tietAddress1.text.toString().trim().isNotEmpty()
                && tietFirstName.text.toString().trim().isNotEmpty()
                && tietLastName.text.toString().trim().isNotEmpty()
                && tietPrimaryContactNumber.text.toString().trim().isNotEmpty()
                && actvCity.text.toString().trim().isNotEmpty()
                && actvProvince.text.toString().trim().isNotEmpty()
                && actvBarangay.text.toString().trim().isNotEmpty()
                && tietZipcode.text.toString().trim().isNotEmpty()
                && actvBirthday.text.toString().trim().isNotEmpty()
    }

    private fun loadJSONFile(filename: String) = try {
        val inputStream = requireActivity().assets.open(filename)
        val size: Int = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        String(buffer)
    } catch (ex: IOException) {
        ex.printStackTrace()
        null
    }

    private fun provincesAdapter() = ArrayAdapter(
        requireContext(),
        R.layout.default_simple_list_item_holder_layout,
        provinces?.map { it.prov_name } ?: listOf()
    )

    private fun citiesAdapter(province: Province) = ArrayAdapter(
        requireContext(),
        R.layout.default_simple_list_item_holder_layout,
        province.cities?.map { it.city_municipality_name } ?: listOf()
    )

    private fun barangaysAdapter(city: City) = ArrayAdapter(
        requireContext(),
        R.layout.default_simple_list_item_holder_layout,
        city.barangays?.map { it.brgy_name } ?: listOf()
    )

    private fun isUserMinor(birthDate: Long) =
        (System.currentTimeMillis() - birthDate) < DateUtils.DAY_IN_MILLIS * 365.25 * 18

    override val logTag = "ProfileDetailsFragment"

    override val analyticsScreenName = "profile.details"
}
