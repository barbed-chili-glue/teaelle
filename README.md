# teaelle

A mobile-first, local-first tea brewing notebook that learns from your cups.

**Live demo:** https://barbed-chili-glue.github.io/teaelle/

## What it does

- **Brew** — pick a tea product, set water, milk, temperature and bag handling, then start the timer as the bag goes in. Stop it when the bag comes out.
- **Rate blind** — the rating sheet hides the brewing parameters until you save. Rate overall (1–7), strength, bitterness/astringency, milk balance, and whether you'd brew again. Multiple raters per cup are supported.
- **History** — unrated cups are queued at the top; every brew keeps its parameters and all of its ratings.
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

## Scope and next steps

This is a demo, not the final product. Next steps:

1. Replace the demo ridge model with an ordinal hierarchical model (rater and product random effects, LOO/PPC).
2. Add a guided experiment mode: repeat / tweak one thing / surprise me, with `pair_id` for paired analysis.
3. Add measured temperature and a kettle calibration flow.
4. Add a proper export schema version and migration path.
5. Decide whether multi-device rating needs sync or one-device hand-over is enough.

## Stack

Single-file HTML/CSS/JS, no build step, no dependencies. Icons generated from `icon.svg`. Service worker cache-first.

## Licence

MIT.
