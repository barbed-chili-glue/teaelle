# teaelle

A mobile-first, local-first tea brewing notebook that learns from your cups.

**Live demo:** https://barbed-chili-glue.github.io/teaelle/

## What it does

- **Brew** — pick a tea product, set water, milk, temperature and bag handling, then start the timer as the bag goes in. Stop it when the bag comes out; a **confirm** sheet lets you correct the actuals (especially milk, weighed after the timer) before saving.
- **Parallel brews** — each running cup gets its own tab at the top of the Brew screen, labelled with who it is for and showing the time left (or how far over). Tap **+** to start another while the first is still steeping; each brew keeps its own timer, recipe and actuals.
- **Manual entry** — **Log a brew without the timer** records a cup after the fact: steep time, actuals, date/time and who it was for. Useful when the phone was not to hand or the tea was made in a pot.
- **Tea library** — add, edit or delete tea products, including the suggested temperature and steep time. Historical brews keep a snapshot of the tea they were brewed with, so deleting a tea never breaks history.
- **Rate blind** — the rating sheet hides the brewing parameters until you save. Rate overall (1–7), strength, bitterness/astringency, milk balance, and whether you'd brew again. Multiple raters per cup are supported.
- **History** — unconfirmed cups and unrated cups are queued at the top as **needs attention**; every brew keeps its parameters, tea snapshot and all of its ratings.
- **Insights** — a demo ridge model fits a quadratic response surface over steep time and temperature, ranks the drivers, shows the best cup so far, and proposes a balanced 2×3 experiment around the predicted peak.
- **Local-first** — everything is stored in `localStorage` on the device. No account, no server, no telemetry. Export/import JSON from Settings.

## Why these dimensions

Research note: `~/.hermes/references/tea-dimensionality-research.md` (outside this repo). The short version:

- Tea is nominally huge — >600 volatile compounds, 31–155 sensory descriptors — but effectively small.
- The app captures **five effective dimensions** plus a tea-product block: concentration (bag mass / water), steep time, temperature, milk fraction, and bag handling/agitation. Water chemistry is a house constant, not a per-brew slider.
- The rating uses **four consumer axes** plus overall, not a 155-attribute flavour wheel.
- The model is deliberately a **demo**: ridge regression with quadratic time and temperature terms. The production model would be an ordinal, hierarchical model with rater and product random effects, LOO/PPC checks, and a proper stopping rule.

## Run locally

```bash
cd teaelle
python3 -m http.server 8766
```

Open `http://localhost:8766/`. The service worker only registers over HTTP(S), so the app also opens as a plain file without offline caching.

## Demo data

Settings → **Load demo data** loads 82 fictional brews across three teas, with Alex and Ellen ratings and seven unrated cups. The data is generated deterministically and clearly labelled fictional. **Reset all data** clears it.

## PWA

The app includes `manifest.webmanifest`, a service worker, and 192/512 icons. On Android Chrome, open the live URL and choose **Add to Home screen**. The app works offline after the first load.

## Android app

`android/` is a minimal, offline WebView shell around the same web app. It
bundles `index.html` in `assets/www`, serves it through
`WebViewAssetLoader`, declares no `INTERNET` permission, and keeps
localStorage across in-place updates.

The release APK is signed with the stable `teaelle` key. Future builds must use
the same package (`com.teaelle.app`), the same key, and a higher `versionCode`;
see [`SIGNING.md`](SIGNING.md). The keystore itself lives outside the
repository.

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

## Scope and next steps

This is a demo, not the final product. Next steps:

1. Replace the demo ridge model with an ordinal hierarchical model (rater and product random effects, LOO/PPC).
2. Add a guided experiment mode: repeat / tweak one thing / surprise me, with `pair_id` for paired analysis.
3. Add measured temperature and a kettle calibration flow.
4. Add backup validation/checksum and a restore preview; schema v2 and the v1→v2 migration are in place.
5. Decide whether multi-device rating needs sync or one-device hand-over is enough.

## Stack

Single-file HTML/CSS/JS, no build step, no dependencies. Icons generated from `icon.svg`. Service worker cache-first.

## Licence

MIT.
