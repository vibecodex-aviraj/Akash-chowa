package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechToTextManager(private val context: Context) {

    private val tag = "SpeechToText"
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(onResult: (String) -> Unit) {
        stopListening()
        _error.value = null
        _spokenText.value = ""

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _error.value = null
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(errorCode: Int) {
                        _isListening.value = false
                        val errorMsg = when (errorCode) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "কোনো কথা শোনা যায়নি, স্পষ্ট করে পুনরায় বলুন।"
                            SpeechRecognizer.ERROR_NETWORK -> "নেটওয়ার্ক সংযোগ পরীক্ষা করুন।"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "সময় অতিক্রান্ত হয়েছে, পুনরায় চেষ্টা করুন।"
                            SpeechRecognizer.ERROR_AUDIO -> "মাইক্রোফোন অডিও রেকর্ডিংয়ে সমস্যা হয়েছে।"
                            SpeechRecognizer.ERROR_CLIENT -> "ভয়েস সেবা চালু করতে সমস্যা হয়েছে।"
                            else -> "ভয়েস শনাক্তকরণে সমস্যা ($errorCode)"
                        }
                        Log.w(tag, "Speech error: $errorCode -> $errorMsg")
                        _error.value = errorMsg
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val transcript = matches[0]
                            _spokenText.value = transcript
                            onResult(transcript)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _spokenText.value = matches[0]
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-IN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-IN")
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("bn-BD", "en-IN"))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "বাংলায় আপনার প্রশ্ন বলুন...")
            }

            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            _isListening.value = false
            _error.value = e.localizedMessage
            Log.e(tag, "Failed to start speech recognition", e)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w(tag, "Error destroying recognizer", e)
        } finally {
            speechRecognizer = null
            _isListening.value = false
        }
    }
}
