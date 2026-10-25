# Controller inputs and hyperdimensional biochemical recipes

## Scope and authorization

Target: old HJS for GTOCore 0.5.6-beta only. The user confirmed the design after
the grilling interview and corrected ordinary maintenance to provide no filter
level. Deliver a Java 21 network-enabled IDEA clean build and deploy the addon;
do not launch, stop or restart Minecraft. No Git or reference-source changes.

## Four controller-mode processing machines

The Plasma Machine Tool, Hadron Catalytic Refinery, Quantum Mass Spectrum Array
and Superconducting Fusion Assembler keep their definitions, structures and
11/11/10/5 native recipe-type lists. Their existing persisted controller mode
selects the type; actual ingredients select a recipe within that type.

`PatternBufferPlacementMethods.serviceFace()` remains their single shared X/Q
service predicate. Its input branch uses native item/fluid/dual-input ability
registrations and excludes `gtocore:me_catalyst_pattern_buffer` explicitly.
That catalyst buffer advertises ordinary item and fluid inputs, so excluding
the catalyst-hatch ability alone would not exclude it. No pattern buffer is
required. Output, maintenance, energy, parallel, thread, accelerate and
overclock limits are preserved.

`OrderAwareElectricMultiblockMachine` retains its native CrossRecipe constructor
without the former HJS buffer-mode/recorded-recipe veto or related diagnostics.
Buffers retain their own inventories and native delivery behavior. The native
controller type setter and scheduler remain responsible for mode changes:
already started jobs are not deliberately cancelled or refunded; newly selected
work uses the selected type. No replacement queue or mode field was added.

## Derived biochemical recipes

Reuse the existing `gtceu:hyperdimensional_biochemical_processing` type and
`gtocore:hyperdimensional_biochemical_factory` definition. No new recipe type or
structure is needed. `HyperdimensionalBiochemicalRecipes` implements the existing
`GTRecipeBatchSource.afterNativeRecipes()` stage, following Chemical Distorter.
The original catalog/CoreMod creates and saves the builders before native
`RecipeBuilder.finish()`; Java providers do not call save.

| Native source type | Native source baseline | Additional nonconsumable input | Exemption |
| --- | ---: | --- | --- |
| biochemical_extraction | 12 | gtohjs:biochemical_extraction_marker | No original extraction-controller startup, staged or continuous-fluid behavior |
| biochemical_reaction | 38 | gtohjs:biochemical_reaction_marker | Omit RADIOACTIVITY data |
| incubator | 62 | gtohjs:incubator_marker | Omit RADIOACTIVITY data; retain maintenance-based FILTER_CASING |

The large and ordinary incubators share `gtceu:incubator`. Its source baseline
includes the magic remnant-erosion activation recipe. The 112-recipe baseline is
from source inspection; actual snapshots include every registered recipe in
these three types and log their counts. It is not a claimed client load result.

Copy item/fluid contents, chances, original nonconsumables, conditions, recipe
extensions, other data, priority, tier and chance function. Halve duration in
ticks and preserve EU/t; current baseline durations are all even. Any later odd
duration uses integer division with a minimum of one tick. Preserve ordinary
cell/DNA buffers and other declared fluids, zero-gravity and sterile-cleanroom
conditions. Existing source definitions remain registered and unmodified.

Markers are existing plain items. Their later acquisition task now supplies
machine-to-marker workbench recipes; see `51_acquisition_recipe_proposal.md`.
Each processing recipe requires one marker with native chance-zero input semantics. Do not invent
machine-wide sharing across isolated native input units.

Each accepted derived definition validates copied IO, marker count/chance,
duration/EU/t, conditions/extensions and retained data. Existing final-table
validation covers the derived IDs; this provider also checks the original
definitions remain registered. The existing Chemical Distorter content checker
is shared rather than duplicated.

## Filtering from maintenance only

`HyperdimensionalBiochemicalRecipeSupport.install()` wraps the definition's
existing modifier without replacing native coil, parallel, threading or
overclock behavior. FILTER_CASING must be satisfied before the saved native
modifier is applied. Its source data is retained for recipe display.

| Maintenance capability | Filter tier |
| --- | ---: |
| Ordinary or automatic without cleaning | 0 |
| Cleaning / configurable cleaning | 1 |
| Sterile / configurable sterile | 2 |
| Absolute-clean / configurable absolute-clean | 3 |
| Modular configuration | Its currently installed native cleaning module |

Higher satisfies lower. Use connected maintenance definitions and the native
CMHatch provider type sets. The modular hatch's public native inventory traits
expose its existing three cleaning-module choices; no reflection, duplicate
module inventory or second cleanroom provider is added. Read each check so
module changes do not leave an HJS cache stale. Nearby environment cleanrooms
cannot satisfy this filter gate. Other native cleanroom conditions are kept.

## Verification

Java 21.0.5 online `./gradlew clean build` through IDEA passed in 14 seconds:
nine actionable tasks, seven executed and two restored from Gradle's cache.
The earlier IDEA build-project call timed out without reporting problems; the
complete wrapper build supplies the successful build evidence.

Packaged checks passed for the new source/support classes, three marker models
and PNGs, both tooltip locales, explicit catalyst exclusion and removal of the
active HJS order veto. A 900,961-byte addon was deployed after retaining the
previous 890,378-byte addon and both resource indexes in deployment backup
`20261024-controller-biochemical`.

The existing user client was detected running. It was not stopped or restarted.
The already established cache-refresh helper waits for its exit before clearing
only the matching addon and aggregate resource indexes. Runtime recipe loading,
112-source-baseline registration and in-world operation remain unverified in a
new client; no client was launched by this task.
