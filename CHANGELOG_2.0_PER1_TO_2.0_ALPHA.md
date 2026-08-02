# GTO HJS 2.0-per1 至 2.0-alpha 更新日志

[中文 README](README_ZH.md) | [English README](README_EN.md) | [完整历史更新日志](CHANGELOG.md)

本文单独记录从内部 `2.0-per1-for-gtocore-0.5.6-beta` 开发基线到 `2.0-alpha-for-gtocore-0.5.6-beta` 的累计变化，不修改或替代现有的完整历史更新日志。

## 中文

### 新增

- 新增 `gtocore:me_super_pattern_buffer`（ME 超级样板总成）。默认 `9×6×6=324` 个样板槽，可通过游戏内配置调整至最多 `18×10×10=1800` 个。
- 新增 `gtocore:me_super_pattern_buffer_proxy`（ME 超级样板总成镜像），保留 GTO 原生代理绑定方式并转发自定义超级总成的样板槽和输出能力。
- 新增 `gtocore:me_super_wildcard_pattern_buffer`（ME 超级通配符样板总成）。默认单页 `3×3=9` 个通配符槽，可配置至最多 `8×8=64` 个，固定只有一页。
- 三个超级总成均新增物品/流体输入、物品/流体输出、双输入和双输出能力。
- 新增完整 AE Key 与 `long` 数量输出桥。配方产物可直接进入 ME 网络，断网或网络容量不足时会持久化保存，并每 20t 自动重试。
- 新增三个超级总成的工作台有序配方，并把最终工作台配方校验范围扩展至全部 18 条 GTOHJS 工作台配方。
- 新增游戏内配置页，可调整普通超级总成的每行样板数量、每页行数和最大页数，以及超级通配符总成的行列数。
- 新增超级样板总成专用的动态宽度适配；列数增加时仅扩宽该总成，不改变普通 GTO 样板总成和超级通配符总成。
- 新增机器模式滚动选择页。超级样板总成和超级通配符总成一次最多显示 5 行所连接主控的机器类型。

### 机制调整

- 配置修改在重启后生效。扩容时按线性槽位顺序保留全部样板和每槽数据，包括电路、物品催化剂、流体催化剂、锁定状态与其他槽配置。
- 缩容时保留新容量能够容纳的线性槽位，删除溢出样板及其完整槽数据。
- 三个超级总成的每个样板槽均保持独立输入处理单元：只能读取本槽私有电路、物品和流体催化剂，同时仍可访问总成级共享催化剂。
- 镜像只有绑定到实现 GTOHJS 输出接口的 ME 超级样板总成时才转发输出，避免改变普通 GTO 镜像和样板总成行为。
- 配置页改为普通纯色背景和标准输入框，补齐中文标题、嵌套字段、范围说明与重启提示。
- `ME Placement Tool for gto` 改为完全独立 Mod；GTOHJS 不再声明强制依赖，也不再引用任何 `meplacementtool:*` 运行时物品 ID，两个 Mod 均可单独安装。
- 新建基础 AE 元件包恢复为 123 类，不再包含外部放置工具物品；已有版本 2/3 元件包保留 backing UUID、既有数量、电量、内容版本和旧外置存储，GTOHJS 不主动重写或删除旧第三方键。

### 修复

- 修复游戏内 GTO HJS 配置页标题和 Configuration 通用控件不显示中文翻译的问题。
- 修复超级样板总成高列数下界面宽度不足的问题，同时避免把动态宽度补丁错误应用到普通 GTO 总成。
- 修复超级通配符样板总成的“串配方”问题：2 号或其他通配符样板不再误用 1 号槽的私有催化剂。
- 通配符生成样板现在同时使用对象身份映射和等价样板映射确定源槽；多个槽生成相同结果时标记为不唯一。
- 无法唯一确认源槽的通配符请求现在直接拒绝执行，不再遍历所有槽，也不再默认回退到 0 号槽。
- 保留总成级共享催化剂语义，因此隔离修复不会阻止所有样板访问玩家明确放入共享区域的催化剂。

### 版本节点

| 版本 | 主要内容 |
| --- | --- |
| `2.0-per1` | 本文的内部开发基线。 |
| `2.0-per2` | 首个包含三种超级样板总成、双向 I/O、持久化 ME 输出和三条工作台配方的清洁构建。 |
| `2.0-per3` | 修复配置页中文翻译；普通超级总成支持动态宽度和最多 1,800 格；通配符总成上限确定为 8×8。 |
| `2.0-alpha` | 完成通配符源槽严格路由和样板隔离修复；彻底解除 GTOHJS 与 ME Placement Tool 的依赖；更新完整中英文功能清单，并在清洁源码包中保留全部开发文档。 |

### 验证

- 使用 Java 21 完成 `clean build`。
- 清洁构建产物为 `gtohjs-2.0-alpha-for-gtocore-0.5.6-beta.jar`。
- 固定 GTO 0.5.6-beta 客户端已加载新版本并到达标题界面；GTOHJS 机器、配方与样板总成注册完成。
- 客户端在完全移除 `ME Placement Tool for gto` JAR 后仍能完成 GTOHJS 注册并到达标题界面；恢复独立工具后两个 Mod 也可共同加载。
- 样板隔离修复已经过玩家实际测试确认。
- 未修改 EMI 源文件。

## English

This document records the cumulative changes from the internal `2.0-per1-for-gtocore-0.5.6-beta` development baseline to `2.0-alpha-for-gtocore-0.5.6-beta`. It does not modify or replace the existing full historical changelog.

### Added

- Added `gtocore:me_super_pattern_buffer` (ME Super Pattern Buffer). It defaults to `9x6x6=324` pattern slots and is configurable up to `18x10x10=1800`.
- Added `gtocore:me_super_pattern_buffer_proxy` (ME Super Pattern Buffer Proxy), preserving native GTO proxy binding while forwarding the custom super buffer's pattern slots and output ability.
- Added `gtocore:me_super_wildcard_pattern_buffer` (ME Super Wildcard Pattern Buffer). It defaults to one `3x3=9` slot page and is configurable up to `8x8=64`; it always has exactly one page.
- Added item/fluid input, item/fluid output, dual-input and dual-output abilities to all three super assemblies.
- Added complete-AE-key and `long`-quantity output bridging. Recipe products enter ME directly; blocked or offline output persists and retries every 20 ticks.
- Added shaped crafting recipes for all three assemblies and expanded final recipe-manager validation to all 18 GTOHJS shaped recipes.
- Added an in-game configuration page for the normal super buffer's columns, rows and pages and for the super wildcard buffer's rows and columns.
- Added dynamic width only for the ME Super Pattern Buffer. Increasing its column count does not resize native GTO buffers or the Super Wildcard buffer.
- Added scrolling machine-mode selectors. The Super Pattern Buffer and Super Wildcard Pattern Buffer display up to five connected-controller recipe types at once.

### Changed

- Configuration changes apply after restart. Expanding migrates patterns and complete per-slot data in linear slot order, including circuits, item/fluid catalysts, lock state and other slot settings.
- Shrinking retains the linear slot range that fits and deletes overflow patterns with their complete slot data.
- Every pattern slot in the three super assemblies remains a distinct input unit. It can read only its own private circuit, item and fluid catalysts while retaining access to machine-level shared catalysts.
- Proxy output forwarding is enabled only when bound to an ME Super Pattern Buffer implementing the GTOHJS output interface, so native GTO proxy and buffer behavior remains unchanged.
- The configuration page now uses a plain background and standard fields with complete Chinese titles, nested-field labels, ranges and restart warnings.
- `ME Placement Tool for gto` is now fully independent. GTOHJS no longer declares a mandatory dependency or references any `meplacementtool:*` runtime item ID, and either Mod can be installed alone.
- New Basic AE Component Packs contain 123 types and no external placement-tool items. Existing version 2/3 packs retain their backing UUID, quantities, charge, content version and old external storage without GTOHJS rewriting or deleting legacy third-party keys.

### Fixed

- Fixed missing Chinese translations for the GTO HJS configuration title and shared Configuration controls.
- Fixed insufficient UI width at high Super Pattern Buffer column counts without applying dynamic width to native GTO buffers.
- Fixed cross-pattern execution in the ME Super Wildcard Pattern Buffer: pattern slot 2 or later can no longer consume the private catalysts of slot 1.
- Generated wildcard patterns now use both identity and equivalent-detail maps to resolve their source slot. Results generated by multiple slots are marked ambiguous.
- Wildcard requests with no unique source are rejected instead of probing every slot or falling back to slot zero.
- Machine-level shared catalysts remain intentionally available to every pattern, so the isolation fix does not remove explicit shared-catalyst behavior.

### Version milestones

| Version | Main content |
| --- | --- |
| `2.0-per1` | Internal development baseline for this document. |
| `2.0-per2` | First clean build containing all three super pattern assemblies, bidirectional I/O, persistent ME output and three shaped recipes. |
| `2.0-per3` | Fixed configuration localization; added adaptive width and up to 1,800 normal slots; finalized the wildcard maximum at 8x8. |
| `2.0-alpha` | Completed strict wildcard source routing and pattern isolation, fully decoupled GTOHJS from ME Placement Tool, expanded the bilingual complete-content README and retained all development documents in the clean source bundle. |

### Verification

- Completed `clean build` with Java 21.
- Clean artifact: `gtohjs-2.0-alpha-for-gtocore-0.5.6-beta.jar`.
- The fixed GTO 0.5.6-beta client loaded the new version and reached the title screen with GTOHJS machines, recipes and pattern assemblies registered.
- The client completed GTOHJS registration and reached the title screen with the `ME Placement Tool for gto` JAR completely absent; both Mods also loaded together after the standalone tool was restored.
- The pattern-isolation correction was confirmed by player testing.
- No EMI source file was modified.
