package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechManager(context: Context) {

    private val tag = "TTSManager"
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                val bengaliLocale = Locale("bn", "IN")
                val langResult = tts?.setLanguage(bengaliLocale)
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(tag, "Bengali TTS not fully installed on device; falling back to default locale")
                    tts?.language = Locale.getDefault()
                }
                tts?.setSpeechRate(0.95f)

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            } else {
                Log.e(tag, "TextToSpeech initialization failed: $status")
            }
        }
    }

    fun speak(text: String) {
        if (!isInitialized) return
        stop()
        // Strip markdown asterisks and hash tags for smoother speech synthesis
        val cleanText = text.replace(Regex("[*#_`~>]"), "")
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "utterance_akash_${System.currentTimeMillis()}")
    }

    fun stop() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
        _isSpeaking.value = false
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
