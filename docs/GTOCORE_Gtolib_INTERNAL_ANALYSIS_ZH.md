# GTOCore 0.5.6-beta / GTOLib 26.7.4 Internal Code Audit

**Audit date:** 2026-07-17  
**Objective:** Establish a verifiable code baseline for refactoring GTOHJS single-block and multiblock GregTech machine registration.  
**Constraint:** No external GTO development resource was modified.

## 1. Executive summary

1. GTOCore is not a black box. The `GTOCore` repository in the resource directory matches the target JAR version at commit `63f96666886a696e3064bff8361cb9c709e8ac5b`, titled `0.5.6 (#489)`, so its complete source can be audited directly.
2. GTOLib 26.7.4 is a Java shell around an encrypted native implementation. Many methods among its 807 classes are only `native` declarations or `UnsatisfiedLinkError("Not Impl")` shells. The real implementation resides in 1,295 `native0/**/*.prod.bin` files (about 5.24 MB) and `native0/ntpt.bin`; these are high-entropy binaries, not JVM classes.
3. The community `gtolib_3` source can recover older Java API semantics but cannot directly replace 26.7.4. The current version relocates Registrate from `com.tterrag.registrate` to `com.gto.registrate` and changes some method signatures.
4. GTO machine definitions are not ordinary Forge DeferredRegister entries. They depend on GTOLib's `GTORegistration.GTO/GTM`, GTCEu Registrate, Mixins that replace `BasicMachineDefinition` and `MultiblockDefinition`, and redirects around native GTCEu registration entry points.
5. Existing GTOHJS failures were not caused by builders being wholly uncallable. They came from missing the normal registration window and then unfreezing and manually patching multiple registries. Logs prove that 14 test lathes entered `GTRegistries.MACHINES`, followed by missing models, global duplicate registrations, concurrent modification, and incomplete registry state.
6. The community patcher is not a general decryptor. It uses JNI to call native APIs already bound by GTOLib and falls back to constructing Block, Item, and BlockEntityType objects by reflection after failure. It does not recover source from 26.7.4's `prod.bin` payloads.

## 2. Audit coverage

### GTOCore

- Target JAR: 8,639 entries and 1,681 classes.
- Complete source: 1,179 Java files and 59 Kotlin files.
- Java total: approximately 188,896 lines.
- Main source resources: 5,394 files.
- Embedded JAR dependencies: GTCEu 26.7.3, GTOLib 26.7.4, AE2 15.267.4, GTMThings 26.7.1, FastCollection, and Commons Math.
- Complete source and the decompiled JAR agree on key registration classes, versions, and ABI.

### GTOLib

- Target JAR: 2,204 entries and 807 classes.
- Vineflower output: 604 top-level Java files; 325 contain native methods.
- Visible shell layer: about 3,189 `native` declarations and 841 `Not Impl` throw sites.
- Encrypted implementation: 1,295 native payloads totaling 5,244,968 bytes.
- Community `gtolib_3`: 593 project-owned Java files totaling about 47,914 lines, sufficient to recover older core semantics.
- Because the current implementation is encrypted, this report cannot honestly claim to recover every native method in 26.7.4. It does cover the class structure, public/private ABI, machine-registration call chains, and relevant older semantics recoverable from community source.

## 3. GTOCore architecture

Java code is distributed primarily as follows:

| Module | Files | Java lines | Responsibility |
|---|---:|---:|---|
| `common` | 442 | 77,306 | Machines, blocks, items, saves, pipelines, and runtime logic |
| `data` | 265 | 72,007 | Recipes, data generation, trades, loot, and localization |
| `mixin` | 222 | 11,778 | Behavior replacement for GTCEu, AE2, Botania, Minecraft, and others |
| `client` | 82 | 9,086 | Renderers, GUIs, screens, and effects |
| `api` | 50 | 8,084 | Machine abilities, AE2, particle accelerators, and reporting APIs |
| `integration` | 83 | 7,455 | AE2, EMI, Jade, FTB, Apotheosis, and other integrations |

The Mixin configuration lists 224 entries: 191 common Mixins and 33 client Mixins. The source tree is dominated by AE2 (90) and GTCEu (49) Mixins.

## 4. Startup and registration lifecycle

The entry point is `com.gtocore.Core`. Its `@Mod` ID comes from GTOLib's `GTOCore.MOD_ID`, whose value is `gtocore`.

Actual primary chain:

```text
Forge constructs com.gtocore.Core
  -> DistExecutor creates CommonProxy / ClientProxy
  -> CommonProxy.init()
     -> GTOCreativeModeTabs.init()
        -> statically accesses GTOMachines.ARC_GENERATOR
        -> triggers GTOMachines.<clinit>, registering single-block machines
  -> GTORegistration.GTO.registerEventListeners(modEventBus)
  -> Forge RegisterEvent submits Registrate Block/Item/BET entries
  -> FMLCommonSetupEvent
     -> Data.init(), abilities, and runtime-data initialization
```

Key details:

- `GTOMachines.init()` is an ordinary static method, not an automatic JVM entry point.
- No current Java or Kotlin source directly calls `GTOMachines.init()`.
- Existing GTOHJS logs contain neither HEAD nor RETURN entries from its `GTOMachines.init` Mixin, proving that treating this method as a fixed registration window was incorrect.
- `GTOMachines.<clinit>` executes because the creative-tab icon accesses it; static field initialization performs single-block registration.
- Group classes such as `MultiBlockA` load through other static dependency and reflection-index chains. Older GTOLib `StringIndex` code reflectively loaded `MultiBlockA` through `MultiBlockZ`. This behavior cannot be reduced to an explicit call to `GTOMachines.init()`.

## 5. Machine-registration scale and organization

GTOCore definitions are concentrated in:

- `common/data/GTOMachines.java`: single-block machines, hatches, parts, monitors, and tier arrays.
- `common/data/machines/*.java`: 18 group files covering ordinary multiblocks, generators, magic, space, GCYM, GTAE, research, and optional machines.
- `utils/register/MachineRegisterUtils.java`: unified factory layer.

Static source counts:

- Nineteen core registration files contain 456 terminal `.register()` calls.
- Approximately 279 are direct multiblock-builder calls, 97 are direct single-block-builder calls, and 25 are tiered registration-helper calls.
- Tier helpers expand across several tiers, so 456 is not the final registry-entry count.
- Runtime logs showed approximately 2,321 entries in the original `GTRegistries.MACHINES` before test registration, including all GTCEu/GTO machines.

## 6. How GTOCore replaces GTCEu registration

### `GTRegistrationMixin`

Replaces global `GTRegistration.REGISTRATE` with `GTORegistration.GTM`. `GTM` uses the `gtceu` namespace, while `GTO` uses `gtocore`.

### `MachineDefinitionMixin`

Overrides `MachineDefinition.createDefinition(ResourceLocation)` to return GTOLib's `BasicMachineDefinition`. Every machine definition can then carry dynamic initial data and extended flags such as independent operation in space.

### `MultiblockMachineDefinitionMixin`

Overrides the multiblock definition factory to return `MultiblockDefinition`, which additionally stores:

- `maxTier`
- `upgradable`
- multiblock preview caches and part lists
- dynamic initial data
- space-operation flags

### `GTMachineUtilsMixin`

Takes over GTCEu registration of tiered machines, generators, large combustion engines, and turbines so that they use `GTOMachineBuilder` and GTO recipe modifiers.

### `GTMultiMachinesMixin`

Redirects selected ordinals in GTCEu multiblock static initialization, including electric furnaces, chemical reactors, distillation towers, vacuum freezers, steam macerators, and steam ovens, replacing them with GTOLib multiblock classes and extended tooltip/upgrade behavior.

### `GTMachineModify`

This is not a new registrar. It modifies existing GTCEu definitions in place by replacing patterns, recipe types, recipe modifiers, tooltips, rendering, and tier/ability semantics. This proves that "register a new machine" and "modify an existing machine" should be separate GTOHJS APIs.

## 7. GTOLib 26.7.4 machine API

### `GTORegistration`

Two singleton instances:

- `GTO`: `gtocore` namespace.
- `GTM`: `gtceu` namespace.

Core entry points:

```java
GTOMachineBuilder machine(String id, Function<MetaMachineBlockEntity, MetaMachine> factory)
GTOMachineBuilder machine(String id, Function<...> factory, TriFunction<...> blockEntityFactory)
MultiblockBuilder multiblock(String id, Function<MetaMachineBlockEntity, ? extends MultiblockControllerMachine> factory)
```

In 26.7.4, the constructor, static initializer, and methods above are all native/encrypted. Older source shows that they create `BasicMachineDefinition` or `MultiblockDefinition` and default to `MetaMachineBlock`, `MetaMachineItem`, and `MetaMachineBlockEntity`.

### `GTOMachineBuilder`

The current ABI supports tier, rotation, recipe type, recipe modifier, part ability, renderer, editable UI, tooltip, localization, space operation, no-recipe-modifier mode, and input/output limits inherited from GTCEu `MachineBuilder`.

Known older `register()` semantics: merge tooltips, invoke parent-builder registration, then write extension properties back to `IGTOMachineDefinition`.

### `MultiblockBuilder`

The current ABI supports:

- recipe type(s) and recipe modifier(s)
- pattern and sub-pattern
- appearance block
- workable casing renderer
- tier, maxTier, and upgradable
- generator, rotation, and flip settings
- ordinary, lossless, parallel, mana, and other overclock entry points
- module, coil, glass, laser, and recipe-type tooltips
- space-operation properties

Known older `register()` semantics: configure the tooltip builder, invoke parent registration, then write back `upgradable`, `maxTier`, and space-operation properties.

### Important differences between current and community versions

1. Registrate package: `com.tterrag.registrate` -> `com.gto.registrate`.
2. Recipe modifier argument: older versions commonly use `RecipeModifierFunction`; the current ABI uses GTCEu `RecipeModifier`.
3. The current version adds or retains methods such as `tooltipsComponent` and `tooltipsSupplier`, while some older helpers no longer appear in the current ABI.
4. Every core builder implementation in the current version is native; community source must not be copied directly into a project depending on 26.7.4.

## 8. How the patcher works

The community patcher's multiblock workflow:

1. Java ASM dynamically creates a subclass of `ElectricMultiblockMachine`.
2. Java constructs the factory, pattern factory, appearance-block supplier, and renderer ResourceLocations.
3. `System.load` loads a DLL and calls exported JNI methods.
4. JNI first calls `MachineRegisterUtils.multiblock(id, lang, factory)`, falling back to `GTORegistration.GTO.multiblock` on failure.
5. JNI chains `nonYAxisRotation`, `recipeTypes`, `pattern`, `block`, `workableCasingRenderer`, and `register`.
6. If normal Registrate products are incomplete, PostReg manually constructs and registers Block, Item, and BlockEntityType objects, then writes the definition supplier and renderer back through reflection.

Patcher limitations:

- Java class names and JNI symbols are hard-coded to `com.gtocutcorners...` and cannot be copied into GTOHJS unchanged.
- The default pattern is one fixed simple structure, not a general multiblock DSL.
- PostReg depends on private Forge/Minecraft/GTCEu fields and registry freeze state, making it version-fragile.
- It does not decrypt `prod.bin`; it only reuses GTOLib's already-bound native methods.
- It provides no complete general-purpose single-block registration implementation.

## 9. Evidence from current GTOHJS failures

The current source version is `fix12`; the available complete error report corresponds to `fix9`. The following therefore documents verified fix9 behavior and must not be presented as fix12 test results.

Log source: external acceptance asset `错误报告-2026-7-16_21.00.31`.

Confirmed facts:

1. The `GTOMachines.init` Mixin did not execute, so registration-window detection failed.
2. The fallback ran during `FMLCommonSetup`, after the normal Forge registry-collection phase.
3. `RegistrySafety` unfroze GT registries and Forge BLOCKS/ITEMS/BLOCK_ENTITY_TYPES; unfreezing the Vanilla wrapper failed with `IllegalAccessException`.
4. Fourteen `*_custom_lathe` definitions were written, and the MACHINES count increased one by one.
5. Missing `gtocore:block/machine/*_custom_lathe` models followed, showing that manual registration did not complete the normal Registrate client model/resource chain.
6. The same lifecycle produced many duplicate-registration errors from other mods, 20 COMMON_SETUP errors, concurrent modification, and a final crash. Global registry state had been corrupted.
7. Current JVMTI source also patches bytecode in `RecipeType.isFrozen()` and GTCEu `RecipeModifier.overclocking`; the latter changes the speed constant from `1.0` to `0.0`. This is unrelated to machine registration and must be removed from the refactor.

## 10. Technical boundaries for the refactor

1. Registration must occur during the normal Registrate collection window, never primarily during CommonSetup, ServerAboutToStart, or ServerStarting.
2. JVMTI should only establish one deterministic early call site. If targeting `GTOMachines`, patch the `<clinit>` that actually executes rather than relying on unused `init()`.
3. After the early call succeeds, use only the GTOLib builder's `register()` path; never normalize PostReg manual patching.
4. Single-block and multiblock APIs may share specification parsing, duplicate checks, and logging, but must call `machine` and `multiblock` builders respectively.
5. "Register a new machine" and "modify an existing definition" must be separate layers. A modification API should change only safely mutable definition properties after registry completion and must not re-register Block, Item, or BET entries.
6. Handle namespaces explicitly. When using `GTORegistration.GTO`, the definition ID belongs to `gtocore`; resource models, renderer paths, and conflict checks must agree.
7. Recipe speed, recipe freeze, and unrelated mod-lifecycle patches are outside this project and must be removed.
8. Extension parameters should cover at least factory type, tier, rotation, recipe types/modifiers, abilities, renderer, appearance block, pattern/sub-pattern, maxTier, upgradable, space flag, tooltip/localization, and block-entity factory.
9. Every registration should log its phase, thread, namespace/ID, definition type, and GT/Forge registry query results for verification in the designated log directory.

## 11. Recommended order for the next phase

1. Preserve this report and the failure logs as a baseline before deleting any existing GTOHJS Java/native source.
2. First create an early-registration probe containing only one single-block machine and one fixed 3x3x3 multiblock, with no unfreeze or PostReg behavior.
3. Verify Block, Item, BET, MachineDefinition, renderer/model, dedicated-server startup, and save reload.
4. Then implement the parameterized API and the existing-machine modification API.
5. Only afterward connect the HJS/KubeJS surface and recipe tasks.

## 12. Final assessment

GTOHJS is feasible, but success depends on returning calls to GTO/Registrate's real early window, not expanding registry unfreezing and patching. GTOCore's extension structure and the GTOLib builder ABI are sufficient for single-block and multiblock registration. The primary engineering risk is lifecycle and namespace/resource consistency, not missing builder functionality.
