# Native fusion Parallel Hatch support (0.5.6)

## Scope and ownership

`machine/FusionParallelSupport.java` decorates exactly these definitions:

- `gtceu:luv_fusion_reactor`
- `gtceu:zpm_fusion_reactor`
- `gtceu:uv_fusion_reactor`
- `gtocore:uhv_fusion_reactor`
- `gtocore:uev_fusion_reactor`

Kuangbiao One is excluded. Controllers, registered IDs, recipe types, startup
heat and `EU_TO_START` remain native. `GTOHJSRegistration.validateLoaded()`
installs the adapter once and fails explicitly if a target or extension point
is missing. The exact five-definition runtime check has not yet run.

## Structure and recipe contract

The adapter adds one optional native Parallel Hatch to positions containing
the definition's appearance casing. Controller predicates are excluded.
It updates the existing memoized pattern and predicate collection, retaining
geometry, conditions, limits and extension metadata instead of rebuilding a
native structure. The same hatch predicate enforces a global maximum of one.

The existing recipe modifier runs first. Without a Parallel Hatch its result
is returned unchanged. With a hatch, native input/output parallel matching
determines actual P. Materials and products scale through the existing
`GTRecipe.modifier(multiplier, false)` path. Whole-batch running EU/t is
`ceil(E * (25 + P) / 25)`, where E is the native modified baseline EU/t.
Startup charging remains native and is not multiplied. P=10 costs 1.4E
running EU/t. Native intrinsic parallels count towards actual P.

BigInteger arithmetic bounds P before converting the final cost to long.
The existing Kotlin `GTOHJSItemTooltipHandler` owns the two explanatory lines;
no second tooltip callback or fusion controller is introduced.

## Verification status

Source/API inspection and exact JAR ABI checks are recorded in the read index.
Java 21 IDEA clean build and runtime verification are pending. On 2026-10-24
the user prohibited this agent from building or deploying because another AI
is working in the same project. No client action is part of this delivery.
