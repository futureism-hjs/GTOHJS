# Automatic Radiation Hatch (0.5.6)

## Registration and native reuse

`bootstrap/AutomaticRadiationHatchRegistration.java` registers
`gtocore:auto_radiation_hatch` (Automatic Radiation Hatch / 自动放射仓).
It uses the original `RadiationHatchPartMachine` factory, ZPM tier,
fuel recipe type, inventory, UI and save fields. The 0.5.6 native hatch has
no `RADIATION_HATCH` PartAbility; native structures use explicit block matches.
The exact production radiation trait expects that concrete native class;
an unrelated replacement part would not satisfy its scan contract.

The front is `gtocore:block/machine/part/radiation_hatch`. The shared
`client/renderer/ManipulatorCasingRenderer.java` replaces the standalone hull
with `gtocore:block/manipulator`, retaining the native overlay. The four
adaptive hatches now reuse this extracted, previously verified renderer.

## Radiation and fuel contract

`machine/AutomaticRadiationSupport.java` adapts only the new definition.
Valid native radioactive fuel is consumed by the original machine and its
remaining `time` is the active-fuel condition. A consumed last item still
supplies radiation until that burn expires; an empty, inactive hatch supplies
zero. Fuel lifetime, inhibition and redstone behavior remain native.

The existing CoreMod script has three exact, count-checked method anchors:

1. `RadiationHatchPartMachine.getRadioactivity()I`: return the automatic dose
   only while native fuel time is positive; other native hatches pass through.
2. `RadioactivityTrait.getRecipeRadioactivity()I`: if a fueled automatic hatch
   is present, return the current recipe requirement; otherwise retain the
   original sum. This also satisfies the native upper radiation bound when
   other native hatches are present.
3. `RadiationHatchPartMachine.addDisplayText(List)V`: read the existing getter
   for its numeric radiation row instead of the raw field.

The requested dose cache uses weak keys and is reconstructed by the native
trait; it adds no persistent fields. The exact private native fuel-time field
is resolved once. Names use real `assets/gtocore/lang` block keys; item details
remain in the existing Kotlin tooltip handler. No crafting recipe was added.

## Native structure replacement repair (2026-10-24)

Using the same native radiation runtime class did not grant structure access:
native machines match `blocks(GTOMachines.RADIATION_HATCH.get())` explicitly.
The first source version therefore could not replace a normal radiation hatch.

`AutomaticRadiationSupport.installPatterns()` now runs in the existing
load-complete validation after the fusion adapter. It follows the existing
fusion adapter's traversal of memoized native patterns, including subpatterns.
Only `PredicateBlocks` entries containing the native radiation hatch are
extended, in place, to accept the automatic block and list it as a candidate.
Predicate identity, global/layer limits, previews and other structural metadata
are retained. Ordinary and automatic hatches share the same original two/eight
limit; no independent unlimited OR branch or new ability is introduced.
The native radiation part scanner and fuel/calibration adapters remain in use.

## Verification status

The three production bytecode anchors and fuel-time field were inspected.
CoreMod JavaScript syntax and source whitespace checks passed for the original
source addition. Native block matching, predicate fields and memoized pattern
factories were checked against the exact local 0.5.6 Core/GTCEu JAR ABI for
this repair. Production Core bytecode confirms all four native explicit-block
sites, and the packaged registration class calls `installPatterns()`.

Java 21.0.5 network-enabled IDEA MCP `./gradlew clean build` passed in 56s,
all nine tasks executed. The project has no test sources. The 903351-byte
addon was deployed alone to the fixed 0.5.6 client after retaining its prior
900961-byte addon under the project deployment-backup cache
`20261024-auto-radiation-pattern`. The client was stopped, so its matching HJS
and aggregate resource indexes were refreshed immediately. No client was
launched or stopped; the existing PCL monitor (PID 22896) remains user-owned
startup support. Formation, mixed-hatch limits and fuel behavior still require
user-owned client acceptance.
