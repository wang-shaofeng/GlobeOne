package ph.com.globe.globeonesuperapp.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface EventHandlerWithResult<out T, R> {

    fun handleEvent(action: (T) -> R?): R?
}

data class EventWithResult<T, R>(
    var event: T?,
    private val onResult: EventWithResult<T, R>.(R?) -> Unit
) : EventHandlerWithResult<T, R> {

    override fun handleEvent(action: (T) -> R?): R? {
        return event?.let(action).also { onResult(this@EventWithResult, it) }
    }

    fun clearEvent() {
        event = null
    }
}

fun <T, R> MutableLiveData<EventWithResult<T, R>>.postEventWithResult(
    event: T,
    onResult: EventWithResult<T, R>.(R?) -> Unit
) {
    this.postValue(EventWithResult(event, onResult))
}

fun <T, R> LiveData<EventWithResult<T, R>>.eventWithResultObserve(
    owner: LifecycleOwner,
    callback: (T) -> R
) {
    this.observe(owner) {
        it.handleEvent { callback(it) }
    }
}
