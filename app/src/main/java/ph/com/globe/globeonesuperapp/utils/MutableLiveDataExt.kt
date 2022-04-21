/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils

import androidx.lifecycle.MutableLiveData

/**
 * Forces [MutableLiveData] to emmit its value again
 */
fun <T> MutableLiveData<T>.reemitValue(){
    this.value = this.value
}
