# GTOHJS Preloaded AE Component Packs

**Status:** 4.0-per3 cluster-recipe baseline (internal documentation)  
**Runtime:** Minecraft 1.20.1, Forge 47.4.20, GTO 0.5.6-beta, Java 21  
**Scope:** Two independent `gtohjs` items; no AE2, GTOCore, GTOLib, EMI, or player-world source modification.

> [!WARNING]
> This document records the external-storage format observed in the target GTO version and the implementation contract. External storage is server-side data and cannot be recreated by copying an ItemStack NBT payload. Initialization failure must preserve the original pack and report the error; it must never silently create a partial pack.

## 1. Active Items and Source Packages

| Active item ID | Display name | Type count in source pack | Source backing UUID (audit only) |
| --- | --- | ---: | --- |
| `gtohjs:normal_ae_component_pack` | Normal AE Component Pack | 129 | `70ea8acb-a0f0-45f7-a0cf-4a94ba17934a` |
| `gtohjs:super_ae_component_pack` | Super AE Component Pack | 17 | `50f86f8d-70c7-46e3-8300-e6c7fd49f282` |

The source consists of two `ae2:portable_item_cell_256k` stacks in the player inventory of the test world `新的世界`. Their UUIDs are audit inputs only. Runtime code must never reuse a source UUID, source current power, cached `type` or `byte` value, or source item quantity.

The prior `basic_ae_component_pack`, `ae_machine_component_pack`, and `advanced_ae_hatch_component_pack` registrations are removed. This replacement does not migrate, rewrite, or directly edit any existing player external-storage record.

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

Both source files were parsed through EOF with no duplicate item IDs or trailing bytes. The binary format is:

1. A four-byte big-endian signed `int` entry count.
2. For each entry, an unnamed NBT `TAG_Compound`; observed fields are `#c="ae2:i"` and `id=<ResourceLocation>`.
3. An immediately following eight-byte big-endian signed `long` source quantity.

`data/storage_cell_data.dat` is not the item list for either package. Do not infer contents from it, and do not have the mod edit world files directly.

## 3. Preload Constants

Every item key from each source package is written with:

```java
16L * 1024L * 1024L == 16_777_216L
```

Here, `16M` uses the binary 16 Mi convention common to AE2/GTO, not decimal 16,000,000.

| Pack | Keys retained | New quantity per key | Initial power |
| --- | ---: | ---: | ---: |
| Normal AE Component Pack | 129 | `16_777_216` | `20_000.0d` |
| Super AE Component Pack | 17 | `16_777_216` | `20_000.0d` |

The packages intentionally bypass the ordinary interactive 256K insertion capacity. Preloading directly uses GTO's authoritative external-store creation path. After initialization, the normal AE2/GTO portable-cell API remains responsible for reading, extracting, displaying, and saving the cell. Do not forge `byte`, `type`, or capacity summaries manually.

## 4. Server-Side Exactly-Once Initialization Contract

Initialization runs only on the server and succeeds only once per new ItemStack:

1. Resolve the complete item-ID list from the checked-in source-package snapshot.
2. Generate a fresh persistent backing UUID.
3. Create storage through the GTO backend and write `16_777_216L` for every key.
4. Set portable-cell maximum and current power to `20000.0d`.
5. Let the API recalculate `type` and `byte`; never copy source summaries.
6. Commit the initialization marker only after every key and summary check succeeds.
7. Save the world and verify UUID, key set, quantities, and power after the next server start.

The client displays only the synchronized ItemStack and must not create or modify external storage. Copying an item must use the new-UUID/new-backend-record path. Directly copying `u` would make two items share one inventory and introduce extraction, saving, and destruction races.

## 5. Replacement Boundary

`PreloadedPortableCellItem` remains the only implementation path. The two item registrations provide its package name and immutable item-ID list only; they do not introduce a second cell-storage implementation or a new migration path.

The removed item IDs intentionally have no compatibility aliases. Existing instances of those removed IDs are outside this task's storage contract and must not be reinitialized, cleared, or mutated by the new registrations.

## 6. Verification Checklist

After the server first creates the items, verify at minimum:

- each new UUID differs from the two source UUIDs and from the other new package;
- the new `type` values are 129 and 17 respectively;
- every expected `ae2:i` key appears exactly once with quantity `16_777_216`, with no extra keys;
- `internalCurrentPower == 20000.0d`;
- data remains correct after reopening the portable-cell UI, extracting one item, saving, and restarting;
- extraction changes the authoritative quantity and initialization does not reset it on the next tick;
- EMI reads the normal AE2/GTO item definitions without an EMI source change.

Verification logs should record generated UUIDs, type counts, total entry counts, and failure reasons, but must not copy complete player storage or player-save data into the JAR. Stop deployment and give the log to the developer if any check fails.

## 7. Resource Constraints

Both item models directly inherit `ae2:item/portable_item_cell_256k`, reusing AE2's portable-cell model, texture layers, and display transforms. LED and housing tinting call AE2's `AbstractPortableCell.getColor`. GTOHJS does not copy or override AE2 textures. English and Chinese localizations state the `16M / 16,777,216` quantity and `20,000 AE` initial charge.
