package humer.UvcCamera

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.fashiontothem.ff.data.local.preferences.AthenaPreferences
import com.fashiontothem.ff.data.local.preferences.LocationPreferences
import com.fashiontothem.ff.data.local.preferences.StorePreferences
import com.fashiontothem.ff.domain.repository.StoreRepository
import com.fashiontothem.ff.presentation.camera.CameraController
import com.fashiontothem.ff.presentation.common.LoadingScreen
import com.fashiontothem.ff.presentation.home.HomeScreen
import com.fashiontothem.ff.presentation.locations.StoreLocationsScreen
import com.fashiontothem.ff.presentation.store.StoreSelectionScreen
import dagger.hilt.android.AndroidEntryPoint
import humer.UvcCamera.ui.theme.FFCameraTheme
import kotlinx.coroutines.launch
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

    // ========== Lifecycle ==========

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraController = CameraController(this)
        setContent {
            FFCameraTheme {
                AppNavigation()
            }
        }

        // Check if camera should be started automatically
        if (intent.getBooleanExtra("start_camera", false)) {
            startCamera()
        }
    }

    @Composable
    private fun AppNavigation() {
        val selectedStoreCode by storePreferences.selectedStoreCode.collectAsState(initial = "")
        val selectedStoreLocationId by locationPreferences.selectedStoreId.collectAsState(initial = "")
        var storeConfigRefreshed by remember { mutableStateOf(false) }

        // Refresh store config on app start if store is already selected
        // Optimized to avoid unnecessary API calls
        LaunchedEffect(selectedStoreCode) {
            if (!selectedStoreCode.isNullOrEmpty() && selectedStoreCode != "" && !storeConfigRefreshed) {
                Log.d(
                    "FFTothem_MainActivity",
                    "Store already selected. Checking if config refresh is needed..."
                )
                lifecycleScope.launch {
                    storeRepository.refreshStoreConfigAndInitAthena().fold(
                        onSuccess = { config ->
                            Log.d(
                                "FFTothem_MainActivity",
                                "✅ Store config and Athena token ready"
                            )
                            storeConfigRefreshed = true
                        },
                        onFailure = { error ->
                            Log.e(
                                "FFTothem_MainActivity",
                                "⚠️ Failed to refresh store config: ${error.message}"
                            )
                            // Continue anyway - app can still work with cached data
                            storeConfigRefreshed = true
                        }
                    )
                }
            }
        }

        when {
            selectedStoreCode == "" -> LoadingScreen()

            selectedStoreCode == null -> {
                // No store selected - show store selection screen
                StoreSelectionScreen(
                    onStoreSelected = {
                        // Store selected, now show location selection
                    }
                )
            }

            selectedStoreLocationId == "" -> {
                // Store selected but loading location
                LoadingScreen()
            }

            selectedStoreLocationId == null -> {
                // Store selected but no location - show location selection
                StoreLocationsScreen(
                    onLocationSelected = {
                        // Location selected, proceed to camera
                    }
                )
            }

            else -> {
                // Everything selected - show home screen
                HomeScreen(
                    onStartCamera = { startCamera() }
                )
            }
        }
    }

}
