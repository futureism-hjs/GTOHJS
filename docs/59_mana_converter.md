# Mana Converter

Target: GTOCore 0.5.6-beta; addon version 5.7-spe1.
Design confirmed by the user on 2026-11-06 after the grilling interview.
Validation: source-audited and compile-verified. Java 21 online clean build
passed through IDEA MCP; client-load and hands-on acceptance remain pending.

## Player behavior

Attribution follow-up (2026-11-07): include `mana_converter` in the existing
`GTOHJSItemTooltipHandler.GTOCORE_ITEM_PATHS` set so its item displays the
same localized Added by GTO HJS line as other addon-owned `gtocore` items.
The original implementation omitted that identity; no second callback or
new language key is needed. Java 21 IDEA MCP online clean-build and packaged
identity/language-key checks passed; client visual acceptance remains pending.

`gtocore:mana_converter` has two manually selected, persisted directions:
electricity to mana (default) and mana to electricity. Both use exactly
2 EU = 1 Mana without an additional converter loss. Use the left mode tab
and the native working toggle. The main view reports direction, ratio,
actual converted amount per tick and blocked/missing-part status.

Ordinary and wireless energy/dynamo and mana input/output hatches are
accepted through their native part abilities. The selected direction needs
its own pair of hatches; the other pair may remain installed. Missing parts
stop conversion without invalidating the shell. Laser, mana-extraction,
maintenance, muffler and processing item/fluid parts are not accepted.
There are no consumables or extra startup conditions.

The converter adds no internal resource buffer. Changing mode, pausing or
invalidating the shell does not clear hatch storage. Native wireless binding,
network scheduling and any network-level losses remain owned by the hatches;
the converter does not rewrite their ledger or transport rules.

## Structure

The supplied Litematic is a 3 x 3 x 3 shell. Replace its original
mana-infuser placeholder with this controller at the bottom front center.
Keep the eight mana-glass blocks and the exact center air block.
All 17 manasteel-casing coordinates accept the four supported abilities;
at least eight actual casings must remain. Hence at most nine hatch blocks
can be installed. Preview counts suggest one of each without making all
four mandatory. The supplied artifact itself is read-only and unchanged.

Layers, north at the first row, bottom-to-top:

```text
CSC   GGG   CCC
CCC   G G   CCC
CCC   GGG   CCC
```

The three columns above are the bottom, middle and top layers respectively:
`S` is the front-bottom controller, `C` manasteel, `G` mana glass, and the
center space is required air. The rear-to-front aisles in source
are `CCC/GGG/CCC`, `CCC/G G/CCC`, `CSC/GGG/CCC`; each aisle is written
bottom-to-top, following the verified Steam Array convention.

Rendering reuses the native mana infuser's manasteel casing texture and
large-chemical-bath front overlay. No new bitmap assets are introduced.

## Transfer implementation

`ManaConverterMachine` extends the native `MultiblockControllerMachine`.
It deliberately does not inherit the input-preferring electric container
aggregation or single-direction `ManaTrait`. Four separate collections are
rebuilt from native abilities and storage traits on formation. Existing
`IEnergyContainer` traits include ordinary and wireless energy containers;
`IManaContainerMachine` exposes the native notifiable mana storage without
coupling the controller to Botania's receiver inheritance.

`ManaConversionTransfer` is the single physical conversion implementation.
Each server tick takes the smaller of the current mode's:

- Electric available balance/room and each electric hatch's own V x A rating.
- Mana available balance/room and each mana hatch's simulated one-tick limit.

The transaction sums per-hatch quotas with saturating arithmetic, converts
whole pairs only, and allows an EU pair to span multiple hatches. Electric
changes run first because a wireless container can refuse them. An odd
accepted EU or a smaller actual mana transfer is refunded to the electric
hatches that changed. Failed transactions reverse accepted changes. Arrays
are reused between ticks; no resource cache is introduced. Native mana
add/remove operations, notifications and electric change operations remain
the storage authority. Rollback bypasses a second mana quota expenditure.

The implementation assumes the native signed-return container contract and
serialized server-thread storage operations. Protected GTOLib bytecode in
the compile dependency exposes declarations/stubs, so the headless suite
cannot establish protected runtime behavior or wireless ledger acceptance.

The native Fancy UI/filter-selector synchronization pattern is reused for a
two-row direction tab. No fake processing recipe types or EMI categories are
created merely to obtain a mode selector. Persisted/synced flags and the
native `IWorkable` renderer contract supply working state.

## Acquisition

The existing `AcquisitionRecipes` provider now has 16 GT recipes. New raw ID:
`gtohjs:acquisition/mana_converter`; the native builder supplies its final
type-prefixed ID. Registration remains in the existing CoreMod recipe window.

Assembler, 120 EU/t, 400 ticks (20 seconds), output one controller:

| Input | Amount |
| --- | ---: |
| MV machine hull | 1 |
| Manasteel plate | 8 |
| Mana diamond | 4 |
| Mana pearl | 4 |
| MV circuit tag | 2 |
| MV voltage coil | 2 |
| MV sensor | 1 |
| MV emitter | 1 |
| Soldering alloy | 288 mB |

This is the accepted post-manasteel pearl/diamond + MV stage. Terrasteel,
mana runes and an existing mana infuser are not ingredients. Frozen pearls
have both an Ars enchanting route and an MV vacuum route; no Ars-only gate
is asserted. Counts/EUt/time are the user-confirmed new balancing proposal.

## Verification boundary

Completed on 2026-11-06: `./gradlew clean build` through IDEA MCP, Java
21.0.5, Gradle 8.8, network enabled, BUILD SUCCESSFUL in 3m 33s,
15 tasks executed. `verifyManaConversion` passed 72,013 assertions and
the pre-existing optimization contracts passed. A byte-for-byte voxel
comparison against the input Litematic matched all 27 positions when
treating its mana-infuser block as the controller marker. Four locale JSON
files parsed successfully. The final JAR passed ZIP integrity and contained
all three new production classes, recipe target, version and translations.

Artifact: `build/libs/gtohjs-5.7-spe1-for-gtocore-0.5.6-beta.jar`
(982,777 bytes). The build uses the shared current worktree, including
concurrent Chemical Distorter changes maintained by another task; those
changes were neither authored nor reverted by this task. Existing local-file
dependency/deprecation warnings remain; no compilation failure occurred.

The first wrapper attempt hit a locked default distribution cache before
compilation. The successful run used the same Wrapper and official Gradle
8.8 ZIP in a task-local Gradle home, reusing dependency caches and allowing
network access. No offline or alternate-Gradle command was substituted.

`verifyManaConversion`, included in Gradle `check`, runs the production
transaction with fake native-contract ports. It checks both directions,
roundtrip conservation, blocked output, split odd balances/room, per-port
quotas, transfer refusal, partial acceptance, rollback and long arithmetic,
plus randomized multi-hatch mode switching. It does not load Minecraft.

The existing registry load-complete window validates registry identity,
pattern construction, renderer and preview availability; the existing
acquisition provider validates exact recipe contents and finalized tables.
Those runtime checks are not executed by compilation alone.

Pending hands-on acceptance: form the supplied shell with minimum casings,
test both directions with ordinary and bound/unbound wireless pairs,
verify rates and 2:1 balance changes, fill outputs, toggle/stop/reload,
check preview orientation and observe the Assembler recipe in EMI. Client
deployment/launch and publication were not requested for this delivery.
