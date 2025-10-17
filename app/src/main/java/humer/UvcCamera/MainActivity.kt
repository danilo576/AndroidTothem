package humer.UvcCamera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.core.content.ContextCompat
import humer.UvcCamera.ui.theme.FFCameraTheme

/**
 * F&F Camera - Main Activity
 * Modern Jetpack Compose UI for UVC Camera application
 */
class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        
        // Camera configuration parameters - shared with camera activity
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
        
        // UVC Descriptor parameters
        @JvmStatic var bUnitID: Byte = 0
        @JvmStatic var bTerminalID: Byte = 0
        @JvmStatic var bNumControlTerminal: ByteArray? = null
        @JvmStatic var bNumControlUnit: ByteArray? = null
        @JvmStatic var bcdUVC: ByteArray? = null
        @JvmStatic var bcdUSB: ByteArray? = null
        @JvmStatic var bStillCaptureMethod: Byte = 0
        
        // Operation modes
        @JvmStatic var LIBUSB = true
        @JvmStatic var moveToNative = false
        @JvmStatic var bulkMode = false
        
        // Load native libraries
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
                CameraScreen()
            }
        }
    }

    // ========== UI Components ==========

    @Composable
    private fun CameraScreen() {
        val hasPermissions = remember { mutableStateOf(checkPermissions()) }
        
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
                // App branding
                AppTitle()
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Main action button
                StartCameraButton(
                    onClick = { 
                        hasPermissions.value = checkPermissions()
                        startCamera() 
                    }
                )
                
                // Permission status hint
                if (!hasPermissions.value) {
                    PermissionHint()
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
    
    /**
     * Start camera - checks permissions first, then launches camera activity
     */
    private fun startCamera() {
        Log.d(TAG, "Starting camera - checking permissions")
        
        if (!checkPermissions()) {
            Log.d(TAG, "Permissions not granted, requesting...")
            shouldStartCamera = true
            requestPermissions()
            return
        }
        
        Log.d(TAG, "Permissions OK, launching camera")
        launchCamera()
    }
    
    /**
     * Launch camera activity with auto-configured parameters
     */
    private fun launchCamera() {
        Log.d(TAG, "Launching camera activity")

        // Auto-configure camera if parameters not set
        if (needsCameraConfiguration()) {
            configureCameraDefaults()
        }

        // Create intent with all camera parameters
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

    /**
     * Set default camera parameters - MJPEG 800x600 @ 30 FPS
     */
    private fun configureCameraDefaults() {
        Log.i(TAG, "Auto-configuring camera: MJPEG 800x600 @ 30 FPS")
        
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
        
        val fps = CameraConfig.calculateFps(camFrameInterval)
        Log.d(TAG, "Camera configured: ${imageWidth}x${imageHeight} $videoformat @ $fps FPS")
    }

    /**
     * Create intent with all camera parameters bundled
     */
    private fun createCameraIntent(): Intent {
        val intent = Intent(this, StartIsoStreamActivityUsbIso::class.java)
        val bundle = Bundle().apply {
            // Stream settings
            putInt("camStreamingAltSetting", camStreamingAltSetting)
            putString("videoformat", videoformat)
            putInt("camFormatIndex", camFormatIndex)
            putInt("camFrameIndex", camFrameIndex)
            putInt("camFrameInterval", camFrameInterval)
            
            // Image settings
            putInt("imageWidth", imageWidth)
            putInt("imageHeight", imageHeight)
            
            // Transfer settings
            putInt("packetsPerRequest", packetsPerRequest)
            putInt("maxPacketSize", maxPacketSize)
            putInt("activeUrbs", activeUrbs)
            
            // UVC descriptor data
            putByte("bUnitID", bUnitID)
            putByte("bTerminalID", bTerminalID)
            putByteArray("bNumControlTerminal", bNumControlTerminal)
            putByteArray("bNumControlUnit", bNumControlUnit)
            putByteArray("bcdUVC", bcdUVC)
            putByte("bStillCaptureMethod", bStillCaptureMethod)
            
            // Operation modes
            putBoolean("libUsb", LIBUSB)
            putBoolean("moveToNative", moveToNative)
            putBoolean("bulkMode", bulkMode)
            
            // Connection state
            putLong("mNativePtr", 0L)  // Initialized in camera activity
            putInt("connected_to_camera", connected_to_camera)
        }
        intent.putExtra("bun", bundle)
        return intent
    }

    // ========== Permission Management ==========
    
    /**
     * Check if all required permissions are granted
     */
    private fun checkPermissions(): Boolean {
        val requiredPermissions = getRequiredPermissions()
        
        val allGranted = requiredPermissions.all { permission ->
            val granted = ContextCompat.checkSelfPermission(this, permission) == 
                         PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "Permission $permission: ${if (granted) "GRANTED" else "DENIED"}")
            granted
        }
        
        Log.d(TAG, "All permissions granted: $allGranted")
        return allGranted
    }

    /**
     * Request all required permissions
     */
    private fun requestPermissions() {
        val requiredPermissions = getRequiredPermissions()
        Log.d(TAG, "Requesting permissions: ${requiredPermissions.joinToString()}")
        requestPermissionLauncher.launch(requiredPermissions)
    }

    /**
     * Get required permissions based on Android version
     */
    private fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.CAMERA
            )
        } else {
            // Android 12 and below
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        }
    }
}
