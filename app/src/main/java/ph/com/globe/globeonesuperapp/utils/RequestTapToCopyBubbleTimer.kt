/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils

import android.os.CountDownTimer
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * [CountDownTimer] implementation for hiding bubble hint in group.
 */
class RequestTapToCopyBubbleTimer @Inject constructor() {
    private var timer: CountDownTimer? = null

    private fun createNewTimer(receiver: RequestTapToCopyBubbleReceiver) =
        object : CountDownTimer(COUNTDOWN_PERIOD, COUNTDOWN_UPDATE_INTERVAL) {

            override fun onFinish() {
                receiver.countDownTapToCopyFinished()
            }

            override fun onTick(timeRemaining: Long) = Unit
        }

    fun startCountDown(receiver: RequestTapToCopyBubbleReceiver) {
        timer?.cancel()
        timer = createNewTimer(receiver).also {
            it.start()
        }
    }

    fun stopCountDown() = timer?.cancel().also { timer = null }

    companion object {
        val COUNTDOWN_PERIOD: Long = TimeUnit.SECONDS.toMillis(3)
        val COUNTDOWN_UPDATE_INTERVAL: Long = TimeUnit.SECONDS.toMillis(1)
    }
}

interface RequestTapToCopyBubbleReceiver {

    fun countDownTapToCopyFinished()
}
