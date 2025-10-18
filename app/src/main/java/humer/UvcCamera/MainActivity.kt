package humer.UvcCamera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.fashiontothem.ff.data.local.preferences.StorePreferences
import com.fashiontothem.ff.presentation.debug.DebugScreen
import com.fashiontothem.ff.presentation.store.StoreSelectionScreen
import dagger.hilt.android.AndroidEntryPoint
import humer.UvcCamera.ui.theme.FFCameraTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var storePreferences: StorePreferences
    
    companion object {
        @JvmStatic var camStreamingAltSetting = 0
        @JvmStatic var camFormatIndex = 0
        @JvmStatic var camFrameIndex = 0
        @JvmStatic var camFrameInterval = 0
        @JvmStatic var packetsPerRequest = 0
        @JvmStatic var maxPacketSize = 0
        @JvmStatic var imageWidth = 0
        @JvmStatic var imageHeight = 0
        @JvmStatic var activeUrbs = 0
        @JvmStatic var videoformat: String? = null
        @JvmStatic var deviceName: String? = null
        @JvmStatic var bUnitID: Byte = 0
        @JvmStatic var bTerminalID: Byte = 0
        @JvmStatic var bNumControlTerminal: ByteArray? = null
        @JvmStatic var bNumControlUnit: ByteArray? = null
        @JvmStatic var bcdUVC: ByteArray? = null
        @JvmStatic var bcdUSB: ByteArray? = null
        @JvmStatic var bStillCaptureMethod: Byte = 0
        @JvmStatic var LIBUSB = false
        @JvmStatic var moveToNative = false
        @JvmStatic var bulkMode = false
        
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
    private var connected_to_camera = 0
    private var shouldStartCamera = false

    // Permission launcher - handles permission request results
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        
        if (allGranted) {
            Toast.makeText(this, "Permissions Granted!", Toast.LENGTH_SHORT).show()
            if (shouldStartCamera) {
                shouldStartCamera = false
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
                connected_to_camera = data.getIntExtra("connected_to_camera", 0)
                val shouldExit = data.getBooleanExtra("closeProgram", false)
                if (shouldExit) finish()
            }
        }
    }

    // ========== Lifecycle ==========
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FFCameraTheme {
                AppNavigation()
            }
        }
    }
    
    @Composable
    private fun AppNavigation() {
        val selectedStoreCode by storePreferences.selectedStoreCode.collectAsState(initial = "")
        
        when {
            selectedStoreCode == "" -> {
                // Loading from DataStore - show splash/loading
                LoadingScreen()
            }
            selectedStoreCode == null -> {
                // No store selected - show store selection screen
                StoreSelectionScreen(
                    onStoreSelected = {
                        // Store selected, UI will automatically update due to Flow
                    }
                )
            }
            else -> {
                // Store already selected - show camera screen
                CameraScreen()
            }
        }
    }
    
    @Composable
    private fun LoadingScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1a1a2e),
                            Color(0xFF16213e),
                            Color(0xFF0f3460)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // No visible content - just background
            // DataStore loads very fast, this screen flashes for <100ms
        }
    }
    
    // ========== UI Components ==========

    @Composable
    private fun CameraScreen() {
        val hasPermissions = remember { mutableStateOf(checkPermissions()) }
        var showDebugScreen by remember { mutableStateOf(false) }
        
        if (showDebugScreen) {
            DebugScreen(
                storePreferences = storePreferences,
                onClose = { showDebugScreen = false }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1a1a2e),
                                Color(0xFF16213e),
                                Color(0xFF0f3460)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    AppTitle()
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    StartCameraButton(
                        onClick = { 
                            hasPermissions.value = checkPermissions()
                            startCamera() 
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Debug button
                    Button(
                        onClick = { showDebugScreen = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF333333)
                        ),
                        modifier = Modifier.width(280.dp)
                    ) {
                        Text("Debug / Cache Status")
                    }
                    
                    if (!hasPermissions.value) {
                        PermissionHint()
                    }
                }
            }
        }
    }

    @Composable
    private fun AppTitle() {
        Text(
            text = "F&F Camera",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Fashion Tothem",
            fontSize = 18.sp,
            color = Color(0xFF03DAC5)
        )
    }

    @Composable
    private fun StartCameraButton(onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .width(280.dp)
                .height(70.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            )
        ) {
            Text(
                text = "START CAMERA",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }

    @Composable
    private fun PermissionHint() {
        Text(
            text = "Camera and storage permissions required",
            fontSize = 12.sp,
            color = Color(0xFFFFAA00),
            modifier = Modifier.padding(top = 16.dp)
        )
    }

    // ========== Camera Logic ==========
    
    private fun startCamera() {
        resetCameraParameters()

        if (!checkPermissions()) {
            shouldStartCamera = true
            requestPermissions()
            return
        }

        launchCamera()
    }
    
    private fun launchCamera() {
        if (needsCameraConfiguration()) {
            configureCameraDefaults()
        }

        val intent = createCameraIntent()
        cameraActivityLauncher.launch(intent)
    }

    /**
     * Check if camera needs default configuration
     */
    private fun needsCameraConfiguration(): Boolean {
        return camFormatIndex == 0 || 
               camFrameIndex == 0 || 
               camFrameInterval == 0 || 
               maxPacketSize == 0 || 
               imageWidth == 0 || 
               activeUrbs == 0
    }

    private fun configureCameraDefaults() {
        packetsPerRequest = CameraConfig.Defaults.PACKETS_PER_REQUEST
        activeUrbs = CameraConfig.Defaults.ACTIVE_URBS
        camStreamingAltSetting = CameraConfig.Defaults.CAM_STREAMING_ALT_SETTING
        maxPacketSize = CameraConfig.Defaults.MAX_PACKET_SIZE
        videoformat = CameraConfig.Defaults.VIDEO_FORMAT
        camFormatIndex = CameraConfig.Defaults.CAM_FORMAT_INDEX
        camFrameIndex = CameraConfig.Defaults.CAM_FRAME_INDEX
        imageWidth = CameraConfig.Defaults.IMAGE_WIDTH
        imageHeight = CameraConfig.Defaults.IMAGE_HEIGHT
        camFrameInterval = CameraConfig.Defaults.CAM_FRAME_INTERVAL
    }

    private fun createCameraIntent(): Intent {
        val intent = Intent(this, StartIsoStreamActivityUsbIso::class.java)
        val bundle = Bundle().apply {
            putInt("camStreamingAltSetting", camStreamingAltSetting)
            putString("videoformat", videoformat)
            putInt("camFormatIndex", camFormatIndex)
            putInt("camFrameIndex", camFrameIndex)
            putInt("camFrameInterval", camFrameInterval)
            putInt("imageWidth", imageWidth)
            putInt("imageHeight", imageHeight)
            putInt("packetsPerRequest", packetsPerRequest)
            putInt("maxPacketSize", maxPacketSize)
            putInt("activeUrbs", activeUrbs)
            putByte("bUnitID", bUnitID)
            putByte("bTerminalID", bTerminalID)
            putByteArray("bNumControlTerminal", bNumControlTerminal)
            putByteArray("bNumControlUnit", bNumControlUnit)
            putByteArray("bcdUVC", bcdUVC)
            putByte("bStillCaptureMethod", bStillCaptureMethod)
            putBoolean("libUsb", LIBUSB)
            putBoolean("moveToNative", moveToNative)
            putBoolean("bulkMode", bulkMode)
            putLong("mNativePtr", 0L)
            putInt("connected_to_camera", connected_to_camera)
        }
        intent.putExtra("bun", bundle)
        return intent
    }

    // ========== Permission Management ==========
    
    private fun checkPermissions(): Boolean {
        val requiredPermissions = getRequiredPermissions()
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == 
                PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(getRequiredPermissions())
    }

    private fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.CAMERA
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        }
    }
    
    private fun resetCameraParameters() {
        camStreamingAltSetting = 0
        camFormatIndex = 0
        camFrameIndex = 0
        camFrameInterval = 0
        packetsPerRequest = 0
        maxPacketSize = 0
        imageWidth = 0
        imageHeight = 0
        activeUrbs = 0
        videoformat = null
        deviceName = null
        bUnitID = 0
        bTerminalID = 0
        bNumControlTerminal = null
        bNumControlUnit = null
        bcdUVC = null
        bcdUSB = null
        bStillCaptureMethod = 0
    }
}
