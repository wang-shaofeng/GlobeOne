/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs

import androidx.annotation.StringRes
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog.Dialog.CustomDialog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverlayAndDialogFactories @Inject constructor() {

    fun createAddAccountMobileNumberSkipDialog(yesCallback: () -> Unit, noCallback: () -> Unit) =
        CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.skip_adding_account,
                R.string.add_account_later,
                rightButtonCallback = yesCallback,
                leftButtonCallback = noCallback,
                rightButtonText = R.string.yes,
                leftButtonText = R.string.no
            )
        }

    fun createAddAccountMobileNumberCancelDialog(yesCallback: () -> Unit, noCallback: () -> Unit) =
        CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.cancel_adding_account,
                R.string.add_later_if_change_mind,
                noCallback,
                yesCallback
            )
        }

    fun createDeleteAccountDialog(yesCallback: () -> Unit, noCallback: () -> Unit) =
        CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.delete_account_question,
                R.string.you_can_add_account_later,
                noCallback,
                yesCallback,
            )
        }

    fun createNoBrandErrorDialog(okCallback: () -> Unit) =
        CustomDialog {
            OneButtonDialogBuilder(this).createDialog(
                getString(R.string.unknown_error),
                getString(R.string.try_again_later),
                getString(R.string.button_ok),
                okCallback
            )
        }

    fun createLogoutDialog(yesCallback: () -> Unit) =
        CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.logout_title,
                R.string.logout_message,
                rightButtonCallback = yesCallback,
                rightButtonText = R.string.yes,
                leftButtonText = R.string.no,
                showCloseButton = true
            )
        }

    fun createRemoveCreditCardDialog(yesCallback: () -> Unit) =
        CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.are_you_sure_you_want_to_remove_this_credit_card,
                R.string.you_can_add_credit_cards_back_later_on,
                rightButtonCallback = yesCallback,
                rightButtonText = R.string.yes,
                leftButtonText = R.string.no,
                showCloseButton = true
            )
        }

    fun createRemoveAccountDialog(yesCallback: () -> Unit) =
        CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.account_removal_rationale_title,
                R.string.account_removal_rationale_description,
                rightButtonCallback = yesCallback,
                rightButtonText = R.string.yes,
                leftButtonText = R.string.no
            )
        }

    fun createRemoveInactiveAccountDialog(yesCallback: () -> Unit) =
        CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.account_removal_rationale_title,
                R.string.account_removal_inactive_description,
                rightButtonCallback = yesCallback,
                rightButtonText = R.string.yes,
                leftButtonText = R.string.no
            )
        }

    fun createRemoveGCashAccountDialog(yesCallback: () -> Unit) =
        CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.are_you_sure_you_want_to_remove_this_gcash_account,
                R.string.you_can_add_it_back_anytime,
                rightButtonCallback = yesCallback,
                rightButtonText = R.string.yes,
                leftButtonText = R.string.no
            )
        }

    fun createTokenExpiredDialog(loginCallback: () -> Unit) =
        CustomDialog {
            OneButtonDialogBuilder(this).createDialog(
                getString(R.string.token_expired_title),
                getString(R.string.token_expired_description),
                getString(R.string.log_in),
                loginCallback
            )
        }

    fun createRemoveGroupMemberDialog(yesCallback: () -> Unit, noCallback: () -> Unit) =
        CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.account_removal_rationale_title,
                R.string.account_removal_rationale_description,
                rightButtonCallback = yesCallback,
                leftButtonCallback = noCallback,
                rightButtonText = R.string.yes,
                leftButtonText = R.string.no
            )
        }

    fun createLeaveGroupDialog(yesCallback: () -> Unit, noCallback: () -> Unit) =
        CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.dialog_leave_group_title,
                R.string.dialog_leave_group_description,
                rightButtonCallback = yesCallback,
                leftButtonCallback = noCallback,
                rightButtonText = R.string.yes,
                leftButtonText = R.string.no
            )
        }

    fun createUnsubscribeContentPromoDialog(
        promoName: String,
        yesCallback: () -> Unit,
        noCallback: () -> Unit
    ) = CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.content_promo_unsubscribe_dialog_title,
                R.string.content_promo_unsubscribe_dialog_description,
                rightButtonCallback = yesCallback,
                leftButtonCallback = noCallback,
                rightButtonText = R.string.yes,
                leftButtonText = R.string.no,
                showCloseButton = true,
                titleArgs = arrayOf(promoName)
            )
        }

    fun createLeaveGlobeOneAppNonZeroRatedDialog(yesCallback: () -> Unit) =
        CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.you_are_about_to_leave_globeone,
                R.string.standard_data_charges_will_apply_for_mobile_data,
                rightButtonCallback = yesCallback,
                rightButtonText = R.string.okay,
                leftButtonText = R.string.cancel,
                showIconDrawable = false
            )
        }

    fun createVoucherCodeDialog(yesCallback: () -> Unit, noCallback: () -> Unit) =
        CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.we_sent_a_text_for_voucher_code_title,
                R.string.we_sent_a_text_for_voucher_code_description,
                rightButtonCallback = yesCallback,
                leftButtonCallback = noCallback,
                rightButtonText = R.string.okay,
                leftButtonText = R.string.back
            )
        }

    fun createVoucherActivationLinkDialog(yesCallback: () -> Unit, noCallback: () -> Unit) =
        CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.we_sent_a_text_for_activation_link_title,
                R.string.we_sent_a_text_for_activation_link_description,
                rightButtonCallback = yesCallback,
                leftButtonCallback = noCallback,
                rightButtonText = R.string.okay,
                leftButtonText = R.string.back
            )
        }

    fun createConfirmUncompleteKYCDialog(yesCallback: () -> Unit, noCallback: () -> Unit) =
        CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.profile_kyc_confirm_skip_completing_title,
                R.string.profile_kyc_confirm_skip_completing_description,
                rightButtonCallback = yesCallback,
                leftButtonCallback = noCallback,
                rightButtonText = R.string.profile_kyc_confirm_button,
                leftButtonText = R.string.back
            )
        }

    fun createRedeemUnsuccessfulDialog(@StringRes dialogDescription: Int) =
        CustomDialog {
            OneButtonDialogBuilder(this).createDialog(
                getString(R.string.you_cant_redeem_this_reward),
                getString(dialogDescription),
                getString(R.string.go_back),
                {}
            )
        }

    fun createGoCreateTryAgainNextTimeDialog(yesCallback: () -> Unit) =
        CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.go_create_try_again_next_time_title,
                R.string.go_create_try_again_next_time_description,
                rightButtonCallback = yesCallback,
                rightButtonText = R.string.yes,
                leftButtonText = R.string.no,
                showCloseButton = true
            )
        }

    fun createHideSpinwheelDialog(yesCallback: () -> Unit) =
        CustomDialog {
            TwoButtonDialogBuilder(this).createDialog(
                R.string.spinwheel_hide_dialog_title,
                R.string.spinwheel_hide_dialog_text,
                rightButtonCallback = yesCallback,
                rightButtonText = R.string.spinwheel_hide_dialog_positive_button_text,
                leftButtonText = R.string.spinwheel_hide_dialog_negative_button_text
            )
        }

    fun createMaxAttemptsReachedDialog(onBackCallback: () -> Unit) =
        CustomDialog {
            OneButtonDialogBuilder(this).createDialog(
                getString(R.string.try_another_verification_method_error),
                getString(R.string.try_another_verification_method_error_info),
                getString(R.string.go_back),
                onBackCallback
            )
        }

    fun createMaxAttemptsForSecurityQuestionsDialog() =
        CustomDialog {
            OneButtonDialogBuilder(this).createDialog(
                getString(R.string.try_another_method_security_questions_error),
                getString(R.string.try_another_method_security_questions_error_info),
                getString(R.string.go_back),
                {}
            )
        }

    fun createAccountDetailsFailedDialog(callback: () -> Unit) =
        CustomDialog {
            OneButtonDialogBuilder(this).createDialog(
                getString(R.string.were_sorry_about_that),
                getString(R.string.something_went_wrong),
                getString(R.string.go_back),
                callback
            )
        }
}
