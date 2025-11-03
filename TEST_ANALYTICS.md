# 🧪 Test Analytics Events

## Kako da testiraš da se eventi šalju

### 1. Instaliraj APK na Philips uređaj

```bash
# Primeni installi APK preko ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 2. Pokreni aplikaciju

```bash
# Pokreni app
adb shell am start -n com.fashioncompany.FandF/humer.UvcCamera.MainActivity
```

### 3. Gledaj logove u realnom vremenu

```bash
# Otvori logcat i filteruj po "Analytics"
adb logcat | grep -i "Analytics"
```

### 4. Šta da vidiš u logovima

**Kad aplikacija startuje, treba da vidiš:**

```
D/Analytics: ===========================================
D/Analytics: 📊 Analytics Event Triggered
D/Analytics: Event Name: screen_view
D/Analytics: Event Parameters: {screen_name=HomeScreen}
D/Analytics: Network Status: ✅ ONLINE
D/Analytics: Sending event to Firebase immediately...
D/Analytics: 📡 Sending to Firebase:
D/Analytics: URL: https://www.google-analytics.com/mp/collect
D/Analytics: Measurement ID: G-G2EHX0QNV4
D/Analytics: User ID: <generated-uuid>
D/Analytics: Request Body: {"client_id":"...","user_id":"...","events":[...]}
D/Analytics: Response Code: 204
D/Analytics: Response Message: No Content
D/Analytics: ✅ SUCCESS: Event sent to Firebase: screen_view
D/Analytics: ===========================================
```

### 5. Ako vidiš greške

**Response Code: 400**
- Pogrešan Measurement ID ili API Secret
- Proveri u `AnalyticsRepositoryImpl.kt`

**Response Code: 403**
- API Secret nije validan ili je istekao
- Kreiraj novi API Secret u Firebase Console

**Response Code: 404**
- Measurement ID nije validan
- Proveri u `AnalyticsRepositoryImpl.kt`

**Exception: Network is offline**
- Event će biti queue-ovan i poslat kad se network vrati

### 6. Gde da vidiš podatke u Firebase

**Realtime (za 5-10 minuta):**
1. Firebase Console → Analytics
2. Realtime Analytics
3. Vidiš aktivne evente

**Historical Data (za 24-48 sati):**
1. Firebase Console → Analytics
2. Events
3. Vidiš sve evente po vremenu

---

## Test Event koji se šalje automatski

Kad otvoriš HomeScreen, automatski se šalje:
- **Event Name:** `screen_view`
- **Parameter:** `screen_name` = `"HomeScreen"`

---

## Dodavanje custom events

U bilo kom ViewModel-u:

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val analytics: Analytics
) : ViewModel() {
    
    fun onButtonClick() {
        viewModelScope.launch {
            analytics.trackCustomEvent(
                eventName = "custom_button_click",
                parameters = mapOf("button_name" to "test_button")
            )
        }
    }
}
```

---

## Debugging Tips

1. **Filter u logcat-u:** `adb logcat | grep Analytics`
2. **Save logs:** `adb logcat | grep Analytics > analytics.log`
3. **Clear logs:** `adb logcat -c`

---

## Fire Firebase DebugView (hitno testiranje)

Ako želiš da vidiš evente **TAKED**, možeš koristiti Firebase DebugView:

1. Firebase Console → Analytics → DebugView
2. Dodaj uređaj preko ADB:
   ```bash
   adb shell setprop debug.firebase.analytics.app com.fashioncompany.FandF
   ```
3. Restartuj aplikaciju
4. Vidiš evente u **realnom vremenu** (bez čekanja 24h)

---

## Česti problemi

### Problem: Ne vidim ništa u logcat-u

**Rešenje:** Proveri da li je `android.util.Log` importovan

### Problem: Event se queue-uje ali nikad ne šalje

**Rešenje:** Proveri network monitoring - možda nije aktiviran

### Problem: Response Code 204 ali ne vidim u Firebase-u

**Rešenje:** Normalno! Firebase Analytics ima delay. Sacekaj 5-10 minuta za Realtime, 24-48h za historical data

---

## Test Komande

```bash
# 1. Build i install
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk

# 2. Start app
adb shell am start -n com.fashioncompany.FandF/humer.UvcCamera.MainActivity

# 3. Watch logs
adb logcat | grep Analytics

# 4. Clear logs i pokreni ponovo
adb logcat -c && adb logcat | grep Analytics
```

---

**Svako pitanje?** Pošalji logove i pomoći ću! 🚀
