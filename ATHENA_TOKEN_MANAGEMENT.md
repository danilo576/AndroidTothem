# 🔍 Athena Search API - Token Management

## ✅ Šta Je Implementirano

Kompletan sistem za **automatsko upravljanje Athena Search API tokenima**.

---

## 🎯 Kako Radi

### **1. Pri Izboru Store-a (Prvi Put)**

```
User bira zemlju (e.g., Serbia)
  ↓
Save store selection (rs_SR, RS)
  ↓
Save Athena config (websiteUrl + wtoken)
  ↓
Fetch Athena access token
  ↓
Save token + expiration time
  ↓
Navigate to Camera Screen
```

### **2. Pri Pokretanju App-a (Store Već Izabran)**

```
App Start
  ↓
LoadingScreen
  ↓
Load selectedStoreCode from DataStore
  ↓
Call refreshStoreConfigAndInitAthena()
  ↓
Fetch fresh store configs from F&F API
  ↓
Update Athena config (websiteUrl + wtoken)
  ↓
Check if token expired
  ↓ YES
Fetch new Athena token
  ↓
Save token + expiration
  ↓
Show Camera Screen
```

### **3. Automatski Token Refresh**

```
API call sa Athena tokenом
  ↓
Token expired? (AthenaTokenManager checks)
  ↓ YES
Refresh token automatically
  ↓
Save new token + expiration
  ↓
Retry API call sa novim tokenom
```

---

## 📁 Implementirani Fajlovi

### **Data Layer:**

1. **AthenaTokenResponse.kt** - DTOs
   - `AthenaTokenResponse` (token_type, expires_in, access_token)
   - `AthenaTokenRequest` (client_id, client_secret, grant_type, scope)

2. **AthenaApiService.kt** - Retrofit interface
   ```kotlin
   POST oauth/token
   ```

3. **AthenaPreferences.kt** - DataStore
   - `accessToken: Flow<String?>`
   - `tokenExpiration: Flow<Long?>`
   - `websiteUrl: Flow<String?>`
   - `wtoken: Flow<String?>`
   - `saveToken(token, expiresIn)`
   - `saveAthenaConfig(url, wtoken)`
   - `isTokenExpiredOrExpiringSoon(): Boolean`

4. **AthenaTokenManager.kt** - Token lifecycle manager
   - `getValidToken()` - Automatski refresh ako je expired
   - `forceRefreshToken()` - Ručni refresh
   - `clearToken()` - Clear na logout
   - **Thread-safe** sa Mutex
   - **Auto-refresh** 5 minuta pre isteka

---

## 🔐 Credentials (gradle.properties)

```properties
ATHENA_CLIENT_ID=3
ATHENA_CLIENT_SECRET=LubJ9bepyCsYdQomadvQhpf8LFEIuQ7VUHgz3t05
```

**BuildConfig Fields:**
```gradle
buildConfigField("String", "ATHENA_CLIENT_ID", "...")
buildConfigField("String", "ATHENA_CLIENT_SECRET", "...")
```

---

## 📡 API Endpoints

### **1. Athena Token Endpoint:**
```
POST https://eu-1.athenasearch.cloud/oauth/token

Request Body:
{
    "client_id": "3",
    "client_secret": "LubJ9bepyCsYdQomadvQhpf8LFEIuQ7VUHgz3t05",
    "grant_type": "client_credentials",
    "scope": "*"
}

Response:
{
    "token_type": "Bearer",
    "expires_in": 3600,
    "access_token": "eyJ0eXAiOi..."
}
```

### **2. Store Config Refresh:**
```
GET https://www.fashionandfriends.com/rest/V1/mobile/store/storeConfigs
(OAuth1 authenticated)

Izvlači se za izabrani store:
- athena_search_website_url
- athena_search_wtoken
```

---

## 💾 DataStore Storage

### **Store Preferences:**
```
ff_tothem_store_prefs.preferences_pb
  - selected_store_code: "rs_SR"
  - selected_country_code: "RS"
```

### **Athena Preferences:**
```
ff_tothem_athena_prefs.preferences_pb
  - athena_access_token: "eyJ0eXAiOi..."
  - athena_token_expiration: 1760808078000 (timestamp)
  - athena_website_url: "https://eu-1.athenasearch.cloud/"
  - athena_wtoken: "ZvdrFLV66BGqHB8Ab0dU"
```

---

## 📊 Debug Screen Prikaz

### **🔍 Athena Search API:**
```
Website URL:   eu-1.athenasearch.cloud/
WToken:        ZvdrFLV66BGqHB8Ab0dU
Access Token:  eyJ0eXAiOiJKV1QiLCJh...
Expires In:    57min 32s ✅
```

---

## 🔧 Token Lifecycle

### **Automatic Refresh Logic:**

1. **getValidToken()** proverava:
   - Token postoji?
   - Token nije expired?
   - Token nije blizu expiriranju (<5 min)?

2. Ako neki uslov nije ispunjen → **auto-refresh**

3. **Thread-safe** sa Mutex (sprečava multiple simultane refreshove)

4. **Save to DataStore** nakon uspešnog refresha

---

## 🚀 Kako Koristiti (Za Sledeće Athena Servise)

### **Primer: Athena Search Request**

```kotlin
// 1. Inject AthenaTokenManager
@Inject lateinit var athenaTokenManager: AthenaTokenManager
@Inject lateinit var athenaApiService: AthenaApiService

// 2. Get valid token (automatski refresh ako treba)
val token = athenaTokenManager.getValidToken(athenaApiService)

// 3. Use token in your Athena API call
val searchResults = athenaApiService.search(
    authorization = "Bearer $token",
    query = "..."
)
```

### **Primer: Athena Interceptor (Za Sve Pozive)**

Kreiraćemo AthenaAuthInterceptor koji automatski dodaje token:

```kotlin
class AthenaAuthInterceptor @Inject constructor(
    private val athenaPreferences: AthenaPreferences,
    private val athenaTokenManager: AthenaTokenManager,
    private val athenaApiService: AthenaApiService
) : Interceptor {
    override fun intercept(chain: Chain): Response {
        val token = runBlocking {
            athenaTokenManager.getValidToken(athenaApiService)
        }
        
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(request)
    }
}
```

---

## 📈 Flow Dijagram

```
┌───────────────────────────────────────┐
│ App Start                             │
├───────────────────────────────────────┤
│ Store selected?                       │
│   ├─ NO  → StoreSelectionScreen       │
│   │         ↓                          │
│   │       User selects Serbia          │
│   │         ↓                          │
│   │       Save: RS, rs_SR              │
│   │         ↓                          │
│   │       Save Athena config           │
│   │         ↓                          │
│   │       Fetch Athena token           │
│   │         ↓                          │
│   │       CameraScreen                 │
│   │                                    │
│   └─ YES → Refresh store config        │
│             ↓                          │
│           Update Athena config         │
│             ↓                          │
│           Check token expiration       │
│             ↓                          │
│           Refresh if needed            │
│             ↓                          │
│           CameraScreen                 │
└───────────────────────────────────────┘
```

---

## ✅ Build Status

```
BUILD SUCCESSFUL in 8s
49 actionable tasks: 20 executed, 29 up-to-date
```

---

## 🎯 Rezultat

- ✅ **Athena token** se automatski fetchuje pri izboru store-a
- ✅ **Token se refresh-uje** svaki put pri app start-u
- ✅ **Auto-refresh** 5 min pre isteka
- ✅ **Thread-safe** token management
- ✅ **Debug screen** prikazuje Athena status
- ✅ **Store config** se osvežava svaki put

**Ready for Athena Search servisi!** 🔍🚀

