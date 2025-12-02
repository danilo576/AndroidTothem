package humer.UvcCamera

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.fashiontothem.ff.core.scanner.BarcodeScannerEvents
import com.fashiontothem.ff.data.local.preferences.AthenaPreferences
import com.fashiontothem.ff.data.local.preferences.LocationPreferences
import com.fashiontothem.ff.data.local.preferences.StorePreferences
import com.fashiontothem.ff.domain.repository.StoreRepository
import com.fashiontothem.ff.navigation.FFNavGraph
import com.fashiontothem.ff.navigation.NavigationManager
import com.fashiontothem.ff.navigation.Screen
import com.fashiontothem.ff.presentation.camera.CameraController
import com.fashiontothem.ff.util.LocaleManager
import com.fashiontothem.ff.util.NetworkConnectivityObserver
import dagger.hilt.android.AndroidEntryPoint
import honeywell.hedc.EngineCommunication
import honeywell.hedc.HsmCompatActivity
import humer.UvcCamera.ui.theme.FFCameraTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : HsmCompatActivity() {

    @Inject
    lateinit var storePreferences: StorePreferences

    @Inject
    lateinit var athenaPreferences: AthenaPreferences

    @Inject
    lateinit var locationPreferences: LocationPreferences

    @Inject
    lateinit var storeRepository: StoreRepository

    @Inject
    lateinit var analyticsRepository: com.fashiontothem.ff.domain.repository.AnalyticsRepository

    companion object {
        init {
            System.loadLibrary("usb1.0")
            System.loadLibrary("jpeg9")
            System.loadLibrary("yuv")
            System.loadLibrary("uvc")
            System.loadLibrary("uvc_preview")
            System.loadLibrary("Uvc_Support")
        }
    }

    // State
    private var shouldStartCamera = false
    private lateinit var cameraController: CameraController

    // Permission launcher - handles permission request results
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }

        if (allGranted) {
            Toast.makeText(this, "Permissions Granted!", Toast.LENGTH_SHORT).show()
            if (shouldStartCamera) {
                shouldStartCamera = false
                // Launch camera directly (countdown is handled in camera activity)
                launchCamera()
            }
        } else {
            Toast.makeText(
                this,
                "Camera and storage permissions are required",
                Toast.LENGTH_LONG
            ).show()
            shouldStartCamera = false
        }
    }

    // Camera activity launcher - handles camera activity results
    private val cameraActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                cameraController.connectedToCamera = data.getIntExtra("connected_to_camera", 0)
                val shouldExit = data.getBooleanExtra("closeProgram", false)
                if (shouldExit) finish()
            }
        }
    }


    private fun startCamera() {
        if (cameraController.checkPermissions()) {
            // Permissions already granted - launch camera directly
            launchCamera()
        } else {
            // Request permissions
            shouldStartCamera = true
            requestPermissionLauncher.launch(cameraController.getRequiredPermissions())
        }
    }

    private fun launchCamera() {
        val intent = cameraController.startCameraIntent()
        cameraActivityLauncher.launch(intent)
    }

    /**
     * Restart the application when internet connection is restored.
     */
    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved locale if available
        runBlocking {
            val savedLocale = storePreferences.selectedLocale.firstOrNull()
            if (!savedLocale.isNullOrEmpty()) {
                LocaleManager.updateLocale(applicationContext, savedLocale)
            }
        }

        // Hide system navigation bar (bottom nav)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        cameraController = CameraController(this)

        // Initialize network observer
        val networkObserver = NetworkConnectivityObserver(this)

//        // ✅ TEST: Send analytics event on app startup
//        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
//            kotlinx.coroutines.delay(2000) // Wait 2 seconds after app starts
//            analyticsRepository.logEvent(
//                com.fashiontothem.ff.domain.model.AnalyticsEvent(
//                    name = "app_started",
//                    parameters = mapOf(
//                        "app_version" to "1.0.0",
//                        "device" to "philips_kiosk"
//                    )
//                )
//            )
//
//            analyticsRepository.logEvent(
//                com.fashiontothem.ff.domain.model.AnalyticsEvent(
//                    name = "kiosk_debug_ping",
//                    parameters = mapOf(
//                        "ts" to System.currentTimeMillis(),
//                        "device" to "philips_kiosk"
//                    )
//                )
//            )
//        }

        // Check initial network state BEFORE starting navigation
        val initialNetworkState = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                networkObserver.isNetworkAvailable()
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error checking initial network state: ${e.message}", e)
                false // Default to no network if check fails
            }
        } else {
            android.util.Log.w("MainActivity", "API level < 23, defaulting to no network")
            false // Default to no network for older API levels
        }

        setContent {
            FFCameraTheme {
                val navController = rememberNavController()
                val navigationManager = NavigationManager(storePreferences, locationPreferences)

                // Determine start destination based on initial network state
                val startDestination = if (initialNetworkState) {
                    navigationManager.getStartDestination()
                } else {
                    Screen.NoInternet.route
                }

                // Monitor network connectivity
                val isConnected by if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    try {
                        networkObserver.observe()
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Error observing network: ${e.message}", e)
                        kotlinx.coroutines.flow.flowOf(initialNetworkState)
                    }
                } else {
                    kotlinx.coroutines.flow.flowOf(initialNetworkState)
                }.collectAsState(initial = initialNetworkState)
                var wasDisconnected by remember { mutableStateOf(!initialNetworkState) }
                var previousRouteBeforeDisconnect by remember { mutableStateOf<String?>(null) }

                // Handle network state changes
                LaunchedEffect(isConnected) {
                    if (!isConnected) {
                        // No internet - save current route and navigate to NoInternet screen
                        wasDisconnected = true
                        // Save current route if not already on NoInternet screen
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute != Screen.NoInternet.route) {
                            previousRouteBeforeDisconnect = currentRoute
                        }
                        // Navigate to NoInternet screen, but don't clear back stack
                        // This allows us to go back when internet is restored
                        if (currentRoute != Screen.NoInternet.route) {
                            navController.navigate(Screen.NoInternet.route)
                        }
                    } else if (wasDisconnected && isConnected) {
                        // Internet restored - go back to previous screen
                        delay(500) // Brief delay to show reconnection
                        wasDisconnected = false
                        
                        // If we're on NoInternet screen, pop it to go back to previous screen
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute == Screen.NoInternet.route) {
                            // Check if there's a previous screen in back stack
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                // No previous screen, navigate to start destination
                                val targetRoute = previousRouteBeforeDisconnect ?: navigationManager.getStartDestination()
                                navController.navigate(targetRoute) {
                                    popUpTo(Screen.NoInternet.route) { inclusive = true }
                                }
                            }
                        }
                        previousRouteBeforeDisconnect = null
                    }
                }

                // Visual search will be handled in HomeScreen after navigation graph is ready

                FFNavGraph(
                    navController = navController,
                    startDestination = startDestination,
                    onStartCamera = { startCamera() },
                    storePreferences = storePreferences,
                    locationPreferences = locationPreferences,
                    storeRepository = storeRepository
                )
            }
        }

        // Check if camera should be started automatically
        if (intent.getBooleanExtra("start_camera", false)) {
            startCamera()
        }
    }

    override fun onResume() {
        // Workaround for SecurityException on Android 13+ (API 33+) where BroadcastReceiver
        // registration requires RECEIVER_EXPORTED or RECEIVER_NOT_EXPORTED flag.
        // The Honeywell HEDC library (HsmCompatActivity) tries to register a BroadcastReceiver
        // in its onResume() without specifying these flags, causing crashes on Android 13+ devices.
        // We wrap the super.onResume() call in a try-catch to prevent the app from crashing,
        // though this means barcode scanner functionality may not work properly on affected devices.
        try {
            super.onResume()
        } catch (e: SecurityException) {
            // Log the error for debugging
            android.util.Log.e(
                "MainActivity",
                "SecurityException in HsmCompatActivity.onResume() (Android ${android.os.Build.VERSION.SDK_INT}): ${e.message}",
                e
            )
            // Continue without the parent's onResume() to allow the app to function.
            // The barcode scanner may not work, but the app won't crash.
            android.util.Log.w(
                "MainActivity",
                "Continuing without HsmCompatActivity.onResume() due to SecurityException. " +
                        "Barcode scanner functionality may be limited."
            )
        } catch (e: RuntimeException) {
            // Catch RuntimeException as well, as SecurityException might be wrapped
            if (e.message?.contains("RECEIVER_EXPORTED") == true || 
                e.message?.contains("RECEIVER_NOT_EXPORTED") == true) {
                android.util.Log.e(
                    "MainActivity",
                    "RuntimeException (receiver registration issue) in HsmCompatActivity.onResume(): ${e.message}",
                    e
                )
                android.util.Log.w(
                    "MainActivity",
                    "Continuing without HsmCompatActivity.onResume() due to receiver registration issue."
                )
            } else {
                // Re-throw if it's a different RuntimeException
                throw e
            }
        }
        
        // Allow engine to connect if it exists
        m_engine?.AllowConnect()
    }

    override fun OnConnectionStateEvent(state: EngineCommunication.ConnectionState) {
        val status = when (state) {
            EngineCommunication.ConnectionState.UNKNOWN -> "Unknown"
            EngineCommunication.ConnectionState.Connected -> "Connected"
            EngineCommunication.ConnectionState.Disconnected -> "Disconnected"
            EngineCommunication.ConnectionState.PAUSED -> "Paused"
            EngineCommunication.ConnectionState.ASKPERMISSION -> "AskPermission"
            EngineCommunication.ConnectionState.CONNECTING -> "Connecting"
        }
    }

    override fun OnBarcodeData(str: String) {
        BarcodeScannerEvents.emit(str)
    }
}
