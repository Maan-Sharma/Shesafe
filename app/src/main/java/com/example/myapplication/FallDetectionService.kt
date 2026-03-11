package com.example.myapplication

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import kotlin.math.abs
import kotlin.math.sqrt

class FallDetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var fallDetected = false
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var handler: Handler
    private var countdownTime = 10
    private var lastAcceleration = 9.8

    private var callCount = 0
    private var smsCount = 0
    private val maxEmergencyActions = 4

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }

        setupSpeechRecognizer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val acceleration = sqrt(
                        (it.values[0] * it.values[0] +
                                it.values[1] * it.values[1] +
                                it.values[2] * it.values[2]).toDouble()
                    )

                    if (lastAcceleration - acceleration > 5.0) {
                        fallDetected = true
                    }
                    lastAcceleration = acceleration
                }

                Sensor.TYPE_GYROSCOPE -> {
                    val rotation = abs(it.values[0]) + abs(it.values[1]) + abs(it.values[2])
                    if (rotation > 2.5) {
                        fallDetected = true
                    }
                }
            }

            if (fallDetected) {
                startCountdown()
            }
        }
    }

    private fun startCountdown() {
        handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (countdownTime > 0) {
                    Log.d("FallDetectionService", "Countdown: $countdownTime seconds")
                    countdownTime--
                    handler.postDelayed(this, 1000)
                } else {
                    triggerEmergency()
                }
            }
        })

        startListeningForStop()
    }

    private fun startListeningForStop() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }
        speechRecognizer.startListening(intent)
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SpeechRecognizer", "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("SpeechRecognizer", "Speech started")
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                Log.d("SpeechRecognizer", "End of speech")
            }

            override fun onError(error: Int) {
                Log.e("SpeechRecognizer", "Error: $error")
            }

            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { matches ->
                    for (result in matches) {
                        if (result.equals("stop", ignoreCase = true)) {
                            stopCountdown()
                        }
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun stopCountdown() {
        handler.removeCallbacksAndMessages(null)
        countdownTime = 10
        fallDetected = false
        Log.d("FallDetectionService", "Emergency countdown stopped.")
    }

    private fun triggerEmergency() {
        val emergencyNumber = "+911234567890"

        if (callCount >= maxEmergencyActions && smsCount >= maxEmergencyActions) {
            Log.d("FallDetectionService", "Max emergency actions reached. No more calls or SMS.")
            return
        }

        getCurrentLocation { location ->
            if (smsCount < maxEmergencyActions) {
                sendSMS(emergencyNumber, location)
                smsCount++
            }

            if (callCount < maxEmergencyActions) {
                makeEmergencyCall(emergencyNumber)
                callCount++
            }

            stopSelf()
        }
    }

    private fun sendSMS(number: String, location: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            val smsManager = SmsManager.getDefault()
            val message = "Emergency! Fall detected. My location: $location"
            smsManager.sendTextMessage(number, null, message, null, null)
            Log.d("FallDetectionService", "SMS Sent: $message")
        } else {
            Log.e("FallDetectionService", "SMS permission not granted.")
        }
    }

    private fun getCurrentLocation(callback: (String) -> Unit) {
        val fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback("Location permission not granted")
            return
        }

        fusedLocationProvider.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val locationUrl = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                Log.d("FallDetectionService", "Location: $locationUrl")
                callback(locationUrl)
            } else {
                callback("Location unavailable")
            }
        }
    }

    private fun makeEmergencyCall(number: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$number")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(callIntent)
        } else {
            Log.e("FallDetectionService", "Call permission not granted.")
        }
    }

    private fun createNotification(): Notification {
        val channelId = "FallDetectionServiceChannel"
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Fall Detection Service", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Fall Detection Active")
            .setContentText("Monitoring for falls...")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .build()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onBind(intent: Intent?): IBinder? = null
}
