package com.fashiontothem.ff.presentation.debug

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.imageLoader
import com.fashiontothem.ff.util.MemoryMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * F&F Tothem - Debug Screen
 * 
 * Shows cache status, memory usage, cached files, and store config.
 */
@Composable
fun DebugScreen(
    storePreferences: com.fashiontothem.ff.data.local.preferences.StorePreferences,
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // Auto-refresh every 2 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            refreshTrigger++
        }
    }
    
    val selectedStoreCode by storePreferences.selectedStoreCode.collectAsState(initial = null)
    val selectedCountryCode by storePreferences.selectedCountryCode.collectAsState(initial = null)
    
    val memoryInfo = remember(refreshTrigger) { MemoryMonitor.getMemoryInfo(context) }
    val cacheInfo = remember(refreshTrigger) { MemoryMonitor.getCacheInfo(context) }
    val cacheFiles = remember(refreshTrigger) { getCachedFiles(context) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a2e))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "F&F Tothem Debug",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                )
            ) {
                Text("Close")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Store Config Status
        DebugSection(title = "🛍️ Selected Store") {
            if (selectedStoreCode != null && selectedCountryCode != null) {
                InfoRow("Country", selectedCountryCode ?: "N/A")
                InfoRow("Store Code", selectedStoreCode ?: "N/A")
                InfoRow("Status", "✅ Saved in DataStore")
            } else {
                Text(
                    text = "❌ No store selected yet",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Memory Status
        DebugSection(title = "📊 Memory Status") {
            InfoRow("App Memory", "${memoryInfo.usedMemoryMB}MB / ${memoryInfo.maxMemoryMB}MB (${memoryInfo.usagePercentage}%)")
            InfoRow("Device Memory", "${memoryInfo.deviceAvailableMemoryMB}MB / ${memoryInfo.deviceTotalMemoryMB}MB available")
            InfoRow("Low Memory", if (memoryInfo.isLowMemory) "⚠️ YES" else "✅ NO")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Cache Status
        DebugSection(title = "💾 Cache Status") {
            InfoRow("Memory Cache", "${cacheInfo.memoryCacheSizeMB}MB / ${cacheInfo.memoryCacheMaxSizeMB}MB (${cacheInfo.memoryCacheUsagePercentage}%)")
            InfoRow("Disk Cache", "${cacheInfo.diskCacheSizeMB}MB / ${cacheInfo.diskCacheMaxSizeMB}MB (${cacheInfo.diskCacheUsagePercentage}%)")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Cached Files
        DebugSection(title = "📁 Cached Files (${cacheFiles.size} files)") {
            if (cacheFiles.isEmpty()) {
                Text(
                    text = "No files cached yet",
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            } else {
                cacheFiles.take(20).forEach { file ->
                    Text(
                        text = "• ${file.name} (${file.size / 1024}KB)",
                        color = Color(0xFF03DAC5),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
                
                if (cacheFiles.size > 20) {
                    Text(
                        text = "... and ${cacheFiles.size - 20} more files",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Actions
        DebugSection(title = "🔧 Actions") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        context.imageLoader.memoryCache?.clear()
                        scope.launch {
                            refreshTrigger++
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B6B)
                    )
                ) {
                    Text("Clear Memory")
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            context.imageLoader.diskCache?.clear()
                            refreshTrigger++
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B6B)
                    )
                ) {
                    Text("Clear Disk")
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            storePreferences.clearSelectedStore()
                            refreshTrigger++
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    )
                ) {
                    Text("Clear Store")
                }
                
                Button(
                    onClick = {
                        MemoryMonitor.logStatus(context)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF03DAC5)
                    )
                ) {
                    Text("Log to Logcat")
                }
            }
        }
    }
}

@Composable
private fun DebugSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2a2a3e)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun getCachedFiles(context: Context): List<FileInfo> {
    val cacheDir = context.cacheDir.resolve("ff_tothem_image_cache")
    if (!cacheDir.exists()) return emptyList()
    
    return cacheDir.walkTopDown()
        .filter { it.isFile }
        .map { FileInfo(it.name, it.length()) }
        .sortedByDescending { it.size }
        .toList()
}

data class FileInfo(
    val name: String,
    val size: Long
)

