# 🔍 Kako da pronađeš Data Streams

## Problem

Ne vidiš opciju "Data Streams" u dropdown meniju Analytics!

## Rešenje

**"Data Streams" nije u dropdown meniju!** 

Moram da klikneš na **glavni "Analytics"** naslov (ne na dropdown strelicu).

### 5 načina da pronađeš Data Streams:

### Način 1: Preko Analytics Dashboard
1. U "Project shortcuts" sekciji, klikni na **"Analytics Dashboard"** (ikona bar charta)
2. To otvara Analytics dashboard
3. U sekciji sa Data Streams (obično na vrhu ili sidebaru), pronađi "Data Streams"
4. Klikni na Android data stream

### Način 2: Preko strelice
1. Klikni na **strelicu pored "Analytics"** (^ strelica gore)
2. To otvara glavnu stranicu Analytics
3. Tamo vidiš "Data Streams"

### Način 3: Direktno URL
Idi na URL direktno:
```
https://console.firebase.google.com/project/fashion-and-friends-rs-sr-app/analytics
```

### Način 4: Preko "Latest Release"
1. Klikni na **"Latest Release"** u dropdown meniju
2. To vas vodi na Analytics homepage
3. Na homepage vidiš "Data Streams"

### Način 5: Preko "Dashboard"
1. Klikni **"Dashboard"**
2. To vodi na Analytics dashboard
3. Možeš da vidiš ili klikneš na "Data Streams"

---

## Šta ako ni tada ne vidiš Data Streams?

**Mogući razlozi:**
1. Proveri da li je **Google Analytics 4 (GA4)** uključen (ne Universal Analytics)
2. Proveri da li imaš **Analytics API omogućen** u Google Cloud Console
3. Proveri da li imaš **"Viewer" ili veće permisije** u Firebase projektu

---

## Alternativa: Korišćenje App ID

Ako i dalje ne možeš da pronađeš Measurement ID, možeš koristiti **App ID** iz Project settings!

U Project settings → Your apps → Android app → vidiš:
- **App ID**: `1:989719399560:android:4a7c32ee4ade1c6743e33d`

Ovo možemo koristiti umesto Measurement ID ako ti treba hitno.

---

## 🎯 NAŠO SAMI!

U dialogu "App stream details" što si upravo otvorio:

**Za API Secret:**
1. Scrolluj do sekcije **"Events"**
2. Klikni na **"Measurement Protocol API secrets"** (desna strelica)
3. Klikni **"Create"** da napraviš novi API secret
4. U dialogu "Create new API secret":
   - Unesi nickname: `Kiosk Philips Device`
   - Klikni **"Create"** (dugme će se aktivirati)
5. Kopiraj taj secret

**Za Measurement ID (G-XXXX):**
Measurement ID **NIGDE** u ovom dialogu!

**Zatvori dialog i vrati se na Data Streams listu:**
1. Klikni **"X"** da zatvoriš dialog
2. U listi Data Streams, levi klik na Android stream (ne desni)
3. Tamo vidiš kolonu sa **"Measurement ID"** ili **"Stream name"**
4. Measurement ID je u formatu `G-XXXXXXXXXX`

**Alternativa ako ni tamo ne vidiš:**
- Mozda se zove **"Google Analytics 4"** ili **"GA4"** property
- Idi u Google Analytics konzolu → Admin → Data Streams
- Tamo vidiš Measurement ID

---

## 🔍 Novi uputstvo

**Sada si na "Measurement Protocol API secrets" stranici.**

1. **Zatvori ovu stranicu** (X u gornjem levom uglu)
2. Vratićeš se na **listu Data Streams**
3. Klikni na **Android stream** ("Fashion&Friends") - **NE desni klik, samo levi klik**
4. To otvara detalje streama
5. **Measurement ID** treba da se vidi tamo - obično **na vrhu** ili **na dnu** stranice
6. Ako opet ne vidiš, pošalji mi screenshot šta vidiš!

---

## 💡 Napomena

Vidio sam u Web stream-u da **"MEASUREMENT ID"** se nalazi u sekciji **"Stream details"** (MEASUREMENT ID: G-9DXFRP1WF1).

**Za Android stream treba da bude ista situacija:**
- Zatvori Web stream modal (X)
- Klikni na **Android** stream ("Fashion&Friends")
- U sekciji **"Stream details"** vidiš **"MEASUREMENT ID"** (G-XXXXXXXXXX)
