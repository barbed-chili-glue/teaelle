# teaelle — design note

## Product shape

A three-tab mobile app:

1. **Brew** — recipe first, timer second. The 80% case is "same tea, same mug, one tap"; the form is pre-filled from the last recipe. A `Bag in` button starts a timestamp-based timer; `Bag out` stops it and saves a draft. The rating sheet opens immediately.
2. **History** — the primary queue is "needs a rating", not "most recent". Every brew keeps all of its ratings, so Alex and Ellen can rate the same cup independently.
3. **Insights** — a demo model, data-health warnings, a time × temperature response surface, the best cup so far, and a 2×3 experiment plan.

## Data model

```text
tea:     id, brand, product, type, bagMassG, bagFormat, targetTempC, targetSteepS
brew:    id, ts, teaId, waterMl, milkMl, tempC, steepS, handling, preheat, notes
rating:  id, ts, rater, overall(1-7), strength(-1/0/1), bitterness(-1/0/1),
         milk(-1/0/1), wouldRepeat, blinded
```

Ratings are one-to-many from brews. The tea product, not the brand, is the unit. Derived features:

- concentration = bag mass × bags / water volume (g/L)
- milk fraction = milk / (water + milk)
- log steep time (centred)

## Rating

The sheet hides the parameters until save. That is a cheap blind. Alex cannot fully blind himself (he just made the cup), but Ellen can use the same sheet without seeing the recipe. The 1–7 overall score is paired with direction-giving tags; a scalar alone cannot tell the model whether to add time, reduce milk, or change temperature.

## Model (demo)

Ridge regression with:

- centred log steep time and its square
- centred temperature and its square
- concentration
- milk fraction
- bag handling (squeeze, dunk vs lift out)
- cup preheat

The model is fit per tea product and per rater (or all raters). It reports in-sample R² and leave-one-out R², permutation importance by factor group, and a predicted response surface. The surface is a demo: it holds concentration, milk and handling at the median for the selected tea.

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
