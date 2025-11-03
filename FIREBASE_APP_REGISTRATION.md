# ✅ Android App za Firebase Analytics

## Rešenje

Promenio sam `applicationId` u `build.gradle` na:
- `com.fashioncompany.FandF` ✅

**Sada aplikacija koristi isti package kao što je već registrovana u Firebase!**

---

## Kako pronaći Measurement ID?

### Koraci:

1. U Firebase Console, **klikni na Android aplikaciju** `com.fashioncompany.FandF` (plavo oznacena)

2. Idi na **"Analytics"** u levoj navigaciji

3. Klikni **"Data Streams"**

4. Klikni na stream za Android aplikaciju

5. Vidiš **Measurement ID** (format `G-XXXXXXXXXX`)

6. Skroluj dole do **"Measurement Protocol API secrets"**

7. Klikni **"Create"** da napraviš novi secret

8. Kopiraj taj secret

---

## Napomena

Sada kiosk aplikacija (Philips) koristi isti Firebase nalog kao mobilna aplikacija `com.fashioncompany.FandF`. 
Analytics događaji će se pratiti pod istim Analytics nalogom.
