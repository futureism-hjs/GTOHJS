# ME Super Wildcard Pattern Buffer / ME超级通配符样板总成

## 中文

- 注册 ID：`gtocore:me_super_wildcard_pattern_buffer`。
- 实现基于 GTOCore `MEWildcardPatternBufferPartMachine` 的通配符搜索、黑名单、概率过滤和输出类型限制；由于原类容量固定为 1 且关键状态私有，新类继承 `MEPatternBufferPartMachineKt` 并移植相关逻辑。
- 默认网格为 `3 x 3`，配置范围为每行 `3-8`、每页行数 `3-8`，固定只有一页，因此最大容量为 `8 x 8 = 64`。
- 配置修改需要重启游戏。槽位按线性索引迁移；扩容保留样板和催化剂等完整槽位数据，缩容删除超出新容量的槽位。
- 左侧机器类型选择复用 `ScrollablePatternBufferModeFancyConfigurator`，最多显示 5 行并支持滚轮。
- 渲染器使用 `amprosium_active_casing` 外壳，以及原生通配符总成的红色 `12 x 12` 前脸覆盖。
- 机器能力与原生通配符总成一致：物品输入、流体输入和双输入能力。
- 机器同时提供物品输入、流体输入、物品输出、流体输出、双输入和双输出能力。配方产物优先直接输出到 ME 网络；网络离线时持久化暂存，恢复后自动重试。
- 每个通配符样板槽只能读取自己的编程电路、物品催化剂和流体催化剂，并可读取总成级共享催化剂；不能读取其他样板槽的私有催化剂。生成样板同时保存精确对象路由和基于 `equals/hashCode` 的稳定路由；若复制后的样板无法唯一确定源槽，则拒绝本次执行并交由 AE 重新规划，绝不回退到 0 号槽或遍历其他槽。

## English

- Registry ID: `gtocore:me_super_wildcard_pattern_buffer`.
- The implementation preserves GTOCore's wildcard search, blacklist, chance filtering, and output-type limits. The native class is fixed to one slot and keeps essential state private, so the custom class extends `MEPatternBufferPartMachineKt` and ports the behavior.
- The default grid is `3 x 3`. Columns and rows each accept `3-8`; the buffer always has one page, so maximum capacity is `8 x 8 = 64`.
- Configuration changes require a restart. Slots migrate by linear index; expansion preserves complete slot data, while shrinking deletes overflow slots.
- Machine mode selection uses `ScrollablePatternBufferModeFancyConfigurator`, showing at most five rows with mouse-wheel scrolling.
- Rendering combines the `amprosium_active_casing` shell with the native wildcard buffer's red `12 x 12` front overlay.
- The buffer provides item import, fluid import, item export, fluid export, dual-input, and dual-output abilities. Outputs are sent directly to the ME network when available, persisted while offline, and retried after reconnection.
- Each wildcard pattern slot is isolated from other slots' private catalyst
  inventories. Search and execution read the slot's own circuit/item/fluid
  inventories and the machine-level shared inventories, which remain usable
  by every slot. Generated patterns retain both exact-object routes and stable
  `equals/hashCode` routes. If a copied detail cannot be mapped to exactly one
  source slot, the push is rejected for AE to re-plan; it never falls back to
  slot 0 or probes another slot.

## Verification

- Java 21 toolchain with Java 17 target bytecode.
- `gradlew build --no-daemon`: passed on 2026-07-30.
- JAR resources contain both valid `en_us.json` and `zh_cn.json` entries for the new machine and configuration.
