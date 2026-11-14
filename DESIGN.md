# teaelle — design note

## Product shape

A three-tab mobile app:

1. **Brew** — recipe first, timer second. The 80% case is "same tea, same mug, one tap"; the form is pre-filled from the last recipe. A `Bag in` button starts a timestamp-based timer; `Bag out` stops it and opens a **confirm** sheet for the actuals (milk is often added after the timer). Confirming saves the brew and opens the rating sheet.
   - **Parallel cups:** the top of the screen is a strip of brew tabs, one per running cup, labelled with `madeFor` and showing time left (`2:14 left`) or time over (`0:35 over`) in a due state. `+` opens the new-cup form without disturbing the others. Each session has its own recipe snapshot and `startedAt`; only one card is rendered at a time.
   - **Manual entry:** `Log a brew without the timer` opens a backfill sheet: made-at date/time, rater, tea, steep minutes/seconds, actuals. It saves a confirmed brew straight to History (unrated, so it joins the needs-attention queue).
2. **History** — the primary queue is **needs attention**: unconfirmed cups first, then unrated cups, not "most recent". Every brew keeps all of its ratings, so Alex and Ellen can rate the same cup independently. Cards show `for <rater>` when the cup was brewed for someone.
3. **Insights** — a demo model, data-health warnings, a response surface with selectable axes (steep time, temperature, concentration, milk fraction), the best cup so far, and a 2×3 experiment plan.

## Data model

```text
tea:     id, brand, product, type, bagMassG, bagFormat, targetTempC, targetSteepS
brew:    id, ts, teaId, teaSnapshot, madeFor, waterMl, milkMl, tempC, steepS,
         handling, preheat, notes, confirmed, source ('timer'|'manual')
rating:  id, ts, rater, overall(1-7), strength(-1/0/1), bitterness(-1/0/1),
         milk(-1/0/1), wouldRepeat, blinded

active session (localStorage `teaelle.active`, array):
         id, madeFor, teaId, teaSnapshot, waterMl, milkMl, tempC, handling,
         preheat, notes, targetSteepS, startedAt
```

`teaSnapshot` is the tea identity at brew time (`brand, product, type, bagMassG, bagFormat, targetTempC, targetSteepS`). It means editing or deleting a tea never rewrites or breaks history. Ratings are one-to-many from brews. The tea product, not the brand, is the unit. `madeFor` is who the cup was brewed for; it is deliberately separate from `rating.rater` — a cup can be made for Ellen and rated by both people. Derived features:

- concentration = bag mass × bags / water volume (g/L)
- milk fraction = milk / (water + milk)
- log steep time (centred)

## Rating

The confirm sheet shows the actuals and is the place to correct them; the rating sheet then hides the parameters until save. That is a cheap blind. Alex cannot fully blind himself (he just made the cup), but Ellen can use the same rating sheet without seeing the recipe. The 1–7 overall score is paired with direction-giving tags; a scalar alone cannot tell the model whether to add time, reduce milk, or change temperature.

## Actuals flow

- **While steeping** — `Edit actuals` opens the same field set and writes back to the active brew; the timer keeps running.
- **At bag-out** — the brew is saved as unconfirmed and a confirm sheet opens. `Confirm & rate` saves the actuals and opens the rating sheet; `Save without rating` confirms without rating; `Discard` removes the cup. Closing the sheet leaves the cup in History under **needs attention** so it can be confirmed later.
- **After saving** — a brew's recipe can be corrected from its detail sheet; this edits the brew record, not the tea library.

## Model (demo)

Ridge regression with:

- centred log steep time and its square
- centred temperature and its square
- concentration
- milk fraction
- bag handling (squeeze, dunk vs lift out)
- cup preheat

The model is fit per tea product and per rater (or all raters). It reports in-sample R² and leave-one-out R², permutation importance by factor group, and a predicted response surface. The surface is a demo: pick any two of steep time, temperature, concentration and milk fraction; the remaining inputs stay held at their median for the selected tea. Handling and preheat are categorical and are not surface axes.

Production model: cumulative-logit ordinal regression with `(1|rater)`, `(1|product)`, `(1|day)`, a spline or quadratic in log time, temperature × time interaction, and weakly informative/regularising priors. Validate with LOO and posterior predictive checks. Report a robust region, not a single peak.

## Experiment plan

The plan is a 2×3 factorial around the predicted peak: two temperatures × three steep times, two replicates = 12 brews. Hold concentration, milk and handling steady. Tap a cell to load it into the brew screen; the timer records the actual steep, not the target.

## Deliberate omissions

- No accounts, cloud sync, telemetry or social features.
- No full 155-attribute flavour wheel; four consumer axes are enough.
- No bag-squeeze slider; squeeze is a 3-level handling tag because the evidence for a large squeeze effect is weak.
- No per-brew water-chemistry slider; water is a house constant.
- No in-app stats engine beyond the demo ridge model; the production analysis belongs in a proper R/Python environment until the workflow proves itself.

## Known limitations

- The demo data is fictional and deterministic; it is there to make the insights screen legible.
- The demo model is not the production model.
- Temperature is recorded as entered; kettle calibration is a setting but not yet used to correct the value.
- No multi-device sync; export/import is the bridge.
- No PWA install prompt UI; use the browser's "Add to Home screen".
- Parallel timers live in the app, not the OS: if the app is closed, no notification fires. Reopening restores the sessions and shows the correct remaining/over time, but there is no alarm while it is shut.
- `madeFor` is not yet a model term. Once there is enough data, it could enter as a random effect or interact with concentration/milk (people take their tea differently), but the demo ridge model ignores it.
- Manual entries carry `source: 'manual'` and are not distinguished in the UI yet; a future analysis should allow their steep time to be less precise than timer-recorded cups.
