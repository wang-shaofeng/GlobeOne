/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.date

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import ph.com.globe.globeonesuperapp.R
import java.util.*

fun pickDate(context: Context, initial: Date? = null, action: (Date?) -> Unit) {
    val calendar = Calendar.getInstance()
    calendar.time = initial ?: Date()

    val datePickerDialog =
        DatePickerDialog(
            context,
            R.style.AppTheme_Calendar,
            { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(year, month, dayOfMonth)

                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                action.invoke(Date(calendar.timeInMillis))
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )

    datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
    datePickerDialog.show()
}
