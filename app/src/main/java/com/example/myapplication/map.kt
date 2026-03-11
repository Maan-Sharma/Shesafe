import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

@Composable
fun map(navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationUrl by remember { mutableStateOf("Fetching location...") }

    // Fetch current location
    LaunchedEffect(Unit) {
        getCurrentLocation(context, fusedLocationClient) { url ->
            locationUrl = url
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // **Fake Map Frame with Image**
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clickable {
                    Toast.makeText(context, "Fake Map Clicked!", Toast.LENGTH_SHORT).show()
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.google), // Replace with your image name
                contentDescription = "Fake Map",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // **Button to share location on WhatsApp**
        Button(
            onClick = {
                sendLocationOnWhatsApp(context, locationUrl)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFBB7E7A)), // Custom Color
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Send Location on WhatsApp", fontSize = 18.sp, color = Color.White)
        }
    }
}

// Function to fetch user location
private fun getCurrentLocation(context: Context, fusedLocationClient: FusedLocationProviderClient, callback: (String) -> Unit) {
    if (androidx.core.app.ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
        callback("Location permission not granted")
        return
    }

    val locationTask: Task<android.location.Location> = fusedLocationClient.lastLocation
    locationTask.addOnSuccessListener { location ->
        if (location != null) {
            val locationUrl = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
            callback(locationUrl)
        } else {
            callback("Location unavailable")
        }
    }
}

// Function to open WhatsApp with the location
private fun sendLocationOnWhatsApp(context: Context, locationUrl: String) {
    val message = "Hey, here is my location: $locationUrl"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://wa.me/?text=${Uri.encode(message)}")
    }
    context.startActivity(intent)
}
