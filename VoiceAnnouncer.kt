package in.vikas.hisabkitab

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import java.util.Locale

object VoiceAnnouncer {
    fun speak(context: Context, direction: String) {
        val prefs = context.getSharedPreferences("hisab_store", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("voice_enabled", true)) return
        val text = if (direction == "received") "पैसा मिल गया" else "Money transfer success"
        lateinit var tts: TextToSpeech
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("hi", "IN")
                // Prefer a voice whose engine metadata says male, if available on this phone.
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    val maleVoice = tts.voices?.firstOrNull { it.locale.language == "hi" && it.name.contains("male", ignoreCase = true) }
                    if (maleVoice != null) tts.voice = maleVoice
                }
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "payment-${System.currentTimeMillis()}")
                Handler(Looper.getMainLooper()).postDelayed({ tts.shutdown() }, 7000)
            } else {
                tts.shutdown()
            }
        }
    }
}
