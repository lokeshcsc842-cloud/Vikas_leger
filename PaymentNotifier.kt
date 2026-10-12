package in.vikas.hisabkitab

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object PaymentNotifier {
    private const val CHANNEL = "payment_alerts"
    fun show(context: Context, payment: Payment) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel(CHANNEL, "UPI payment alerts", NotificationManager.IMPORTANCE_DEFAULT))
        val title = if (payment.direction == "received") "UPI Money Received" else "Money Transfer Success"
        val amount = "₹" + String.format(java.util.Locale.US, "%.2f", payment.amount)
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, CHANNEL) else Notification.Builder(context)
        val notification = builder.setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title).setContentText("$amount • payment SMS detected").setAutoCancel(true).build()
        manager.notify(payment.id.hashCode(), notification)
    }
}
