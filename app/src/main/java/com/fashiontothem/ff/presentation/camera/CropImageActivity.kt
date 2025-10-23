package com.fashiontothem.ff.presentation.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import androidx.appcompat.app.AppCompatActivity
import humer.UvcCamera.MainActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

class CropImageActivity : AppCompatActivity() {

    private var originalBitmap: Bitmap? = null

    companion object {
        const val EXTRA_IMAGE_PATH = "image_path"
        const val RESULT_CROPPED_IMAGE = "cropped_image"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
        if (imagePath == null) {
            Toast.makeText(this, "No image provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load the captured image
        originalBitmap = BitmapFactory.decodeFile(imagePath)
        if (originalBitmap == null) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupComposeView()
    }

    private fun setupComposeView() {
        val composeView = ComposeView(this)
        setContentView(composeView)

        composeView.setContent {
            CapturedImageScreen(
                bitmap = originalBitmap!!,
                onRetake = {
                    // Navigate to MainActivity and start camera
                    val intent = Intent(this@CropImageActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.putExtra("start_camera", true) // Flag to start camera automatically
                    startActivity(intent)
                    finish()
                },
                onClose = {
                    // Just close the activity
                    finish()
                },
                onContinue = {
                    // Save original image to file and return path
                    try {
                        val fileName = "captured_image_${System.currentTimeMillis()}.jpg"
                        val externalFilesDir = getExternalFilesDir(null)
                        val file = File(externalFilesDir, fileName)
                        
                        val fos = FileOutputStream(file)
                        originalBitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                        fos.close()

                        val result = Intent()
                        result.putExtra(RESULT_CROPPED_IMAGE, file.absolutePath)
                        setResult(Activity.RESULT_OK, result)
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@CropImageActivity, "Failed to save image", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                }
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapturedImageScreen(
    bitmap: Bitmap,
    onRetake: () -> Unit,
    onClose: () -> Unit,
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Image
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    setImageBitmap(bitmap)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top bar
        TopAppBar(
            title = {
                Text(
                    "Captured Image",
                    color = Color.White,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onClose) { // X button just closes the activity
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.8f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Bottom buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // "Slikaj ponovo" button
            Button(
                onClick = {
                    android.util.Log.d("CropImage", "Slikaj ponovo clicked")
                    onRetake()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    "Slikaj ponovo",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            // "Nastavi" button
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB50938)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    "Nastavi",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}