package com.fashiontothem.ff.presentation.camera

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import humer.UvcCamera.CameraConfig
import humer.UvcCamera.StartIsoStreamActivityUsbIso
import android.Manifest

class CameraController(private val context: Context) {

    // Camera runtime parameters (moved from MainActivity)
    var camStreamingAltSetting = 0
    var camFormatIndex = 0
    var camFrameIndex = 0
    var camFrameInterval = 0
    var packetsPerRequest = 0
    var maxPacketSize = 0
    var imageWidth = 0
    var imageHeight = 0
    var activeUrbs = 0
    var videoformat: String? = null
    var bUnitID: Byte = 0
    var bTerminalID: Byte = 0
    var bNumControlTerminal: ByteArray? = null
    var bNumControlUnit: ByteArray? = null
    var bcdUVC: ByteArray? = null
    var bcdUSB: ByteArray? = null
    var bStillCaptureMethod: Byte = 0
    var LIBUSB = false
    var moveToNative = false
    var bulkMode = false
    var connectedToCamera = 0

    fun startCameraIntent(): Intent {
        applyBrioStable()
        return buildIntentEnsuringDefaults()
    }

    fun applyBrioStable() {
        packetsPerRequest = CameraConfig.BrioStable.PACKETS_PER_REQUEST
        activeUrbs = CameraConfig.BrioStable.ACTIVE_URBS
        camStreamingAltSetting = CameraConfig.BrioStable.CAM_STREAMING_ALT_SETTING
        maxPacketSize = CameraConfig.BrioStable.MAX_PACKET_SIZE
        videoformat = CameraConfig.BrioStable.VIDEO_FORMAT
        camFormatIndex = CameraConfig.BrioStable.CAM_FORMAT_INDEX
        camFrameIndex = CameraConfig.BrioStable.CAM_FRAME_INDEX
        imageWidth = CameraConfig.BrioStable.IMAGE_WIDTH
        imageHeight = CameraConfig.BrioStable.IMAGE_HEIGHT
        camFrameInterval = CameraConfig.BrioStable.CAM_FRAME_INTERVAL
    }

    fun needsDefaults(): Boolean {
        return camFormatIndex == 0 ||
                camFrameIndex == 0 ||
                camFrameInterval == 0 ||
                maxPacketSize == 0 ||
                imageWidth == 0 ||
                activeUrbs == 0
    }

    fun applyDefaults() {
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

    fun createCameraIntent(): Intent {
        val intent = Intent(context, StartIsoStreamActivityUsbIso::class.java)
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
            putInt("connected_to_camera", connectedToCamera)
        }
        intent.putExtra("bun", bundle)
        return intent
    }

    /**
     * Build camera intent, ensuring default parameters are applied if missing.
     */
    fun buildIntentEnsuringDefaults(): Intent {
        if (needsDefaults()) {
            applyDefaults()
        }
        return createCameraIntent()
    }

    fun checkPermissions(): Boolean {
        val required = getRequiredPermissions()
        return required.all { p ->
            ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getRequiredPermissions(): Array<String> {
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
}


