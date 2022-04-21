/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.register

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.custom.AccountRegisteredByEmail
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.RegisterFragmentBinding
import ph.com.globe.globeonesuperapp.register.utils.EMAIL_MAX_LENGTH
import ph.com.globe.globeonesuperapp.register.utils.EmailValidator
import ph.com.globe.globeonesuperapp.register.utils.setupEmailInputFilter
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.ui.setImageViewColorFilter
import ph.com.globe.globeonesuperapp.utils.ui.setTextViewColor
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment :
    NoBottomNavViewBindingFragment<RegisterFragmentBinding>(bindViewBy = {
        RegisterFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val viewModel: RegisterViewModel by viewModels()
    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    private lateinit var tvOneUpperCaseCharacter: TextView
    private lateinit var imgOneUpperCaseCharacter: ImageView

    private lateinit var tvOneLowerCaseCharacter: TextView
    private lateinit var imgOneLowerCaseCharacter: ImageView

    private lateinit var tvOneNumber: TextView
    private lateinit var imgOneNumber: ImageView

    private lateinit var tvOneSpecialCharacter: TextView
    private lateinit var imgOneSpecialCharacter: ImageView

    private lateinit var tvAlphanumericCharacters: TextView
    private lateinit var imgAlphanumericCharacters: ImageView

    private lateinit var tvPasswordsMatch: TextView
    private lateinit var imgPasswordsMatch: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:register screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        with(viewBinding) {
            val text = getString(R.string.privacy_policy)
            val linkedTextOne = getString(R.string.data_privacy_act_of_2012)
            val indexOfOne = text.indexOf(linkedTextOne)
            val linkedTextTwo = getString(R.string.privacy_policy_of_globe)
            val indexOfTwo = text.indexOf(linkedTextTwo)

            val spannableString = SpannableStringBuilder(text)

            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        handleUrl(PRIVACY_ACT)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = ContextCompat.getColor(requireContext(), R.color.primary)
                        ds.isFakeBoldText = true
                        ds.isUnderlineText = false
                    }
                },
                indexOfOne,
                indexOfOne + linkedTextOne.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        handleUrl(PRIVACY_POLICY)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = ContextCompat.getColor(requireContext(), R.color.primary)
                        ds.isFakeBoldText = true
                        ds.isUnderlineText = false
                    }
                },
                indexOfTwo,
                indexOfTwo + linkedTextTwo.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            tvPrivacyPolicy.movementMethod = LinkMovementMethod.getInstance()
            tvPrivacyPolicy.text = spannableString

            cbPrivacyPolicy.setOnCheckedChangeListener { _, isChecked ->
                viewModel.privacyPolicyIsChecked(isChecked)
            }

            val secondLayout = incPasswordStrength.expandable.secondLayout

            tvOneUpperCaseCharacter = secondLayout.findViewById(R.id.tv_one_uppercase_character)
            imgOneUpperCaseCharacter = secondLayout.findViewById(R.id.iv_one_uppercase_character)

            tvOneLowerCaseCharacter = secondLayout.findViewById(R.id.tv_one_lowercase_character)
            imgOneLowerCaseCharacter = secondLayout.findViewById(R.id.iv_one_lowercase_character)

            tvOneNumber = secondLayout.findViewById(R.id.tv_one_number)
            imgOneNumber = secondLayout.findViewById(R.id.iv_one_number)

            tvOneSpecialCharacter = secondLayout.findViewById(R.id.tv_one_special_character)
            imgOneSpecialCharacter = secondLayout.findViewById(R.id.iv_one_special_character)

            tvAlphanumericCharacters = secondLayout.findViewById(R.id.tv_9_alphanumeric_characters)
            imgAlphanumericCharacters = secondLayout.findViewById(R.id.iv_9_alphanumeric_characters)

            tvPasswordsMatch = secondLayout.findViewById(R.id.tv_passwords_match)
            imgPasswordsMatch = secondLayout.findViewById(R.id.iv_passwords_match)

            clBackWrapper.setOnClickListener {
                findNavController().navigateUp()
            }

            tvGetStarted.setOnClickListener {
                findNavController().popBackStack(R.id.selectSignMethodFragment, false)
            }

            with(viewModel) {
                etEmailAddress.apply {
                    setupEmailInputFilter { maxLengthReached ->
                        if (maxLengthReached) {
                            requireContext().showError(
                                tilEmailAddress,
                                etEmailAddress,
                                getString(R.string.error_email_max_length, EMAIL_MAX_LENGTH),
                                textColor = R.color.functional_primary
                            )
                            etEmailAddress.setOutlinedErrorBackground()
                        }
                    }
                    addTextChangedListener {
                        emailIsValid(it.toString())
                        requireContext().hideError(
                            tilEmailAddress,
                            etEmailAddress
                        )
                        etEmailAddress.setOutlinedBackground()
                    }
                    setOnFocusChangeListener { _, hasFocus ->
                        if (!hasFocus && emailValid.value != EmailValidator.Status.Ok) {
                            val messageId = R.string.error_invalid_email_format

                            requireContext().showError(
                                tilEmailAddress,
                                etEmailAddress,
                                getString(messageId),
                                textColor = R.color.functional_primary
                            )
                            etEmailAddress.setOutlinedErrorBackground()
                        } else {
                            requireContext().hideError(
                                tilEmailAddress,
                                etEmailAddress
                            )
                            etEmailAddress.setOutlinedBackground()
                        }
                    }
                }

                etPassword.addTextChangedListener {
                    tilPassword.isPasswordVisibilityToggleEnabled = !it.isNullOrEmpty()
                    validatePassword(it.toString(), viewBinding.etConfirmPassword.text.toString())
                }

                etConfirmPassword.addTextChangedListener {
                    tilConfirmPassword.isPasswordVisibilityToggleEnabled = !it.isNullOrEmpty()
                    validatePasswordsMatch(
                        viewBinding.etPassword.text.toString(),
                        it.toString()
                    )
                }

                btnSetPassword.setOnClickListener {
                    register(
                        etEmailAddress.text.toString(),
                        etPassword.text.toString(),
                        etConfirmPassword.text.toString()
                    )
                }

                canProceed.observe(viewLifecycleOwner, {
                    btnSetPassword.isEnabled = it
                })

                progressBarValue.observe(viewLifecycleOwner, {
                    incPasswordStrength.lpiPasswordStrengthIndicator.progress = it
                })

                passwordContainsUpperCharacters.observe(
                    viewLifecycleOwner,
                    { containsUpperCaseCharacters ->

                        displayPasswordConditionResult(
                            requireContext(),
                            containsUpperCaseCharacters,
                            tvOneUpperCaseCharacter,
                            imgOneUpperCaseCharacter
                        )

                    })

                viewModel.passwordContainsLowerCharacters.observe(
                    viewLifecycleOwner,
                    { containsLowerCaseCharacters ->

                        displayPasswordConditionResult(
                            requireContext(),
                            containsLowerCaseCharacters,
                            tvOneLowerCaseCharacter,
                            imgOneLowerCaseCharacter
                        )

                    })

                passwordContainsNumber.observe(viewLifecycleOwner, { containsNumbers ->

                    displayPasswordConditionResult(
                        requireContext(),
                        containsNumbers,
                        tvOneNumber,
                        imgOneNumber
                    )

                })

                passwordContainsSpecialCharacters.observe(
                    viewLifecycleOwner,
                    { containsSpecialCharacters ->

                        displayPasswordConditionResult(
                            requireContext(),
                            containsSpecialCharacters,
                            tvOneSpecialCharacter,
                            imgOneSpecialCharacter
                        )

                    })

                passwordContains9Characters.observe(viewLifecycleOwner, { contains9Characters ->

                    displayPasswordConditionResult(
                        requireContext(),
                        contains9Characters,
                        tvAlphanumericCharacters,
                        imgAlphanumericCharacters
                    )

                })

                passwordsMatch.observe(viewLifecycleOwner, { passwordsMatch ->

                    displayPasswordConditionResult(
                        requireContext(),
                        passwordsMatch,
                        tvPasswordsMatch,
                        imgPasswordsMatch
                    )

                })

                registerResult.observe(viewLifecycleOwner, {

                    it.handleEvent { registrationResult ->

                        when (registrationResult) {

                            is RegisterViewModel.RegisterResult.RegisterSuccessful -> {
                                logCustomEvent(AccountRegisteredByEmail)
                                findNavController().safeNavigate(R.id.action_registerFragment_to_emailVerificationFragment)
                            }

                            is RegisterViewModel.RegisterResult.EmailAddressAlreadyInUse -> {
                                tilEmailAddress.editText?.setText("")
                                findNavController().safeNavigate(R.id.action_registerFragment_to_emailExistsFragment)
                            }
                        }
                    }
                })

                passwordStrength.observe(viewLifecycleOwner, { strength ->

                    when (strength) {

                        is RegisterViewModel.PasswordStrength.KeepGoingStrength -> {
                            incPasswordStrength.tvStrength.visibility = View.VISIBLE
                            incPasswordStrength.tvStrength.text = getString(R.string.keep_going)
                            incPasswordStrength.tvStrength.setTextColor(
                                ResourcesCompat.getColor(
                                    resources,
                                    R.color.password_strength_low_red,
                                    null
                                )
                            )
                            incPasswordStrength.lpiPasswordStrengthIndicator.setIndicatorColor(
                                ResourcesCompat.getColor(
                                    resources,
                                    R.color.password_strength_low_red,
                                    null
                                )
                            )
                        }

                        is RegisterViewModel.PasswordStrength.AlmostThereStrength -> {
                            incPasswordStrength.tvStrength.visibility = View.VISIBLE
                            incPasswordStrength.tvStrength.text = getString(R.string.almost_there)
                            incPasswordStrength.tvStrength.setTextColor(
                                ResourcesCompat.getColor(
                                    resources,
                                    R.color.password_strength_moderate_yellow,
                                    null
                                )
                            )
                            incPasswordStrength.lpiPasswordStrengthIndicator.setIndicatorColor(
                                ResourcesCompat.getColor(
                                    resources,
                                    R.color.password_strength_moderate_yellow,
                                    null
                                )
                            )
                        }

                        is RegisterViewModel.PasswordStrength.GoodJobStrength -> {
                            incPasswordStrength.tvStrength.visibility = View.VISIBLE
                            incPasswordStrength.tvStrength.text = getString(R.string.strong)
                            incPasswordStrength.tvStrength.setTextColor(
                                ResourcesCompat.getColor(
                                    resources,
                                    R.color.password_strength_full_green,
                                    null
                                )
                            )
                            incPasswordStrength.lpiPasswordStrengthIndicator.setIndicatorColor(
                                ResourcesCompat.getColor(
                                    resources,
                                    R.color.password_strength_full_green,
                                    null
                                )
                            )
                        }

                        is RegisterViewModel.PasswordStrength.NoPassword -> {
                            incPasswordStrength.tvStrength.visibility = View.GONE
                        }
                    }
                })
            }
        }


    }

    private fun handleUrl(url: String): Boolean {
        generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        return true
    }

    /**
     * Displays condition results for any of the password conditions.
     */
    private fun displayPasswordConditionResult(
        context: Context,
        conditionsResult: Boolean,
        textView: TextView,
        imageView: ImageView
    ) {

        if (conditionsResult) {

            context.setTextViewColor(
                textView,
                R.color.password_strength_full_green
            )

            context.setImageViewColorFilter(
                imageView,
                R.color.password_strength_full_green
            )

        } else {

            context.setTextViewColor(
                textView,
                R.color.black_70
            )

            context.setImageViewColorFilter(
                imageView,
                R.color.black_30
            )
        }
    }

    override val logTag = "RegisterPasswordFragment"

    override val analyticsScreenName: String = "get_started.sign_up.register_password"
}

private const val PRIVACY_ACT = "https://www.privacy.gov.ph/data-privacy-act/"
private const val PRIVACY_POLICY = "https://www.globe.com.ph/privacy-policy.html"
