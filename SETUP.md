# F&F Camera - HTTP & Image Loading Setup
## ⚡ Optimizovano za Philips 32BDL3751E
**4GB RAM | Mali-G52 GPU | 32GB Storage**

## ✅ Šta Je Instalirano

### 🔌 HTTP Client (Retrofit + OkHttp)
- **Retrofit 2.9.0** - REST API pozivi
- **OkHttp 4.12.0** - HTTP engine sa logging-om
- **Moshi 1.15.0** - JSON parser

### 🖼️ Image Loading (Coil) - Optimizovano za 4GB RAM & Mali-G52
- **Coil 2.5.0** - Učitavanje i keširanje slika
  - **Memory cache: 20% RAM (~800MB)** - dovoljno za 200+ slika
  - **Disk cache: 100MB** - offline-first capability
  - **Hardware bitmaps: Enabled** - Mali-G52 GPU ubrzanje
  - **Crossfade animations: Enabled** ✨ - smooth UX
  - **Auto-retry: Enabled** - stabilnost

### 💉 Dependency Injection (Hilt)
- **Hilt 2.50** - Automatsko ubrizgavanje zavisnosti

### ⚡ Async
- **Kotlin Coroutines** - Za async operacije

---

## 📁 Struktura

```
app/src/main/java/com/fashiontothem/ff/
├── FFApplication.kt          # Hilt Application + Coil config
├── data/remote/
│   └── ApiService.kt         # API endpoints (prazno - dodaj svoje)
└── di/
    └── NetworkModule.kt      # Retrofit + OkHttp config
```

---

## 🚀 Kako Koristiti

### 1. **HTTP Request**

**Dodaj endpoint u `ApiService.kt`:**
```kotlin
interface ApiService {
    
    @GET("products")
    suspend fun getProducts(): Response<List<ProductDto>>
    
    @POST("upload")
    suspend fun uploadImage(@Body data: ImageData): Response<UploadResponse>
}
```

**Koristi u kodu:**
```kotlin
@Inject lateinit var apiService: ApiService

suspend fun fetchData() {
    val response = apiService.getProducts()
    if (response.isSuccessful) {
        val products = response.body()
        // Use data
    }
}
```

### 2. **Učitaj Sliku sa Coil**

```kotlin
AsyncImage(
    model = "https://example.com/image.jpg",
    contentDescription = "Description",
    modifier = Modifier.size(200.dp)
)
```

**Sa placeholder-om:**
```kotlin
AsyncImage(
    model = imageUrl,
    contentDescription = null,
    placeholder = painterResource(R.drawable.placeholder),
    error = painterResource(R.drawable.error),
    contentScale = ContentScale.Crop
)
```

### 3. **Promeni API URL**

**`di/NetworkModule.kt`:**
```kotlin
private const val BASE_URL = "https://tvoj-api.com/"
```

### 4. **Dodaj Headers (npr. Auth Token)**

**`di/NetworkModule.kt`:**
```kotlin
fun provideOkHttpClient(
    loggingInterceptor: HttpLoggingInterceptor
): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer YOUR_TOKEN")
            .build()
        chain.proceed(request)
    }
    .addInterceptor(loggingInterceptor)
    .connectTimeout(30, TimeUnit.SECONDS)
    .build()
```

---

## 📝 HTTP Logging

Svi HTTP zahtevi se **automatski loguju** u Logcat:

```
→ REQUEST
GET https://api.example.com/products
Headers: [...]

← RESPONSE 200 OK (245ms)
Body: {"data": [...]}
```

Filter u Logcat-u: `OkHttp`

---

## 💡 Primeri

### Primer 1: GET Request

```kotlin
// 1. Kreiraj DTO
@JsonClass(generateAdapter = true)
data class Product(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String
)

// 2. Dodaj u ApiService
@GET("products")
suspend fun getProducts(): Response<List<Product>>

// 3. Koristi
val response = apiService.getProducts()
if (response.isSuccessful) {
    val products = response.body() ?: emptyList()
}
```

### Primer 2: POST Request

```kotlin
// 1. DTO
@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

// 2. ApiService
@POST("auth/login")
suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

// 3. Koristi
val response = apiService.login(LoginRequest("user@email.com", "pass"))
```

---

## ✅ Build Status

```
✅ BUILD SUCCESSFUL
✅ Retrofit HTTP client konfigurisan
✅ OkHttp logging omogućen
✅ Coil image loader spreman
✅ Hilt dependency injection aktivan
✅ Kamera aplikacija radi
```

---

## 🎯 Sledeći Koraci

Kada budeš spreman da dodaš API funkcionalnost:

1. **Promeni BASE_URL** u `NetworkModule.kt`
2. **Dodaj svoje endpoint-e** u `ApiService.kt`
3. **Kreiraj DTO klase** za API responses
4. **Inject ApiService** gde ti treba sa `@Inject`
5. **Pozovi API** iz Coroutine scope-a

Sve je spremno! 🚀

