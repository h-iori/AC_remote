<div align="center">
  <h1>❄️ AC Remote by Iori Studios</h1>
  <p><b>A native, offline infrared remote control for Videocon Air Conditioners.</b></p>
  <p><i>Zero ads. Zero internet. 100% direct hardware control.</i></p>

  <p>
    <a href="https://github.com/h-iori/AC_remote/releases/latest">
      <img src="https://img.shields.io/github/v/release/h-iori/AC_remote?style=flat-square&color=30D158" alt="Latest Release" />
    </a>
    <img src="https://img.shields.io/badge/Platform-Android_IR_Blaster-0A84FF?style=flat-square" alt="Platform" />
    <img src="https://img.shields.io/badge/Kotlin-2.3.20-7F52FF?style=flat-square" alt="Kotlin" />
  </p>
</div>

---

## 🌟 Why I Built This

*“If my phone already has the hardware, why am I still hunting for AAA batteries?”*

I was frustrated with constantly replacing dead batteries in my AC remote—especially when they ran out unexpectedly. Since my phone already has a built-in Infrared (IR) Blaster, I decided to build a standalone native remote rather than dealing with physical batteries or ad-riddled Play Store apps.

**AC Remote** sends raw, protocol-accurate infrared commands directly to the air conditioner. It requires no internet, no Wi-Fi, no Bluetooth, and no third-party accounts.

---

## ⚡ Remote Features

- **Direct IR Blaster Transmission:** Encodes and transmits the 280-pulse, 38 kHz Kelvinator/YB1FA infrared protocol via Android's native `ConsumerIrManager`.
- **Power Control:** Instant power ON / OFF toggle.
- **Precision Temperature Control:** Set temperature between 16°C and 30°C in 1°C increments.
- **5 Operating Modes:** Complete support for **Cool**, **Heat**, **Dry (Dehumidify)**, **Fan Only**, and **Auto**.
- **5-Stage Fan Speeds:** Select between **Auto**, **Low**, **Medium**, **High**, and **Turbo** airflow.
- **Dual-Axis Louver & Swing Control:** Full vertical louver angle adjustment (auto-swing, fixed ceiling, high, mid, low) and horizontal sweep toggling.
- **Auxiliary AC Functions:**
  - **Turbo:** Maximum cooling/heating output.
  - **X-Fan / Blow:** Keeps indoor fan running after shutoff to dry coils and prevent mildew.
  - **Light:** Turns the AC indoor unit LED temperature display ON or OFF.
  - **Quiet:** Ultra-low fan speed for silent nighttime operation.
  - **Sleep:** Automatically adjusts temperature curve for overnight comfort.
  - **Timer:** Configurable countdown timer for automatic shutoff.
- **Quick Comfort Presets:** Instant one-tap profiles (*Cool Freeze*, *Eco Night*, *Gentle Breeze*).
- **Persistent State Memory:** Automatically saves your last-used settings locally so your remote is ready the moment you open it.

---

## ❄️ Target Hardware

- **AC Manufacturer:** Videocon
- **Series:** Vita Air
- **Model:** `VBS53.CVL1-CR`
- **Original Remote Model:** `YB1FA`
- **Protocol:** Kelvinator (16-byte state, 38 kHz carrier frequency, checksum validated)

---

## 📲 Download & Installation

1. Download the latest `AC-Remote-v2.0.0.apk` from [GitHub Releases](https://github.com/h-iori/AC_remote/releases/latest).
2. Install the APK on any Android phone equipped with an infrared emitter (Xiaomi, Redmi, POCO, Vivo, Huawei, etc.).
3. Point your device at the AC unit and control it immediately.

---

<div align="center">
  <p>Built with ❤️ by <b>Iori Studios</b></p>
</div>
