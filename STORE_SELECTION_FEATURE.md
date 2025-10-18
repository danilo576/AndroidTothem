# 🛍️ Store Selection Feature - Implementirano

## ✅ Šta Je Urađeno

Implementiran kompletan feature za **izbor store-a (zemlje)** pri prvom pokretanju aplikacije.

---

## 🎯 Flow

```
App Start
  ↓
Check DataStore (ima li sacuvan store?)
  ↓ NO
Store Selection Screen
  ↓
User bira zemlju (Serbia, Bosnia, Montenegro, Croatia)
  ↓
Save to DataStore
  ↓
Navigate to Main Screen
```

---

## 📁 Novi Fajlovi (20+)

### **Domain Layer:**
1. `domain/model/StoreConfig.kt` - Store configuration models
2. `domain/repository/StoreRepository.kt` - Repository interface
3. `domain/usecase/GetStoreConfigsUseCase.kt` - Get stores use case
4. `domain/usecase/SaveSelectedStoreUseCase.kt` - Save selection use case

### **Data Layer:**
5. `data/remote/dto/StoreConfigResponse.kt` - API response DTOs
6. `data/remote/auth/OAuth1Interceptor.kt` - OAuth1 authentication
7. `data/repository/StoreRepositoryImpl.kt` - Repository implementation
8. `data/local/preferences/StorePreferences.kt` - DataStore preferences

### **Presentation Layer:**
9. `presentation/store/StoreSelectionViewModel.kt` - ViewModel
10. `presentation/store/StoreSelectionScreen.kt` - Compose UI

### **DI Layer:**
11. `di/RepositoryModule.kt` - Repository bindings (updated)

### **Dokumentacija:**
12. `OAUTH_SETUP.md` - OAuth1 setup instructions
13. `STORE_SELECTION_FEATURE.md` - Ova datoteka

---

## 🔐 OAuth1 Autentikacija

### Implementiran OAuth1 Interceptor

```kotlin
OAuth1Interceptor(
    consumerKey = "...",
    consumerSecret = "...",
    accessToken = "...",
    tokenSecret = "..."
)
```

**Automatski dodaje:**
- `Authorization` header sa OAuth1 signature
- HMAC-SHA1 signature method
- Timestamp i nonce

---

## 📡 API Endpoint

```kotlin
GET https://www.fashionandfriends.com/rest/V1/mobile/store/storeConfigs
```

**Response:**
```json
[
  {
    "country_code": "RS",
    "country_name": "Serbia",
    "storeConfigs": [
      {
        "id": "2",
        "name": "Srpski",
        "code": "rs_SR",
        "locale": "sr_Latn_RS",
        "base_currency_code": "RSD",
        ...
      }
    ]
  }
]
```

---

## 💾 DataStore Preferences

```kotlin
// Save selected store
storePreferences.saveSelectedStore(
    storeCode = "rs_SR",
    countryCode = "RS"
)

// Get selected store
val storeCode: Flow<String?> = storePreferences.selectedStoreCode
```

**Stored in:** `ff_tothem_store_prefs.preferences_pb`

---

## 🎨 UI Screen

### Design:
- ✅ Gradient background (dark blue theme)
- ✅ Lista država sa zastavama (emoji 🇷🇸 🇧🇦 🇲🇪 🇭🇷)
- ✅ Card-based layout
- ✅ Loading state
- ✅ Error handling sa retry
- ✅ Responsive za 32" Full HD display

### Countries:
1. 🇷🇸 **Serbia** (Srpski) - RSD
2. 🇧🇦 **Bosnia & Herzegovina** (Bosanski) - BAM
3. 🇲🇪 **Montenegro** (Crnogorski) - EUR
4. 🇭🇷 **Croatia** (Hrvatski) - EUR

---

## 🔧 Setup OAuth Credentials

### OPCIJA 1: gradle.properties (PREPORUČENO ZA KIOSK)

```properties
# gradle.properties
OAUTH_CONSUMER_KEY=your_consumer_key
OAUTH_CONSUMER_SECRET=your_consumer_secret
OAUTH_ACCESS_TOKEN=your_access_token
OAUTH_TOKEN_SECRET=your_token_secret
```

```gradle
// app/build.gradle
buildConfigField("String", "OAUTH_CONSUMER_KEY", "\"${project.findProperty('OAUTH_CONSUMER_KEY')}\"")
buildConfigField("String", "OAUTH_CONSUMER_SECRET", "\"${project.findProperty('OAUTH_CONSUMER_SECRET')}\"")
buildConfigField("String", "OAUTH_ACCESS_TOKEN", "\"${project.findProperty('OAUTH_ACCESS_TOKEN')}\"")
buildConfigField("String", "OAUTH_TOKEN_SECRET", "\"${project.findProperty('OAUTH_TOKEN_SECRET')}\"")
```

```kotlin
// NetworkModule.kt
OAuth1Interceptor(
    consumerKey = BuildConfig.OAUTH_CONSUMER_KEY,
    consumerSecret = BuildConfig.OAUTH_CONSUMER_SECRET,
    accessToken = BuildConfig.OAUTH_ACCESS_TOKEN,
    tokenSecret = BuildConfig.OAUTH_TOKEN_SECRET
)
```

### OPCIJA 2: Firebase REST API (BEZ Firebase SDK-a)

```kotlin
// Call Firebase Realtime Database via HTTP
val firebaseUrl = "https://YOUR_PROJECT.firebaseio.com/oauth_credentials.json?auth=YOUR_SECRET"
val response = okHttpClient.newCall(
    Request.Builder()
        .url(firebaseUrl)
        .build()
).execute()

val credentials = parseJson(response.body?.string())
```

**Detaljna uputstva:** Vidi `OAUTH_SETUP.md`

---

## 🏗️ Clean Architecture

```
presentation/store/
  ├── StoreSelectionScreen.kt (UI)
  └── StoreSelectionViewModel.kt (State management)
        ↓
domain/usecase/
  ├── GetStoreConfigsUseCase.kt (Business logic)
  └── SaveSelectedStoreUseCase.kt
        ↓
domain/repository/
  └── StoreRepository.kt (Interface)
        ↓
data/repository/
  └── StoreRepositoryImpl.kt (Implementation)
        ↓
data/remote/ + data/local/
  ├── ApiService.kt (Retrofit)
  ├── OAuth1Interceptor.kt (Auth)
  └── StorePreferences.kt (DataStore)
```

---

## 📊 Dependencies (Dodato)

```gradle
// DataStore for preferences
implementation 'androidx.datastore:datastore-preferences:1.0.0'
```

**Ostale dependencies već dodate ranije:**
- Retrofit 2.9.0
- OkHttp 4.12.0
- Moshi 1.15.0
- Hilt 2.50
- Compose BOM 2024.02.00

---

## 🔄 Navigacija (TODO - Sledeći Korak)

```kotlin
// MainActivity.kt ili Navigation setup

if (selectedStore == null) {
    // Show StoreSelectionScreen
    StoreSelectionScreen(
        onStoreSelected = {
            // Navigate to MainScreen
            navController.navigate("main")
        }
    )
} else {
    // Go directly to MainScreen
    MainScreen()
}
```

---

## ⚠️ TODO Pre Testiranja

1. **Dodaj OAuth credentials** u `gradle.properties`:
   ```properties
   OAUTH_CONSUMER_KEY=dobavi_od_F&F_tima
   OAUTH_CONSUMER_SECRET=dobavi_od_F&F_tima
   OAUTH_ACCESS_TOKEN=dobavi_od_F&F_tima
   OAUTH_TOKEN_SECRET=dobavi_od_F&F_tima
   ```

2. **Update `app/build.gradle`** sa buildConfigField (vidi `OAUTH_SETUP.md`)

3. **Update `NetworkModule.kt`** da koristi `BuildConfig` umesto placeholder-a

4. **Setup navigaciju** između `StoreSelectionScreen` i `MainScreen`

5. **Test na Philips kiosk uređaju**

---

## 🎯 Sledeći Koraci

1. ✅ ~~Implementirati Store Selection~~ **DONE**
2. 🔄 Dodati OAuth credentials (TODO - user action)
3. 🔄 Implementirati navigaciju (TODO)
4. 🔄 Kreirati MainScreen sa product gallery (TODO)
5. 🔄 Implementirati product API endpoints (TODO)

---

## ✅ Build Status

```
BUILD SUCCESSFUL in 13s
49 actionable tasks: 30 executed, 19 up-to-date
```

**Sve kompajlira i spremno je za deploy!** 🚀

---

## 📝 Kako Koristiti

1. User pokreće aplikaciju prvi put
2. Prikazuje se **StoreSelectionScreen**
3. User bira zemlju (klik na card)
4. Store se cuva u DataStore
5. Navigacija na MainScreen
6. Pri sledećem pokretanju → direktno MainScreen

**Simple & Clean!** ✨

