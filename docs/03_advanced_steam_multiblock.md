# 高级蒸汽多方块机器注册 / Advanced Steam Multiblocks

## 中文

高级蒸汽机器以 `gtocore:large_steam_centrifuge`、`gtocore:large_steam_macerator` 为基准。核心区别是控制器使用 `LargeSteamMultiblockMachine`，builder 使用大型蒸汽参数；是否接受普通高级输入仓仍然完全由 pattern predicate 决定。

### 已验证的大型蒸汽写法

```java
MachineRegisterUtils.multiblock(
        "example_large_steam", "示例高级蒸汽机",
        holder -> new LargeSteamMultiblockMachine(holder, 8))
    .langValue("Example Large Steam Machine")
    .nonYAxisRotation()
    .recipeTypes(GTRecipeTypes.CENTRIFUGE_RECIPES)
    .multipleRecipesTooltips()
    .steamOverclock(0)
    .block(GTBlocks.CASING_BRONZE_BRICKS)
    .pattern(machine -> FactoryBlockPattern.start(machine)
        .where('S', Predicates.controller(machine))
        .where('A', Predicates.blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
            .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1)
                .setPreviewCount(1))
            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS)
                .setMaxGlobalLimited(1).setPreviewCount(1))
            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS)
                .setMaxGlobalLimited(1).setPreviewCount(1))
            // 普通高级输入仓：不是 STEAM_IMPORT_ITEMS。
            .or(Predicates.abilities(GTOPartAbility.IMPORT_ITEMS)
                .setMaxGlobalLimited(1).setPreviewCount(1))
            .or(Predicates.abilities(GTOPartAbility.EXPORT_ITEMS)
                .setMaxGlobalLimited(4))
            .or(Predicates.abilities(GTOPartAbility.IMPORT_FLUIDS)
                .setMaxGlobalLimited(1))
            .or(Predicates.abilities(GTOPartAbility.EXPORT_FLUIDS)
                .setMaxGlobalLimited(4))
            .or(Predicates.blocks(GTOMachines.STEAM_VENT_HATCH.get())
                .setExactLimit(1).setPreviewCount(1)))
        .build())
    .workableCasingRenderer(
        GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
        GTCEu.id("block/multiblock/steam_oven"))
    .register();
```

`LargeSteamMultiblockMachine(holder, 8)` 是当前 GTO 原生大型蒸汽离心机使用的构造方式。不要为了增加模式而把类放进 `com.gtocore.*` 包；GTOHJS JAR 与 GTOCore 会产生 Java 21 拆分包错误。fix47 使用原生大型蒸汽控制器，并通过 GTOHJS coremod 对 `BaseSteamMultiblockMachine` 做条件 UI 注入。

必须注意：该构造器的第二个 `int` 是基础可用 EU/t，不是最大并行数；最大并行来自 `LargeSteamMultiblockMachine.STEAM_MULTIBLOCK_MAX_PARALLELS`。`8` 对应 ULV，`32` 对应 LV，`128` 对应 MV。

### 通用蒸汽厂当前契约

`gtocore:universal_steam_factory` 的 fix51 结构来自外部开发素材 `通用蒸汽厂.litematic`，为 `5 x 5 x 5`。结构包含 80 个 `gtceu:steam_machine_casing` 主机壳位、2 个青铜框架、4 个青铜管道机壳、1 个青铜齿轮箱和 1 个 `gtohjs:integral_bronze_framework`。控制器位于最后一层底部中央，模型空格使用 `Predicates.any()`，不参与结构监听。

80 个主机壳位接受蒸汽能源仓/蒸汽物品仓/蒸汽流体仓/排气仓，以及最多一个普通高级物品输入仓、四个普通物品输出仓、一个普通流体输入仓和四个普通流体输出仓。青铜框架、管道、齿轮箱和整体青铜框架必须使用模型中的精确方块。维护仓、并行仓、加速仓没有开放。

机器模式共 15 个：

```text
bender, rolling, wiremill, loom, fluid_solidifier,
lathe, extractor, packer, unpacker, extruder, forming_press,
cluster, forge_hammer, chemical_bath, circuit_assembler
```

模式按钮只对该机器 ID 生效，并在 GTO 原生 OC 点击逻辑前拦截，避免切换模式修改超频档位。模式 UI 不修改 EMI。

fix53 将通用蒸汽厂构造器的基础功率设为 `GTValues.V[MV] = 128 EU/t`，并使用 `.steamOverclock(GTValues.MV)` 写入正确的等级元数据与提示。必须注意，`BaseSteamMultiblockMachine` 的原生判定为 `baseEut << steamHatchMultiplier`；仅修改构造参数仍会让高级蒸汽仓把可接受配方等级推到 MV 以上。因此 GTOHJS coremod 在 `BaseSteamMultiblockMachine.getRealRecipe` 入口调用按机器 ID 过滤的检查，仅对 `gtocore:universal_steam_factory` 强制要求原始配方 `EUt <= 128`。15 个模式均可运行 ULV、LV 和 MV 配方，HV 及以上始终拒绝；其他 GTO 蒸汽机器、蒸汽仓超频和动态并行逻辑不变。

fix60 在同一个已验证的 `getRealRecipe` 变换中增加最终耗时锁：GTO 完成本机并行、蒸汽耗时倍率和蒸汽仓超频后，只对通用蒸汽厂的非空返回配方写入 `duration=1`。不能只在 builder 上挂普通 `recipeModifier`，因为 `BaseSteamMultiblockMachine.getRealRecipe` 覆盖了父实现且不会调用 definition modifier。其他大型蒸汽机器仍保留原生耗时。

fix47 把模式切换放入与大型切割机相同的左侧 Fancy UI 页面：主页面仍显示原生蒸汽屏幕，侧栏使用兼容 GTCEu `MachineModeFancyConfigurator` 同步协议的滚动列表，点击后调用原生 `setActiveRecipeType`。列表每条高 20 px，视口一次最多显示 5 条，支持纵向滚动条和鼠标滚轮逐条滚动。只有通用蒸汽厂的 `createUI` 被 coremod 条件替换，其他蒸汽机器继续使用原生旧式 UI。

### 高级输入仓的语义

`GTOPartAbility.IMPORT_ITEMS` 是普通高级输入总线能力；`PartAbility.STEAM_IMPORT_ITEMS` 是蒸汽物品输入仓，二者不是同一个能力。若要只允许特定巨型总线，应使用 `Predicates.blocks(GTOMachines.HUGE_ITEM_IMPORT_BUS.get())`，不要误用 `ITEMS_INPUT_BUS`（它是能力集合，会注册多个 tier）。

## English

Use `LargeSteamMultiblockMachine(holder, 8)` and `.steamOverclock(0)` for machines modeled on `large_steam_centrifuge` or `large_steam_macerator`. Ordinary advanced item input is `GTOPartAbility.IMPORT_ITEMS`; it is deliberately different from `PartAbility.STEAM_IMPORT_ITEMS`. Add each ability and quantity limit explicitly in the `where` predicate.

The constructor's second integer is base recipe EU/t, not parallelism. Maximum parallelism comes from `STEAM_MULTIBLOCK_MAX_PARALLELS`. Fix53 gives the universal steam factory 128 EU/t (`V[MV]`) and records MV through `.steamOverclock(GTValues.MV)`. Native GTO checks `baseEut << steamHatchMultiplier`, so the constructor value alone is not a hard tier cap. A machine-ID-guarded Coremod check at `BaseSteamMultiblockMachine.getRealRecipe` rejects original recipe EU/t above 128 only for this controller. ULV, LV and MV remain accepted even with advanced steam hatches, while HV and above remain rejected; all other steam controllers retain native behavior.

The current universal steam factory is registered as `gtocore:universal_steam_factory` and uses the supplied `5 x 5 x 5` Litematic conversion. It contains 80 `gtceu:steam_machine_casing` positions, exact bronze frames, pipe casings, one bronze gearbox and one GTOHJS integral bronze framework. Ignored spaces use `Predicates.any()` and are not monitored by the pattern. Only the 80 primary casing positions accept steam parts and limited ordinary advanced I/O. Maintenance, parallel and accelerate hatches remain disabled. The original `gtceu:block/multiblock/steam_oven` overlay, native GTO large-steam controller, 15 recipe modes and five-row scrollable Fancy UI page are unchanged.

Fix60 locks every accepted universal-factory recipe to one tick after native parallelism, steam duration scaling and steam-hatch overclocking have finished. This is implemented in the existing machine-ID-guarded `BaseSteamMultiblockMachine.getRealRecipe` transform because that override does not call the definition-level recipe modifier. Other steam multiblocks retain native duration behavior.

Use `Predicates.blocks(GTOMachines.HUGE_ITEM_IMPORT_BUS.get())` only when an exact huge bus is required. `GTOPartAbility.ITEMS_INPUT_BUS` represents a tiered ability collection, not one specific hatch.
