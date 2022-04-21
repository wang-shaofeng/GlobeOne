/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler

fun CoroutineScope.launchWithLoadingOverlay(
    handler: GeneralEventsHandler,
    lambda: suspend () -> Unit
) =
    launch {
        withContext(Dispatchers.Main + NonCancellable) {
            handler.startLoading()
            lambda.invoke()
            delay(150)
            handler.endLoading()
        }
    }

// TODO revisit this approach
// This is far from perfect and should be changed/improved later
fun <T> Flow<T>.withLoadingOverlay(handler: GeneralEventsHandler): Flow<T> {
    var loadingNum = 0
    return onStart {
        withContext(Dispatchers.Main + NonCancellable) {
            loadingNum++
            handler.startLoading()
        }
        // We must have this delay, because onStart and onEach happen very fast,
        // so overlay dialog doesn't succeed to be displayed while this coroutine calls dismiss.
        delay(150)
    }.onEach {
        withContext(Dispatchers.Main + NonCancellable) {
            loadingNum--
            if (loadingNum == 0) {
                handler.endLoading()
            }
        }
    }
}
