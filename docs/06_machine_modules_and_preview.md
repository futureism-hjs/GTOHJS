# 带模块机器、仓室加成与预览 / Machines with Modules, Bonuses and Previews

## 中文

### 六类原机的开发定位

| 代表机器 | 应研究的部分 | 不可直接推断的部分 |
| --- | --- | --- |
| `gtocore:steam_pressor` | 低级蒸汽仓、基础并行和蒸汽换算 | 不能据此开放高级仓 |
| `gtocore:large_steam_macerator` | 大型蒸汽 controller、普通高级 I/O | 不等于所有高级模块都可用 |
| `gtceu:vacuum_freezer` | 电力多方块、普通升级仓 | 没有消声仓并不代表所有机器都无消声要求 |
| `gtceu:electric_blast_furnace` | 线圈温度、消声、维护、可选扩展仓 | 线圈 predicate 和普通 casing 不可混用 |
| `gtceu:large_circuit_assembler` | GCYM casing、并行/加速/线程/等级框架 | 需要 GCYM controller 与 modifier |
| `gtocore:nano_forge` | 激光仓、线程/超频、等级框架 | 普通能源仓不能替代激光仓 |

### 模块化注册原则

1. 先复制目标原机的 controller 构造器和 `recipeTypes`，再增加一个能力。
2. 先在 pattern 中开放仓室，再确认 controller 的 `getParts`/`modifyRecipe`/modifier 实际读取它。
3. 任何数量上限都同时写入 pattern、tooltip 和验证日志。
4. 结构预览中的替代方块只用于展示；正式成型仍由 `where` predicate 判定。
5. 不要通过 EMI 变换器伪造仓室加成。GTO definition 正确时，EMI 会自动取得结构与配方。

### GTO 预览链

```text
MachineRegisterUtils.multiblock
  -> MultiblockDefinition / patternFactory / renderer
  -> MultiblockDefinition.init()
  -> 世界结构预览 + XEI/EMI definition
```

`.multiblockPreviewRenderer(true, true)` 的两个参数分别打开世界预览和 XEI/EMI 结构预览。pattern 供应器不能返回 null，renderer 不能为 null，controller 字符不能映射成普通方块。结构导出工具可以生成草稿，但不能代替注册器的 `Predicates.controller(machine)`。

### 当前通用蒸汽厂模式 UI

通用蒸汽厂使用原生 `LargeSteamMultiblockMachine`，材质覆盖层恢复为 `gtceu:block/multiblock/steam_oven`，不是把新类放入 `com.gtocore`。fix47 的 `UniversalSteamFactoryModeSupport` 位于 `com.gtohjs.machine`，coremod 只在 `SteamParallelMultiblockMachine.createUI` 中按机器 ID 条件返回 Fancy UI 适配器；模式页保留 GTCEu `MachineModeFancyConfigurator` 的同步协议，并使用固定 5 条的滚动视口、纵向滚动条和逐条鼠标滚轮。模式切换在 server side 调用 `setActiveRecipeType`。其他蒸汽机继续使用原生旧式 UI，因此不会影响其行为，也不会产生 Java 21 split package。

### 验证日志

可靠的验收证据包括：

```text
Registered gtocore:universal_steam_factory ... recipeTypes=[15 entries]
Validated loaded gtocore:universal_steam_factory; patternBuilt=true; cachedPatterns=1
Load complete; ... universalSteamFactoryState=REGISTERED
Validated 408 bulk forge-hammer recipes (64 ingots -> 64 dust)
```

## English

Treat the six representative machines as separate contracts: low-level steam, large steam, ordinary electric, coil electric, GCYM modular and laser-powered. Copy the controller, recipe modifier, casing predicate and module abilities from the representative machine before adding anything. Pattern acceptance alone never implements a bonus.

GTO preview is generated from the registered definition, pattern factory and renderer. Enable both preview flags, keep the controller predicate explicit, and never patch EMI to compensate for a bad definition. Fix47 keeps the native large-steam controller, restores the original `gtceu:block/multiblock/steam_oven` overlay, and conditionally adapts only the universal factory to the same Fancy mode page used by large electric machines. Its mode page is capped at five visible rows and adds a vertical scrollbar with row-by-row mouse-wheel movement. Other steam machines retain their native UI; no EMI code is changed.

Use load-complete logs, built-pattern validation and recipe-table validation as acceptance evidence. The current client run verified the universal factory, all 15 recipe types and 408 bulk forge-hammer recipes.
