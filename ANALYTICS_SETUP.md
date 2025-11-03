# 📊 Firebase Analytics Setup for Philips Kiosk (No Google Play Services)

## Overview

This implementation uses **Firebase Measurement Protocol REST API** to track analytics **without requiring Google Play Services**. Perfect for Philips kiosk devices.

---

## 🔧 Setup Instructions

### 1. Get Firebase Credentials

You need two values from your Firebase console:

1. **Firebase Project ID**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Select your project
   - Go to **Project Settings** → **General**
   - Copy **Project ID** (e.g., `fashion-friends-kiosk-12345`)

2. **Measurement Protocol API Secret**
   - In Firebase Console, go to **Project Settings** → **Data Streams**
   - Select your Android app or create one
   - Expand **Measurement Protocol API secrets**
   - Click **Create credential** and copy the secret

### 2. Add Android App to Firebase (if not already added)

**Important:** You need to register your Android app in Firebase first!

1. Go to **Project settings** → **Your apps** section
2. If you don't see an Android app with package name `com.fashiontothem.ff`:
   - Click **"Add app"** → Select **"Android"** icon
   - Android package name: `com.fashiontothem.ff`
   - App nickname (optional): Fashion & Friends Kiosk
   - Click **"Register app"**
   - **Skip** downloading `google-services.json` (we don't need it for Measurement Protocol)

### 3. Get Measurement ID and API Secret

1. Go to **Firebase Console** → **Analytics** → **Data Streams**
2. Find your Android data stream (with `com.fashiontothem.ff`)
3. Click on it to open details
4. Copy the **Measurement ID** (format: `G-XXXXXXXXXX`)
5. Scroll down to **Measurement Protocol API secrets**
6. Click **"Create"** to generate a new secret
7. Copy the secret value

### 4. Update Configuration

Edit `app/src/main/java/com/fashiontothem/ff/data/repository/AnalyticsRepositoryImpl.kt`:

```kotlin
private const val MEASUREMENT_ID = "G-XXXXXXXXXX" // Replace with your GA4 Measurement ID
private const val API_SECRET = "YOUR_SECRET_HERE" // Replace with your API secret
```

---

## 📱 How It Works

### ✅ Features

1. **Offline Support**: Events are queued locally when offline, sent when online
2. **No Google Play Services**: Uses REST API directly
3. **Automatic Retry**: Failed events are retried when network available
4. **Persistent Storage**: Events saved in DataStore (max 100 events)
5. **Network Monitoring**: Automatically flushes queue when internet connects

### 🔄 Event Flow

```
User Action → logEvent() → Check Network
                            ↓
                    [Online]  [Offline]
                    ↓            ↓
              Send to Firebase  Queue in DataStore
              ↓            ↓
              Success    Network Available
                            ↓
                        Flush Queue
```

---

## 🚀 Usage

### Basic Usage

Inject `AnalyticsRepository` in your ViewModel:

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val analytics: AnalyticsRepository
) : ViewModel() {
    
    fun onCameraButtonClick() {
        viewModelScope.launch {
            analytics.logEvent(
                AnalyticsEvent(
                    name = AnalyticsEvents.CAMERA_OPENED
                )
            )
        }
    }
}
```

### Using Helper Utility

Inject the `Analytics` class in your ViewModel:

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val analytics: Analytics,
    // ... other dependencies
) : ViewModel() {
    
    fun onScreenLoaded() {
        viewModelScope.launch {
            analytics.trackScreenView("HomeScreen")
        }
    }
}

// Track product click
analytics.trackProductClick("prod123", "Nike Shoes")

// Track visual search
analytics.trackVisualSearchStart()

// Track filters
analytics.trackFilterApplied("brand", "Nike")
```

### Custom Events

```kotlin
analytics.trackCustomEvent(
    eventName = "custom_event_name",
    parameters = mapOf(
        "key1" to "value1",
        "key2" to 42,
        "key3" to true
    )
)
```

Or directly use the repository:

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {
    
    fun trackEvent() {
        viewModelScope.launch {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    name = "custom_event",
                    parameters = mapOf("key" to "value")
                )
            )
        }
    }
}
```

---

## 📋 Available Event Types

### Screen Navigation
- `SCREEN_VIEW` - Screen viewed
- `PAGE_VIEW` - Page viewed

### Visual Search
- `VISUAL_SEARCH_START` - Search started
- `VISUAL_SEARCH_RESULTS` - Results displayed
- `VISUAL_SEARCH_ERROR` - Search error

### Products
- `PRODUCT_LIST_VIEW` - Product list viewed
- `PRODUCT_CLICK` - Product clicked
- `PRODUCT_FAVORITE` - Product favorited
- `PRODUCT_SHARE` - Product shared

### Filters
- `FILTER_APPLIED` - Filter applied
- `FILTER_CLEAR` - Filters cleared
- `FILTER_CATEGORY` - Category filter
- `FILTER_BRAND` - Brand filter
- `FILTER_SIZE` - Size filter
- `FILTER_COLOR` - Color filter

### Store & Location
- `STORE_SELECTED` - Store selected
- `LOCATION_SELECTED` - Location selected

### Camera
- `CAMERA_OPENED` - Camera opened
- `CAMERA_PHOTO_CAPTURED` - Photo captured

### Navigation
- `NAVIGATION_BACK` - Back button clicked
- `NAVIGATION_HOME` - Home button clicked
- `NAVIGATION_LOGO_CLICK` - Logo clicked

---

## 🧪 Testing

### 1. Check Logs

Watch for these logs in Logcat:

```
D/Analytics: Event sent: screen_view
D/Analytics: Network connected - flushing event queue
D/Analytics: Flushing 5 queued events
D/Analytics: Event queue cleared
```

### 2. Verify in Firebase Console

1. Go to Firebase Console → **Analytics** → **Events**
2. Wait 24-48 hours for data to appear
3. Or use **DebugView** for real-time testing

### 3. Debug Mode (Optional)

Add debug logging in `AnalyticsRepositoryImpl.kt`:

```kotlin
override suspend fun logEvent(event: AnalyticsEvent) {
    Log.d(TAG, "Logging event: ${event.name} with params: ${event.parameters}")
    // ... rest of implementation
}
```

---

## ⚠️ Important Notes

### 1. Event Limits

- **Max 100 queued events** (prevents storage bloat)
- **Oldest events dropped** if queue exceeds 100
- **Firebase free tier**: 1M events/month

### 2. Network Requirements

- Events are queued offline but won't be lost
- Automatic flush when network reconnects
- No battery drain from constant polling

### 3. Privacy & GDPR

- No personal data collected by default
- User ID is anonymous (installation-based)
- Can be customized per your privacy policy

---

## 🐛 Troubleshooting

### Events Not Appearing in Firebase

1. **Check API Secret**: Ensure `FIREBASE_API_SECRET` is correct
2. **Wait 24-48 hours**: Firebase data can be delayed
3. **Check Logs**: Look for error messages in Logcat
4. **Verify Network**: Ensure device has internet connection

### Too Many Queued Events

- Check if network is consistently offline
- Increase `maxQueueSize` in `AnalyticsPreferences.kt` if needed
- Consider batch sending instead of individual events

### Performance Issues

- Analytics operations run on background thread (`Dispatchers.IO`)
- No blocking of UI thread
- Minimal memory footprint

---

## 📚 Additional Resources

- [Firebase Measurement Protocol](https://developers.google.com/analytics/devguides/collection/protocol/ga4)
- [Firebase Console](https://console.firebase.google.com/)
- [GA4 Event Reference](https://developers.google.com/analytics/devguides/collection/ga4/reference/events)

---

## ✅ Next Steps

1. **Add Android app** to Firebase if not already added (package: `com.fashiontothem.ff`)
2. **Get Firebase credentials** (Measurement ID + API Secret) from Data Streams
3. **Update** `AnalyticsRepositoryImpl.kt` with your credentials
4. **Build and test** locally
5. **Deploy** to Philips kiosk
6. **Monitor events** in Firebase Console

**Need help?** Contact support or check Firebase documentation.

---

## 🆘 Troubleshooting: Can't Find Measurement ID?

**If you don't see "Data Streams" in Firebase Analytics:**

1. Make sure you have **Google Analytics 4 (GA4)** enabled (not Universal Analytics)
2. Go to **Project settings** → Check if Android app is registered
3. If not registered:
   - Go to **Project settings** → **Your apps** → **Add app** → **Android**
   - Package name: `com.fashiontothem.ff`
   - Complete the registration
   - Then go to **Analytics** → **Data Streams** to see your new stream
