# 🔐 OAuth1 Setup Instructions for F&F Tothem

## Opcija 1: Dobavljanje Credentials iz Firebase Realtime Database (BEZ Firebase SDK-a)

Pošto Philips kiosk ne podržava Firebase services, možeš da pozoveš Firebase Realtime Database **preko REST API-ja** bez Firebase SDK-a.

### Firebase REST API Pristup

```bash
# GET request na Firebase Realtime Database
curl "https://YOUR_PROJECT_ID.firebaseio.com/integration_token.json?auth=YOUR_DATABASE_SECRET"
```

### Implementacija u Kotlin:

```kotlin
// 1. Dodaj endpoint u ApiService.kt za Firebase:
@GET
suspend fun getFirebaseToken(
    @Url url: String
): Response<FirebaseTokenDto>

// 2. Pozovi pre prvog API poziva:
val firebaseUrl = "https://YOUR_PROJECT_ID.firebaseio.com/integration_token.json?auth=YOUR_SECRET"
val tokenResponse = apiService.getFirebaseToken(firebaseUrl)

// 3. Parsiraj JWT token i izvuci OAuth credentials:
val jwtToken = tokenResponse.body()?.token
val (consumerKey, consumerSecret, accessToken, tokenSecret) = parseJWT(jwtToken)
```

---

## Opcija 2: Hardcode u gradle.properties (ZA KIOSK - NAJBOLJE)

Pošto je ovo **dedicirani kiosk uređaj** koji je fizički kontrolisan, **najjednostavnije** i **dovoljno sigurno** je da staviš credentials u `gradle.properties`.

### 1. Otvori `gradle.properties` i dodaj:

```properties
# OAuth1 Credentials for Fashion & Friends API
OAUTH_CONSUMER_KEY=your_consumer_key_here
OAUTH_CONSUMER_SECRET=your_consumer_secret_here
OAUTH_ACCESS_TOKEN=your_access_token_here
OAUTH_TOKEN_SECRET=your_token_secret_here
```

### 2. Update `app/build.gradle`:

```gradle
android {
    // ...
    
    defaultConfig {
        // ...
        
        // Add BuildConfig fields from gradle.properties
        buildConfigField("String", "OAUTH_CONSUMER_KEY", "\"${project.findProperty('OAUTH_CONSUMER_KEY') ?: ''}\"")
        buildConfigField("String", "OAUTH_CONSUMER_SECRET", "\"${project.findProperty('OAUTH_CONSUMER_SECRET') ?: ''}\"")
        buildConfigField("String", "OAUTH_ACCESS_TOKEN", "\"${project.findProperty('OAUTH_ACCESS_TOKEN') ?: ''}\"")
        buildConfigField("String", "OAUTH_TOKEN_SECRET", "\"${project.findProperty('OAUTH_TOKEN_SECRET') ?: ''}\"")
    }
    
    buildFeatures {
        buildConfig = true  // Make sure this is enabled
    }
}
```

### 3. Update `NetworkModule.kt`:

```kotlin
@Provides
@Singleton
fun provideOAuth1Interceptor(): OAuth1Interceptor {
    return OAuth1Interceptor(
        consumerKey = humer.UvcCamera.BuildConfig.OAUTH_CONSUMER_KEY,
        consumerSecret = humer.UvcCamera.BuildConfig.OAUTH_CONSUMER_SECRET,
        accessToken = humer.UvcCamera.BuildConfig.OAUTH_ACCESS_TOKEN,
        tokenSecret = humer.UvcCamera.BuildConfig.OAUTH_TOKEN_SECRET
    )
}
```

---

## Opcija 3: Napravi Backend Proxy (NAJSIGURNIJE, ALI SLOŽENIJE)

Ako želiš maksimalnu sigurnost, napravi **svoj backend** koji će da radi OAuth1 autentikaciju.

### Backend (Node.js primer):

```javascript
// server.js
const express = require('express');
const axios = require('axios');
const OAuth = require('oauth-1.0a');

const app = express();

const oauth = OAuth({
  consumer: {
    key: process.env.CONSUMER_KEY,
    secret: process.env.CONSUMER_SECRET
  },
  signature_method: 'HMAC-SHA1',
  hash_function(base_string, key) {
    return crypto.createHmac('sha1', key).update(base_string).digest('base64');
  }
});

app.get('/api/store/storeConfigs', async (req, res) => {
  const request_data = {
    url: 'https://www.fashionandfriends.com/rest/V1/mobile/store/storeConfigs',
    method: 'GET'
  };
  
  const token = {
    key: process.env.ACCESS_TOKEN,
    secret: process.env.TOKEN_SECRET
  };
  
  const headers = oauth.toHeader(oauth.authorize(request_data, token));
  
  const response = await axios.get(request_data.url, { headers });
  res.json(response.data);
});

app.listen(3000);
```

### Android app poziva tvoj backend:

```kotlin
// ApiService.kt
@GET("store/storeConfigs")  // Calls your backend, not F&F directly
suspend fun getStoreConfigs(): Response<List<StoreConfigResponse>>
```

---

## Kako Dobiti OAuth Credentials?

### 1. Iz Firebase (ako već postoje):

```bash
# Pristup Firebase Realtime Database preko REST API-ja
# (bez potrebe za Firebase SDK-om)

curl "https://your-project.firebaseio.com/oauth_credentials.json?auth=YOUR_DATABASE_SECRET"
```

### 2. Od Fashion & Friends Backend Tima:

Kontaktiraj F&F backend tim i traži:
- Consumer Key
- Consumer Secret
- Access Token
- Token Secret

---

## 🎯 Preporuka za Kiosk Uređaj

**Za dedicirani kiosk uređaj, najbolje je Opcija 2 (gradle.properties):**

✅ **Pros:**
- Jednostavno za setup
- Nema dodatnih network poziva
- Dovoljno sigurno (uređaj je fizički kontrolisan)
- ProGuard obfuscation dodatno štiti credentials

❌ **Cons:**
- Credentials su u kodu (ali obfuscated)
- Treba rebuild za update credentials-a

---

## 🔒 Dodatna Sigurnost (ProGuard)

U `proguard-rules.pro` dodaj:

```proguard
# Obfuscate BuildConfig
-keep class humer.UvcCamera.BuildConfig { *; }
-keepclassmembers class humer.UvcCamera.BuildConfig {
    public static final java.lang.String OAUTH_*;
}
```

---

## ✅ Quick Start

1. Dobavi OAuth credentials (od F&F tima ili iz Firebase)
2. Dodaj ih u `gradle.properties`
3. Update `app/build.gradle` sa `buildConfigField`
4. Update `NetworkModule.kt` da koristi `BuildConfig`
5. Build i deploy na kiosk

**Done!** 🚀

