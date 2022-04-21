/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.inputcontroller

import android.widget.EditText

/**
 * Notifies users when [EditText] is changed with the detected change type.
 */
interface ChainedTextListener {

    enum class ChainedTextEvent {
        DELETE,
        APPEND,
        CHAIN_FULL,
        CHAIN_EMPTY
    }

    /**
     * Passing last [EditText] where change happened.
     * You shouldn't modify [EditText] text here. It's safe to read any info.
     *
     * @param editText        where change happened.
     * @param changeTypeEvent type of change.
     */
    fun onChangedText(editText: EditText, changeTypeEvent: ChainedTextEvent)
}
