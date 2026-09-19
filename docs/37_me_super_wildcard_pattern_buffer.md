# ME Super Wildcard Pattern Buffer

- Registry ID: `gtocore:me_super_wildcard_pattern_buffer`.
- The implementation preserves GTOCore `MEWildcardPatternBufferPartMachine` wildcard search, blacklist, chance filtering, and output-type limits. Because the native class is fixed at one slot and keeps essential state private, the custom class extends `MEPatternBufferPartMachineKt` and ports the relevant behavior.
- The default grid is `3 x 3`. Columns and rows each accept `3-8`; the buffer always has one page, so maximum capacity is `8 x 8 = 64`.
- Configuration changes require a game restart. Slots migrate by linear index: expansion preserves complete slot data, including patterns and catalysts, while shrinking deletes slots beyond the new capacity.
- Left-side machine-mode selection reuses `ScrollablePatternBufferModeFancyConfigurator`, showing at most five rows with mouse-wheel scrolling.
- Rendering combines the `amprosium_active_casing` shell with the native wildcard buffer's red `12 x 12` front overlay.
- The buffer provides item import, fluid import, item export, fluid export, dual-input, and dual-output abilities. Recipe products are sent directly to the ME network when possible, persist while the network is offline, and retry automatically after reconnection.
- Each wildcard pattern slot can read only its own programmed circuit, item catalysts, and fluid catalysts plus machine-level shared catalysts. It cannot read another slot's private catalysts. Generated patterns retain both exact-object routes and stable `equals/hashCode` routes. If a copied pattern cannot be mapped uniquely to one source slot, execution is rejected so AE can re-plan; it never falls back to slot 0 or probes other slots.

## Verification

- Java 21 toolchain with Java 17 target bytecode.
- `gradlew build --no-daemon`: passed on 2026-07-30.
- JAR resources contain valid `en_us.json` and `zh_cn.json` entries for the new machine and configuration.
