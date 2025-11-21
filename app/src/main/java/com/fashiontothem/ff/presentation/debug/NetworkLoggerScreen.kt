package com.fashiontothem.ff.presentation.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import com.fashiontothem.ff.ui.theme.Fonts
import com.fashiontothem.ff.util.NetworkLogEntry
import com.fashiontothem.ff.util.NetworkLoggerManager
import com.fashiontothem.ff.util.rememberDebouncedClick
import kotlinx.coroutines.delay

/**
 * Network Logger Screen for QA to inspect network requests
 */
@Composable
fun NetworkLoggerScreen(
    networkLoggerManager: NetworkLoggerManager,
    onBack: () -> Unit = {}
) {
    val logs = remember { mutableStateOf(networkLoggerManager.logs) }
    var selectedLogIndex by remember { mutableStateOf<Int?>(null) }
    
    // Refresh logs periodically
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            logs.value = networkLoggerManager.logs
        }
    }
    
    val debouncedBack = rememberDebouncedClick(onClick = onBack)
    val debouncedClear = rememberDebouncedClick(onClick = {
        networkLoggerManager.clearLogs()
        logs.value = networkLoggerManager.logs
    })
    
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1A))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2D2D2D))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = debouncedBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Network Logger",
                        fontFamily = Fonts.Poppins,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Row {
                    IconButton(onClick = debouncedClear) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear",
                            tint = Color.White
                        )
                    }
                }
            }
            
            // Stats bar
            val totalLogs = logs.value.size
            val successLogs = logs.value.count { it.isSuccess }
            val errorLogs = logs.value.count { it.isError }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF252525))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Total", totalLogs.toString(), Color.White)
                StatItem("Success", successLogs.toString(), Color(0xFF4CAF50))
                StatItem("Errors", errorLogs.toString(), Color(0xFFF44336))
            }
            
            // Logs list
            if (logs.value.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No network requests yet",
                        fontFamily = Fonts.Poppins,
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = rememberLazyListState(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(logs.value.reversed()) { index, log ->
                        NetworkLogItem(
                            log = log,
                            isExpanded = selectedLogIndex == index,
                            onClick = {
                                selectedLogIndex = if (selectedLogIndex == index) null else index
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = Fonts.Poppins,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontFamily = Fonts.Poppins,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun NetworkLogItem(
    log: NetworkLogEntry,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val statusColor = when {
        log.isError -> Color(0xFFF44336)
        log.isSuccess -> Color(0xFF4CAF50)
        else -> Color(0xFFFF9800)
    }
    
    val statusEmoji = when {
        log.statusCode == null -> "⏳"
        log.statusCode in 200..299 -> "✅"
        log.statusCode in 300..399 -> "⚠️"
        log.statusCode in 400..499 -> "❌"
        log.statusCode >= 500 -> "🔥"
        else -> "❓"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D2D)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = statusEmoji,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${log.method} ${getShortUrl(log.url)}",
                            fontFamily = Fonts.Poppins,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (log.statusCode != null) {
                            Text(
                                text = "${log.statusCode} ${log.statusMessage ?: ""} • ${log.responseTime}ms",
                                fontFamily = Fonts.Poppins,
                                fontSize = 12.sp,
                                color = statusColor
                            )
                        } else {
                            Text(
                                text = "Pending...",
                                fontFamily = Fonts.Poppins,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                Text(
                    text = log.timestamp,
                    fontFamily = Fonts.Poppins,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
            
            // Expanded details
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // URL
                DetailSection("URL", log.url)
                
                // Request Headers
                if (log.requestHeaders.isNotEmpty()) {
                    DetailSection(
                        "Request Headers",
                        log.requestHeaders.entries.joinToString("\n") { (key, values) ->
                            "$key: ${values.joinToString(", ")}"
                        }
                    )
                }
                
                // Request Body
                if (!log.requestBody.isNullOrBlank()) {
                    DetailSection("Request Body", log.requestBody)
                }
                
                // Response Headers
                if (log.responseHeaders.isNotEmpty()) {
                    DetailSection(
                        "Response Headers",
                        log.responseHeaders.entries.joinToString("\n") { (key, values) ->
                            "$key: ${values.joinToString(", ")}"
                        }
                    )
                }
                
                // Response Body
                if (!log.responseBody.isNullOrBlank()) {
                    DetailSection("Response Body", log.responseBody)
                }
                
                // Error
                if (!log.error.isNullOrBlank()) {
                    DetailSection("Error", log.error, Color(0xFFF44336))
                }
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: String, titleColor: Color = Color(0xFF4CAF50)) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            fontFamily = Fonts.Poppins,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            fontFamily = Fonts.Poppins,
            fontSize = 11.sp,
            color = Color(0xFFCCCCCC),
            modifier = Modifier
                .background(Color(0xFF1A1A1A), RoundedCornerShape(4.dp))
                .padding(8.dp)
        )
    }
}

private fun getShortUrl(url: String): String {
    return try {
        val uri = android.net.Uri.parse(url)
        val path = uri.path ?: ""
        if (path.length > 50) {
            "..." + path.takeLast(47)
        } else {
            path
        }
    } catch (e: Exception) {
        if (url.length > 50) {
            "..." + url.takeLast(47)
        } else {
            url
        }
    }
}

