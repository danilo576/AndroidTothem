package com.fashiontothem.ff.presentation.camera

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.fashiontothem.ff.data.local.preferences.LocationPreferences
import com.fashiontothem.ff.ui.theme.Fonts
import com.fashiontothem.ff.util.ImageUtil
import com.fashiontothem.ff.util.clickableDebounced
import dagger.hilt.android.AndroidEntryPoint
import humer.UvcCamera.MainActivity
import humer.UvcCamera.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CropImageActivity : AppCompatActivity() {

    @Inject
    lateinit var locationPreferences: LocationPreferences

    private var originalBitmap: Bitmap? = null

    companion object {
        const val EXTRA_IMAGE_PATH = "image_path"
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

    @OptIn(DelicateCoroutinesApi::class)
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
                    // Convert bitmap to base64 for visual search
                    try {
                        Log.d("FFTothem_VisualSearch", "Converting image to base64...")
                        val base64Image = ImageUtil.bitmapToBase64(originalBitmap!!, quality = 85)
                        Log.d("FFTothem_VisualSearch", "Base64 length: ${base64Image.length}")

                        // Save to DataStore instead of passing through intent
                        GlobalScope.launch {
                            locationPreferences.saveVisualSearchImage(base64Image)

                            // Navigate to MainActivity with visual search flag
                            runOnUiThread {
                                val intent =
                                    Intent(this@CropImageActivity, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                intent.putExtra("start_visual_search", true)
                                startActivity(intent)
                                finish()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("FFTothem_VisualSearch", "Failed to process image", e)
                        Toast.makeText(
                            this@CropImageActivity,
                            "Failed to process image",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CapturedImageScreen(
    bitmap: Bitmap,
    onRetake: () -> Unit,
    onClose: () -> Unit,
    onContinue: () -> Unit,
) {
    val poppins = Fonts.Poppins

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background with subtle overlay
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with close button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B2B2B))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Close button (X)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickableDebounced { onClose() }
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.x_white_icon),
                        contentDescription = "Close"
                    )
                }

                // Title
                Text(
                    text = stringResource(id = R.string.crop_image_title),
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Captured image in center with card style
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            setImageBitmap(bitmap)
                            scaleType = ImageView.ScaleType.FIT_CENTER
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                )
            }

            // Bottom action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B2B2B))
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // "Slikaj ponovo" button
                Button(
                    onClick = onRetake,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF424242)
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.crop_image_retake),
                        fontFamily = poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                // "Nastavi" button with gradient
                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4F0418),
                                    Color(0xFFB50938)
                                )
                            ),
                            shape = RoundedCornerShape(50.dp)
                        ),
                ) {
                    Text(
                        text = stringResource(id = R.string.crop_image_continue),
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}