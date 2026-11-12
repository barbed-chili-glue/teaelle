# Android release signing

## App identity

- Package: `com.teaelle.app`
- Release key alias: `teaelle`
- Keystore: `~/.teaelle-signing/teaelle-release.keystore` (PKCS12, RSA 4096)
- Certificate SHA-256:

```text
15:02:EA:D6:71:E1:CD:86:EF:24:21:5E:06:A3:B2:67:6A:55:FB:EF:15:75:37:90:50:1C:7E:EE:B6:49:37:67
```

The keystore and password are **not** in this repository and must never be
committed or sent through chat. They are the app's update identity.

## Update rule

Android identifies an app by **package name + signing certificate**. For an
in-place update that preserves localStorage data:

1. Same package: `com.teaelle.app`
2. Same signing key: the `teaelle` key above
3. Higher `versionCode` than the installed APK

A different key is a different app. There is no in-place key rotation for
sideloaded apps.

## Build recipe

```bash
cd android
export ANDROID_HOME=/root/android-sdk
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
export RELEASE_KEYSTORE_FILE=/root/.teaelle-signing/teaelle-release.keystore
export RELEASE_STORE_PASSWORD="$(cat /root/.teaelle-signing/storepass.txt)"
export RELEASE_KEY_ALIAS=teaelle
export RELEASE_KEY_PASSWORD="$(cat /root/.teaelle-signing/storepass.txt)"
export VERSION_CODE=2
export VERSION_NAME=1.0.1
/root/gradle-8.9/bin/gradle --no-daemon assembleRelease
```

## Verification

```bash
"$ANDROID_HOME/build-tools/35.0.0/apksigner" verify --print-certs \
  app/build/outputs/apk/release/app-release.apk
```

The V2 signer certificate SHA-256 must match the fingerprint above. Also check:

```bash
"$ANDROID_HOME/build-tools/35.0.0/aapt2" dump badging \
  app/build/outputs/apk/release/app-release.apk | head
```

Expected: `package: name='com.teaelle.app' versionCode='N'`, no
`uses-permission: name='android.permission.INTERNET'`, and
`application-debuggable` absent.

## Version history

| versionCode | versionName | Notes |
|---|---|---|
| 1 | 1.0.0 | First signed release. |
| 2 | 1.0.1 | Tea library edit/delete; confirm actuals at bag-out; edit actuals mid-brew; history snapshots. |

## Custody

Keep an offline copy of the keystore and password. If the key is lost, every
installed copy is stranded and a new package identity is required. If it leaks,
someone else can sign a malicious update.
