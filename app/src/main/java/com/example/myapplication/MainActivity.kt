package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.myapplication.Routes.map
import com.example.myapplication.service.EmergencyScreen
import com.example.myapplication.service.SettingsScreen
import map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()

        // Start fall detection service as a foreground service
        val fallDetectionServiceIntent = Intent(this, FallDetectionService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(fallDetectionServiceIntent)
        } else {
            startService(fallDetectionServiceIntent)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, VoiceService::class.java))
            startForegroundService(Intent(this, FallDetectionService::class.java))
        } else {
            startService(Intent(this, VoiceService::class.java))
            startService(Intent(this, FallDetectionService::class.java))
        }

        setContent {
            MyApp()
        }
    }

    @Composable
    fun MyApp() {
        val navController = rememberNavController()
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "map",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("map") { map(navController) }
                composable("inbox") { EmergencyScreen(navController) }
                composable("account") { SettingsScreen(
                    navController,
                    onNumberSaved = {""}
                ) }
            }
        }
    }

    @Composable
    fun BottomNavigationBar(navController: NavHostController) {
        val items = listOf(
            NavItemState("map", Icons.Filled.Home, Icons.Outlined.Home, "map"),
            NavItemState("SOS", Icons.Filled.Email, Icons.Outlined.Email, "inbox"),
            NavItemState("Account", Icons.Filled.Face, Icons.Outlined.Face, "account")
        )
        var selectedItem by remember { mutableStateOf(0) }

        NavigationBar(
            containerColor = Color(0xFFE0A9A5),
            modifier = Modifier.padding(10.dp)
        ) {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = selectedItem == index,
                    onClick = {
                        selectedItem = index
                        navController.navigate(item.route) {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selectedItem == index) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title
                        )
                    },
                    label = { Text(text = item.title) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF552A27),
                        selectedTextColor = Color(0xFF63332F),
                        indicatorColor = Color(0xFFBB7E7A)
                    )
                )
            }
        }
    }

    data class NavItemState(
        val title: String,
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector,
        val route: String
    )

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.FOREGROUND_SERVICE_HEALTH,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions.plus(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION)
        }

        if (permissions.any {
                ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(this, permissions, 1001)
        }
    }

}
