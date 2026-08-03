# GTOHJS 预载 AE 元件包 / Preloaded AE Component Packs

**状态 / Status:** 2.0-alpha 独立化基线（内部文档）  
**运行基线 / Runtime:** Minecraft 1.20.1、Forge 47.4.20、GTO 0.5.6-beta、Java 21  
**范围 / Scope:** 三个独立的 `gtohjs` 物品；不修改 AE2、GTOCore、GTOLib 或 EMI 的源码与资源。

> 2.0-alpha 独立化约束：`ME Placement Tool for gto` 是完全独立的 Mod。
> GTOHJS 不声明该 Mod 为依赖，也不引用任何 `meplacementtool:*` 运行时物品键。
> 两个 Mod 可以分别安装和运行。

> [!WARNING]
> 本文记录的是当前 GTO 版本的实测格式和实现约束。外置存储是服务端数据，不能通过复制客户端 ItemStack NBT 来伪造。任何初始化失败都必须保留原包并报告，不能静默生成半成品。

## 中文

### 1. 物品与来源包

| 新物品 ID | 显示名 | 来源包中的类型数 | 来源 backing UUID（仅审计） |
| --- | --- | ---: | --- |
| `gtohjs:basic_ae_component_pack` | 基础 AE 元件包 | 123 | `517b6ab8-6fd8-46a1-b514-b391de963d66` |
| `gtohjs:ae_machine_component_pack` | AE 机器元件包 | 42 | `ee7bd918-e035-408e-ac83-37cacae6aff6` |
| `gtohjs:advanced_ae_hatch_component_pack` | 高级 AE 仓室元件包 | 21 | `5f6bc61c-96ec-4249-bffa-89d47bc874d3` |

来源是测试世界 `新的世界` 玩家背包中的三个 `ae2:portable_item_cell_256k`。UUID 只用于一次性读取和审计；运行时绝不能把这些 UUID 写入新 ItemStack，也不能让两个新包共享 UUID。

### 2. GTO 外置存储格式（实测）

GTO/AE2 修改版便携元件单元的 ItemStack NBT 只保存摘要和后端指针：

```text
u: [I;4]                 backing-storage UUID
type: int                当前唯一物品类型数（缓存摘要）
byte: long               已用字节摘要（缓存摘要）
internalCurrentPower: double  当前 AE 电量
```

实际物品键和数量不在 ItemStack NBT 内，而在世界的外置目录：

```text
<世界>/data/storage_data/a/<uuid>
```

本次三个文件均按以下格式解析，并准确读到 EOF，没有重复条目或尾随字节：

1. 4 字节 big-endian signed `int`：条目数；
2. 每个条目是一个无名 NBT `TAG_Compound`，当前实测键为 `#c="ae2:i"` 与 `id=<ResourceLocation>`；
3. 紧随一个 8 字节 big-endian signed `long`：该物品的数量。

`data/storage_cell_data.dat` 不是这三个包的物品清单，不应据此推断内容，也不应由模组直接编辑世界文件。

### 3. 预载常量与“16M”含义

每个来源包的每一个物品键都必须写入：

```java
16L * 1024L * 1024L == 16_777_216L
```

这里的 `16M` 使用 AE2/GTO 常见的二进制 16 Mi 显示语义，不是十进制 16,000,000。三个包的数量映射如下：

| 包 | 要保留的键 | 每个键的新数量 | 初始电量 |
| --- | ---: | ---: | ---: |
| 基础 AE 包 | 123 | `16_777_216` | `20_000.0d` |
| AE 机器包 | 42 | `16_777_216` | `20_000.0d` |
| 高级 AE 仓室包 | 21 | `16_777_216` | `20_000.0d` |

用户明确要求忽略 256K 元件的正常空间容量，因此“预载写入”是受控的一次性初始化操作：它必须直接使用 GTO 修改版后端的权威写入/建库路径，不经过会按普通 256K 容量拒绝的交互式插入路径。初始化之后，单元仍由真实 AE2/GTO portable-cell API 负责读取、提取、显示和保存；不要手工伪造 `byte`、`type` 或容量摘要。

若后端 API 不允许安全的超容量直写，或者写入后无法读回全部键，初始化应失败并报告“无法在当前 GTO 后端预载 16M”，不得降低数量、删除键或生成共用 UUID 的替代品。

### 4. 服务端一次性初始化合同

初始化必须只在服务端执行，并且对每个新 ItemStack 只成功一次：

1. 从来源包定义/只读快照取得完整键集合，不能从显示名猜测键；
2. 为新包生成独立且持久化的 backing UUID；
3. 通过真实后端 API 创建存储并对所有键写入 `16_777_216L`；
4. 将 portable-cell 电量设为 `20000.0d`；
5. 让 API 重新计算 `type` 和 `byte` 摘要；代码不得把旧包的摘要复制过来；
6. 写入完成标记（或等价的后端存在性检查），随后任何 tick、打开界面或客户端同步都不得重复初始化；
7. 保存世界并在下一次服务器启动后验证 UUID、键集合、数量和电量。

客户端只显示同步后的 ItemStack，不得创建或修改外置存储。复制物品时也必须走新 UUID/新后端记录路径；直接复制 `u` 会让两个物品共享库存并造成提取、保存或销毁时的数据竞争。

### 5. 历史迁移与 fix1 独立化

per7 将基础包从 123 类扩展为 128 类，新增 `gtohjs:me_placement_tool`、`gtohjs:multiblock_placement_tool`、`gtohjs:me_cable_placement_tool`、`gtohjs:prism_core` 和 `gtohjs:key_of_spectrum`；高级 AE 仓室包从 20 类扩展为 21 类，新增 `gtocore:me_wireless_connection_machine`。六个新增键各写入 `16_777_216`。

已有物品不能清除初始化标记后重建，否则会更换 backing UUID 并丢弃玩家已修改的库存。正确迁移流程为：读取原 UUID 对应的权威映射，复制原键和值，只对不存在的 per7 新键写入 16M，重新计算 `byte`/`type` 摘要，并从 GTO 后端逐键读回，确认所有旧键数量原样保留、所有新键数量准确。随后在 ItemStack 仍标记为版本 1 时执行一次同步世界保存；只有外置存储已经落盘且 `CellDataStorage.isDirty()` 归零，才提交内容版本 2。迁移不重置旧键、不重置当前电量；验证或保存失败时恢复原映射、原字节值和原摘要并再次持久化回滚。若进程在后端写入后、版本标记写入前中断，下一次检查发现新键已存在，只完成验证和版本提交，不会重复增加数量。

Beta 拆分阶段的基础包内容版本为 3。该历史版本的新实例曾直接写入五个 `meplacementtool:*` 键，并把版本 1 或版本 2 的现有实例原地迁移到这些键。

2.0-alpha 取消此跨 Mod 内容合同。当前新实例只写入原始 123 个基础 AE 键，并使用无外部迁移列表的构造路径。已有版本 2/3 元件包保持原 backing UUID、既有数量、电量和内容版本；GTOHJS 不主动重写或删除其外置存储中的旧第三方键。第三方 Mod 未安装时，未知键如何显示由当前 AE2/GTO 外置存储加载器决定，但不会阻止 GTOHJS 自身注册或启动。

### 6. 验证清单

服务端首次生成后至少检查：

- 三个新物品的 UUID 两两不同，且均不同于上述三个来源 UUID；
- 新建包的 `type` 分别为 123、42、21；
- 每个预期 `ae2:i` 键恰好出现一次，数量恰为 `16_777_216`，没有额外键；
- `internalCurrentPower == 20000.0d`；
- 重新进出便携单元界面、提取一个物品、保存并重启后数据仍正确；
- 提取后数量发生真实变化，且不会因为下一 tick 被初始化逻辑重置为 16M；
- EMI 只读取正常的 AE2/GTO 物品定义，不需要任何 EMI 源码改动。

验证日志应记录新 UUID、类型数、总条目数和失败原因，但不要把完整物品清单或玩家存档复制进 JAR。若任一步失败，停止部署并把日志交给开发者。

### 7. 资源约束

三个物品模型直接继承 `ae2:item/portable_item_cell_256k`，因此完整复用 AE2 的 256K 便携物品元件单元模型、四层纹理和显示变换；LED 与外壳染色仍调用 AE2 的 `AbstractPortableCell.getColor`。GTOHJS 不复制或覆盖 AE2 纹理。中英文翻译明确标注“每种物品 16M / 16,777,216”和“默认满电 20,000 AE”。

## English

### 1. Items and source packages

The three independent items are `gtohjs:basic_ae_component_pack` (123 types), `gtohjs:ae_machine_component_pack` (42 types), and `gtohjs:advanced_ae_hatch_component_pack` (21 types). They were derived from the three 256K portable cells in the test player's inventory. The source backing UUIDs (`517b6ab8-6fd8-46a1-b514-b391de963d66`, `ee7bd918-e035-408e-ac83-37cacae6aff6`, and `5f6bc61c-96ec-4249-bffa-89d47bc874d3`) are audit evidence only. Never copy them into a new stack or share one UUID between stacks.

### 2. External storage contract

The modified GTO cell stores only a backing UUID (`u`), cached type count (`type`), cached byte summary (`byte`) and `internalCurrentPower` in ItemStack NBT. Item keys and amounts live under `<world>/data/storage_data/a/<uuid>`. The tested files contain a big-endian 32-bit entry count, followed by an unnamed NBT compound (`#c="ae2:i"`, `id=<ResourceLocation>`) and a big-endian signed 64-bit amount for each entry. `storage_cell_data.dat` is not the item-list backing store.

### 3. Initialization values

Every source key must be initialized to `16L * 1024L * 1024L = 16,777,216` (the binary 16M convention), and every new cell starts at `20000.0d`. The requested 16M preload intentionally ignores normal 256K capacity admission. Only a real GTO backend/direct initialization path may perform this one-time write; do not hand-edit `byte` or `type`. If the backend cannot safely accept the over-capacity write, fail loudly instead of reducing amounts or creating a shared-UUID fake.

### 4. Server-only, exactly-once lifecycle

Create a unique persistent backing UUID per stack, write the complete source-key set, set power, let the API recalculate summaries, and record an initialization marker or equivalent backend-existence check. Initialization is server-only and must not run again on ticks, client sync, GUI open, or item copy. A copied cell must receive a new backend record. On restart, verify UUID uniqueness, exact key set, 16,777,216 amounts, 20,000 AE power, persistence after extraction, and absence of reset-to-16M behavior.

### 5. Historical migrations and fix1 independence

Per7 expands the basic pack from 123 to 128 types with the five ported GTOHJS items and expands the advanced hatch pack from 20 to 21 types with `gtocore:me_wireless_connection_machine`. Existing initialized stacks retain their backing UUID, existing amounts and current charge. Migration copies the authoritative old map, adds only absent per7 keys at 16,777,216, reads every key and amount back from GTO storage, and performs a synchronous world save while the stack still advertises version 1. Content version 2 is committed only after the external store reports clean. Failed validation or persistence restores and durably saves the original map, byte count and ItemStack summaries; interrupted migrations are idempotent because existing new keys are never incremented again.

During the beta split, Basic-pack content version 3 added the five upstream `meplacementtool:*`
keys to version 1 or version 2 external stores. This is retained here as historical behavior only.

2.0-alpha removes that cross-Mod content contract. New Basic packs contain only the original
123 AE keys and use the constructor with no external migration list. Existing version 2/3 packs keep
their backing UUID, quantities, charge and stored content version; GTOHJS does not rewrite or delete
old third-party keys in their external store. Visibility of an unknown key while the third-party Mod
is absent is decided by the current AE2/GTO external-store loader, but it cannot block GTOHJS from
registering or starting. `ME Placement Tool for gto` and GTOHJS can be installed independently.

### 6. Resource and validation boundary

The item models directly inherit `ae2:item/portable_item_cell_256k`, reusing AE2's complete 256K portable item-cell model, four texture layers and display transforms. LED and housing tinting still delegates to AE2's `AbstractPortableCell.getColor`. GTOHJS does not modify AE2, GTOCore, GTOLib or EMI resources. Validation logs should contain UUIDs, type counts and failure reasons, but not embed player saves or a complete private inventory dump in the JAR.
