package in.vikas.hisabkitab

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object PaymentStore {
    private const val PREFS = "hisab_store"
    private const val ITEMS = "payments"
    private const val SEEN = "seen_ids"

    @Synchronized fun add(context: Context, payment: Payment): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val seen = prefs.getStringSet(SEEN, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (payment.id in seen) return false
        val array = try { JSONArray(prefs.getString(ITEMS, "[]")) } catch (_: Exception) { JSONArray() }
        val obj = JSONObject().apply {
            put("id", payment.id); put("label", payment.label); put("amount", payment.amount)
            put("date", payment.date); put("details", payment.details); put("direction", payment.direction)
        }
        array.put(0, obj)
        prefs.edit().putString(ITEMS, array.toString()).apply()
        seen.add(payment.id)
        // Keep only the most recent 500 SMS fingerprints.
        prefs.edit().putStringSet(SEEN, seen.toList().takeLast(500).toSet()).apply()
        return true
    }

    fun all(context: Context): List<Payment> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val array = try { JSONArray(prefs.getString(ITEMS, "[]")) } catch (_: Exception) { JSONArray() }
        return (0 until array.length()).mapNotNull { i ->
            try {
                val o = array.getJSONObject(i)
                Payment(o.getString("id"), o.getString("label"), o.getDouble("amount"), o.getLong("date"), o.optString("details"), o.getString("direction"))
            } catch (_: Exception) { null }
        }
    }
}
