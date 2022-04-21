/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.inputcontroller

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import ph.com.globe.globeonesuperapp.utils.inputcontroller.Preconditions.checkArgument
import java.util.ArrayList
import java.util.Collections

/**
 * Convenience controller that handles chained input navigation of [EditText]s.
 * It will navigates between [EditText]s in supplied [List] order
 * and handle input to appropriate place.
 * It will always refocus to currently active [EditText].
 * Currently it works for one character [EditText]s. <br></br>
 *
 *
 * !!! [EditText]s using this controller must have max length set to 2 !!!
 */
class EditTextChainedInputController(editTexts: List<EditText>?, shouldRefocusOnLastItem: Boolean) {
    private val editTexts: List<EditText>
    private var chainedTextListeners: MutableList<ChainedTextListener>? = null
    private var currentIndex = 0
    private val shouldRefocusOnLastItemInChain: Boolean

    @SuppressLint("ClickableViewAccessibility")
    constructor(editTexts: List<EditText>?) : this(editTexts, true)

    /**
     * Adds [ChainedTextListener] to be notified of changes to [EditText]s in chain.
     */
    fun addOnChangedTextListener(listener: ChainedTextListener?) {
        if (chainedTextListeners == null) {
            chainedTextListeners = ArrayList<ChainedTextListener>()
        }
        chainedTextListeners!!.add(checkNotNull(listener))
    }

    /**
     * Removes [ChainedTextListener] if was previusly added by
     * [EditTextChainedInputController.addOnChangedTextListener].
     *
     * @param listener to be removed.
     */
    fun removeOnChangedTextListener(listener: ChainedTextListener) {
        checkNotNull(listener)
        if (chainedTextListeners != null) {
            chainedTextListeners!!.remove(listener)
            if (chainedTextListeners!!.isEmpty()) {
                chainedTextListeners = null
            }
        }
    }

    /**
     * Convenience method that returns concatenated String of all [EditText]s text.
     */
    val chainedEditTextString: String
        get() {
            val sb = StringBuilder(editTexts.size)
            for (edit in editTexts) {
                val dig = edit.text
                if (dig != null && dig.length <= 1) {
                    sb.append(dig)
                } else {
                    throw IllegalStateException("Text can only be of length 1 $dig")
                }
            }
            return sb.toString()
        }

    private inner class RefocusingListener : View.OnFocusChangeListener {
        override fun onFocusChange(v: View, hasFocus: Boolean) {
            if (v !== current && hasFocus) {
                refocusOnCurrentEditText(false)
            }
        }
    }

    private inner class RefocusingTouchListener : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            refocusOnCurrentEditText(true)
            return true
        }
    }

    private fun recalculateCurrentIndex() {
        var cnt = -1
        for (digit in editTexts) {
            if (digit.text.toString().isNotEmpty()) cnt++
        }
        currentIndex = if (cnt > 0) cnt else 0
    }

    private val current: EditText
        get() = editTexts[currentIndex]

    /**
     * Method for figuring out whether the passed item is the last in the chain and whether it should prevent refocusing on it.
     * Functionality highly dependant on the [.shouldRefocusOnLastItemInChain] parameter.
     *
     *
     *
     *
     * When set, this always returns **false** and lets the [RefocusingListener] / [RefocusingTouchListener] do all the work
     *
     *
     * If not set, this returns **true** when the *editText* is the last one in the chain, and should prevent refocus
     *
     *
     *
     * @return whether the current EditText is the last one in the chain and should prevent refocusing on it
     */
    private fun shouldPreventRefocusIfLastInChain(editText: EditText): Boolean {
        return if (shouldRefocusOnLastItemInChain) {
            false
        } else {
            editTexts[editTexts.size - 1] === editText
        }
    }

    private inner class NavigatingTextWatcher(private val parent: EditText) :
        SimpleTextWatcher() {
        // isShadow prevents the re-triggering of text field listeners
        // when one character is carried over and a new text field value is set
        var isShadow = false
        var wasEmpty = false

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            wasEmpty = s.isEmpty()
        }

        override fun afterTextChanged(s: Editable) {
            checkArgument(s.length <= 2)
            if (current !== parent) {
                recalculateCurrentIndex()
            }
            if (s.isEmpty() && !wasEmpty) {
                notifyChainedTextListeners(current, ChainedTextListener.ChainedTextEvent.DELETE)
                if (isOnFirstEditText) {
                    notifyChainedTextListeners(
                        current,
                        ChainedTextListener.ChainedTextEvent.CHAIN_EMPTY
                    )
                }
                tryMoveToPreviousEditText()
            } else if (s.length == 1) {
                if (!isShadow) {
                    notifyChainedTextListeners(current, ChainedTextListener.ChainedTextEvent.APPEND)
                    if (isOnLastEditText) {
                        notifyChainedTextListeners(
                            current,
                            ChainedTextListener.ChainedTextEvent.CHAIN_FULL
                        )
                    }
                }
                isShadow = false
            } else if (s.length == 2) {
                val carry = s[1]
                isShadow = true
                s.delete(1, 2)
                tryMoveToNextEditText(carry)
            }
        }
    }

    private fun notifyChainedTextListeners(
        changedElement: EditText,
        eventType: ChainedTextListener.ChainedTextEvent
    ) {
        if (chainedTextListeners != null) {
            for (listener in chainedTextListeners!!) {
                listener.onChangedText(changedElement, eventType)
            }
        }
    }

    private fun tryMoveToPreviousEditText() {
        if (currentIndex > 0) {
            currentIndex--
            refocusOnCurrentEditText(false)
        }
    }

    private val isOnFirstEditText: Boolean
        get() = currentIndex == 0
    private val isOnLastEditText: Boolean
        get() = editTexts.size - 1 == currentIndex

    private fun tryMoveToNextEditText(carry: Char) {
        if (currentIndex < editTexts.size - 1) {
            val editText = editTexts[++currentIndex]
            editText.setText(carry.toString())
            editText.setSelection(1)
            refocusOnCurrentEditText(false)
        }
    }

    private fun refocusOnCurrentEditText(fromTouch: Boolean) {
        val editText = current
        // If automatic refocusing on the last item is prohibited, it should still honour touch events
        val allowFocus = !shouldPreventRefocusIfLastInChain(editText) || fromTouch
        if (allowFocus && editText.requestFocus()) {
            val imm =
                editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    class InvalidEditTextsException internal constructor(s: String?) : IllegalArgumentException(s) {
        companion object {
            val exception: InvalidEditTextsException
                get() = InvalidEditTextsException(
                    "EditTexts provided as arguments must have " +
                            "max length set to 2"
                )
        }
    }

    /**
     * Alternative constructor that parametrizes refocusing on the last item in the chain
     *
     * @param editTexts               list of EditTexts
     * @param shouldRefocusOnLastItem whether refocusing on the last item should be enforced
     */
    init {
        checkArgument(editTexts != null && editTexts.isNotEmpty())
        this.editTexts = Collections.unmodifiableList(editTexts)
        var hadFilter: Boolean
        val refocusingListener = RefocusingListener()
        val refocusingTouchListener = RefocusingTouchListener()
        for (digit in editTexts!!) {
            val filters = digit.filters
            if (filters.isEmpty()) {
                throw InvalidEditTextsException.exception
            }
            hadFilter = false
            for (filter in filters) {
                if (filter is InputFilter.LengthFilter) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (filter.max != 2) {
                            throw InvalidEditTextsException.exception
                        }
                    }
                    // if VERSION.SDK is lower we don't have a way to check
                    hadFilter = true
                }
            }
            if (!hadFilter) {
                throw InvalidEditTextsException.exception
            }
            digit.addTextChangedListener(NavigatingTextWatcher(digit))
            digit.setOnTouchListener(refocusingTouchListener)
            digit.onFocusChangeListener = refocusingListener
        }
        shouldRefocusOnLastItemInChain = shouldRefocusOnLastItem
    }
}