package com.example.myapplication.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

@Composable
fun EmergencyScreen() {
    val context = LocalContext.current
    var emergencyNumber by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isListening by remember { mutableStateOf(false) }

    // Speech recognition launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenText: String? = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
        if (spokenText?.lowercase(Locale.getDefault()) == "help me" && emergencyNumber != null) {
            makeCall(context, emergencyNumber!!)
            sendWhatsAppMessage(context, emergencyNumber!!)
        }
    }

    // Permission Launcher for CALL_PHONE
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()

    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Call permission required!", Toast.LENGTH_SHORT).show()
        }
    }

    // Fetch emergency number from Firebase
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("emergency_contacts").document("user_1")
            .get()
            .addOnSuccessListener { document ->
                emergencyNumber = document.getString("emergency_number")
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
                Toast.makeText(context, "Failed to fetch emergency number", Toast.LENGTH_SHORT).show()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Emergency Actions", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            if (!emergencyNumber.isNullOrEmpty()) {
                Text("Saved Number: $emergencyNumber", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val hasCallPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CALL_PHONE
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasCallPermission) {
                            makeCall(context, emergencyNumber!!)
                        } else {
                            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Call Emergency Contact")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { sendWhatsAppMessage(context, emergencyNumber!!) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Send WhatsApp Message")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isListening = true
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say 'Help me' to trigger emergency call.")
                        }
                        speechRecognizerLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isListening) "Listening..." else "Voice Trigger")
                }
            } else {
                Text("No emergency number saved!", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

fun makeCall(context: Context, number: String) {
    val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
    try {
        context.startActivity(callIntent)
    } catch (e: SecurityException) {
        Toast.makeText(context, "Call permission required!", Toast.LENGTH_SHORT).show()
    }
}

fun sendWhatsAppMessage(context: Context, number: String) {
    try {
        val message = "Emergency! Please help!"
        val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$number&text=$encodedMessage")

        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.whatsapp") // Ensures it opens in WhatsApp
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp not installed!", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEmergencyScreen() {
    EmergencyScreen()
}
