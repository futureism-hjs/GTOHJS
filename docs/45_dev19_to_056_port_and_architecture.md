# Dev19 feature port to GTOCore 0.5.6-beta

## EnhancedCore structure attribution (2026-10-25)

The seven EnhancedCore-derived machines now append the fixed per-character
rainbow `结构来源：GTL-Enhancedcore` through the existing
`gtl/GtlPortedMachineText.localizeHoverText()` call. The palette and component
construction are reused from dev19; there is no time-dependent color or scroll.
The registered-machine filter limits the line to those seven paths.
`chemical_distort` retains its separate animated GregTech Leisure attribution.
No machine behavior, recipe, structure, ID or tooltip event registration changed.
Build and deployment evidence is tracked in workspace task 2026-10-25_08.
Java 21.0.5 online IDEA MCP `./gradlew clean build` passed in 42 seconds,
with all nine tasks executed. The 922,883-byte addon was deployed alone after
backing up the previous 922,067-byte addon. Installed tooltip class bytes
match the build; static bytecode verification confirms both source branches.
Assets/data are unchanged, so no resource-index refresh was needed.
The running client was preserved; the line becomes active on the user's next
restart. In-game visual acceptance remains pending. No Git operation.

The 2026-10-24 controller-input and biochemical recipe changes are documented in
`49_controller_inputs_and_biochemical_recipes.md`: source/package checks and
Java 21 online clean build passed, addon deployed, existing client retained.
Its resource cache refresh waits for the user's client to exit.

Target source version: `5.6-spe1-for-gtocore-0.5.6-beta`. This document is the
maintenance entry point for the old GTOHJS repository. The dev19 tree is a
read-only feature reference; the 0.5.6 client and its Core JAR define the API.

Local additions on 2026-10-23/24 are documented in
`native_fusion_parallel.md`, `automatic_radiation_hatch.md` and
`biochemical_machine_markers.md`. They are source-only in this delivery.
The user prohibited this agent from building/deploying on 2026-10-24 while
another AI works here; earlier acceptance evidence below does not cover them.

## Ownership and iteration path

| Area | Single owner | Contract |
| --- | --- | --- |
| Native registration windows | `bootstrap/GTOHJSRegistration.java` | Lists recipe types, machines and AE machines once. CoreMod injects the calls; Mixins only provide fallback dispatch. |
| Existing content | `bootstrap`, `machine`, `api`, `recipes` | Keep established IDs, recipe builders, saves and runtime classes. Extend the existing implementation before adding a new one. |
| Seven migrated machines | `gtl` | One registration class per machine; shared order and ME output helpers are used by the four order-aware machines. Structure files stay in `data/gtohjs/structures`. |
| Adaptive wireless content | `adaptivenet` | One registration class for five definitions, one hatch implementation for four directions, one terminal, one saved frequency registry, one template registry and one scheduler. The terminal owns its two left-side configuration pages. |
| Infinite storage unit | `adaptivenet` plus existing CoreMod script | Block entity owns item-carried charge; `InfiniteWirelessEnergyLifecycle` owns mechanics and Kotlin `InfiniteEnergyPresentation` owns text. The 0.5.6 tower owns its team ledger. Tower text and the binding-tool rate calculation change only when the addon unit is present. |
| Client text and presentation | `assets` and existing tooltip hook | Language files own wording. The existing Kotlin tooltip handler owns item attribution. Java and Kotlin do not implement parallel versions of the same behavior. |

For each further change, identify its owner in this table, inspect the exact
0.5.6 API and structure, change the smallest owner, then run Java 21 online
`./gradlew clean build` through IDEA MCP. Validate registration from the client
log and enter a save. Update this document and `AGENT/READ_INDEX.md` before a
source commit. World entry is a startup gate, not an energy-transfer or
multiblock-formation test.

## Feature mapping

| Dev19 feature | 0.5.6 implementation | Current evidence |
| --- | --- | --- |
| Existing HJS machines, ME parts, recipe editor, structure exporter, component packs and method-mode recipes | Reuse the old implementations and IDs. Registration lists now have one Java owner. | Clean build and 0.5.6 client registration/recipe validation passed. |
| Neutron Control Factory, Platinum Refining Matrix, Dragon Field Proliferation Core, Plasma Machine Tool, Hadron Catalytic Refinery, Quantum Mass Spectrum Array and Superconducting Fusion Assembler | Seven `gtl` registrations and seven structure resources, using 0.5.6 predicates and recipe types. | Clean build and load validation passed; structure formation was not tested. |
| Four adaptive energy/laser hatches and the tower terminal | Five `gtocore` machine IDs; one shared 0.5.6 ledger hatch class; frequency, team, template tier, amperage, buffer, balance and ledger-rate checks. Template amps are capped before `voltage * amps` can overflow a native `long`. The terminal separates native team-ledger input/output/net flow from its four configured template limits. Frequency and templates use actual left-side pages. Hatch priority 0–4 orders scarce-ledger transfers, with higher values first. | Earlier registration and save entry passed; the latest rendering/UI repair has its own build and startup gate below. |
| Terminal on the wireless tower | One terminal may occupy a steel `A` position in the actual 0.5.6 tower pattern. Glass positions remain glass. | Pattern installation passed at load; formation was not tested. |
| Infinite Energy Unit | Existing `gtohjs:infinite_wireless_energy_storage_unit` ID at the tower's `D` positions; `2^126 - 1 EU` backing capacity, MAX tier, zero loss, `Long.MAX_VALUE` ledger rate, block-entity/item NBT handoff. Player-facing capacity and rate say animated rainbow “Infinite/无限”. | Existing tower parameter readback passed after the CoreMod correction; transfer, removal and charge handoff were not separately tested. |
| Frequency map and template list | `/gtohjs netmap` and `/gtohjs dumptemplates`; the latter exports the registered 0.5.6 template list. | Command registration passed; command output was not tested. |
| Status display | Native machine text and optional Jade providers. | Client entered a world with Jade loaded; individual overlays were not tested. |

The old ledger is team-wide. The accepted 0.5.6 transfer path does not emulate
dev19's dimension nodes, line tokens or native device rewrites. Native Core
wireless devices keep their 0.5.6 behavior. The abandoned fully adaptive
maintenance hatch is outside this port.

## Structure and renderer compatibility

### Deep Chemical Distorter

The additional `gtl/ChemicalDistortRegistration` owns the exact GTL 1.2.3.2
11 x 28 x 11 structure. Its dedicated recipe page and deferred native
Polymer Twisting Module recipe family are documented in
`47_deep_chemical_distorter.md`. Original materials/hatch positions remain;
nanite inputs/outputs are removed only from new copies, with voltage and
minimum heating-coil requirements lowered three tiers. Kotlin tooltip routing
reuses native full_color for the animated GregTech Leisure attribution.
Java 21 online IDEA clean build and addon-only deployment passed. PCL launch,
formation and actual processing are pending the user's test.

### Plasma Machine Tool recipe modes

`gtl/PlasmaMachineToolRegistration.java` owns the machine's recipe-type array.
The original nine native types (lathe, bender, compressor, forge hammer,
cutter, forming press, wiremill, extruder and polarizer) now also include
0.5.6 `GTORecipeTypes.ROLLING_RECIPES` (`gtceu:rolling`, rolling mill) and
`GTORecipeTypes.CLUSTER_RECIPES` (`gtceu:cluster`, cluster mill): eleven total.
Both added types have item-only IO. Existing pattern-buffer input, output
buses and `OrderAwareElectricMultiblockMachine` remain compatible.
The new modes are appended, preserving the original nine mode indices.
`PatternOrderMethods` checks the controller's available types dynamically,
so no second recipe path or copied recipes are needed. Native recipe IDs,
ingredients, output amounts, duration, EU/t and conditions remain native.
The existing registration validation compares the full array, and the native
multi-recipe UI/tooltip uses that same array. Java 21 online IDEA clean build
passed on retry in 42 seconds after the user addressed a Forge certificate
failure. The 864141-byte addon was deployed alone after backup/cache refresh
(`20261023-214827-plasma-rolling-cluster`). Current-run registration at
21:48:57 on 2026-10-23 lists all eleven types. Save entry at 21:50:00 passed;
no separate recipe-processing test followed.

### Four pattern-order machines: upgrade hatches

The Plasma Machine Tool, Hadron Catalytic Refinery, Quantum Mass Spectrum
Array and Superconducting Fusion Assembler now use the same
`PatternBufferPlacementMethods.serviceFace(casing)` at their existing eight X
and 26 Q positions. It replaces four identical service-rule copies, retaining
the existing pattern-buffer whitelist/minimum and native IO/module limits.
Accelerate and Overclock Hatches are optional, each with global maximum one.

All four retain `OrderAwareElectricMultiblockMachine`; its native CrossRecipe
parent owns thread, parallel and overclock execution, and the native power-
amplifier handling owns acceleration. PatternOrderMethods and the pattern-only
input boundary remain unchanged. Kotlin `GtlPortedMachineText.iv()` owns one
shared bilingual upgrade tooltip. No new controller or injection is required.
Java 21.0.5 online IDEA clean build passed in 14 seconds (nine tasks).
Packaged bytecode verifies all four shared calls and both added abilities.
The 863307-byte addon was deployed alone after backup/cache refresh
(`20261023-215423-pattern-machine-upgrades`). Current-run server start at
21:57:44 and player login at 21:57:51 on 2026-10-23 establish save entry
(PID 2508). The acceptance frame found the already-entered game; no separate
formation or hatch-operation test followed. No Git push.

### Platinum matrix follow-up

The platinum matrix follow-up is documented in `46_platinum_refining_matrix.md`.
Its existing recipe type is now exclusive to the migrated one-stop sludge
recipe; native cross-recipe execution and three optional upgrade hatch
abilities use its existing X service positions. Geometry and machine ID remain
unchanged. The recipe's final type-prefixed ID changes with its category.

The 0.5.6 wireless tower uses steel `A`, controller `B`, glass `C` and unit
space `D`. The terminal extends only `A`; the addon unit extends only `D`.
The tower pattern is installed at the end of `MultiBlockG.<clinit>` through the
existing CoreMod window, then checked during HJS load completion.

The exact 0.5.6 Core's nested GTMThings JAR does contain the native wireless
models. The earlier conclusion that they were absent was incorrect. Four
hatches now use `gtocore:block/manipulator` as their base model and texture,
reusing the existing casing-overlay renderer pattern. Energy input/output
share `gtmthings:block/machine/part/wireless_energy_hatch`; laser target/source
share `gtmthings:block/machine/part/wireless_laser_hatch.target`, matching the
native IV 4096A wireless hatch. The terminal uses the native workable-casing
renderer with the same manipulator texture and the wireless-energy-monitor
overlay. Native formed-part casing replacement is intentionally retained.

Missing packaged HJS assets were caused by the filename-keyed GTOLib resource
tree cache. The installed addon cache was still dated 01:45 despite subsequent
same-name JAR replacements. Keep the requested spe1 version; with the client
stopped, back up and refresh only its matching `.jar.bin` and the aggregate
`gtocore/cache/resources` index when deploying this correction. No runtime
cache-deletion hook is added to HJS.

The terminal homepage uses the native monitor's 236x125 display and scrolling
component panel. Its tower's team ledger supplies `EnergyStat` input, output
and net EU/t: native 20-tick samples, display text refreshed every 10 ticks.
The four dev15-style configuration rows remain separate (power output,
energy input, laser source, laser target). Disconnecting clears cached flow
text and displays `--` with the offline status while retaining template rows.
The UI does not call the native monitor's global observation/list-clearing
method. Frequency and template pages use `attachSideTabs` / `IFancyUIProvider`,
following the existing advanced-intake UI. The former configurator adapter
was replaced, not kept as a second UI path.

Configuration rows now include tier and voltage colored with `GTValues.VCM`,
the native tier main color. `Power.bufferCapacity()` is the single calculation
for the configured per-hatch buffer (64 ticks of configured power, saturated
to long); terminal descriptions and the actual hatch container both use it.
The hatch has a scrolling component display and separate labeled frequency /
priority input rows. It displays actual stored/capacity EU separately from the
available EU/t limit. Previously retained charge may temporarily exceed a new,
lower configured buffer; once the surplus drains, the normal capacity returns.

The frequency icon retains dev15's HJS drawing. The adaptive page uses the
unchanged GTCEu 26.10.41 `widget_icons.png` atlas, ENERGY_ON at zero-based
column 1 / row 16. The existing encoded-asset resource task decodes this atlas;
`THIRD_PARTY_NOTICES.md` records its upstream origin and license.

The first client run also exposed incorrect infinite-unit presentation and
terminal pages. The unit now uses dev19's original 16×64 four-frame texture,
stored as a text-encoded source asset and decoded by `processResources`; the
model points to the resulting HJS texture and its `.mcmeta` runs at 10 ticks
per frame. `InfiniteEnergyPresentation` is the single owner of the animated
word. The 0.5.6 GTMThings binding tool periodically calculates tower rate as
`capacity / 4096` and narrows it to `long`; `2^126 - 1` overflows that path.
The binding-tool CoreMod hook returns `Long.MAX_VALUE` only for a formed tower with
the HJS unit. The tower load hook binds the team ledger to that tower and
initializes the same rate, so the periodic refresh sees the HJS unit. Its native
MAX tier is retained.

The original infinite marker was incorrectly placed in an outer
`TraceabilityPredicate.test` override. Native `or` copies only inner predicates,
so the marker callback was lost despite the native BlockData entry matching.
The marker is now written inside the existing dataBlock callback, with immutable
positions. The same outer-wrapper problem affected the one-terminal guard;
the native global maximum predicate replaces that custom wrapper.

Native tower text groups units by tier and resolves each group using
`WirelessEnergyUnitBlock.get(tier)`, which labels an HJS MAX-tier subtype as
the ordinary MAX unit. A focused injection into the verified 0.5.6 group-label
lambda splits genuine MAX and HJS counts; it retains native MAX handling and
does not globally replace the native unit table. The existing load hook can
now detect HJS units and apply `2^126-1` capacity, zero loss, Long.MAX_VALUE
rate, MAX dimension tier and tower binding. It logs the values actually read
back from the ledger after application. Jade and infinite tower wording require
a formed tower with an actual HJS marker rather than a capacity threshold.

Tooltip routing was checked against the exact 1.20.1 Forge `BlockItem`: its
`appendHoverText` delegates to the block's `appendHoverText`. The 0.5.6
`WirelessEnergyUnitBlockItem` does not replace that method, so the HJS block
Kotlin override owns capacity, MAX voltage, rate and loss wording. The existing
Kotlin `ItemTooltipEvent` hook adds the HJS attribution only. Static routing
does not prove the visible client tooltip. The exact Core's native
`GTOMachineTooltips` and GTOLib's `TooltipsSortedWrapper` are Kotlin implementations;
the Java machine builder installs their suppliers. Language choice does not
replace callback verification. Adaptive item-specific detail suppliers are
currently unconfigured, so those items retain native generic text and HJS
attribution; the new terminal dashboard is a separate native UI callback.

All future tooltip implementation must use Kotlin. This correction replaces
the Java infinite-unit block/presentation classes and both adaptive Jade
providers with Kotlin classes at the same qualified names. The native item
name override now belongs to `InfiniteEnergyUnitItem.kt`; the frequency stick
also has a single Kotlin behavior owner. Registration remains Java and only
constructs these classes. Do not add parallel Java hover/name fallbacks.

The seven migrated GTL machines previously resolved names only inside the
Kotlin item-tooltip event. This hid missing `block.gtocore.<id>` language keys
in EMI, while the held-item name still displayed the raw key. Both locales now
provide the seven real block keys. The event no longer overwrites the first
name line, and unused alternate namespace/name mappings were removed. Detail
text continues through the existing Kotlin supplier/event path.

### Infinite-unit regression: use the established CoreMod owner

The user reported the unit still had no effect after the previous save-entry
acceptance. All parameter hooks had depended on two external Mixins. Reviewed
GTOLib `MixinExtension` source filters external mixins targeting GTO, GTCEu and
GTMThings classes. The exact production plugin is native, so its body is not
verified by that reference; its declaration and a successful addon compile
cannot demonstrate that our mixins were applied.

Replace those two mixins with the project's existing, working Forge CoreMod
path. The exact F: Core bytecode has two native tier reads and four void
returns in `loadContainer`, three returns in `customText`, and the verified
MAX-label lambda descriptor. The script checks these counts, redirects only
the capacity glass-tier read, calls the existing lifecycle owner after load,
releases before invalidation and calls Kotlin presentation for dashboard/count
text. The binding-tool method uses a guarded MAX-rate return for an actual HJS
tower, retaining its native body otherwise. Each transform emits an explicit
startup log. Former `WirelessTowerInfiniteUnitMixin` and `InfiniteUnitRateMixin`
sources/config entries were deleted; there is one active implementation path.

## Validation and open fidelity work

### Adaptive hatch controller recognition

The four hatch families use one `AdaptiveNetHatchPartMachine`. Its initial
zero-V/A container has native `IO.NONE`, so an early controller scan caches
`RecipeHandlerUnit.NO_DATA` in the part. `resetBasicInfo()` changes the live
container fields but does not invalidate that recipe-handler cache. The former
controller refresh could consequently keep omitting the configured hatch.

`applySpec()` now resets the native part cache before its existing controller
rebuild. This lets the native handler scanner include the container and the
controller reconstruct its snapshotted `EnergyContainerList`. `getTier()` also
returns the synchronized template tier instead of the fixed LV registration
tier. No global Core hook or duplicate handler implementation is involved.

For 64 ordinary HV wireless input templates the expected hatch specification
is 512 V, 128 A, 65,536 EU/t and 4,194,304 EU buffer. Amperage stacks; voltage
does not. Native controller aggregation and overclock rules remain native.
Java 21.0.5 online IDEA MCP `./gradlew clean build` passed in 20 seconds, all
nine tasks executed. Packaged bytecode confirms cache invalidation precedes
controller refresh and the tier accessor reads the configured tier. After
normal client exit and backup (`20261023-015628-adaptive-handler-cache`), the
863049-byte addon was deployed alone and both resource indexes refreshed.
Codex launched the exact F: client through the verified PCL window. PID 5932
entered the only remaining save at 01:58:05 on 2026-10-23. Testing stopped at
save entry; no separate blast-furnace UI/transfer check or Git push followed.

### Earlier validation records

- Jade EU bar follow-up: the text-only replacement removed the native graphical
  bar. The same Kotlin provider now renders Jade's native progress element
  with the original yellow fill, white text and gray border. Server data adds
  actual ledger capacity; BigDecimal DECIMAL32 computes stored/capacity before
  float conversion, clamped to 0–1 (zero capacity gives zero fill). The bar text
  retains `%s/%s EU` and animated infinity. Java 21.0.5 online IDEA clean build
  passed in 27 seconds (eight tasks executed, unchanged Java compilation from
  cache). Packaged bytecode calls the native progress API and syncs capacity.
  The 862960-byte addon was deployed alone after normal client exit and backup
  (`20261023-013249-jade-eu-bar`), with both resource indexes refreshed. Current
  provider registration logged at 01:34:11 and player login at 01:34:44 on
  2026-10-23 satisfies the user's gate. Client PID 18604 remains running; no
  separate bar visual/transfer test or Git push followed.
- Jade tower energy text: the existing Kotlin provider reads the actual ledger
  BigInteger balance and formats `%s/%s EU`, with animated infinite wording.
  Exact Core `CommonProxyMixin.loadComplete` overwrites Jade's annotation scan
  with a fixed plugin list that excludes HJS. The former annotation entry was
  ineffective. `GTOHJSJadePlugin.kt` now registers both existing HJS providers
  through the CoreMod head hook in `snownee.jade.Jade.loadComplete()`, after the
  native plugin list and before Jade sorts/finalizes providers. The annotation
  and Java plugin owner were removed. At priority 100, the infinite provider
  removes the native priority-0 `gtceu:electric_container_provider` row and
  emits the requested actual/infinite text. Java 21.0.5 online IDEA clean build
  passed in 58 seconds (nine tasks). Packaged text and Kotlin hook descriptor
  passed checks. The 862071-byte addon was installed alone after backup/cache
  refresh (`20261023-012538-jade-energy-row`). Current-run Kotlin registration
  logged at 01:27:40; both HJS UIDs now appear in Jade sorting and both plugin
  toggles are enabled. The PCL-launched client entered the only remaining save
  at 01:27:53 on 2026-10-23 (PID 8884). Tests stopped at that gate; the Jade row
  was not separately inspected visually, and no transfer test/Git push followed.
- CoreMod regression correction: Java 21.0.5 online IDEA MCP clean build passed
  in 18 seconds (nine tasks). Packaged Kotlin descriptors and five CoreMod
  entries were checked; obsolete infinite Mixins are absent. The 861517-byte
  addon was deployed alone after normal client exit and backup/cache refresh
  (`20261022-203159-infinite-coremod`). Current-run startup logs confirm all
  five transformations at 20:35:17–19. PCL-launched PID 26612 entered the only
  remaining save at 20:36:33 on 2026-10-22. Normal startup logged the existing
  tower at (-1,-60,7) applying one infinite unit at 20:36:45: capacity
  `85070591730234615865843651857942052863`, rate `9223372036854775807`, loss 0,
  voltage tier 14. This establishes actual parameter application. No separate
  transfer/visual/removal test or Git push was performed; client remains running.
- Previous voltage/buffer/infinite-unit/name correction: Java 21.0.5 online
  IDEA MCP clean build passed in 58 seconds, nine tasks executed. The packaged
  name keys and Kotlin classes passed static checks. After normal client exit,
  the 862161-byte addon was installed alone; its previous JAR and both resource
  indexes are backed up in `20261022-201049-kotlin-names-tower-buffer`.
  PCL launched the fixed client. Codex selected the only remaining save,
  `新的世界 (1)`; integrated-server start at 20:14:03 and player login at
  20:14:10 on 2026-10-22 satisfy the user's acceptance rule. The game remains
  running; testing stopped at entry. No infinite-tower apply/readback line
  appeared during startup, so parameter application and transfers were not
  independently demonstrated in a formed tower. No Git push was performed.
- Previous approved native-front, actual-flow and side-page correction: Java
  21.0.5 online IDEA MCP clean build passed in 15 seconds, all nine tasks
  executed. Packaging/native-resource checks passed. The 857500-byte JAR was
  installed alone after backing up the previous addon and both resource-index
  caches. The rebuilt addon index includes both new PNGs. The PCL-launched F:
  client entered the remaining save at 19:47:13 on 2026-10-22, satisfying the
  user's confirmed acceptance rule. No separate visual or transfer test was
  performed; the game remained running.
- Java 21.0.5, network-enabled IDEA MCP `./gradlew clean build`: passed after
  the native model and dev19 frequency icon correction, nine tasks executed.
- A direct Java 25 client launch from the Codex shell loaded the second save's
  integrated server, then failed before player login in JDK `Selector.open()`
  with `UnixDomainSockets.connect0` / `Invalid argument: connect`. The same
  selector probe fails under Java 17, 21 and 25 from that shell; this is a
  launch-environment issue, not an HJS client failure.
- With the final JAR installed, PCL launched the exact F: client. The only
  remaining save folder was `新的世界 (1)`; the 18:45:57 integrated-server start
  and 18:46:04 player login in `logs/latest.log` satisfy the user's stated
  save-entry acceptance rule. This run reported no missing adaptive overlay
  model paths. The client remained running at acceptance.
- The user entered a new save in the exact 0.5.6-beta client with the prior
  built JAR. Its log records HJS validation, integrated server start and player
  login; no new crash report was generated. That run exposed the five missing
  renderer models and does not validate the corrected JAR visually.
- The 0.5.6 Core stores wireless energy in a team-shared container keyed by
  owner UUID. The addon's infinite-unit release currently moves available
  team balance into an item when its tower invalidates. Interactions with more
  than one tower or native consumers need a fidelity decision before Git push.
- Visual appearance, tooltip wording and actual energy transfer were not
  separately observed in this run because the user directed work to end once
  the save was entered. Source publication remains pending under the earlier
  fidelity gate for team-shared charge handoff.

Preserve this evidence boundary. A successful build or save entry must not be
reported as proof of tower formation, power transfer, item charge recovery or
visual parity.
