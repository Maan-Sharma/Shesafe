# She – Women Safety App 🚺

**She** is a smart **Women Safety Android Application** built using **Kotlin**. The app helps women quickly send emergency alerts, share their live location, and trigger safety actions using voice commands or automatic fall detection.

The goal of this project is to provide **fast and reliable emergency assistance** using smartphone technology.

---

# Tagline

**She – Safety in Every Step.**

---

# The Problem It Solves

Women often face unsafe situations while traveling alone, commuting at night, or being in unfamiliar places. In emergencies, it may not always be possible to unlock the phone or manually call for help.

The **She app** helps solve this problem by providing **instant emergency features** that can quickly notify trusted contacts and share the user's location.

This ensures that help can be requested **even when the user cannot interact with the phone normally**.

---

# Features ✨

### 🚨 SOS Emergency Alert

Send instant emergency alerts to saved contacts with a single tap.

### 📍 Live Location Sharing

Automatically sends the user's location to emergency contacts.

### 🎤 Voice Command Activation

The app can recognize commands like **“Help me”** and trigger emergency actions even if the phone is locked.

### 📳 Fall Detection

Using the phone’s accelerometer sensor, the app detects sudden falls and starts a **10-second countdown**. If the user does not cancel, an emergency alert is sent automatically.

### 📞 Quick Emergency Calling

Allows users to immediately call trusted contacts during emergencies.

### 💬 Automatic SMS Alerts

Sends SMS messages with location information to emergency contacts.

---

# Tech Stack 🛠

* **Kotlin**
* **Android SDK**
* **Firebase (for storing emergency contacts)**
* **Speech Recognition API**
* **Sensor Manager (Gyroscope sensor)**
* **Location Services**

---

# How It Works ⚙️

1. User registers emergency contacts.
2. The app continuously monitors **voice commands and sensor data**.
3. If the user says **“Help me”** or a **fall is detected**, the app triggers an emergency protocol.
4. The app sends:

   * SMS alert
   * Live location
   * Emergency call option

This ensures that the user can get help **quickly and automatically**.

---

# Challenges I Ran Into

### Background Voice Detection

Android restricts microphone access in background apps.

**Solution:**
Implemented a **foreground service with speech recognition** to allow emergency voice detection while respecting Android permissions.

### Accurate Fall Detection

Normal phone movement could trigger false fall alerts.

**Solution:**
Used **accelerometer thresholds with a countdown confirmation** so users can cancel false alerts.

### Location Fetching Delay

Sometimes GPS takes time to get accurate coordinates.

**Solution:**
Optimized location requests to use the **last known location quickly before sending alerts**.

---

# Open Innovation Impact 🌍

This project fits into the **Open Innovation track** by solving a **real-world social problem** using scalable technology.

The project encourages collaboration and improvement by allowing developers to extend the system with features like:

* AI-based danger detection
* Smartwatch integration
* Real-time safety maps
* Integration with public emergency services

By making this project open, it can help create **safer communities through technology**.

---

# Future Improvements 🚀

* Smartwatch emergency trigger
* AI-based risk detection
* Offline emergency alerts
* Integration with police helplines
* Emergency safe zone map

---


# Download APK

Download the APK from the **Releases** section.

---

# Author 👨‍💻

**Man Sharma || Aditya Routh || Asif Husssain Tahiri || Ayush sharma**

Android Developer | Kotlin Developer

---

# License

This project is open source and available under the MIT License.
