package ph.com.globe.globeonesuperapp.utils

import android.content.SharedPreferences
import org.json.JSONObject
import ph.com.globe.globeonesuperapp.utils.shared_preferences.DASHBOARD_BUBBLES_KEY
import java.util.*

/**
 * Helper class for bubbles displaying on Dashboard.
 *
 * Bubble for some account can appear when user opens the app first time for the day.
 * To follow this logic we should store in SharedPreferences information about bubbles
 * in this way: Map<String, Int> which means primary msisdn to day of month.
 * Since we're not able to store Map, used JSONObject to store String type.
 * */
class DashboardBubblesHelper(private val sharedPreferences: SharedPreferences) {

    private val shownBubbles = mutableMapOf<String, Int>()

    init {
        sharedPreferences.getString(DASHBOARD_BUBBLES_KEY, null)?.let { json ->
            val jsonObject = JSONObject(json)
            val keysIterator = jsonObject.keys()
            while (keysIterator.hasNext()) {
                val key = keysIterator.next()
                val value = jsonObject.getInt(key)

                shownBubbles[key] = value
            }
        }
    }

    fun isBubbleAvailableToShow(primaryMsisdn: String): Boolean {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        return if (shownBubbles.containsKey(primaryMsisdn)) {
            if (currentDay == shownBubbles[primaryMsisdn]) {
                false
            } else {
                shownBubbles[primaryMsisdn] = currentDay
                storeInfo()
                true
            }
        } else {
            shownBubbles[primaryMsisdn] = currentDay
            storeInfo()
            true
        }
    }

    private fun storeInfo() {
        val map = shownBubbles as Map<String, Int>
        val json = JSONObject(map).toString()
        sharedPreferences.edit().putString(DASHBOARD_BUBBLES_KEY, json).apply()
    }
}
