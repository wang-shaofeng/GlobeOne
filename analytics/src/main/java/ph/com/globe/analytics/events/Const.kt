/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.events

// Ui Keys
const val EMAIL_KEY = "email"
const val PAGE_KEY = "page"
const val ACTION_KEY = "action"
const val TARGET_KEY = "target"

// Ui Actions
const val ACTION_CLICK = "click"
const val ACTION_DIGIT_INPUT = "Digit Input"
const val ACTION_SCROLL_LEFT = "Scroll left"

const val BACK_BUTTON = "back_button"
const val X = "x"

// Api error
const val API = "api"
const val ERROR = "error"


// Ui transition
const val SOURCE_PAGE = "source_page"
const val DESTINATION_PAGE = "destination_page"

const val LINK = "link"

// Other
const val NON_EXISTANT = "N/A"
const val TYPE = "type"

// Logged in status
const val LOGGED_IN = "logged_in"
const val NON_LOGGED_IN = "non_logged_in"

// Analytics bundle fields
const val EVENT_LABEL = "event_label"
const val USER_AGENT = "user_agent"
const val LOGGED_IN_STATUS = "loggedin_status"
const val SCREEN_NAME = "screenName"
const val PRODUCT_NAME = "product_name"
const val LOGIN_SIGNUP_METHOD = "login_signup_method"
const val SEARCH_KEYWORD = "search_keyword"
const val USER_ID = "user_id"
const val CXS_MESSAGE_ID = "cxsmessage_id"
const val MSISDN = "msisdn"
const val BRAND = "brand"

//Event Labels
const val PURCHASE_CAMPAIGN_PROMO = "PurchaseCampaignPromo"
const val VERIFY_OTP = "VerifyOTP"
const val ENROLL_ACCOUNT = "EnrollAccount"
const val MULTIPLE_PURCHASE_PROMO = "MultiplePurchasePromo"
const val LOAN_PROMO = "LoanPromo"
const val GET_TRANSACTION_HISTORY = "GetTransactionHistory"
const val MODIFY_ENROLLED_ACCOUNT = "ModifyEnrolledAccount"
const val GET_LOYALTY_REWARDS = "GetLoyaltyRewards"
const val GET_PAYMENT_METHOD = "GetPaymentMethod"
const val USER_LOGOUT = "UserLogout"

// Event label keyword
const val KEYWORD_LABEL = "Label"
const val KEYWORD_TYPE = "Type"

// Alternate boolean state
const val YES = "Yes"
const val NO = "No"

// Ui sections
const val LOGIN_SCREEN = "LoginScreen"
const val SIGN_UP_SCREEN = "SignupScreen"
const val MERGE_LOGIN_SCREEN = "MergeLoginScreen"
const val LINK_EMAIL_SCREEN = "LinkEmailScreen"
const val ADD_ACCOUNT_SCREEN = "AddAccountScreen"
const val PAYMENT_OPTIONS_SCREEN = "PaymentOptionsScreen"
const val LOAD_SCREEN = "LoadScreen"
const val HOME_SCREEN = "HomeScreen"
const val REWARDS_SCREEN = "RewardsScreen"
const val PROMOS_SCREEN = "PromosScreen"
const val BORROW_SCREEN = "BorrowScreen"
const val CONTENT_SCREEN = "ContentScreen"
const val CONVERSION_SCREEN = "ConversionScreen"
const val SUBSCRIBE_SCREEN = "SubscribeScreen"
const val ACTIVATION_SCREEN = "ActivationScreen"
const val SUBSCRIPTION_SCREEN = "SubscriptionScreen"
const val SETUP_SCREEN = "SetupScreen"
const val PAYMENT_SCREEN = "PaymentScreen"
const val OTP_SCREEN = "OTPScreen"
const val VERIFICATION_SCREEN = "VerificationScreen"
const val MY_PROFILE_SCREEN = "MyProfileScreen"
const val LOAN_CATALOG_SCREEN = "LoanCatalogScreen"
const val PRODUCTS_SCREEN = "ProductsScreen"
const val SUBSCRIPTION_SUCCESS_SCREEN = "SubscriptionSuccessScreen"
const val GROUP_DATA_SCREEN = "GroupDataScreen"
const val ACCOUNT_DETAILS_SCREEN = "AccountDetailsScreen"
const val PERMISSIONS_SCREEN = "PermissionsScreen"
const val ACCOUNT_MIGRATION_SCREEN = "AccountMigrationScreen"
const val MODEM_PRODUCTS_SCREEN = "ModemProductsScreen"
const val REFUND_SCREEN = "RefundScreen"
const val PAYMENT_SUCCESS_SCREEN = "PaymentSuccessScreen"
const val CONFIRMATION_SCREEN = "ConfirmationScreen"
const val REDEEM_REWARDS_SCREEN = "RedeemRewardsScreen"

// Ui elements
const val BUTTON = "Button"
const val CLICKABLE_TEXT = "ClickableText"
const val CLICKABLE_ICON = "ClickableIcon"
const val CLICKABLE_BANNER = "ClickableBanner"

// Click text
const val I_HAVE_AN_EXISTING_ACCOUNT = "IHaveAnExistingAccount"
const val FORGOT_PASSWORD = "ForgotPassword"
const val ADD_ACCOUNT = "AddAccount"
const val GCASH = "GCash"
const val CREDIT_CARD = "CreditCard"
const val SUBSCRIBE = "Subscribe"
const val ADD = "Add"
const val LOAD = "Load"
const val REWARDS = "Rewards"
const val LOAN = "Loan"
const val SHOP = "Shop"
const val SIGN_UP = "SignUp"
const val MAYBE_LATER = "MaybeLater"
const val SIGN_UP_TO_REDEEM = "SignUpToRedeem"
const val REDEEM = "Redeem"
const val VIEW_ALL_PROMOS = "ViewAllPromos"
const val MOST_POPULAR = "MostPopular"
const val LOWEST_TO_HIGHEST = "LowestToHighest"
const val HIGHEST_TO_LOWEST = "HighestToLowest"
const val BORROW = "Borrow"
const val CHARGE_TO_LOAD = "ChargeToLoad"
const val PROCEED_TO_ACTIVATION = "ProceedToActivation"
const val UNSUBSCRIBE = "Unsubscribe"
const val TRY_AGAIN = "TryAgain"
const val CANCEL_TRANSACTION = "CancelTransaction"
const val CLAIM_MY_GIFT = "ClaimMyGift"
const val SETUP_GROUP_DATA = "SetupGroupData"
const val ENTER_CODE_MANUALLY = "EnterCodeManually"
const val PAY = "Pay"
const val NEXT = "Next"
const val RESEND = "Resend"
const val CANCEL = "Cancel"
const val SIGN_IN_TO_GLOBE_ONE = "SignInToGlobeOne"
const val I_WILL_DO_IT_LATER = "I'llDoItLater"
const val PROCEED = "Proceed"
const val SAVE_CHANGES = "SaveChanges"
const val BACK = "Back"
const val PAYMENT_METHODS = "PaymentMethods"
const val VIEW_ALL_REWARDS = "ViewAllRewards"
const val CONVERT_DATA = "ConvertData"
const val DONE = "Done"
const val REMOVE = "Remove"
const val SAVE = "Save"
const val LEAVE_GROUP = "LeaveGroup"
const val ALLOW_SERVICES = "AllowServices"
const val DENY_SERVICES = "DenyServices"
const val FIND_MY_HPW_NUMBER = "FindMyHPWNumber"
const val FIND_MY_USERNAME_AND_PASSWORD = "FindMyUsernameAndPassword"
const val ACTIVATE_LATER = "ActivateLater"
const val OKAY = "Okay"
const val PROFILE = "Profile"
const val CAMERA_SCANNING = "CameraScanning"
const val GO_BACK = "GoBack"
const val ENTER_MERCHANT_CODE = "EnterMerchantCode"

// Acquisition events
const val ACQUISITION_LOGIN = "acquisition_login"
const val ACQUISITION_SIGNUP = "acquisition_signup"

const val NO_EMAIL_STORED = "NO_EMAIL"
