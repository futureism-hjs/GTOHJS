# GTO HJS 更新日志 / Changelog

## 2.3-alpha-for-gtocore-0.5.6-beta - 2026-08-22

### 中文

与上一份清洁构建 `2.2-alpha-for-gtocore-0.5.6-beta` 相比：

修复：

- 修复热力仓模式标签、独立机器的模式/目标温度标签以及热量输出侧栏标题，统一使用 GTOCore 的客户端 `Component.translatable(...).setClientSideWidget()` 方法。现有中英文翻译会显示为文本，不再显示原始翻译键。
- 删除两个热力形态中新增加的当前模式行，使热力仓复用独立机器的共享目标温度 UI；两种形态都不再依赖已删除的模式翻译键。

新增：

- 电磁热力控制仓拥有两种 MV 形态：多方块热量输入仓和独立的零能耗热力机器。两种形态共用主界面的目标温度 UI；机器默认 `300 K`，范围为 `0..3600 K`，并将热量条件精确锁定到选择的目标温度。
- 玩家可见的热力设置保持使用 K。控制器本地的 `HeatHandler` 将环境温度校准为 `0 K`，并使用每 K 两个原始热量单位，使 `0..3600 K` 范围内的实际温度与设定值完全一致。
- 通用蒸汽厂现在有十七种模式，新增 Mixer 和原生 `gtceu:centrifuge`；所有模式继续保留 MV 配方等级限制和最终 `1t` 时长锁定。
- 进阶和终极无限进气仓：分别为 MV/IV 流体输入仓，可选择空气、氧气或气态氮，支持外部流体仓双向能力、左侧下方标准工作开关、可配置输出和正面阻挡处理。
- 新增真空覆盖 `gtohjs:vacuum_cover`，可为受支持的单方块机器和多方块维护仓提供真空等级 1-3。

更改：

- 普通螺丝刀右键现在只在热力仓与无能源机器之间切换。Shift+螺丝刀不会转换任一形态。切换提示只显示目标形态；仓室切换到机器时仍会中断关联配方并销毁仓内全部物品和流体。
- 热力渲染器使用 MV `gtceu:mv_machine_casing` 外壳和用户提供的八帧 `overlay_front.png`，排除旧的蓝色发光前面层，同时保留 GTOCore 侧面的温度计。热力提示保留黄色的仓室/机器模式行以及已验证的白色 K 温度/切换说明。
- 热力配方仅保留居中的 `gtocore:heater -> gtocore:electromagnetic_thermal_control_hatch` 工作台配方，不再存在热力形态转换配方。
- 进阶和终极进气仓在多方块匹配中仍使用 `IO.IN`，外部能力仍使用 `IO.BOTH`；Shift+螺丝刀不再将输入仓转换为输出仓。
- 进阶发电阵列的发电量固定为 `2x`，无线电网传输损耗固定为 `0%`，普通发电阵列配置不受影响。
- 英文开发文档、公开 README、翻译和验证索引已与最终双形态热力行为及十七模式通用蒸汽厂契约同步。

### English

Compared with the previous clean build `2.2-alpha-for-gtocore-0.5.6-beta`:

Fixed:

- Fixed the thermal hatch's mode label, the standalone machine's mode/target labels, and the heat-output sidebar heading to use GTOCore's client-side `Component.translatable(...).setClientSideWidget()` pattern. The existing Chinese and English entries now render as text instead of raw translation keys.
- Removed the newly added current-mode rows from both thermal forms and made the hatch reuse the standalone machine's shared target-temperature UI, so neither form depends on the removed mode translation keys.

Added:

- Electromagnetic Thermal Control Hatch has two MV forms: a multiblock heat-input hatch and a standalone, zero-energy thermal machine. Both forms share the same primary target-temperature UI; the machine defaults to `300 K`, configures `0..3600 K`, and locks its heat condition exactly to the selected target.
- Player-facing thermal settings remain K. The controller-local `HeatHandler` calibration fixes ambient temperature to `0 K` and uses two raw heat units per K, so actual temperature exactly matches the configured value from `0..3600 K`.
- The Universal Steam Factory has seventeen modes, adding both Mixer and native `gtceu:centrifuge`; all modes retain the MV recipe limit and final `1t` duration lock.
- Advanced and Ultimate Infinite Intake Hatches: MV/IV fluid-input parts with selectable air, oxygen, or gaseous nitrogen, bidirectional external tank capability, standard lower-left work toggles, configurable output, and front-obstruction handling.
- Vacuum Cover `gtohjs:vacuum_cover`, supplying vacuum tiers 1-3 to supported single-block machines and multiblock Maintenance Hatches.

Changed:

- Normal screwdriver right-click now switches only between the thermal hatch and the unpowered machine. Shift+screwdriver does not convert either form. Switch messages report only the destination form; hatch-to-machine conversion still interrupts attached recipes and destroys all hatch items and fluids.
- The thermal renderer uses the MV `gtceu:mv_machine_casing` hull with the user-supplied eight-frame `overlay_front.png`, excludes the old emissive blue front layer, and retains the GTOCore side thermometers. The thermal tooltip retains the yellow hatch/machine line plus the verified white K-temperature/switch instruction.
- The thermal recipe is solely the centered `gtocore:heater -> gtocore:electromagnetic_thermal_control_hatch` crafting recipe; no thermal-form conversion recipe exists.
- Advanced and Ultimate Intake Hatches retain `IO.IN` for multiblock matching while their external capability remains `IO.BOTH`; Shift+screwdriver can no longer change either input hatch into an output hatch.
- Advanced Generator Array generation is fixed at `2x` and its wireless-grid transfer loss is `0%`, without changing the stock array configuration.
- English development documentation, public READMEs, localization, and validation indexes were synchronized with the final two-form thermal behavior and seventeen-mode factory contract.

## 2.2-alpha-for-gtocore-0.5.6-beta - 2026-08-17

### 中文

与上一个版本 `2.1-alpha-for-gtocore-0.5.6-beta` 对比：

修复：

- 修复超维度锻炉只显示 `524288` 并行、实际配方仍以 1 并行运行的问题；无能源控制器现在通过 GTCEu 原生精确并行算法生成真实并行配方。
- 高级炼金锅的 14 个内部空间改为完全忽略位置，可以放置任意方块且不影响结构成型，也不会附加额外仓室能力。

调整：

- 删除 Coremod 中一份重复的配方功率与耗时辅助函数，保持生成字节码不变。
- 新增固定并行运行链、结构忽略位、Coremod/Java 职责边界及文档优先维护流程的中英文开发文档。

构建：

- 使用 Java 21 完成构建；按用户要求未执行额外源码审查、客户端验证或服务端验证。

### English

Compared with `2.1-alpha-for-gtocore-0.5.6-beta`:

Fixed:

- Fixed the Hyperdimensional Forge displaying 524288 parallelism while its running recipe remained at one parallel. The no-energy controller now creates the actual parallel recipe through GTCEu's accurate parallel algorithm.
- Changed the Advanced Alchemy Cauldron's fourteen internal spaces into fully ignored positions. Arbitrary blocks may occupy them without invalidating the structure or attaching additional part abilities.

Changed:

- Removed one duplicate Coremod helper for recipe power and duration without changing the emitted bytecode.
- Added bilingual development documentation for fixed-parallel execution, ignored pattern positions, Coremod/Java responsibilities, and the documentation-first maintenance workflow.

Build:

- Built with Java 21. Per user instruction, no additional source review, client validation, or server validation was performed.

## 2.1-alpha-for-gtocore-0.5.6-beta - 2026-08-12

### 中文

与上一个清洁构建 `2.0-alpha-for-gtocore-0.5.6-beta` 对比：

修复：

- 修复专用服务端注册 `gtocore:advanced_generator_array`、`gtocore:steam_array` 和 `gtocore:advanced_steam_array` 时被错误判定失败的问题。
- GTCEu 在服务端按设计使用 `IRenderer.EMPTY`；GTOHJS 现在只在客户端检查 `ArrayMachineRenderer`，服务端继续验证机器注册表、配方类型、结构供应器和最终注册状态。
- 服务端现在可以完成 GTOHJS 的 `FMLLoadCompleteEvent`，不再因阵列机器的客户端渲染器校验抛出致命加载错误。

### English

Compared with the previous clean build `2.0-alpha-for-gtocore-0.5.6-beta`:

Fixed:

- Fixed dedicated-server registration for `gtocore:advanced_generator_array`, `gtocore:steam_array`, and `gtocore:advanced_steam_array`.
- GTCEu intentionally uses `IRenderer.EMPTY` on a dedicated server. GTOHJS now checks the concrete `ArrayMachineRenderer` type only on the client while continuing to validate the machine registry, recipe type, pattern supplier, and finalized registration state on both sides.
- GTOHJS no longer aborts `FMLLoadCompleteEvent` because of a client-only renderer assertion, allowing the dedicated server to finish loading.

## 2.0-alpha-for-gtocore-0.5.6-beta - 2026-08-02

### 中文

与上一个清洁构建 `2.0-per3-for-gtocore-0.5.6-beta` 对比：

修复：

- 修复 ME 超级通配符样板总成的串配方问题；每个样板槽只读取本槽私有电路、物品和流体催化剂，同时仍可访问总成级共享催化剂。
- 通配符生成样板通过对象身份与等价样板双重映射严格定位源槽；来源不唯一或无法确认时拒绝执行，不再遍历其他槽或回退到 0 号槽。

调整：

- `ME Placement Tool for gto` 改为完全独立 Mod。GTOHJS 删除对 `meplacementtool` 的强制依赖和全部运行时物品 ID 引用，两个 Mod 均可单独安装。
- 新建基础 AE 元件包恢复为 123 类，不再包含外部放置工具物品；已有版本 2/3 元件包的 backing UUID、数量、电量、内容版本及旧外置存储不被主动重写或删除。
- 完整重写中英文 README，列出当前全部物品、方块、机器、仓室、配方类型、配方和主要机制；本次清洁源码按用户要求包含全部开发文档、注册模板、项目规则与索引。

### English

Compared with the previous clean build `2.0-per3-for-gtocore-0.5.6-beta`:

Fixed:

- Fixed cross-pattern execution in the ME Super Wildcard Pattern Buffer. Each pattern slot reads only its private circuit and item/fluid catalysts while retaining access to machine-level shared catalysts.
- Generated wildcard patterns now resolve their source through both identity and equivalent-pattern mappings. Ambiguous or unknown origins are rejected instead of probing other slots or falling back to slot zero.

Changed:

- Made `ME Placement Tool for gto` a fully independent Mod. GTOHJS no longer declares a mandatory `meplacementtool` dependency or references any of its runtime item IDs; either Mod can be installed alone.
- New Basic AE Component Packs contain 123 types and no external placement-tool items. Existing version 2/3 packs keep their backing UUID, quantities, charge, content version and old external storage without active rewriting or deletion.
- Rewrote the English and Chinese READMEs to enumerate all current items, blocks, machines, parts, recipe types, recipes and major mechanics. At the user's request, this clean source release includes all development documents, registration templates, project rules and indexes.

## 2.0-per3-for-gtocore-0.5.6-beta - 2026-07-31

### 中文

与上一个清洁构建 `2.0-per2-for-gtocore-0.5.6-beta` 对比：

修复：

- 修复游戏内配置页标题、嵌套字段和 Configuration 通用控件未显示中文翻译的问题。

调整：

- ME 超级样板总成保持最多 `18 × 10 × 10 = 1800` 个样板槽，仅该总成根据列数动态扩展界面宽度；普通 GTO 样板总成和超级通配符样板总成仍保持原生宽度。
- ME 超级通配符样板总成保持默认 `3 × 3`，可配置上限调整为 `8 × 8 × 1 = 64` 个槽位。

### English

Compared with the previous clean build `2.0-per2-for-gtocore-0.5.6-beta`:

Fixed:

- Fixed missing Chinese translations for the in-game configuration title, nested fields, and common Configuration controls.

Changed:

- The ME Super Pattern Buffer supports up to `18 × 10 × 10 = 1800` pattern slots and is now the only buffer whose UI width follows its configured column count. Native GTO pattern buffers and the super wildcard buffer retain the native width.
- The ME Super Wildcard Pattern Buffer still defaults to `3 × 3`; its configurable maximum is now `8 × 8 × 1 = 64` slots.

## 2.0-per2-for-gtocore-0.5.6-beta - 2026-07-31

### 中文

与上一个清洁构建 `1.0-beta-for-gtocore-0.5.6-beta` 对比：

新增：

- ME 超级样板总成。
- ME 超级样板总成镜像。
- ME 超级通配符样板总成。
- 三个总成均支持物品/流体输入、物品/流体输出、双输入和双输出；输出可直接进入 ME 网络，离线时持久保存并自动重试。
- 三个总成的工作台配方。

### English

Compared with the previous clean build `1.0-beta-for-gtocore-0.5.6-beta`:

Added:

- ME Super Pattern Buffer.
- ME Super Pattern Buffer Proxy.
- ME Super Wildcard Pattern Buffer.
- All three assemblies support item/fluid input, item/fluid output, dual-input and dual-output abilities. Outputs enter the ME network directly, persist while offline and retry automatically.
- Crafting-table recipes for all three assemblies.

## 1.0-beta-for-gtocore-0.5.6-beta - 2026-07-30

### 中文

新增：

- ME 超级样板总成、镜像和超级通配符样板总成现在可同时作为物品/流体输入仓与输出仓；产物直接进入连接的 ME 网络，离线或网络满时持久保存并自动重试。
- 新增上述三个总成的工作台配方，并对 18 条 GTOHJS 工作台配方执行最终 `RecipeManager` 校验。

调整：

- 将 GTO 适配版 ME Placement Tool 分离为必需的独立 Mod `ME Placement Tool for gto`。
- 从 GTOHJS 移除工具 Java、菜单、网络、配置、客户端渲染、材质、翻译和五条重复配方。
- 将五个物品与配方迁回原版 `meplacementtool:*` 和 `meplacementtool:shaped/*` 命名空间。
- GTOHJS 基础 AE 元件包仍预装上述五个外部物品，每种 16M，并显式依赖 `meplacementtool`；GTOHJS 内不再保留对应注册或实现代码。
- 基础 AE 元件包内容版本升级为 3，为版本 1/2 的既有外置存储补入新的 `meplacementtool:*` 键，同时保留 backing UUID、可解析的既有键、数量与当前电量。

### English

Added:

- The ME Super Pattern Buffer, its proxy, and the ME Super Wildcard Pattern Buffer now provide item/fluid input and output simultaneously. Products enter the connected ME network directly, while offline or blocked output is persisted and retried automatically.
- Added crafting-table recipes for all three assemblies and extended final `RecipeManager` validation to all 18 GTOHJS crafting recipes.

Changed:

- Split the GTO-compatible ME Placement Tool port into the required standalone `ME Placement Tool for gto` mod.
- Removed the embedded tool Java, menus, network, configs, client renderers, assets, translations, and five duplicate recipes from GTOHJS.
- Moved all five item and recipe IDs back to the upstream `meplacementtool:*` and `meplacementtool:shaped/*` namespaces.
- The Basic AE Component Pack still preloads 16M of each external item and GTOHJS explicitly depends on `meplacementtool`; no matching registration or implementation code remains in GTOHJS.
- Raised the Basic AE Component Pack content version to 3. Existing version 1/2 external stores receive the new `meplacementtool:*` keys while retaining their backing UUID, resolvable existing keys, amounts, and current charge.

## 1.0-alpha-for-gtocore-0.5.6-beta - 2026-07-29

### 中文

新增：

- 移植并适配“ME 放置工具”Mod。

### English

Added:

- Ported and adapted the "ME Placement Tool" mod.

## 1.0-per7-for-gtocore-0.5.6-beta - 2026-07-29

### 中文

新增：

- 加载配方目录中的五个 ME Placement Tool 工作台配方；对应正式 JSON 已按相同配方 ID 注册，避免重复注入。
- 基础 AE 元件包新增 ME 放置工具、ME 多方块放置工具、ME 线缆放置工具、棱镜原体和光谱的钥匙，每种 16M（16,777,216）。
- 高级 AE 仓室元件包新增 16M（16,777,216）个 `gtocore:me_wireless_connection_machine`。

修复：

- ME 线缆放置工具配方使用 AE2 官方 `ae2:smart_dense_cable` Tag，可接受 16 种染色线缆和福鲁伊克斯色线缆。
- 完整迁入原 ME Placement Tool 的物品模型、纹理、GUI 贴图和中英文翻译，修复旧 `per5` 客户端中出现的紫黑材质与空白 UI。
- 已初始化的旧元件包现在会原地迁移：保留 backing UUID、旧物品数量和当前电量，只补入本次新增物品；迁移验证失败时恢复原后端映射与摘要。

### English

Added:

- Loaded all five ME Placement Tool crafting drafts through their existing, matching datapack recipe IDs without duplicate Java registration.
- Added 16M (16,777,216) each of the ME Placement Tool, ME Multiblock Placement Tool, ME Cable Placement Tool, Prism Core and Key of Spectrum to the Basic AE Component Pack.
- Added 16M (16,777,216) `gtocore:me_wireless_connection_machine` items to the Advanced AE Hatch Component Pack.

Fixed:

- The ME Cable Placement Tool recipe uses AE2's `ae2:smart_dense_cable` tag and accepts all sixteen dyed variants plus Fluix.
- Packaged the complete upstream item models, textures, GUI assets and English/Chinese translations, fixing the missing textures and blank UI seen in the stale `per5` client.
- Already initialized packs now migrate in place. Their backing UUID, existing amounts and current charge are retained while only the newly requested keys are added; failed validation restores the original backend map and summaries.

## 1.0-per6-for-gtocore-0.5.6-beta - 2026-07-29

### 中文

新增：

- 将 ME Placement Tool 的核心功能移植到 GTOHJS，新增 ME 放置工具、ME 多方块放置工具、ME 线缆放置工具、棱镜原体和光谱的钥匙。
- 新增三种工具的配置界面、轮盘菜单、放置预览、批量撤销、线缆模式、合成配方及中英文文本。

修复：

- 适配 GTO 定制 AE2 的引用型库存计数器，修复原工具连接无线访问点后因条目类型强制转换异常而无法放置的问题。
- 所有 ME 网络操作使用非空玩家与无线访问点操作来源，并按“模拟、预留、放置、失败回滚”顺序处理方块、AE2 部件、线缆和流体。
- 修复批量方块撤销快照、线缆撤销包校验、液体容器放置返回值和线缆分支模式耗电配置。
- 完整打包原 Mod 的物品模型、物品贴图和 GUI 贴图，补全颜色标记快捷键注册及轮盘界面的中文文本。
- 线缆批量染色会先核算真实重染数量并检查全部染料；按每 8 根成功重染线缆结算，失败时回滚本批染料。

调整：

- 所有注册 ID、资源位置、翻译键和网络频道统一使用 `gtohjs` 命名空间；不引入 JEI、REI、EMI 或 Mekanism 集成代码。
- 补充 ME Placement Tool、Construction Wand、Ars Nouveau、AE2 及第三方素材的许可与归属记录。

### English

Added:

- Ported the core ME Placement Tool feature set into GTOHJS, adding the ME Placement Tool, ME Multiblock Placement Tool, ME Cable Placement Tool, Prism Core and Key of Spectrum.
- Added configuration screens, radial menus, placement previews, bulk undo, cable modes, crafting recipes and English/Chinese text for the three tools.

Fixed:

- Adapted inventory matching to GTO's reference-keyed AE2 counter, fixing the runtime entry-cast failure that prevented placement after a wireless access point was linked.
- All ME operations now use a non-null player/access-point action source and follow simulate, reserve, place and rollback ordering for blocks, AE2 parts, cables and fluids.
- Fixed bulk block undo snapshots, cable undo packet validation, liquid-container result handling and cable branch-mode power costs.
- Packaged the upstream item models, item textures and GUI textures, registered the color-mark shortcut, and localized radial-menu fallback text.
- Cable recoloring now preflights the actual recolor count and available dye, charges once per eight successful recolors, and refunds failed batches.

Changed:

- Unified registry IDs, resource locations, translation keys and the network channel under the `gtohjs` namespace. No JEI, REI, EMI or Mekanism integration code was imported.
- Added license and attribution records for ME Placement Tool, Construction Wand, Ars Nouveau, AE2 and the imported third-party assets.

## 1.0-per5-for-gtocore-0.5.6-beta - 2026-07-27

### 中文

新增：

- 新增碎片世界采集配方页、碎片世界采集器、大型碎片世界采集器和 16 种独立注册的 `gtohjs:world_fragments_*` 物品。
- 注册全部 254 条碎片世界采集配方：15 条世界碎片生成、101 条原矿采集、130 条流体采集、7 条特殊资源和 1 条大马士革钢粉配方。
- 新增碎片世界采集器、大型碎片世界采集器与 ULV 碎片世界采集器工作台配方；大型机配方接受任意 IV 等级电路。
- 新增蒸汽阵列：右下槽最多放置 16 个低压固体或低压液体锅炉，按锅炉燃料配方连续产汽。
- 新增进阶蒸汽阵列：右下槽最多放置 64 个低压/高压固体、液体或太阳能锅炉；太阳能锅炉仅在满足 GTO 日照判定时运行。
- 新增蒸汽阵列和进阶蒸汽阵列的工作台有序配方。

调整：

- GTOCore 中不存在 `mining_crystal`、`treasures_crystal` 和 `miracle_crystal`；仅排除这三种不可用的晶体概率输出。
- GTO 未注册上游 `gtceu:titanium_drill_head`，26 条第二档流体配方使用现有 `gtocore:titanium_ti64_drill_head`；倍率、概率、流体、数量、电路、功率和耗时保持原数据。中子素档使用明确的 `gtceu:neutronium_drill_head`，避免 GTO 材料字段重映射。
- 大型采集器保留稳定钛机械外壳、标准能源仓、物品输入总线和物品输出总线结构；结构没有流体仓位，因此流体采集配方由单方块采集器运行。
- 大型碎片世界采集器由固定 64 并行改为左侧标签页可调的特殊并行，范围为 `1..9,007,199,254,740,991`；实际执行量仍受配方输入、输出容量和电压限制。
- 两种蒸汽阵列复用阵列机器展示效果，只允许输入/输出类仓室，并以当前配置中的锅炉额定产量为基准输出 1.5 倍蒸汽。
- 取消蒸汽阵列和进阶蒸汽阵列的预热与冷却过程；有效配方开始后，在首个 10t 蒸汽节拍直接按额定值产汽。

### English

Added:

- Added the Fragment World Collection recipe page, Fragment World Collection Machine, Large Fragment World Collection Machine and sixteen independently registered `gtohjs:world_fragments_*` items.
- Registered all 254 Fragment World Collection recipes: 15 world-fragment conversions, 101 ore recipes, 130 fluid recipes, seven special-resource recipes and one Damascus steel dust recipe.
- Added shaped recipes for the Fragment World Collection Machine and the Large and ULV Fragment World Collection Machines; the large-machine recipe accepts any IV-tier circuit.
- Added Steam Array, accepting up to 16 LP solid or LP liquid boilers in its lower-right storage slot and continuously producing steam from their fuel recipes.
- Added Advanced Steam Array, accepting up to 64 LP/HP solid, liquid or solar boilers. Solar boilers run only when GTO's sunlight check succeeds.
- Added shaped crafting recipes for the Steam Array and Advanced Steam Array.

Changed:

- GTOCore has no `mining_crystal`, `treasures_crystal` or `miracle_crystal`; only those unavailable probabilistic crystal outputs are omitted.
- GTO does not register the upstream `gtceu:titanium_drill_head`; the 26 second-tier fluid recipes use `gtocore:titanium_ti64_drill_head` while retaining the source multiplier, chance, fluid, amount, circuit, power and duration. The neutronium tier resolves the explicit `gtceu:neutronium_drill_head` ID to bypass GTO's material-field remap.
- The large collector retains its stable-titanium casing, standard energy hatch, item input bus and item output bus structure. It has no fluid hatch positions, so fluid collection recipes run in the single-block collector.
- Replaced the Large Fragment World Collection Machine's fixed 64 parallelism with left-tab special parallelism from `1` to `9,007,199,254,740,991`; effective work remains constrained by recipe inputs, output capacity and voltage.
- Both steam arrays reuse the array machine display effect, accept only input/output hatches and produce 1.5 times the currently configured nominal boiler output.
- Removed warmup and cooling from both steam arrays. A valid recipe now produces at nominal output on its first ten-tick steam cadence.

## 1.0-per4-for-gtocore-0.5.6-beta - 2026-07-27

### 中文

新增：

- 新增通用蒸汽厂工作台有序配方：使用青铜长杆、青铜板、大型青铜流体管、青铜齿轮和双层青铜板合成通用蒸汽厂。

调整：

- 使用新草稿替换通用蒸汽厂和大型花药台的旧配方；大型花药台现为 `7 EU/t`、`400t`。

### English

Added:

- Added a shaped crafting recipe for the Universal Steam Factory using long bronze rods, bronze plates, large bronze fluid pipes, a bronze gear and double bronze plates.

Changed:

- Replaced the previous Universal Steam Factory and Large Petal Apothecary recipes with the new drafts; the Large Petal Apothecary recipe now uses `7 EU/t` for `400t`.

## 1.0-per3-for-gtocore-0.5.6-beta - 2026-07-26

### 中文

新增：

- 新增大型花药台装配机配方、超维度化工厂装配线配方和超维度冶炼炉装配线配方。
- 新增超维度锻炉与超维度蒸汽熔炉工作台有序配方。

调整：

- 超维度化工厂配方接受 16 个任意 ZPM 等级电路；超维度冶炼炉配方接受 64 个任意 ZPM 等级电路。
- 超维度冶炼炉装配线配方功率调整为 `122,880 EU/t`。
- 高级炼金锅禁止使用普通导热仓与高级导热仓成型，并增加“不能拿来泡澡”提示。

### English

Added:

- Added the Large Petal Apothecary assembler recipe and assembly-line recipes for the Hyperdimensional Chemical Factory and Hyperdimensional Smelter.
- Added shaped crafting recipes for the Hyperdimensional Forge and Hyperdimensional Steam Furnace.

Changed:

- The Hyperdimensional Chemical Factory recipe accepts 16 circuits from the ZPM circuit tag, while the Hyperdimensional Smelter recipe accepts 64.
- Changed the Hyperdimensional Smelter assembly-line recipe to `122,880 EU/t`.
- The Advanced Alchemy Cauldron rejects both normal and advanced heat hatches and now displays the `Cannot be used for bathing` tooltip.

## 1.0-pre2-for-gtocore-0.5.6-beta - 2026-07-24

### 中文

修复：

- 修复任务内 AE 元件包无效的问题；基础 AE 元件包、AE 机器元件包和高级 AE 仓室元件包现在会正确初始化独立库存，每种物品为 16M（16,777,216），并默认满电 20,000 AE。
- 三个元件包正确复用 `ae2:portable_item_cell_256k` 的材质，并补全中文名称与说明。

### English

Fixed:

- Fixed the non-functional AE component packs distributed through quests. The Basic AE Component Pack, AE Machine Component Pack and Advanced AE Hatch Component Pack now initialize independent inventories with 16M (16,777,216) of every item and 20,000 AE of charge.
- The three packs now correctly reuse the `ae2:portable_item_cell_256k` model and include complete Chinese names and tooltips.

## 1.0-pre1-for-gtocore-0.5.6-beta - 2026-07-23

### 中文

新增：

- 通用蒸汽厂、超维度系列机器、一站式稀土处理厂、进阶发电阵列、高级炼金锅和大型花药台。
- ME 输入总成与 ME 库存输入总成。
- 基础 AE 包、AE 机器包和高级 AE 仓室包；保留玩家原包的物品种类，每种预装 16M（16,777,216），并默认满电。
- GTO 原生配方、工作台配方、批量锻造锤配方以及化学与稀土处理配方。
- 配方编辑器和自定义多方块结构导出工具。

调整：

- 三个 AE 元件包直接复用 `ae2:portable_item_cell_256k` 的模型材质，并补全清晰的中文物品名称与说明。
- 模组介绍改为 `Expands GTO with more machines and recipes.`。
- 源代码使用 `LGPL-3.0-only`；GTOHJS 原创材质与任务内容使用 `CC-BY-NC-SA-4.0`。
- 所有公开 README 增加 AI 生成或 AI 辅助内容警告。
- 公开预发布暂不包含详细开发文档、注册模板、内部分析和历史验收记录。

移除：

- 自定义机床、大型自定义切割机及其专用配方和旧 native/JVMTI 加载代码。

### English

Added:

- Universal Steam Factory, Hyperdimensional machines, One-stop Rare Earth Processing Plant, Advanced Generator Array, Advanced Alchemy Cauldron and Large Petal Apothecary.
- ME Input Assembly and ME Stocking Input Assembly.
- Basic AE Component Pack, AE Machine Component Pack and Advanced AE Hatch Component Pack; each preserves the source package's item types, preloads 16M (16,777,216) of every item, and starts fully charged.
- Native GTO recipes, crafting recipes, bulk forge-hammer recipes, and chemical and rare-earth processing recipes.
- Recipe Editor and Custom Multiblock Structure Export Tool.

Changed:

- The three AE component packs now directly reuse the `ae2:portable_item_cell_256k` model and include complete Chinese item names and tooltips.
- Changed the mod description to `Expands GTO with more machines and recipes.`.
- Source code is licensed under `LGPL-3.0-only`; original GTOHJS textures and quest content are licensed under `CC-BY-NC-SA-4.0`.
- Added an AI-generated or AI-assisted content warning to every public README.
- Detailed development documentation, registration templates, internal analysis and historical validation records are not included in the public pre-release.

Removed:

- Custom Lathe, Large Custom Cutter, their dedicated recipe, and the old native/JVMTI loading code.

