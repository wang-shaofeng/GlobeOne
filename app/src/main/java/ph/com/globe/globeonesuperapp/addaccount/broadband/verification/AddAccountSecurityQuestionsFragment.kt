/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband.verification

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.data.network.util.VERIFICATION_TYPE_SECURITY_QUESTIONS
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.confirmaccount.ConfirmAccountArgs
import ph.com.globe.globeonesuperapp.databinding.AddAccountSecurityQuestionsFragmentLayoutBinding
import ph.com.globe.globeonesuperapp.utils.date.pickDate
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.auth.SecurityQuestion
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.toFormattedStringOrEmpty

@AndroidEntryPoint
class AddAccountSecurityQuestionsFragment :
    NoBottomNavViewBindingFragment<AddAccountSecurityQuestionsFragmentLayoutBinding>(
        bindViewBy = {
            AddAccountSecurityQuestionsFragmentLayoutBinding.inflate(it)
        }
    ) {

    private val viewModel: AddAccountSecurityQuestionsViewModel by viewModels()

    private val securityQuestionsArgs: AddAccountSecurityQuestionsFragmentArgs by navArgs()

    // SecurityQuestions
    private val securityQuestions: List<SecurityQuestion> by lazy {
        listOf(
            securityQuestionsArgs.securityQuestions.question1,
            securityQuestionsArgs.securityQuestions.question2,
            securityQuestionsArgs.securityQuestions.question3,
            securityQuestionsArgs.securityQuestions.question4
        )
    }

    // Question TextViews
    private var questionViews: List<TextView> = emptyList()

    // Answers TextViews
    private var stagingAnswers: List<TextView> = emptyList()

    // TextInputLayouts
    private var answerTilViews: List<TextInputLayout> = emptyList()

    // AutoCompleteTextViews views
    private var answerActvViews: List<AutoCompleteTextView> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setDarkStatusBar()

        with(viewBinding) {
            questionViews = listOf(
                tvQuestion1,
                tvQuestion2,
                tvQuestion3,
                tvQuestion4
            )
            stagingAnswers = listOf(
                tvStagingAnswer1,
                tvStagingAnswer2,
                tvStagingAnswer3,
                tvStagingAnswer4
            )
            answerTilViews = listOf(
                tilAnswer1,
                tilAnswer2,
                tilAnswer3,
                tilAnswer4
            )
            answerActvViews = listOf(
                actvAnswer1,
                actvAnswer2,
                actvAnswer3,
                actvAnswer4
            )
            ivClose.setOnClickListener {
                findNavController().popBackStack(R.id.addAccountNumberFragment, false)
            }
            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }
            btnTryDifferentMethod.setOnClickListener {
                findNavController().navigateUp()
            }
            btnNext.setOnClickListener {
                viewModel.validateSecurityAnswers(
                    securityQuestionsArgs.referenceId,
                    securityQuestionsArgs.msisdn
                )
            }
            tvWhereCanIFindAnswers.setOnClickListener {
                findNavController().safeNavigate(R.id.action_addAccountSecurityQuestionsFragment_to_securityQuestionsFAQFragment)
            }
            with(viewModel) {
                for (index in 0 until NUMBER_OF_SECURITY_QUESTIONS) {
                    questionViews[index].text = securityQuestions[index].question
                    answerTilViews[index].hint = getString(
                        R.string.placeholder_example,
                        securityQuestions[index].placeholderText
                    )
                    answerActvViews[index].apply {
                        addTextChangedListener {
                            if (getView() == null) return@addTextChangedListener
                            inputAnswer(it.toString(), securityQuestions[index].questionId, index)
                            when (securityQuestions[index].fieldType) {
                                // if the input type is dropdown we set dropdown icon
                                DAY_IN_A_MONTH_INPUT_TYPE ->
                                    answerTilViews[index].setDropdownArrow(requireContext())
                                // if the input type is calender we set calendar icon
                                DATE_INPUT_TYPE ->
                                    answerTilViews[index].setCalendarIcon(requireContext())
                                // else we remove the existing icon
                                else -> answerTilViews[index].endIconDrawable = null
                            }
                        }
                        when (securityQuestions[index].fieldType) {
                            NUMERIC_INPUT_TYPE -> {
                                inputType = InputType.TYPE_CLASS_NUMBER
                            }
                            DAY_IN_A_MONTH_INPUT_TYPE -> {
                                val daysInAMonthArrayAdapter =
                                    ArrayAdapter(
                                        requireContext(),
                                        R.layout.default_simple_list_item_holder_layout,
                                        resources.getStringArray(R.array.days_in_a_month).copyOf()
                                    )
                                isFocusable = false
                                answerTilViews[index].setDropdownArrow(requireContext())
                                (answerTilViews[index].editText as? AutoCompleteTextView)?.apply {
                                    setAdapter(daysInAMonthArrayAdapter)
                                    setOnItemClickListener { _, _, position, _ ->
                                        answerActvViews[index].setText(resources.getStringArray(R.array.days_in_a_month)[position].filter { it.isDigit() })
                                    }
                                }
                                setOnClickListener {
                                    answerTilViews[index].setDropdownArrow(requireContext())
                                    answerActvViews[index].showDropdown(daysInAMonthArrayAdapter)
                                }
                            }
                            DATE_INPUT_TYPE -> {
                                answerTilViews[index].setCalendarIcon(requireContext())
                                isFocusable = false
                                setOnClickListener {
                                    pickDate(requireContext()) { date ->
                                        setText(date.toFormattedStringOrEmpty(GlobeDateFormat.SecurityQuestionApi))
                                    }
                                }
                            }
                            else -> Unit // The input type will be a simple text box
                        }
                    }
                }
                nextButtonEnabled.observe(viewLifecycleOwner) {
                    it.handleEvent { enabled ->
                        btnNext.isEnabled = enabled
                    }
                }
                validateSecurityAnswersResult.observe(viewLifecycleOwner) {
                    it.handleEvent { result ->
                        when (result) {
                            is ValidateSecurityAnswersResult.ValidateSecurityAnswersSuccess -> {
                                loadAccountDetails(
                                    securityQuestionsArgs.msisdn,
                                    securityQuestionsArgs.referenceId
                                )
                            }
                            is ValidateSecurityAnswersResult.ValidateSecurityAnswersFailed -> {
                                findNavController().navigateUp()
                            }
                            is ValidateSecurityAnswersResult.ValidateSecurityAnswersInsufficient -> {
                                for (index in 0 until NUMBER_OF_SECURITY_QUESTIONS) {
                                    if (index in result.indexesOfWrongAnswers) {
                                        answerTilViews[index].setValidateResultIcon(
                                            requireContext(),
                                            R.drawable.ic_error_circle
                                        )
                                    } else if (answerActvViews[index].text?.isNotEmpty() == true) {
                                        answerTilViews[index].setValidateResultIcon(
                                            requireContext(),
                                            R.drawable.ic_success_circle
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                loadAccountDetailsResult.observe(viewLifecycleOwner) {
                    it.handleEvent { accountDetailsLoaded ->
                        when (accountDetailsLoaded) {
                            true -> {
                                findNavController().safeNavigate(
                                    AddAccountSecurityQuestionsFragmentDirections.actionAddAccountSecurityQuestionsFragmentToAddAccountConfirmFragment(
                                        ConfirmAccountArgs(
                                            mobileNumber = null,
                                            brand = securityQuestionsArgs.brand,
                                            brandType = securityQuestionsArgs.brand.brandType,
                                            segment = AccountSegment.Broadband,
                                            referenceId = securityQuestionsArgs.referenceId,
                                            accountStatus = accountStatus,
                                            accountNumber = accountNumber,
                                            landlineNumber = landlineNumber,
                                            accountName = accountName,
                                            verificationType = VERIFICATION_TYPE_SECURITY_QUESTIONS
                                        )
                                    )
                                )
                            }
                            false -> findNavController().navigateUp()
                        }
                    }
                }
                getSecurityAnswers(securityQuestionsArgs.referenceId)
                getSecurityAnswersResult.observe(viewLifecycleOwner) { securityAnswers ->
                    for (answerInstance in securityAnswers) {
                        val indexOfTheAnswerView =
                            securityQuestions.indexOf(securityQuestions.find { questionInstance -> questionInstance.questionId == answerInstance.questionId })
                        stagingAnswers[indexOfTheAnswerView].apply {
                            visibility = View.VISIBLE
                            text = answerInstance.answer
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        questionViews = emptyList()
        answerActvViews = emptyList()
        answerTilViews = emptyList()
        stagingAnswers = emptyList()
        super.onDestroyView()
    }

    override val logTag = "AddAccountSecurityQuestionsFragment"
}

fun AutoCompleteTextView.showDropdown(adapter: ArrayAdapter<String>?) {
    if (!this.text.isNullOrEmpty()) {
        adapter?.filter?.filter(null)
    }
}

fun TextInputLayout.setDropdownArrow(context: Context) {
    endIconMode =
        TextInputLayout.END_ICON_DROPDOWN_MENU
    setEndIconTintList(
        AppCompatResources.getColorStateList(
            context,
            R.color.accent_dark
        )
    )
    setEndIconTintMode(PorterDuff.Mode.MULTIPLY)
}

fun TextInputLayout.setCalendarIcon(context: Context) {
    endIconMode =
        TextInputLayout.END_ICON_CUSTOM
    setEndIconTintList(
        AppCompatResources.getColorStateList(
            context,
            R.color.transparent
        )
    )
    setEndIconTintMode(PorterDuff.Mode.SCREEN)
    endIconDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_calendar)
}

fun TextInputLayout.setValidateResultIcon(context: Context, drawableId: Int) {
    endIconMode =
        TextInputLayout.END_ICON_CUSTOM
    setEndIconTintList(
        AppCompatResources.getColorStateList(
            context,
            R.color.transparent
        )
    )
    setEndIconTintMode(PorterDuff.Mode.SCREEN)
    endIconDrawable = AppCompatResources.getDrawable(context, drawableId)
}

const val DAY_IN_A_MONTH_INPUT_TYPE = "Date Picker"
const val DATE_INPUT_TYPE = "Complete Date Picker"
const val NUMERIC_INPUT_TYPE = "Numeric Text Box"
