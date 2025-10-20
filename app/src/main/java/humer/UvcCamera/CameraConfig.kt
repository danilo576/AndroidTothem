package humer.UvcCamera

/**
 * Camera configuration constants and default values
 */
object CameraConfig {
    
    /**
     * Default camera configuration - MJPEG 800x600 @ 30 FPS
     * Optimized for most USB cameras
     */
    object Defaults {
        const val PACKETS_PER_REQUEST = 32
        const val ACTIVE_URBS = 32
        const val CAM_STREAMING_ALT_SETTING = 4
        const val MAX_PACKET_SIZE = 1024
        const val VIDEO_FORMAT = "MJPEG"
        const val CAM_FORMAT_INDEX = 2      // MJPEG format
        const val CAM_FRAME_INDEX = 14      // 800x600 SVGA
        const val IMAGE_WIDTH = 800
        const val IMAGE_HEIGHT = 600
        const val CAM_FRAME_INTERVAL = 333333  // 30 FPS (10,000,000 / fps)
    }

    /**
     * BRIO Stable configuration - optimized for Logitech BRIO
     * MJPEG 640x480 @ 30 FPS with optimized USB parameters
     */
    object BrioStable {
        const val PACKETS_PER_REQUEST = 96
        const val ACTIVE_URBS = 64
        const val CAM_STREAMING_ALT_SETTING = 4
        const val MAX_PACKET_SIZE = 1024
        const val VIDEO_FORMAT = "MJPEG"
        const val CAM_FORMAT_INDEX = 2      // MJPEG format
        const val CAM_FRAME_INDEX = 16      // Optimized frame index
        const val IMAGE_WIDTH = 640
        const val IMAGE_HEIGHT = 480
        const val CAM_FRAME_INTERVAL = 333333  // 30 FPS
    }
    
    /**
     * Calculate FPS from frame interval
     */
    fun calculateFps(frameInterval: Int): Int {
        return if (frameInterval > 0) 10000000 / frameInterval else 0
    }
    
    /**
     * Calculate frame interval from FPS
     */
    fun calculateFrameInterval(fps: Int): Int {
        return if (fps > 0) 10000000 / fps else 0
    }
}

