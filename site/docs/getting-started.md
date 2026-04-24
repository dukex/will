# getting started

## prerequisites

- Android Studio Meerkat (2024.3) or later
- Android SDK 36
- device or emulator running Android 7.0+ (minSdk 24)

## build from source

```bash
git clone https://github.com/dukex/will.git
cd will
./gradlew assembleDebug
./gradlew installDebug
```

## set as default launcher

1. install the APK
2. press home button
3. select "Will" from the launcher picker
4. grant notification access in Settings → Apps → Special app access

## permissions

| permission | required for |
|-----------|-------------|
| BIND_NOTIFICATION_LISTENER_SERVICE | capturing notifications as tasks |
| SYSTEM_ALERT_WINDOW | active task overlay |
| RECEIVE_BOOT_COMPLETED | restore overlay on reboot |
