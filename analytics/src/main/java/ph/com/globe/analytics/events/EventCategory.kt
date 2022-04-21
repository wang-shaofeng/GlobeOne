/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.events

sealed class EventCategory {
    data class Acquisition(val type: String) : EventCategory()
    object Conversion : EventCategory()
    object Core : EventCategory()
    object Engagement : EventCategory()
}
