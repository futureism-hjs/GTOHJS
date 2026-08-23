# ME Super Pattern Buffer Configuration

> This document records the currently verified GTOHJS configuration entry, capacity calculation, and pattern migration behavior. A configuration change alters a client/server data structure and requires a game restart.

## Configuration Entry

From the main menu or pause menu, open **Mods**, select **GTO HJS**, and press its configuration button. The Configuration mod registers this screen; it is separate from the GTOCore configuration page and does not require any EMI modification.

The page uses a flat solid background with filled buttons and edit boxes. Field labels remain neutral, migration rules appear in field descriptions, and red text is reserved for warnings that cannot be ignored.

## Fields

| Field | Range | Meaning |
| --- | --- | --- |
| Patterns per Row | 1-18 | Columns on each page |
| Rows per Page | 1-10 | Rows on each page |
| Maximum Pages | 1-10 | Number of available pages |

Total capacity is:

```text
Patterns per Row * Rows per Page * Maximum Pages
```

The current implementation supports up to `18 * 10 * 10 = 1800` slots. After values are saved, the Configuration mod marks all three fields as taking effect after a game restart.

Only the ME Super Pattern Buffer adjusts its machine UI width to the configured column count, using `max(176, columns * 18 + 14)`. At eighteen columns, its width is 338 pixels. Rows continue using GTO's native vertical scrolling area, so ten rows do not increase the overall screen height. Native GTO pattern buffers and the Super Wildcard Pattern Buffer retain the native 176-pixel width.

## Super Wildcard Pattern Buffer

The wildcard buffer defaults to a `3 * 3 = 9` slot grid. Patterns per row and rows per page are each configurable from `3-8`, giving a maximum of `8 * 8 = 64` slots. It always has exactly one page, exposes no maximum-pages option, and cannot gain pages through configuration.

## Pattern Migration

Pattern slots use a stable linear index:

```text
index = page * (patternsPerRow * rowsPerPage)
      + row * patternsPerRow
      + column
```

After restart, GTO/AE pattern item NBT is read by its `Slot` index:

- When the new capacity is at least the old capacity, all stored patterns remain. Changing columns, rows, or pages only repaginates the same linear sequence; catalysts, amounts, and other per-pattern NBT remain attached to their pattern.
- When the new capacity is smaller, only indices below the new capacity remain. Overflow patterns and all of their NBT are deleted. The configuration page presents this behavior as a red warning.
- When a portable or dropped buffer is placed again, GTO's native loader assumes the persisted internal-slot list is as long as the new configuration. GTOHJS overrides that path and reads only entries that exist and fit, so expansion cannot overrun the list and shrinking cannot restore overflow slots.
- Configuration changes do not rewrite internal pattern NBT. They change only the slot-to-page/row/column mapping. Use the existing machine clear action when all patterns must be removed.

Back up a world before first loading a save made with a version older than the migration-compatibility fix. The configuration page does not create a world backup automatically.

## Bidirectional Item and Fluid I/O

`gtocore:me_super_pattern_buffer`, `gtocore:me_super_pattern_buffer_proxy`, and `gtocore:me_super_wildcard_pattern_buffer` all register these six abilities:

```text
IMPORT_ITEMS, IMPORT_FLUIDS, EXPORT_ITEMS, EXPORT_FLUIDS, DUAL_INPUT, DUAL_OUTPUT
```

The regular and wildcard super buffers retain GTO's independent `IO.IN` handler unit for every pattern slot and add one separate `IO.OUT` handler unit. Input and output traits must not be combined in one `RecipeHandlerUnit`, because GTO/GTCEu requires every handler in a unit to have the same direction.

Item and fluid products first enter two persistent `KeyStorage` buffers using the complete AE Key, including NBT, and a `long` amount. AE power is then used to insert them into the connected ME network. If the network is offline, lacks power, or cannot currently accept the products, nothing is discarded; the buffer retries every 20 ticks. Pending outputs persist in both world data and portable item data and resume after the part is placed again.

The proxy retains GTO's native pattern-slot forwarding and adds definition-ID-scoped output forwarding only for `gtocore:me_super_pattern_buffer_proxy`. Every simulation and execution resolves the current binding again. Output is rejected when the proxy is unbound or points to an ordinary GTO buffer, preventing a stale infinite-output cache from producing a false decision.

## Crafting Recipes

| Final recipe ID | Pattern | Key inputs | Output |
| --- | --- | --- | --- |
| `gtohjs:shaped/me_super_pattern_buffer` | ` A ` / ` B ` / `   ` | A=`gtocore:cell_component_64m`, B=`gtocore:me_extend_pattern_buffer_ultra` | `gtocore:me_super_pattern_buffer` |
| `gtohjs:shaped/me_super_pattern_buffer_proxy` | `A  ` / `   ` / `   ` | A=`gtocore:me_super_pattern_buffer` | `gtocore:me_super_pattern_buffer_proxy` |
| `gtohjs:shaped/me_super_wildcard_pattern_buffer` | `AB ` / `   ` / `   ` | A=`gtocore:me_wildcard_pattern_buffer`, B=`gtocore:me_super_pattern_buffer` | `gtocore:me_super_wildcard_pattern_buffer` |

All three recipes register through the existing native `Data.commonInit()` window and `VanillaRecipeHelper`. `ShapedRecipeBuilder` resolves each raw ID to the listed `gtohjs:shaped/*` final ID; the server's final `RecipeManager` validates its crafting type and output item.

## Pattern-Slot Isolation

Every pattern slot is isolated from every other slot's private data. A slot can read its own programmed circuit, item catalysts, and fluid catalysts, plus the machine-level shared circuit, item catalysts, and fluid catalysts. Shared catalysts are deliberately visible to all pattern slots, but private catalysts from any other slot are never visible. The mirror proxy follows the same boundary.

## Translation Keys

Machine names use the keys actually read by GTO machine registration:

```text
block.gtocore.me_super_pattern_buffer
block.gtocore.me_super_pattern_buffer_proxy
block.gtocore.me_super_wildcard_pattern_buffer
```

The `item.*` and `machine.*` compatibility keys remain for item tooltips and older screens. Regular-buffer fields use `config.gtohjs.option.meSuperPatternBuffer.*`, while wildcard-buffer fields use `config.gtohjs.option.meSuperWildcardPatternBuffer.*`. Both language files are strict UTF-8 JSON.
