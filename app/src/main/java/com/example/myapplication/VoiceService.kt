package com.example.myapplication

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import java.util.*

class VoiceService : Service() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var recognizerIntent: Intent
    private val handler = Handler(Looper.getMainLooper())
    private var isListening = false

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())

        initSpeechRecognizer()
        startListening()
    }

    private fun initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)  // Continuous detection
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 500)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("VoiceService", "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("VoiceService", "Speech started")
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d("VoiceService", "End of speech")
                restartListening()  // Restart immediately after end of speech
            }

            override fun onError(error: Int) {
                Log.e("VoiceService", "Speech recognition error: $error")
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> restartListening(delay = 500)
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> restartListening(delay = 1000)
                    else -> restartListening(delay = 2000)
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.forEach { result ->
                    Log.d("VoiceService", "Recognized: $result")
                    if (result.equals("help me", ignoreCase = true)) {
                        triggerEmergency()
                    }
                }
                restartListening()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.forEach { partial ->
                    Log.d("VoiceService", "Partial result: $partial")
                    if (partial.equals("help me", ignoreCase = true)) {
                        triggerEmergency()
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        if (!isListening) {
            isListening = true
            handler.postDelayed({
                speechRecognizer.startListening(recognizerIntent)
            }, 500)
        }
    }

    private fun restartListening(delay: Long = 500) {
        handler.postDelayed({
            isListening = false
            speechRecognizer.stopListening()
            speechRecognizer.destroy()
            initSpeechRecognizer()
            startListening()
        }, delay)
    }

    private fun triggerEmergency() {
        makeEmergencyCall("+911234567890") // Replace with actual number
        sendWhatsAppMessage("+911234567890")
    }

    private fun makeEmergencyCall(number: String) {
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent)
        } else {
            Toast.makeText(this, "Call permission required!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendWhatsAppMessage(number: String) {
        val url = "https://api.whatsapp.com/send?phone=$number&text=Emergency! Please help!"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun createNotification(): Notification {
        val channelId = "VoiceServiceChannel"
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Voice Service", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Voice Detection Active")
            .setContentText("Listening for 'Help Me'...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }

    override fun onBind(intent: Intent?) = null
}
