/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData

class PostedEvent<out T>(
    private val delegate: EventHandler<T>
) : EventHandler<T> by delegate {

    override fun handleEvent(action: (T) -> Unit) {
        mainHandler.post {
            delegate.handleEvent(action)
        }
    }

    companion object {
        private val mainHandler = Handler(Looper.getMainLooper())
    }
}

class OneTimePostedEvent<out T>(
    eventContents: T
) : EventHandler<T> by PostedEvent(OneTimeEvent(eventContents))

typealias NavigationEvent<T> = OneTimePostedEvent<T>


fun <T> MutableLiveData<OneTimeEvent<T>>.postOneTimeEvent(event: T) {
    this.postValue(OneTimeEvent(event))
}

fun <T> MutableLiveData<OneTimeEvent<T>>.setOneTimeEvent(event: T) {
    this.value = OneTimeEvent(event)
}

fun <T> MutableLiveData<NavigationEvent<T>>.postNavigationEvent(event: T) {
    this.postValue(NavigationEvent(event))
}

fun <T> MutableLiveData<NavigationEvent<T>>.setNavigationEvent(event: T) {
    this.value = NavigationEvent(event)
}
