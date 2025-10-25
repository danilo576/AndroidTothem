package humer.UvcCamera

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import humer.UvcCamera.ui.theme.FFCameraTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var storePreferences: StorePreferences

    @Inject
    lateinit var athenaPreferences: AthenaPreferences

    @Inject
    lateinit var locationPreferences: LocationPreferences

    @Inject
    lateinit var storeRepository: StoreRepository

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

    @RequiresApi(Build.VERSION_CODES.M)
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

        // Check initial network state BEFORE starting navigation
        val initialNetworkState = networkObserver.isNetworkAvailable()

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
                val isConnected by networkObserver.observe()
                    .collectAsState(initial = initialNetworkState)
                var wasDisconnected by remember { mutableStateOf(!initialNetworkState) }

                // Handle network state changes
                LaunchedEffect(isConnected) {
                    if (!isConnected) {
                        // No internet - navigate to NoInternet screen
                        wasDisconnected = true
                        navController.navigate(Screen.NoInternet.route) {
                            // Clear back stack
                            popUpTo(0) { inclusive = true }
                        }
                    } else if (wasDisconnected && isConnected) {
                        // Internet restored - restart app
                        delay(1000) // Brief delay to show reconnection
                        restartApp()
                    }
                }

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


}
