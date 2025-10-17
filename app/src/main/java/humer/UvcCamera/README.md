# F&F Camera - Code Structure

## 📁 Project Organization

### Main Entry Point
- **MainActivity.kt** - Modern Jetpack Compose UI
  - Permissions handling
  - Camera launch logic
  - Clean, readable code structure

### Camera Functionality
- **StartIsoStreamActivityUsbIso.java** - Camera preview and capture
  - USB camera connection
  - Image capture functionality
  - Video encoding support

### Configuration
- **CameraConfig.kt** - Camera configuration constants
  - Default camera parameters
  - FPS calculations
  - Reusable configuration values

### UI Theme
- **ui/theme/Theme.kt** - Material 3 theme configuration
  - Dark color scheme
  - Brand colors

### Native Support
- **UsbIso64/** - USB isochronous transfer support
- **UVC_Descriptor/** - UVC (USB Video Class) protocol
- **JNA_I_LibUsb/** - JNA bindings for libusb

### Utilities
- **BitmapToVideoEncoder.java** - Video encoding
- **SetCameraVariables.java** - Camera controls (brightness, contrast, etc.)
- **LockCameraVariables.java** - Camera control synchronization

## 🎯 Key Features

1. **Simple UI** - Single button to start camera
2. **Auto-configuration** - Camera parameters set automatically
3. **Modern Architecture** - Jetpack Compose + Kotlin
4. **Permission Handling** - Automatic permission requests
5. **Image Capture** - Take photos from USB camera

## 🔧 How It Works

1. User clicks "START CAMERA"
2. App checks/requests permissions
3. Camera parameters auto-configured (if needed)
4. Camera activity launched
5. User can capture images

## 📝 Code Quality

- ✅ Clear function names
- ✅ Comprehensive comments
- ✅ Separated concerns
- ✅ Modern Kotlin idioms
- ✅ Type-safe configurations

