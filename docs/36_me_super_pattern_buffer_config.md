# ME 超级样板总成配置 / ME Super Pattern Buffer Configuration

> 本文记录 GTOHJS 当前已验证的配置入口、容量计算和样板迁移行为。配置改动属于客户端和服务端共同使用的数据结构变更，修改后必须重启游戏。
>
> This document describes the verified configuration entry, capacity calculation, and pattern migration behavior. A configuration change alters a client/server data shape and requires a game restart.

## 中文

### 配置入口

在主菜单或暂停菜单打开 **模组（Mods）**，选择 **GTO HJS**，点击配置按钮。配置页面由 Configuration 模组注册；它不是 GTOCore 的配置页面，也不需要修改 EMI。

配置页使用普通纯色背景、填充式按钮和输入框。字段标题保持普通颜色，迁移规则显示在字段说明中，红色文字只用于不可忽略的警告。

### 字段

| 字段 | 范围 | 含义 |
| --- | --- | --- |
| 每行样板数量 | 1-18 | 每一页的列数 |
| 每页行数 | 1-10 | 每一页的行数 |
| 最大页数 | 1-10 | 可用页面数 |

总容量为：

```text
每行样板数量 × 每页行数 × 最大页数
```

当前实现的最大容量是 `18 × 10 × 10 = 1800` 个槽位。字段保存后由 Configuration 模组标记为“重启游戏后生效”。

只有 ME 超级样板总成会根据列数动态调整机器界面宽度，计算方式为 `max(176, 列数 × 18 + 14)`；使用 18 列时宽度为 338 像素。行数继续使用 GTO 原生纵向滚动区域，因此 10 行不会拉高整个界面。普通 GTO 样板总成和超级通配符样板总成仍保持原生 176 像素宽度。

### 超级通配符样板总成

通配符总成默认网格为 `3 × 3 = 9` 个槽位。每行样板数量和每页行数均可在 `3-8` 范围内调整，因此最大为 `8 × 8 = 64` 个槽位。该总成固定只有一页，不提供最大页数配置，也不能通过配置增加页数。

### 样板迁移

样板槽位使用稳定的线性索引：

```text
索引 = 页码 × (每行样板数量 × 每页行数)
     + 行号 × 每行样板数量
     + 列号
```

重新启动后，GTO/AE 样板物品的 NBT 槽位按 `Slot` 索引读取，因此：

- 新容量大于或等于原容量时，原有样板保持不变；改变列数、行数或页数只会按线性索引重新分页，样板中的催化剂、数量和其他 NBT 配置随样板一起保留。
- 新容量小于原容量时，只保留线性索引小于新容量的槽位；超出范围的样板及其全部 NBT 配置会被删除。这是配置页中的红色警告。
- 便携或掉落后重新放置总成时，GTO 原生加载器对扩容后的内部槽列表存在长度假设。GTOHJS 已覆盖该加载路径：只读取旧数据实际存在且落在新容量内的槽位，扩容不会越界，缩容不会读回溢出槽位。
- 配置改变不会重排样板内部 NBT；它只改变槽位到页面、行和列的映射。若需要完全清空样板，请在机器界面使用原有清空功能。

如果旧存档来自早于本迁移兼容修复的版本，首次加载前请备份存档。配置页面本身不会自动备份世界数据。

### 双向物品与流体 I/O

`gtocore:me_super_pattern_buffer`、`gtocore:me_super_pattern_buffer_proxy` 和
`gtocore:me_super_wildcard_pattern_buffer` 同时注册以下六项能力：

```text
IMPORT_ITEMS, IMPORT_FLUIDS, EXPORT_ITEMS, EXPORT_FLUIDS, DUAL_INPUT, DUAL_OUTPUT
```

普通超级总成与超级通配符总成仍按 GTO 原生方式为每个样板建立独立的 `IO.IN`
处理单元，并额外提供一个独立的 `IO.OUT` 处理单元。不能把输入和输出 trait 合并到同一个
`RecipeHandlerUnit`，因为 GTO/GTCEu 会要求同一单元中的 handler 方向完全一致。

物品和流体产物先按完整 AE Key（包括物品/流体 NBT）和 `long` 数量写入两个持久化
`KeyStorage`，随后使用 AE 能量把它们插入当前连接的 ME 网络。网络离线、能源不足或库存暂时
无法接收时，剩余产物不会丢失；总成每 20 tick 重试一次。待输出内容同时写入世界存档和便携
物品数据，重新放置后继续输出。

镜像保留 GTO 原生样板槽代理，仅为 `gtocore:me_super_pattern_buffer_proxy` 增加受 ID
限制的输出转发。每次模拟和执行都会重新确认镜像已绑定到实现 GTOHJS 输出接口的超级总成；
未绑定或绑定到普通 GTO 总成时拒绝输出，避免旧的无限输出缓存造成错误判定。

### 工作台配方

| 最终配方 ID | 图案 | 关键输入 | 输出 |
| --- | --- | --- | --- |
| `gtohjs:shaped/me_super_pattern_buffer` | ` A ` / ` B ` / `   ` | A=`gtocore:cell_component_64m`，B=`gtocore:me_extend_pattern_buffer_ultra` | `gtocore:me_super_pattern_buffer` |
| `gtohjs:shaped/me_super_pattern_buffer_proxy` | `A  ` / `   ` / `   ` | A=`gtocore:me_super_pattern_buffer` | `gtocore:me_super_pattern_buffer_proxy` |
| `gtohjs:shaped/me_super_wildcard_pattern_buffer` | `AB ` / `   ` / `   ` | A=`gtocore:me_wildcard_pattern_buffer`，B=`gtocore:me_super_pattern_buffer` | `gtocore:me_super_wildcard_pattern_buffer` |

三条配方通过现有 `Data.commonInit()` 原生窗口和 `VanillaRecipeHelper` 注册。raw ID 会由
`ShapedRecipeBuilder` 解析成上表的 `gtohjs:shaped/*` 最终 ID，并在服务端最终
`RecipeManager` 中校验配方类型与输出物品。

### 样板槽隔离

每个样板槽的槽内数据与其他样板槽相互隔离。一个样板槽只能读取自己的编程电路、物品催化剂
和流体催化剂，同时可以读取总成级共享的编程电路、物品催化剂和流体催化剂。共享催化剂会按
设计提供给所有样板槽，但任何其他样板槽的私有催化剂都不可见；镜像总成遵循相同边界。

### 翻译键

总成使用 GTO 机器注册实际读取的键：

```text
block.gtocore.me_super_pattern_buffer
block.gtocore.me_super_pattern_buffer_proxy
block.gtocore.me_super_wildcard_pattern_buffer
```

`item.*` 和 `machine.*` 兼容键也保留，便于物品提示和旧版界面读取。普通总成配置字段使用 `config.gtohjs.option.meSuperPatternBuffer.*`，通配符总成使用 `config.gtohjs.option.meSuperWildcardPatternBuffer.*`；中英文语言文件均为 UTF-8 严格 JSON。

## English

### Configuration entry

From the main menu or pause menu, open **Mods**, select **GTO HJS**, and press its configuration button. The screen is registered by the Configuration mod; it is separate from the GTOCore configuration screen and does not modify EMI.

The page uses a flat solid background with filled buttons and edit boxes. Field labels remain neutral. The migration rules are shown in the field descriptions, and red text is reserved for warnings that affect saved patterns.

### Fields

| Field | Range | Meaning |
| --- | --- | --- |
| Patterns per Row | 1-18 | Columns on each page |
| Rows per Page | 1-10 | Rows on each page |
| Maximum Pages | 1-10 | Number of pages |

Total capacity is:

```text
Patterns per Row * Rows per Page * Maximum Pages
```

The current implementation supports up to `18 * 10 * 10 = 1800` slots. Configuration marks all three fields as requiring a game restart.

Only the ME Super Pattern Buffer adjusts its machine UI width according to the configured column count, using `max(176, columns * 18 + 14)`. At eighteen columns its width is 338 pixels. Rows continue to use GTO's native vertical scrolling area, so ten rows do not increase the overall screen height. Native GTO pattern buffers and the super wildcard buffer keep the native 176-pixel width.

### Super wildcard pattern buffer

The wildcard buffer defaults to a `3 * 3 = 9` slot grid. Both patterns per row and rows per page are configurable from `3-8`, for a maximum of `8 * 8 = 64` slots. It always has exactly one page: there is no maximum-pages option and configuration cannot add pages.

### Pattern migration

Slots use a stable linear index:

```text
index = page * (patternsPerRow * rowsPerPage)
      + row * patternsPerRow
      + column
```

After the restart, GTO/AE pattern item data is read by its `Slot` index:

- When the new capacity is at least the old capacity, every stored pattern is retained. Changing columns, rows, or pages only repaginates the same linear sequence; catalysts, amounts, and other per-pattern NBT remain attached to their pattern.
- When the new capacity is smaller, only indices below the new capacity remain. Overflow patterns and all of their NBT are deleted. This behavior is called out in red in the configuration page.
- When a portable or dropped buffer is placed again, GTO's native loader assumes that the persisted internal-slot list is as long as the new configuration. GTOHJS overrides that load path and reads only entries that exist and fit, so expansion does not throw and shrinking does not restore overflow slots.
- Configuration changes do not rewrite a pattern's internal NBT. They only change the page/row/column mapping. Use the existing machine clear action if all patterns must be removed.

Back up a world before loading a save made with a version older than this migration fix. The configuration page does not create a world backup automatically.

### Bidirectional item and fluid I/O

`gtocore:me_super_pattern_buffer`, `gtocore:me_super_pattern_buffer_proxy`, and
`gtocore:me_super_wildcard_pattern_buffer` all register six abilities:

```text
IMPORT_ITEMS, IMPORT_FLUIDS, EXPORT_ITEMS, EXPORT_FLUIDS, DUAL_INPUT, DUAL_OUTPUT
```

The regular and wildcard super buffers retain GTO's independent `IO.IN` unit for each
pattern slot and add a separate `IO.OUT` unit. Input and output traits must not be folded
into one `RecipeHandlerUnit`, because GTO/GTCEu requires every handler in a unit to use the
same direction.

Item and fluid products are first written to two persistent `KeyStorage` buffers using the
exact AE Key, including NBT, and a `long` amount. They are then inserted into the connected
ME network using AE power. If the network is offline, lacks power, or cannot currently
accept the products, nothing is discarded and the buffer retries every 20 ticks. Pending
outputs are included in both world persistence and portable item data and resume after the
part is placed again.

The proxy retains GTO's native pattern-slot forwarding. A definition-ID-scoped bridge is
added only for `gtocore:me_super_pattern_buffer_proxy`. Every simulation and commit resolves
the current binding again and rejects output when the proxy is unbound or points at an
ordinary GTO buffer, avoiding a stale infinite-output decision.

### Crafting recipes

| Final recipe ID | Pattern | Key inputs | Output |
| --- | --- | --- | --- |
| `gtohjs:shaped/me_super_pattern_buffer` | ` A ` / ` B ` / `   ` | A=`gtocore:cell_component_64m`, B=`gtocore:me_extend_pattern_buffer_ultra` | `gtocore:me_super_pattern_buffer` |
| `gtohjs:shaped/me_super_pattern_buffer_proxy` | `A  ` / `   ` / `   ` | A=`gtocore:me_super_pattern_buffer` | `gtocore:me_super_pattern_buffer_proxy` |
| `gtohjs:shaped/me_super_wildcard_pattern_buffer` | `AB ` / `   ` / `   ` | A=`gtocore:me_wildcard_pattern_buffer`, B=`gtocore:me_super_pattern_buffer` | `gtocore:me_super_wildcard_pattern_buffer` |

All three recipes use the existing `Data.commonInit()` native window and
`VanillaRecipeHelper`. `ShapedRecipeBuilder` resolves each raw ID to the listed
`gtohjs:shaped/*` final ID, whose crafting type and output are checked in the server's final
`RecipeManager`.

### Slot isolation

Every pattern slot is isolated from every other slot's slot-specific data. A
slot can use its own circuit, item catalyst inventory, and fluid catalyst
inventory, plus the machine-level shared circuit/item/fluid inventories. The
shared inventories remain intentionally available to every slot; a catalyst
stored in another slot's private inventory is never visible. The mirror proxy
uses the same boundary.

### Translation keys

The machine names use the keys read by GTO's machine registration:

```text
block.gtocore.me_super_pattern_buffer
block.gtocore.me_super_pattern_buffer_proxy
block.gtocore.me_super_wildcard_pattern_buffer
```

The `item.*` and `machine.*` compatibility keys remain for item tooltips and older screens. Regular buffer fields use `config.gtohjs.option.meSuperPatternBuffer.*`, while wildcard buffer fields use `config.gtohjs.option.meSuperWildcardPatternBuffer.*`; both language files are strict UTF-8 JSON.
