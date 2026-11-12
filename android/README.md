# teaelle — Android

A minimal, offline Android shell around the same PWA in the repository root.
The web app is bundled in `assets/www` and served through
`WebViewAssetLoader` at `https://appassets.androidplatform.net`, so
localStorage, ES modules and the service worker behave as they do on the web.
No `INTERNET` permission is declared.

## Build

Requires JDK 17+ and Android SDK platform 35 / build-tools 35.0.0.

```bash
# from this directory
export ANDROID_HOME=/root/android-sdk
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
export RELEASE_KEYSTORE_FILE=/path/to/teaelle-release.keystore
export RELEASE_STORE_PASSWORD='...'
export RELEASE_KEY_ALIAS=teaelle
export RELEASE_KEY_PASSWORD='...'
export VERSION_CODE=1
export VERSION_NAME=1.0.0
/root/gradle-8.9/bin/gradle --no-daemon assembleRelease
```

The release APK is written to
`app/build/outputs/apk/release/app-release.apk`.

The `syncWebAssets` Gradle task copies the web app from the repository root
into `app/src/main/assets/www`; the generated copy is git-ignored, so there is
only one source of truth.

## Signing

The release key is stored outside the repository at
`~/.teaelle-signing/teaelle-release.keystore` (alias `teaelle`, PKCS12).
Certificate SHA-256:

```text
15:02:EA:D6:71:E1:CD:86:EF:24:21:5E:06:A3:B2:67:6A:55:FB:EF:15:75:37:90:50:1C:7E:EE:B6:49:37:67
```

Keep that keystore and its password. Every future APK must be signed with the
same key and a higher `versionCode`; otherwise Android treats it as a different
app and refuses to update in place.

## Update semantics

- The first install is a fresh app identity; no uninstall is needed because
  this package has not been installed before.
- After that, release APKs signed by the same key with a higher `versionCode`
  install over each other and preserve localStorage data.
- A different key means every user must uninstall; there is no in-place key
  rotation for sideloaded apps.
