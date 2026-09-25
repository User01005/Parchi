package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

/**
 * Continuous Voice Input Manager for Kirana Store POS.
 * Explicitly prioritizes Hindi language input (hi-IN) with fallback support for Hinglish/English (en-IN).
 * Keeps listening and streaming transcribed items INDEFINITELY until the user manually triggers a stop,
 * seamlessly auto-restarting through pauses, silence timeouts, and transient errors.
 */
class VoiceInputManager(
    private val context: Context,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onRmsChanged: (Float) -> Unit = {}
) {
    private var speechRecognizer: SpeechRecognizer? = null
    var isListening: Boolean = false
        private set

    @Volatile
    private var shouldKeepListening: Boolean = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var restartRunnable: Runnable? = null
    private var watchdogRunnable: Runnable? = null
    private var lastEventTimeMs: Long = 0L
    private var lastPartialTranscript: String = ""

    companion object {
        private const val TAG = "VoiceInputManager"
        private const val WATCHDOG_CHECK_INTERVAL_MS = 5000L
        private const val WATCHDOG_MAX_SILENCE_MS = 12000L
        private const val PRIMARY_LANG_HINDI = "hi-IN"
        private const val SECONDARY_LANG_ENGLISH = "en-IN"
    }

    init {
        initializeRecognizer()
    }

    private fun initializeRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w(TAG, "SpeechRecognizer not available on device")
            return
        }

        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createRecognitionListener())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing recognizer", e)
        }
    }

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            isListening = true
            lastEventTimeMs = System.currentTimeMillis()
        }

        override fun onBeginningOfSpeech() {
            isListening = true
            lastEventTimeMs = System.currentTimeMillis()
        }

        override fun onRmsChanged(rmsdB: Float) {
            lastEventTimeMs = System.currentTimeMillis()
            onRmsChanged.invoke(rmsdB)
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            lastEventTimeMs = System.currentTimeMillis()
        }

        override fun onEndOfSpeech() {
            lastEventTimeMs = System.currentTimeMillis()
            // In continuous listening, onEndOfSpeech occurs when shopkeeper pauses between items.
            // onResults or onError will follow immediately, which will re-arm the recognizer.
        }

        override fun onError(error: Int) {
            lastEventTimeMs = System.currentTimeMillis()
            Log.d(TAG, "onError: code=$error, shouldKeepListening=$shouldKeepListening")

            // If stopped or stopping, do NOT surface any error to user!
            if (!shouldKeepListening) {
                isListening = false
                Log.d(TAG, "Recognition stopped gracefully (error code $error suppressed)")
                return
            }

            // Only report if microphone permission was revoked
            if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                shouldKeepListening = false
                isListening = false
                onError.invoke("Microphone permission required")
                return
            }

            // Flush any partial speech captured before timeout so spoken grocery items are never lost
            if (lastPartialTranscript.isNotBlank()) {
                val pending = lastPartialTranscript.trim()
                lastPartialTranscript = ""
                onFinalResult.invoke(pending)
            }

            // Indefinite listening recovery:
            // When user pauses between items or network glitches, silently restart listening
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    scheduleRestart(60L)
                }
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
                SpeechRecognizer.ERROR_CLIENT -> {
                    // Reinitialize to clear binder state and restart
                    reinitializeAndRestart(200L)
                }
                SpeechRecognizer.ERROR_AUDIO,
                SpeechRecognizer.ERROR_NETWORK,
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                SpeechRecognizer.ERROR_SERVER,
                11 -> { // SpeechRecognizer.ERROR_SERVER_DISCONNECTED
                    // Silent retry without notifying or alarming the user
                    scheduleRestart(350L)
                }
                else -> {
                    scheduleRestart(150L)
                }
            }
        }

        override fun onResults(results: Bundle?) {
            lastEventTimeMs = System.currentTimeMillis()
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val spoken = matches[0].trim()
                if (spoken.isNotBlank()) {
                    lastPartialTranscript = ""
                    onFinalResult.invoke(spoken)
                }
            } else if (lastPartialTranscript.isNotBlank()) {
                val pending = lastPartialTranscript.trim()
                lastPartialTranscript = ""
                onFinalResult.invoke(pending)
            }

            // Keep listening indefinitely until manual stop
            if (shouldKeepListening) {
                scheduleRestart(50L)
            } else {
                isListening = false
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            lastEventTimeMs = System.currentTimeMillis()
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val text = matches[0]
                lastPartialTranscript = text
                onPartialResult.invoke(text)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            lastEventTimeMs = System.currentTimeMillis()
        }
    }

    private fun getErrorMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
            SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please speak clearly."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
            else -> "Recognition error ($error)"
        }
    }

    private fun createRecognizerIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // 1. Language Model: Free-form dictation
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

            // 2. Bilingual configuration: prioritize Hindi (India) with seamless English (India)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-IN", "hi-Latn", "en-US"))

            // 3. Attribution & continuous dictation configuration
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra("android.speech.extra.DICTATION_MODE", true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)

            // 4. Cloud models
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
            putExtra("android.speech.extra.PREFER_OFFLINE", false)

            // 5. Extended silence & minimum duration limits
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 300000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 60000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 60000L)
            putExtra("android.speech.extras.SPEECH_INPUT_MINIMUM_LENGTH_MILLIS", 300000L)
            putExtra("android.speech.extras.SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS", 60000L)
            putExtra("android.speech.extras.SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS", 60000L)
        }
    }

    private fun scheduleRestart(delayMs: Long) {
        if (!shouldKeepListening) return
        restartRunnable?.let { mainHandler.removeCallbacks(it) }
        restartRunnable = Runnable {
            if (shouldKeepListening) {
                try {
                    speechRecognizer?.cancel()
                    speechRecognizer?.startListening(createRecognizerIntent())
                    isListening = true
                    lastEventTimeMs = System.currentTimeMillis()
                } catch (e: Exception) {
                    Log.w(TAG, "Restart failed, re-initializing", e)
                    reinitializeAndRestart(100L)
                }
            }
        }
        mainHandler.postDelayed(restartRunnable!!, delayMs)
    }

    private fun reinitializeAndRestart(delayMs: Long) {
        if (!shouldKeepListening) return
        restartRunnable?.let { mainHandler.removeCallbacks(it) }
        restartRunnable = Runnable {
            if (shouldKeepListening) {
                initializeRecognizer()
                try {
                    speechRecognizer?.startListening(createRecognizerIntent())
                    isListening = true
                    lastEventTimeMs = System.currentTimeMillis()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start after reinit", e)
                    scheduleRestart(400L)
                }
            }
        }
        mainHandler.postDelayed(restartRunnable!!, delayMs)
    }

    /**
     * Watchdog timer: checks every 5 seconds. If shouldKeepListening is true but recognizer
     * has been completely inactive or frozen for over 12 seconds, kicks it back alive.
     */
    private fun startWatchdog() {
        stopWatchdog()
        watchdogRunnable = object : Runnable {
            override fun run() {
                if (!shouldKeepListening) return

                val now = System.currentTimeMillis()
                if (now - lastEventTimeMs > WATCHDOG_MAX_SILENCE_MS) {
                    Log.d(TAG, "Watchdog triggered after ${now - lastEventTimeMs}ms silence; re-arming recognizer")
                    lastEventTimeMs = now
                    reinitializeAndRestart(80L)
                }

                if (shouldKeepListening) {
                    mainHandler.postDelayed(this, WATCHDOG_CHECK_INTERVAL_MS)
                }
            }
        }
        mainHandler.postDelayed(watchdogRunnable!!, WATCHDOG_CHECK_INTERVAL_MS)
    }

    private fun stopWatchdog() {
        watchdogRunnable?.let { mainHandler.removeCallbacks(it) }
        watchdogRunnable = null
    }

    fun startListening() {
        shouldKeepListening = true
        lastPartialTranscript = ""
        lastEventTimeMs = System.currentTimeMillis()

        restartRunnable?.let { mainHandler.removeCallbacks(it) }
        stopWatchdog()

        if (speechRecognizer == null) {
            initializeRecognizer()
        }

        if (speechRecognizer == null) {
            shouldKeepListening = false
            onError.invoke("Speech recognition is not available on this device.")
            return
        }

        try {
            speechRecognizer?.cancel()
            speechRecognizer?.startListening(createRecognizerIntent())
            isListening = true
        } catch (e: Exception) {
            Log.w(TAG, "Initial startListening failed, reinitializing", e)
            initializeRecognizer()
            try {
                speechRecognizer?.startListening(createRecognizerIntent())
                isListening = true
            } catch (ex: Exception) {
                Log.e(TAG, "Could not start microphone", ex)
                isListening = false
                shouldKeepListening = false
                onError.invoke("Could not start microphone: ${ex.localizedMessage}")
                return
            }
        }

        startWatchdog()
    }

    /**
     * Explicit manual stop triggered only when user taps the stop button.
     */
    fun stopListening() {
        shouldKeepListening = false
        stopWatchdog()
        restartRunnable?.let { mainHandler.removeCallbacks(it) }
        restartRunnable = null

        // Flush any remaining partial transcript
        if (lastPartialTranscript.isNotBlank()) {
            val pending = lastPartialTranscript.trim()
            lastPartialTranscript = ""
            onFinalResult.invoke(pending)
        }

        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        isListening = false
    }

    fun destroy() {
        shouldKeepListening = false
        stopWatchdog()
        restartRunnable?.let { mainHandler.removeCallbacks(it) }
        restartRunnable = null
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        isListening = false
    }
}
