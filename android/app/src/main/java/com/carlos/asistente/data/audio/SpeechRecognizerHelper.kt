package com.carlos.asistente.data.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SpeechRecognizerHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var onResult: ((String) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null
    private var onListening: ((Boolean) -> Unit)? = null

    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onListening: (Boolean) -> Unit
    ) {
        this.onResult = onResult
        this.onError = onError
        this.onListening = onListening

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("El reconocimiento de voz no está disponible en este dispositivo")
            return
        }

        // Recreate recognizer each session to avoid reuse issues
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(buildListener())
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // Use device default language, with Spanish as preference
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        onResult = null
        onError = null
        onListening = null
    }

    private fun buildListener() = object : RecognitionListener {

        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "Ready for speech")
            onListening?.invoke(true)
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "Speech started")
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Not used — could be wired to a visual meter in the future
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            Log.d(TAG, "Speech ended")
            onListening?.invoke(false)
        }

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
                SpeechRecognizer.ERROR_CLIENT -> "Error del cliente"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permisos insuficientes"
                SpeechRecognizer.ERROR_NETWORK -> "Error de red"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout de red"
                SpeechRecognizer.ERROR_NO_MATCH -> "No se entendió nada, intenta de nuevo"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El reconocedor está ocupado"
                SpeechRecognizer.ERROR_SERVER -> "Error del servidor"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz"
                else -> "Error desconocido ($error)"
            }
            Log.w(TAG, "Recognition error: $message (code=$error)")
            onListening?.invoke(false)
            onError?.invoke(message)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull()
            Log.d(TAG, "Final result: $text")
            if (!text.isNullOrBlank()) {
                onResult?.invoke(text)
            } else {
                onError?.invoke("No se detectó texto")
            }
            onListening?.invoke(false)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
            if (!partial.isNullOrBlank()) {
                Log.d(TAG, "Partial: $partial")
                // Deliver partial results so the UI can show live feedback in the text field
                onResult?.invoke(partial)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    companion object {
        private const val TAG = "SpeechRecognizerHelper"
    }
}
