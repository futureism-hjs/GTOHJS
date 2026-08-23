# GTOHJS Preloaded AE Component Packs

**Status:** 2.0-alpha standalone baseline (internal documentation)  
**Runtime:** Minecraft 1.20.1, Forge 47.4.20, GTO 0.5.6-beta, Java 21  
**Scope:** Three independent `gtohjs` items; no AE2, GTOCore, GTOLib, or EMI source or resource modification.

> 2.0-alpha standalone constraint: `ME Placement Tool for gto` is a fully independent mod.
> GTOHJS does not declare it as a dependency and does not reference any `meplacementtool:*` runtime item key.
> The two mods can be installed and run independently.

> [!WARNING]
> This document records the storage format observed in the current GTO version and its implementation constraints. External storage is server-side data and cannot be forged by copying client ItemStack NBT. Any initialization failure must preserve the original pack and report the error; it must never silently create a partial pack.

## 1. Items and Source Packages

| New item ID | Display name | Type count in source pack | Source backing UUID (audit only) |
| --- | --- | ---: | --- |
| `gtohjs:basic_ae_component_pack` | Basic AE Component Pack | 123 | `517b6ab8-6fd8-46a1-b514-b391de963d66` |
| `gtohjs:ae_machine_component_pack` | AE Machine Component Pack | 42 | `ee7bd918-e035-408e-ac83-37cacae6aff6` |
| `gtohjs:advanced_ae_hatch_component_pack` | Advanced AE Hatch Component Pack | 21 | `5f6bc61c-96ec-4249-bffa-89d47bc874d3` |

The source consists of three `ae2:portable_item_cell_256k` stacks in the player inventory of the test world `新的世界`. UUIDs are used only for one-time reading and auditing; runtime code must never write those UUIDs into new ItemStacks or make two new packs share one UUID.

## 2. Observed GTO External-Storage Format

The modified GTO/AE2 portable component cell stores only a summary and a backend pointer in ItemStack NBT:

```text
u: [I;4]                     backing-storage UUID
type: int                    current unique item-type count (cached summary)
byte: long                   used-byte count (cached summary)
internalCurrentPower: double current AE power
```

Actual item keys and quantities are stored outside ItemStack NBT under the world's external directory:

```text
<world>/data/storage_data/a/<uuid>
```

All three files were parsed with this exact format through EOF, with no duplicate entries or trailing bytes:

1. A four-byte big-endian signed `int` entry count.
2. For each entry, an unnamed NBT `TAG_Compound`; observed keys are `#c="ae2:i"` and `id=<ResourceLocation>`.
3. An immediately following eight-byte big-endian signed `long` item quantity.

`data/storage_cell_data.dat` is not the item list for these three packs. Do not infer their contents from it, and do not have the mod edit world files directly.

## 3. Preload Constants and the Meaning of "16M"

Every item key from each source pack must be written with this quantity:

```java
16L * 1024L * 1024L == 16_777_216L
```

Here, `16M` uses the binary 16 Mi convention common to AE2/GTO, not decimal 16,000,000. The package quantities are:

| Pack | Keys retained | New quantity per key | Initial power |
| --- | ---: | ---: | ---: |
| Basic AE pack | 123 | `16_777_216` | `20_000.0d` |
| AE Machine pack | 42 | `16_777_216` | `20_000.0d` |
| Advanced AE Hatch pack | 21 | `16_777_216` | `20_000.0d` |

The user explicitly requires normal 256K-cell capacity to be ignored. Preloading is therefore a controlled one-time initialization operation: it must directly use the authoritative write/database-creation path of GTO's modified backend, rather than an interactive insertion path that rejects content under the ordinary 256K capacity limit. After initialization, the real AE2/GTO portable-cell API remains responsible for reading, extracting, displaying, and saving the cell. Do not forge `byte`, `type`, or capacity summaries manually.

If the backend API cannot safely write beyond capacity, or if all keys cannot be read back after the write, initialization must fail and report `cannot preload 16M with the current GTO backend`. It must not reduce quantities, remove keys, or create an alternative with a shared UUID.

## 4. Server-Side Exactly-Once Initialization Contract

Initialization must run only on the server and succeed only once per new ItemStack:

1. Obtain the complete key set from the source-pack definition/read-only snapshot; never infer keys from display names.
2. Generate a unique persistent backing UUID for the new pack.
3. Create storage through the real backend API and write `16_777_216L` for every key.
4. Set portable-cell power to `20000.0d`.
5. Let the API recalculate `type` and `byte`; never copy the source pack's summaries.
6. Write an initialization marker, or perform an equivalent backend-existence check. No later tick, GUI open, or client synchronization may reinitialize the pack.
7. Save the world and verify UUID, key set, quantities, and power after the next server start.

The client only displays the synchronized ItemStack and must not create or modify external storage. Copying an item must also follow the new-UUID/new-backend-record path. Directly copying `u` makes two items share one inventory and introduces data races during extraction, saving, or destruction.

## 5. Historical Migration and fix1 Standalone Behavior

Per7 expanded the Basic pack from 123 to 128 types by adding `gtohjs:me_placement_tool`, `gtohjs:multiblock_placement_tool`, `gtohjs:me_cable_placement_tool`, `gtohjs:prism_core`, and `gtohjs:key_of_spectrum`. It expanded the Advanced AE Hatch pack from 20 to 21 types by adding `gtocore:me_wireless_connection_machine`. Each of the six added keys received `16_777_216`.

Do not migrate an existing item by clearing its initialization marker and rebuilding it. That changes its backing UUID and discards player-modified inventory. The correct migration reads the authoritative map for the original UUID, copies every old key and value, writes 16M only for absent per7 keys, recalculates `byte`/`type`, and reads every key back from the GTO backend to prove that all old quantities remain unchanged and all new quantities are exact. While the ItemStack still reports content version 1, perform a synchronous world save. Commit content version 2 only after external storage is persisted and `CellDataStorage.isDirty()` returns to zero. Migration does not reset old keys or current power. If validation or saving fails, restore the original map, byte value, and summaries and durably persist the rollback. If the process stops after backend writing but before the version marker is written, the next run sees the new keys and completes only validation and version commit without adding quantities again.

During the beta split, Basic-pack content version 3 wrote five `meplacementtool:*` keys into new instances and migrated existing version 1 or version 2 stores in place to include those keys.

2.0-alpha removes that cross-mod content contract. Current new instances contain only the original 123 Basic AE keys and use a constructor path with no external migration list. Existing version 2/3 packs retain their backing UUID, quantities, power, and content version. GTOHJS does not rewrite or remove old third-party keys from their external storage. When the third-party mod is absent, the current AE2/GTO external-store loader decides how unknown keys appear, but they cannot block GTOHJS registration or startup.

## 6. Verification Checklist

After the server first creates the items, verify at minimum:

- all three new-item UUIDs differ from each other and from the three source UUIDs above;
- the new packs' `type` values are 123, 42, and 21 respectively;
- every expected `ae2:i` key appears exactly once with quantity `16_777_216`, with no extra keys;
- `internalCurrentPower == 20000.0d`;
- data remains correct after reopening the portable-cell UI, extracting one item, saving, and restarting;
- extraction changes the actual quantity and initialization does not reset it to 16M on the next tick;
- EMI reads only the normal AE2/GTO item definitions and requires no EMI source change.

Verification logs should record new UUIDs, type counts, total entry counts, and failure reasons, but must not copy a complete item list or player save into the JAR. Stop deployment and give the log to the developer if any step fails.

## 7. Resource Constraints

All three item models directly inherit `ae2:item/portable_item_cell_256k`, fully reusing AE2's 256K portable item-cell model, four texture layers, and display transforms. LED and housing tinting still call AE2's `AbstractPortableCell.getColor`. GTOHJS neither copies nor overrides AE2 textures. English and Chinese localizations explicitly state `16M / 16,777,216 of each item` and `20,000 AE full charge by default`.
