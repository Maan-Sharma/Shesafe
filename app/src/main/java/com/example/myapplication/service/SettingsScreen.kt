package com.example.myapplication.service


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SettingsScreen(onNumberSaved: (() -> Unit)?) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var phoneNumber by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Enter Emergency Contact", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (phoneNumber.isNotEmpty()) {
                    isSaving = true
                    saveEmergencyNumber(db, phoneNumber) {
                        isSaving = false
                        Toast.makeText(context, "Emergency Number Saved!", Toast.LENGTH_SHORT).show()
                        if (onNumberSaved != null) {
                            onNumberSaved()
                        }
                    }
                } else {
                    Toast.makeText(context, "Please enter a number!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            Text(if (isSaving) "Saving..." else "Save Number")
        }
    }
}

fun saveEmergencyNumber(db: FirebaseFirestore, number: String, onComplete: () -> Unit) {
    val emergencyData = hashMapOf("emergency_number" to number)

    db.collection("emergency_contacts").document("user_1")
        .set(emergencyData)
        .addOnSuccessListener { onComplete() }
        .addOnFailureListener { onComplete() }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen(onNumberSaved = {})
}
