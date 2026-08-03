# 低级蒸汽多方块机器注册 / Low-level Steam Multiblocks

## 中文

适用对象是 `gtocore:steam_pressor` 一类只能使用低级蒸汽仓的机器。低级蒸汽机应使用 GTO 的 `SteamMultiblockMachine` 注册链，不要把大型蒸汽控制器或电力多方块模板直接套过来。

### 推荐注册骨架

```java
definition = MachineRegisterUtils.multiblock(
        "example_steam_multiblock", "示例低级蒸汽机", SteamMultiblockMachine::new)
    .langValue("Example Low-level Steam Machine")
    .nonYAxisRotation()
    .recipeTypes(GTRecipeTypes.COMPRESSOR_RECIPES)
    .steamOverclock()
    .block(GTBlocks.CASING_BRONZE_BRICKS)
    .multiblockPreviewRenderer(true, true)
    .pattern(machine -> FactoryBlockPattern.start(machine)
        .aisle("XXX", "XXX", "XXX")
        .aisle("XXX", "XSX", "XXX")
        .aisle("XXX", "XXX", "XXX")
        .where('S', Predicates.controller(machine))
        .where('X', Predicates.blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
            .or(Predicates.abilities(PartAbility.STEAM)
                .setExactLimit(1).setPreviewCount(1))
            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS)
                .setMaxGlobalLimited(1).setPreviewCount(1))
            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS)
                .setMaxGlobalLimited(1).setPreviewCount(1))
            .or(Predicates.blocks(GTOMachines.STEAM_VENT_HATCH.get())
                .setExactLimit(1).setPreviewCount(1)))
        .build())
    .workableCasingRenderer(
        GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
        GTCEu.id("block/multiblock/steam_pressor"))
    .register();
```

具体仓室能力以目标原机源码为准。`STEAM` 的 `setExactLimit(1)` 表示必须恰好一个蒸汽能源仓；`STEAM_IMPORT_ITEMS` 和 `STEAM_EXPORT_ITEMS` 只接受 GTO 注册的低级蒸汽物品仓。排气仓通常使用 `GTOMachines.STEAM_VENT_HATCH` 的精确方块 predicate。

### 不要默认开放的能力

低级蒸汽模式默认不开放 `GTOPartAbility.IMPORT_ITEMS`（普通高级输入总线）、维护仓、并行控制仓、加速仓、线程仓或超频仓。除非控制器类明确实现这些能力，否则把它们加入 pattern 只会产生“能放但机器不工作”的结构。

### 结构方向

`FactoryBlockPattern.aisle(...)` 的第一个 aisle 是结构最后面；在默认 `FactoryBlockPattern.start(machine)` 下，字符串按 `minY -> maxY` 从下到上排列，第 0 行是该层底面。生成器导出的三层若写成：

```text
AAA
ASA
AAA
```

实际结构按下到上的三行解释；不要在导入时再次翻转。控制器字符必须映射到 `Predicates.controller(machine)`。

### 预览和验证

启用 `.multiblockPreviewRenderer(true, true)`，并保证 `patternFactory`、renderer 和 controller predicate 都非空。低级蒸汽机器的 EMI 预览由 GTO definition 生成，不能通过修改 EMI 代码修复。客户端验收应确认：结构可成型、低级蒸汽仓可识别、超频/并行参数符合原机、缺少排气仓时结构不能误成型。

## English

Use this pattern for machines comparable to `gtocore:steam_pressor`, which accept only low-level steam parts. Register them with GTO's `SteamMultiblockMachine` path, `.steamOverclock()`, a bronze casing and an explicit steam energy hatch, steam item input/output and vent predicate. Do not copy the large-steam controller or an electric multiblock template.

`STEAM` with `setExactLimit(1)` means exactly one steam energy hatch. `STEAM_IMPORT_ITEMS` and `STEAM_EXPORT_ITEMS` are the low-level steam item buses registered by GTO. Keep ordinary higher-tier `GTOPartAbility.IMPORT_ITEMS`, maintenance, parallel, accelerate, thread and overclock hatches closed unless the controller implements them.

The first `aisle` is the back of the structure. With the default `FactoryBlockPattern.start(machine)`, rows are bottom-to-top (`minY -> maxY`). Use `Predicates.controller(machine)` for the controller character. Enable both world and EMI/XEI previews on the definition and validate the actual formed structure in the client.
