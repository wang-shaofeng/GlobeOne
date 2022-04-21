/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment

import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor() : BaseViewModel() {
    override val logTag = "PaymentViewModel"
}
