package in.vikas.hisabkitab

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import java.security.MessageDigest
import java.util.Date
import java.util.Locale

class PaymentSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val body = messages.joinToString(" ") { it.messageBody.orEmpty() }
        val sender = messages.firstOrNull()?.originatingAddress.orEmpty()
        val parsed = parsePayment(body) ?: return
        val fingerprint = sha256("${sender.lowercase(Locale.ROOT)}|${body.lowercase(Locale.ROOT)}")
        val payment = Payment(
            id = fingerprint,
            label = if (parsed.first == "received") "UPI Money Received" else "Money Transfer Success",
            amount = parsed.second,
            date = messages.firstOrNull()?.timestampMillis ?: System.currentTimeMillis(),
            details = "SMS alert • ${Date(messages.firstOrNull()?.timestampMillis ?: System.currentTimeMillis())}",
            direction = parsed.first
        )
        if (PaymentStore.add(context, payment)) {
            PaymentNotifier.show(context, payment)
            VoiceAnnouncer.speak(context, payment.direction)
        }
    }

    private fun parsePayment(raw: String): Pair<String, Double>? {
        val text = raw.replace(",", "").lowercase(Locale.ROOT)
        // Require both a money amount and a clear credit/debit or UPI transfer signal.
        val amountMatch = Regex("(?:inr|rs\\.?|₹)\\s*([0-9]+(?:\\.[0-9]{1,2})?)|([0-9]+(?:\\.[0-9]{1,2})?)\\s*(?:inr|rs\\.?|₹)").find(text)
        val amount = amountMatch?.groupValues?.drop(1)?.firstOrNull { it.isNotBlank() }?.toDoubleOrNull() ?: return null
        val failure = listOf("failed", "failure", "declined", "unsuccessful", "reversed", "not successful").any { it in text }
        if (failure) return null
        val upiSignal = listOf("upi", "vpa", "imps", "credited", "credit", "deposited", "received", "debited", "debit", "sent", "paid", "transfer").any { it in text }
        if (!upiSignal) return null
        val received = listOf("credited", "credit", "deposited", "received", "added to your account", "money received").any { it in text }
        val sent = listOf("debited", "debit", "sent", "paid to", "transferred", "transfer to", "money sent", "withdrawn").any { it in text }
        if (received == sent) return null // ambiguous or no clear direction; don't guess
        return (if (received) "received" else "sent") to amount
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}
