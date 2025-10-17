/*
Copyright 2019 Peter Stoiber

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

Please contact the author if you need another license.
This Repository is provided "as is", without warranties of any kind.

*/

package humer.UvcCamera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.freeapps.hosamazzam.androidchangelanguage.MyContextWrapper;

public class Main extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("usb1.0");
        System.loadLibrary("jpeg9");
        System.loadLibrary("yuv");
        System.loadLibrary("uvc");
        System.loadLibrary("uvc_preview");
        System.loadLibrary("Uvc_Support");
        isLoaded = true;
    }

    // JNI METHODS
    public native long nativeCreate(long camera_pointer);

    // Native UVC Camera
    private long mNativePtr;
    private int connected_to_camera;

    // Camera parameters
    public static int       camStreamingAltSetting;
    public static int       camFormatIndex;
    public static int       camFrameIndex;
    public static int       camFrameInterval;
    public static int       packetsPerRequest;
    public static int       maxPacketSize;
    public static int       imageWidth;
    public static int       imageHeight;
    public static int       activeUrbs;
    public static String    videoformat;
    public static String    deviceName;
    public static byte      bUnitID;
    public static byte      bTerminalID;
    public static byte[]    bNumControlTerminal;
    public static byte[]    bNumControlUnit;
    public static byte[]    bcdUVC;
    public static byte[]    bcdUSB;
    public static byte      bStillCaptureMethod;
    public static boolean   LIBUSB = true;
    public static boolean   moveToNative;
    public static boolean   bulkMode;

    private final int       REQUEST_PERMISSION_STORAGE_READ=1;
    private final int       REQUEST_PERMISSION_STORAGE_WRITE=2;
    private final int       REQUEST_PERMISSION_CAMERA=3;
    private static int      ActivityStartIsoStreamRequestCode = 2;

    private static boolean isLoaded;

    // Language Support
    private String LANG_CURRENT = "en";
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        LANG_CURRENT = preferences.getString("Language", "en");
        super.attachBaseContext(MyContextWrapper.wrap(newBase, LANG_CURRENT));
    }

    // Permissions 13+
    public static String[] storage_permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] storage_permissions_33 = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO
    };

    public static String[] permissions() {
        String[] p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            p = storage_permissions_33;
        } else {
            p = storage_permissions;
        }
        return p;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        
        // Full screen mode
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_STORAGE_READ:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(Main.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Main.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_PERMISSION_STORAGE_WRITE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(Main.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Main.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_PERMISSION_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(Main.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Main.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ActivityStartIsoStreamRequestCode && resultCode == RESULT_OK && data != null) {
            connected_to_camera = data.getIntExtra("connected_to_camera", 0);
            boolean exit = data.getBooleanExtra("closeProgram", false);
            if (exit == true) finish();
        }
    }

    // Main button - Start camera stream
    public void isoStream(View view){
        boolean permissions_t;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions_t = showStoragePermissionRead();
        } else {
            permissions_t = (showStoragePermissionRead() && showStoragePermissionWrite());
        }
        
        log("Starting camera stream");
        
        if (LIBUSB && mNativePtr == 0) {
            mNativePtr = nativeCreate(mNativePtr);
            log("mNativePtr = " + mNativePtr);
        }
        
        if (permissions_t) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(!showCameraPermissionCamera()) return;
            }
            
            // Auto-set camera parameters if not already set
            if (camFormatIndex == 0 || camFrameIndex == 0 ||camFrameInterval == 0 ||maxPacketSize == 0 ||imageWidth == 0 || activeUrbs == 0 ) {
                Log.i("UVC_Camera_Main", "Camera parameters not set, using MJPEG 800x600 SVGA 30 FPS");
                packetsPerRequest = 32;
                activeUrbs = 32;
                camStreamingAltSetting = 4;
                maxPacketSize = 1024;
                videoformat = "MJPEG";
                camFormatIndex = 2;  // MJPEG format
                camFrameIndex = 14;  // Frame 14 = 800x600 SVGA
                imageWidth = 800;
                imageHeight = 600;
                camFrameInterval = 333333; // 30 FPS
                
                Log.i("UVC_Camera_Main", "Auto-set parameters:");
                Log.i("UVC_Camera_Main", "packetsPerRequest = " + packetsPerRequest);
                Log.i("UVC_Camera_Main", "activeUrbs = " + activeUrbs);
                Log.i("UVC_Camera_Main", "camStreamingAltSetting = " + camStreamingAltSetting);
                Log.i("UVC_Camera_Main", "maxPacketSize = " + maxPacketSize);
                Log.i("UVC_Camera_Main", "videoformat = " + videoformat);
                Log.i("UVC_Camera_Main", "camFormatIndex = " + camFormatIndex);
                Log.i("UVC_Camera_Main", "camFrameIndex = " + camFrameIndex);
                Log.i("UVC_Camera_Main", "imageWidth = " + imageWidth);
                Log.i("UVC_Camera_Main", "imageHeight = " + imageHeight);
                Log.i("UVC_Camera_Main", "camFrameInterval = " + camFrameInterval);
            }
            
            // Start camera activity
            Intent intent = new Intent(this, StartIsoStreamActivityUsbIso.class);
            Bundle bundle=new Bundle();
            bundle.putInt("camStreamingAltSetting",camStreamingAltSetting);
            bundle.putString("videoformat",videoformat);
            bundle.putInt("camFormatIndex",camFormatIndex);
            bundle.putInt("imageWidth",imageWidth);
            bundle.putInt("imageHeight",imageHeight);
            bundle.putInt("camFrameIndex",camFrameIndex);
            bundle.putInt("camFrameInterval",camFrameInterval);
            bundle.putInt("packetsPerRequest",packetsPerRequest);
            bundle.putInt("maxPacketSize",maxPacketSize);
            bundle.putInt("activeUrbs",activeUrbs);
            bundle.putByte("bUnitID",bUnitID);
            bundle.putByte("bTerminalID",bTerminalID);
            bundle.putByteArray("bNumControlTerminal", bNumControlTerminal);
            bundle.putByteArray("bNumControlUnit", bNumControlUnit);
            bundle.putByteArray("bcdUVC", bcdUVC);
            bundle.putByte("bStillCaptureMethod",bStillCaptureMethod);
            bundle.putBoolean("libUsb", LIBUSB);
            bundle.putBoolean("moveToNative", moveToNative);
            bundle.putBoolean("bulkMode", bulkMode);
            bundle.putLong("mNativePtr", mNativePtr);
            bundle.putInt("connected_to_camera", connected_to_camera);
            intent.putExtra("bun",bundle);
            startActivityForResult(intent, ActivityStartIsoStreamRequestCode);
            
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            // For older Android versions
            if (camFormatIndex == 0 || camFrameIndex == 0 ||camFrameInterval == 0 ||packetsPerRequest == 0 ||maxPacketSize == 0 ||imageWidth == 0 || activeUrbs == 0 ) {
                Log.i("UVC_Camera_Main", "Camera parameters not set, using default values");
                packetsPerRequest = 32;
                activeUrbs = 32;
                camStreamingAltSetting = 4;
                maxPacketSize = 1024;
                videoformat = "YUY2";
                camFormatIndex = 1;
                camFrameIndex = 1;
                imageWidth = 640;
                imageHeight = 480;
                camFrameInterval = 333333; // 30 FPS
            }
            
            Intent intent = new Intent(this, StartIsoStreamActivityUsbIso.class);
            Bundle bundle=new Bundle();
            bundle.putInt("camStreamingAltSetting",camStreamingAltSetting);
            bundle.putString("videoformat",videoformat);
            bundle.putInt("camFormatIndex",camFormatIndex);
            bundle.putInt("imageWidth",imageWidth);
            bundle.putInt("imageHeight",imageHeight);
            bundle.putInt("camFrameIndex",camFrameIndex);
            bundle.putInt("camFrameInterval",camFrameInterval);
            bundle.putInt("packetsPerRequest",packetsPerRequest);
            bundle.putInt("maxPacketSize",maxPacketSize);
            bundle.putInt("activeUrbs",activeUrbs);
            bundle.putByte("bUnitID",bUnitID);
            bundle.putByte("bTerminalID",bTerminalID);
            bundle.putByteArray("bNumControlTerminal", bNumControlTerminal);
            bundle.putByteArray("bNumControlUnit", bNumControlUnit);
            bundle.putByteArray("bcdUVC", bcdUVC);
            bundle.putByte("bStillCaptureMethod",bStillCaptureMethod);
            bundle.putBoolean("libUsb", LIBUSB);
            bundle.putBoolean("moveToNative", moveToNative);
            bundle.putBoolean("bulkMode", bulkMode);
            bundle.putLong("mNativePtr", mNativePtr);
            bundle.putInt("connected_to_camera", connected_to_camera);
            intent.putExtra("bun",bundle);
            startActivityForResult(intent, ActivityStartIsoStreamRequestCode);
        }
    }

    // Permission handling methods
    private boolean showStoragePermissionRead() {
        int permissionCheck;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionCheck = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_VIDEO);
            if(permissionCheck != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_VIDEO)){
                    showExplanation("Permission Needed:", "Read media video", Manifest.permission.READ_MEDIA_VIDEO, permissionCheck);
                    return false;
                } else {
                    permissions();
                    return true;
                }
            }
            else {
                return true;
            }
        } else {
            permissionCheck = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showExplanation("Permission Needed:", "Read External Storage", Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_PERMISSION_STORAGE_READ);
                    return false;
                } else {
                    requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_PERMISSION_STORAGE_READ);
                    return false;
                }
            } else {
                return true;
            }
        }
    }

    private boolean showStoragePermissionWrite() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showExplanation("Permission Needed:", "Write to External Storage", Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_PERMISSION_STORAGE_WRITE);
                return false;
            } else {
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_PERMISSION_STORAGE_WRITE);
                return false;
            }
        } else {
            return true;
        }
    }

    private boolean showCameraPermissionCamera() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                showExplanation("Permission Needed:", "Camera", Manifest.permission.CAMERA, REQUEST_PERMISSION_CAMERA);
                return false;
            } else {
                requestPermission(Manifest.permission.CAMERA, REQUEST_PERMISSION_CAMERA);
                return false;
            }
        } else {
            return true;
        }
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

    public void log(String msg) {
        Log.i("UVC_Camera_Main", msg);
    }
}
