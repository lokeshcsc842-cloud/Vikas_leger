package in.vikas.hisabkitab

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var content: LinearLayout
    private val requestCode = 412

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 24, 24, 16); setBackgroundColor(0xFFF5F7F5.toInt()) }
        val title = TextView(this).apply { text = "Vikas ka Hisab Kitab"; textSize = 24f; setTextColor(0xFF124E35.toInt()); gravity = Gravity.CENTER; setPadding(0, 12, 0, 18) }
        root.addView(title, match())
        val subtitle = TextView(this).apply { text = "UPI payment tracker"; textSize = 15f; gravity = Gravity.CENTER; setPadding(0, 0, 0, 18) }
        root.addView(subtitle, match())
        val switch = Switch(this).apply {
            text = "Payment voice alerts"
            textSize = 16f
            isChecked = getSharedPreferences("hisab_store", MODE_PRIVATE).getBoolean("voice_enabled", true)
            setOnCheckedChangeListener { _, checked -> getSharedPreferences("hisab_store", MODE_PRIVATE).edit().putBoolean("voice_enabled", checked).apply() }
        }
        root.addView(switch, match())
        val permissionButton = Button(this).apply {
            text = "Enable SMS permission"
            setOnClickListener { requestNeededPermissions() }
        }
        root.addView(permissionButton, match())
        val refresh = Button(this).apply { text = "Refresh payment list"; setOnClickListener { renderPayments() } }
        root.addView(refresh, match())
        val scroll = ScrollView(this)
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 14, 0, 0) }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
        requestNeededPermissions()
        renderPayments()
    }

    private fun requestNeededPermissions() {
        val needed = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) needed.add(Manifest.permission.RECEIVE_SMS)
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) needed.add(Manifest.permission.READ_SMS)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) needed.add(Manifest.permission.POST_NOTIFICATIONS)
        if (needed.isNotEmpty()) requestPermissions(needed.toTypedArray(), requestCode)
        else Toast.makeText(this, "SMS permission is enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode) {
            val smsGranted = checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
            Toast.makeText(this, if (smsGranted) "Payment SMS detection enabled" else "SMS permission is required for automatic detection", Toast.LENGTH_LONG).show()
        }
    }

    private fun renderPayments() {
        if (!::content.isInitialized) return
        content.removeAllViews()
        val payments = PaymentStore.all(this)
        val received = payments.filter { it.direction == "received" }.sumOf { it.amount }
        val sent = payments.filter { it.direction == "sent" }.sumOf { it.amount }
        val summary = TextView(this).apply {
            text = "UPI Money Received: ₹${money(received)}\nMoney Transfer Success: ₹${money(sent)}\n\nRecent payments"
            textSize = 17f; setTextColor(0xFF202820.toInt()); setPadding(14, 16, 14, 20)
        }
        content.addView(summary, match())
        if (payments.isEmpty()) {
            content.addView(TextView(this).apply { text = "Abhi koi payment SMS detect nahi hua. SMS permission allow karein.\n\nNote: Bank SMS formats alag hote hain; kuch messages manual verification maang sakte hain."; textSize = 15f; setPadding(14, 18, 14, 18) }, match())
        } else {
            payments.take(100).forEach { payment ->
                val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(payment.date))
                val row = TextView(this).apply {
                    text = "${payment.label}\n₹${money(payment.amount)}   •   $date\n${payment.details}"
                    textSize = 15f; setTextColor(0xFF1C2A21.toInt()); setPadding(14, 14, 14, 14)
                    setBackgroundColor(0xFFFFFFFF.toInt())
                }
                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.bottomMargin = 10
                content.addView(row, params)
            }
        }
    }

    private fun money(value: Double) = String.format(Locale.US, "%.2f", value)
    private fun match() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
}
