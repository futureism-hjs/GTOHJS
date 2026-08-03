# GTOHJS fix65 ME 输入总成 / ME Input Assemblies

**基线 / Baseline:** Minecraft 1.20.1, Forge 47.4.20, Java 21, GTCEu 26.7.3, GTOCore 0.5.6-beta, GTOLib 26.7.4, AE2 15.267.4.

## 中文

### 1. 注册结果

Fix65 新增两个 GTO 多方块部件：

| ID | 名称 | 等级 | 合并来源 |
| --- | --- | --- | --- |
| `gtocore:me_input_assembly` | ME输入总成 | EV | `gtceu:me_input_bus` + `gtceu:me_input_hatch` |
| `gtocore:me_stocking_input_assembly` | ME库存输入总成 | LuV | `gtceu:me_stocking_input_bus` + `gtceu:me_stocking_input_hatch` |

两者都注册 `PartAbility.IMPORT_ITEMS`、`PartAbility.IMPORT_FLUIDS` 和 `GTOPartAbility.DUAL_INPUT`。因此一个部件可以同时向多方块控制器提供物品与流体输入能力，也能替代明确要求输入总成的结构位。只给一个方块附加三个 ability 不足以实现功能；控制器类必须同时建立两个 `NotifiableContentHandler` 并接入同一 AE 节点。

### 2. 材质与生命周期

两个部件都使用 `GTCEu.id("block/machine/part/me_pattern_buffer")` 的 `OverlayTieredMachineRenderer`，即 `gtceu:me_pattern_buffer` 的现有材质。GTOHJS 不复制、不覆盖 GTCEu 或 GTOCore 资源。

注册入口是 `MEInputAssemblyRegistration.register()`，coremod 目标为 `com.gtocore.common.data.machines.GTAEMachines.<clinit>()V` 的每个 `RETURN`。Mixin 仅保留相同窗口的回退调用。不要把这两个部件移到普通 `FMLCommonSetupEvent`。

### 3. 普通输入总成

`MEInputAssemblyPartMachine` 使用同一个 ME 节点维护：

- 16 个物品配置位；
- 16 个流体配置位；
- 一个共用电路配置；
- 一个共用优先级和 distinct 状态；
- 数据棒复制/粘贴物品、流体、电路和 distinct 配置。

每 40 tick 与 ME 网络同步目标库存。机器移除时，实际暂存在两个处理器中的内容都会退回同一 ME 网络。UI 上半区为物品配置，下半区为流体配置。

### 4. 库存输入总成

`MEStockingInputAssemblyPartMachine` 不把网络内容复制到本地方块库存。配置槽只显示网络库存快照；配方实际消耗时使用 `Actionable.MODULATE` 直接从 ME 网络扣除物品或流体。模拟检查使用 `Actionable.SIMULATE`。

螺丝刀循环四种自动库存模式：关闭、物品和流体、仅物品、仅流体。自动模式分别选择网络中数量最多的 16 种物品或 16 种流体；显示数量通过 ME `SIMULATE` 查询真实可提取量。切换自动/手动边界时会清除自动生成的配置，避免旧快照变成意外的手动配置。多个同类总成位于同一控制器时，手动配置会检查同介质键的重复项，自动选择也会跳过其他总成已经占用的键。断网时只清除自动生成配置，保留玩家的手动配置。

### 5. 删除内容

Fix65 完整删除：

- `gtocore:custom_lathe`；
- `gtocore:large_custom_cutter`；
- `gtohjs:lathe/ev_machine_casing_to_iv_machine_casing`；
- `CustomLatheMachineCondition`；
- 对应 Java 注册类、GTOMachines/GCYMMachines 注入、翻译、来源提示列表和 GTOHJS 车床纹理。
- 仅服务于旧自定义机床注册的 `NativeLoader`、JVMTI `patcher.c` 与打包 DLL。

历史测试报告仍可记录这些旧功能曾经存在，但现行源码、资源和注册脚本不得再引用它们。

### 6. 编译依赖

GTOCore JAR 对 AE2、GTO-AE、GTMThings 和 RecipeSearch 类型存在公开 ABI 引用，但这些嵌套依赖不会自动进入当前 ForgeGradle 的 Java 编译类路径。Fix65 在项目 `libs` 中保存只读资源的本地副本，并以 `compileOnly` 接入：AE2 15.267.4、GTMThings 26.7.1 和 RecipeSearch 1.3。运行时仍使用整合包中 GTOCore 的嵌套依赖与现有 FastRecipeSearch mod；GTOHJS JAR 不重复打包它们。

## English

### 1. Registered parts

Fix65 adds `gtocore:me_input_assembly` at EV and `gtocore:me_stocking_input_assembly` at LuV. The first combines the normal ME item bus and fluid hatch semantics; the second combines their stocking variants. Both register `IMPORT_ITEMS`, `IMPORT_FLUIDS` and `DUAL_INPUT`, and both provide real item and fluid recipe handlers rather than ability labels alone.

### 2. Renderer and lifecycle

Both definitions use the existing `gtceu:me_pattern_buffer` overlay through `GTCEu.id("block/machine/part/me_pattern_buffer")`. No GTCEu or GTOCore resource is modified. Registration runs at every return from `GTAEMachines.<clinit>()V`; the Mixin invokes the same idempotent registration only as a fallback.

### 3. Runtime behavior

The normal assembly exposes sixteen configured item keys, sixteen configured fluid keys, one circuit setting, shared priority and distinct state, and data-stick copy/paste. It synchronizes target stock with one ME node every forty ticks and refunds locally held contents when removed.

The stocking assembly exposes network-backed snapshots and extracts with `Actionable.MODULATE` only when a recipe consumes an ingredient. Its screwdriver modes are disabled, both, items only and fluids only. Automatic modes select the sixteen largest matching network entries per medium, skip keys claimed by another stocking assembly on the same controller, and display the amount actually extractable through `Actionable.SIMULATE`. Generated automatic configurations are cleared when crossing an automatic/manual mode boundary or losing the network; manual configurations survive a disconnect.

### 4. Removed legacy content

Fix65 removes the Custom Lathe, Large Custom Cutter, their dedicated casing recipe and condition, all registration hooks, translations, tooltip ownership entries and the copied lathe textures. It also removes the obsolete native loader, JVMTI source and DLL that only called the deleted Custom Lathe registration. Historical reports remain historical records only.

### 5. Verification contract

The release check must confirm both registry IDs, all three ability memberships, the Pattern Buffer renderer path, successful client reload, and absence of all three removed IDs from live logs and recipe registration. Functional in-world testing should also configure one item and one fluid on each assembly and confirm that a multiblock recipe sees and consumes both media from the same AE network.
