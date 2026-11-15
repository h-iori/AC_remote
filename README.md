<div align="center">
  <h1>❄️ AC Remote by Iori Studios</h1>
  <p><b>A modern, high-performance infrared AC remote built natively for Android.</b></p>
  <p><i>Zero ads. Zero bloat. 100% offline hardware control.</i></p>

  <p>
    <a href="https://github.com/h-iori/AC_remote/releases/latest">
      <img src="https://img.shields.io/github/v/release/h-iori/AC_remote?style=flat-square&color=30D158" alt="Latest Release" />
    </a>
    <img src="https://img.shields.io/badge/Platform-Android_IR_Blaster-0A84FF?style=flat-square" alt="Platform" />
    <img src="https://img.shields.io/badge/Kotlin-2.3.20-7F52FF?style=flat-square" alt="Kotlin" />
    <img src="https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=flat-square" alt="Jetpack Compose" />
  </p>
</div>

---

## 🌟 Why I Built This

*“If my phone already has the hardware, why am I still hunting for AAA batteries?”*

I was completely frustrated with constantly changing batteries in my physical AC remote—especially when they died in the middle of the night. My phone has a built-in Infrared (IR) Blaster, yet most remote apps on the Play Store are bloated with intrusive pop-up ads, subscription walls, and unnecessary cloud trackers.

So, I decided to build my own.

**AC Remote** speaks directly to the air conditioner through native infrared pulses. No internet connection, no Bluetooth, no accounts, and no third-party telemetry—just pure, instant climate control directly from the device that's already in my hand.

---

## 🚀 Features

- **Direct IR Blaster Engine:** Uses Android's native `ConsumerIrManager` to transmit verified 38 kHz Kelvinator/YB1FA frames (280 alternating microsecond pulses).
- **Sticky Power Bar:** Prominent Power ON/OFF toggle pinned permanently to the top app bar for instant, zero-scroll access.
- **Glassmorphism UI:** Pure Jetpack Compose interface with dynamic ambient lighting that reacts in real-time to the active mode (Cool Blue, Heat Amber, Dry Teal, Fan Purple, Auto Green).
- **Tactile Temperature Dial:** Interactive touch & drag circular dial (16°C – 30°C) alongside discrete tactile `−` and `+` step buttons.
- **5-Stage Fan Stepper:** Quick toggling between Auto, Low, Medium, High, and Turbo airflow.
- **Full Airflow & Swing Control:** Dedicated bottom sheet for vertical louver angles and horizontal sweep.
- **Smart Comfort Presets:** Instant one-tap modes: *Cool Freeze*, *Eco Night*, and *Gentle Breeze*.
- **Edge-to-Edge Adaptive Layout:** Perfectly accommodates punch-hole camera cutouts, system status bars, and Android gesture navigation bars without overlap.
- **Persistent State:** Uses Android DataStore Preferences to save and restore your exact temperature, fan, and swing settings across app launches.

---

## ❄️ Target Hardware

- **AC Brand:** Videocon
- **Series:** Vita Air
- **Model:** `VBS53.CVL1-CR`
- **Original Remote:** `YB1FA`
- **Protocol:** Kelvinator 16-byte state, 38 kHz carrier frequency, checksum-validated.

---

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** Clean MVVM with Unidirectional Data Flow (StateFlow)
- **Asynchronous Pipeline:** Kotlin Coroutines & Mutex-guarded IR transmission queue
- **Hardware Driver:** Android `ConsumerIrManager`
- **Persistence:** Jetpack DataStore Preferences

---

## 📲 Download & Installation

1. Grab the latest APK from [GitHub Releases](https://github.com/h-iori/AC_remote/releases/latest).
2. Install `AC-Remote-v2.0.0.apk` directly onto any Android device with an integrated IR blaster (Xiaomi, Redmi, POCO, Vivo, Huawei, etc.).
3. Point your phone at the AC and enjoy.

---

<div align="center">
  <p>Built with ❤️ by <b>Iori Studios</b></p>
</div>
