# ME Placement Tool GTO Port

**Status:** Beta has been split from GTOHJS into the independent `ME Placement Tool for gto` mod and restored to the upstream `meplacementtool` technical namespace. The Java 21 clean build, GTO client loading, final recipe tables, and resource-cache rebuild passed acceptance. Actual placement behavior still requires hands-on player verification.  
**Target runtime:** Minecraft 1.20.1, Forge 47.4.20, GTO 0.5.6-beta, Java 21  
**Upstream:** ME Placement Tool `2.1.4-forge1.20.1`

## Beta Split Baseline

- Independent active project: `work/ME-Placement-Tool-for-gto`
- Formal source directory: `[your workspace directory]\ME-Placement-Tool-for-gto-Development-Project\ME Placement Tool for gto`
- Independent mod ID: `meplacementtool`
- All five items, models, translations, GUIs, and final recipe IDs use the upstream `meplacementtool:*` namespace.
- The menu, network channel, configuration lifecycle, and Forge entry point belong to the independent mod. GTOHJS no longer embeds Placement Tool Java, resources, or recipe registration.
- Five recipes continue to use an independent minimal coremod to inject into GTO's verified native `Data.commonInit()` window. Final IDs are `meplacementtool:shaped/*`.
- `ME Placement Tool for gto` and GTOHJS are fully independent. Neither declares the other as a dependency, GTOHJS does not reference `meplacementtool:*` item keys, and either mod can be installed and run separately.

> [!WARNING]
> This document records the current port boundary. Do not mark the feature `Verified` until the Java integration build succeeds and actual placement is tested in the GTO client.

## 1. Goal and Failure Baseline

The upstream tool can retain a wireless-access-point link, so the observed `links but cannot place` failure is not missing link NBT. GTO ships a customized AE2 runtime whose inventory matching, network extraction, and temporary held-stack behavior differ from the upstream development environment. The port must satisfy these constraints:

1. Create network actions with `IActionSource.ofPlayer(player, accessPoint)`, preserving player and wireless-access-point context.
2. Read matches from `getCachedInventory()` without modifying or retaining its shared `KeyCounter` or entries.
3. Perform extraction in `SIMULATE -> MODULATE -> placement` order.
4. Preserve the original `AEKey` from before extraction for rollback. After failed placement, reinsert that key; if reinsertion also fails, create an item stack from that key and return it to the player.
5. Never use a zero-count temporary `ItemStack` for rollback. Under GTO it may already behave as `AIR`.
6. Keep AE2 part placement on the target runtime's `PartPlacement.getPartPlacement/placePart` path.

## 2. Imported Resources

All resources use the upstream `meplacementtool` namespace:

- 5 item models: ME Placement Tool, ME Multiblock Placement Tool, ME Cable Placement Tool, Key of Spectrum, and Prism Core;
- 6 item textures plus animated-light metadata for the cable tool;
- 12 GUI textures and 1 cable-tool screen layout;
- 5 crafting-recipe specifications, registered through GTO's native code-recipe window;
- the Forge tools item tag, preserving upstream semantics with only the ME Placement Tool and ME Multiblock Placement Tool;
- matching `en_us` and `zh_cn` text.

No Fumo blocks, models, or translations, standalone mod icon, AE2 Guide pages, JEI resources, REI resources, or EMI integration resources were imported. GTOHJS does not modify EMI code or resources.

## 3. License Boundary

- Upstream ME Placement Tool source: `LGPL-3.0-only`, by MOAKIEE/moakiee, CystrySU, and `_leng`;
- Construction Wand source for the multiblock placement algorithm: MIT;
- Ars Nouveau source for the radial menu: LGPL-3.0;
- AE2 API: MIT; referenced AE2 implementation under its applicable LGPL terms;
- GUI textures modified upstream from AE2 textures: CC BY-NC-SA 3.0;
- item models and item textures by `_leng` (麦淇淋), Copyright (c) 2025-2026: CC BY-NC-SA 4.0.

See the root `THIRD_PARTY_NOTICES.md` for complete attribution. These third-party files are not covered by the CC BY-NC-SA 4.0 license for original GTOHJS assets.

## 4. Current Validation Status

- On 2026-07-30, clean Java 21 builds of GTOHJS beta and the independent mod both succeeded.
- The target client explicitly loaded `gtohjs-1.0-beta-for-gtocore-0.5.6-beta.jar` and `ME-Placement-Tool-for-gto-2.1.4-for-gtocore-0.5.6-beta.jar`, registered the GTO-compatible wireless-link handlers, and entered the test world.
- The independent JAR contains 130 entries: five items and models, six item-texture groups, twelve GUI textures, the cable-screen layout, and three languages. It contains no `assets/gtohjs` entry or technical lowercase `gtohjs` string.
- All five crafting recipes have final IDs under `meplacementtool:shaped/*`. The cable-tool ingredient retains the `ae2:smart_dense_cable` tag containing 16 colors plus Fluix.
- GTCEu's `ShapedRecipeBuilder.getId()` transforms raw ID `meplacementtool:<path>` into final ID `meplacementtool:shaped/<path>`. The alpha validator queried raw IDs incorrectly, falsely reported all five recipes missing, and deliberately stopped the server.
- The client ultimately loaded 12,526 recipes, validated 15 GTOHJS and 5 independent-mod crafting recipes, and EMI baked 85,320 recipes. Targeted counts were: old tool runtime IDs 0, tag errors 0, ME texture/model errors 0, and target registration failures 0.
- GTOCore had retained a resource cache for an older JAR with the same filename. That cache was renamed as a backup and rebuilt from the new JAR. The rebuilt cache contains none of the five old `gtohjs:*` tool IDs, and client logs no longer report old `forge:tools` references. Old-world statistics files may still report removed alpha statistic keys; this is a historical warning caused by returning to the upstream namespace, not a current registration failure.

Hands-on player verification still required:

- a wireless access point links all three tools;
- ordinary blocks, NBT-sensitive blocks, AE2 parts, and fluids place from network storage;
- insufficient network contents, blocked placement, and occupied targets neither lose nor duplicate items;
- multiblock bulk placement and undo leave no count discrepancy;
- all three cable modes, dye consumption, and Key of Spectrum upgrade work;
- basic placement does not depend on JEI, REI, or EMI.
