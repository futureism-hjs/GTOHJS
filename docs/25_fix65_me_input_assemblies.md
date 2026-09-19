# GTOHJS fix65 ME Input Assemblies

**Baseline:** Minecraft 1.20.1, Forge 47.4.20, Java 21, GTCEu 26.7.3, GTOCore 0.5.6-beta, GTOLib 26.7.4, AE2 15.267.4.

## 1. Registered Parts

Fix65 adds two GTO multiblock parts:

| ID | Name | Tier | Combined sources |
| --- | --- | --- | --- |
| `gtocore:me_input_assembly` | ME Input Assembly | EV | `gtceu:me_input_bus` + `gtceu:me_input_hatch` |
| `gtocore:me_stocking_input_assembly` | ME Stocking Input Assembly | LuV | `gtceu:me_stocking_input_bus` + `gtceu:me_stocking_input_hatch` |

Both register `PartAbility.IMPORT_ITEMS`, `PartAbility.IMPORT_FLUIDS`, and `GTOPartAbility.DUAL_INPUT`. One part can therefore provide both item and fluid input to a multiblock controller and can satisfy structure positions that explicitly require a dual-input assembly. Attaching three ability labels to one block is insufficient; the controller class must also create both `NotifiableContentHandler` instances and connect them to the same AE node.

## 2. Renderer and Lifecycle

Both parts use an `OverlayTieredMachineRenderer` with `GTCEu.id("block/machine/part/me_pattern_buffer")`, the existing texture of `gtceu:me_pattern_buffer`. GTOHJS neither copies nor overrides GTCEu or GTOCore resources.

The registration entry point is `MEInputAssemblyRegistration.register()`. The coremod targets every `RETURN` in `com.gtocore.common.data.machines.GTAEMachines.<clinit>()V`; the Mixin retains only a fallback call in the same window. Do not move these parts to an ordinary `FMLCommonSetupEvent`.

## 3. Normal Input Assembly

`MEInputAssemblyPartMachine` maintains these values through one ME node:

- 16 item configuration positions;
- 16 fluid configuration positions;
- one shared circuit configuration;
- shared priority and distinct state;
- data-stick copy/paste for item, fluid, circuit, and distinct configuration.

It synchronizes target stock with the ME network every 40 ticks. When the machine is removed, contents actually buffered in either handler return to the same ME network. The upper half of its UI configures items and the lower half configures fluids.

## 4. Stocking Input Assembly

`MEStockingInputAssemblyPartMachine` does not copy network contents into local block inventory. Configuration slots display network inventory snapshots; when a recipe consumes an ingredient, it directly extracts items or fluids from the ME network with `Actionable.MODULATE`. Simulation uses `Actionable.SIMULATE`.

The screwdriver cycles four automatic-stocking modes: disabled, items and fluids, items only, and fluids only. Automatic modes independently select the 16 most abundant items or fluids in the network. Displayed amounts query the actually extractable amount through ME `SIMULATE`. Crossing the automatic/manual boundary clears automatically generated configurations so stale snapshots cannot become unintended manual entries. When multiple assemblies of the same kind belong to one controller, manual configuration rejects duplicate keys for the same medium, and automatic selection skips keys already claimed by another assembly. A network disconnect clears only generated automatic configurations and retains player-authored manual configurations.

## 5. Removed Content

Fix65 completely removes:

- `gtocore:custom_lathe`;
- `gtocore:large_custom_cutter`;
- `gtohjs:lathe/ev_machine_casing_to_iv_machine_casing`;
- `CustomLatheMachineCondition`;
- the corresponding Java registration classes, GTOMachines/GCYMMachines injections, localizations, source-hint list entries, and GTOHJS lathe textures;
- the `NativeLoader`, JVMTI `patcher.c`, and packaged DLL used only by the former custom-machine registration.

Historical test reports may still record that this content existed, but current source, resources, and registration scripts must not reference it.

## 6. Compile Dependencies

The GTOCore JAR exposes ABI references to AE2, GTO-AE, GTMThings, and RecipeSearch types, but those nested dependencies do not automatically enter the current ForgeGradle Java compile classpath. Fix65 stores local copies from the read-only resources under the project's `libs` directory and adds them as `compileOnly`: AE2 15.267.4, GTMThings 26.7.1, and RecipeSearch 1.3. At runtime, the modpack still uses GTOCore's nested dependencies and the existing FastRecipeSearch mod; the GTOHJS JAR does not package duplicate copies.

## 7. Verification Contract

Release checks must confirm both registry IDs, all three ability memberships, the Pattern Buffer renderer path, successful client reload, and absence of all three removed IDs from live logs and recipe registration. Functional in-world testing must also configure one item and one fluid on each assembly and confirm that a multiblock recipe sees and consumes both media from the same AE network.
