# 普通电力多方块机器注册模板 / Basic Electric Multiblock Template

## 中文

### Java 模板

```java
public final class ExampleElectricMultiblockRegistration {
    private static final String PATH = "example_electric_multiblock";
    private static final ResourceLocation ID = new ResourceLocation("gtocore", PATH);
    private static MultiblockMachineDefinition definition;

    public static synchronized void register() {
        if (definition != null) return;
        MachineDefinition existing = GTRegistries.MACHINES.get(ID);
        if (existing != null) {
            if (!(existing instanceof MultiblockMachineDefinition m))
                throw new IllegalStateException("Existing definition is not multiblock: " + ID);
            definition = m;
            return;
        }

        definition = MachineRegisterUtils.multiblock(
                PATH, "示例电力多方块", ElectricMultiblockMachine::new)
            .langValue("Example Electric Multiblock")
            .nonYAxisRotation()
            .recipeTypes(GTRecipeTypes.VACUUM_RECIPES)
            .block(GTBlocks.CASING_STABLE_TITANIUM)
            .multiblockPreviewRenderer(true, true)
            .pattern(machine -> FactoryBlockPattern.start(machine)
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .aisle("XXX", "XXX", "XXX")
                .where('S', Predicates.controller(machine))
                .where('X', Predicates.blocks(GTBlocks.CASING_STABLE_TITANIUM.get())
                    .or(Predicates.abilities(PartAbility.INPUT_ENERGY)
                        .setExactLimit(1).setPreviewCount(1))
                    .or(Predicates.abilities(PartAbility.IMPORT_ITEMS)
                        .setMaxGlobalLimited(1).setPreviewCount(1))
                    .or(Predicates.abilities(PartAbility.EXPORT_ITEMS)
                        .setMaxGlobalLimited(1).setPreviewCount(1)))
                .build())
            .workableCasingRenderer(
                GTCEu.id("block/casings/solid/machine_casing_stable_titanium"),
                GTCEu.id("block/multiblock/vacuum_freezer"))
            .register();

        if (GTRegistries.MACHINES.get(ID) != definition)
            throw new IllegalStateException("Multiblock registration failed: " + ID);
    }
}
```

请把 `recipeTypes`、控制器类、外壳、renderer 和仓室 predicate 替换成目标原机的真实值。普通电力机器不应自动获得线圈温度、激光输入、维护、并行或加速效果；这些是独立功能，需要相应 controller/runtime trait 和 modifier。

### 结构和预览

第一个 `aisle` 是最后面；在默认 `FactoryBlockPattern.start(machine)` 中，行必须按 `minY -> maxY` 从下到上排列，第 0 行是底层。`Predicates.controller(machine)` 是正式控制器；临时替代方块只适合导出器预览，不能代替正式 controller predicate。`setPreviewCount` 只影响结构预览，不改变实际可安装数量；`setExactLimit`/`setMaxGlobalLimited` 才是结构数量约束。

### 注入与检查

在目标机器分组 `<clinit>` 的每个 `RETURN` 前注入 `ExampleElectricMultiblockRegistration.register()`。注册后检查 `patternFactory.length == 1`、renderer 非空、recipe type 数组顺序正确，并在客户端进入 load complete 后调用 `validateLoaded()`。

## English

Use `MachineRegisterUtils.multiblock(...)` with the actual electric controller, casing, recipe types, predicates and renderer from the target machine. Inject `register()` before the `RETURN` of the owning machine-group `<clinit>()V`. The first aisle is the back; with the default `FactoryBlockPattern.start(machine)`, rows are bottom-to-top (`minY -> maxY`). Use `Predicates.controller(machine)` for the controller character.

`setPreviewCount` affects only previews, while `setExactLimit` and `setMaxGlobalLimited` enforce structure limits. Do not add coil, laser, maintenance, parallel or acceleration behavior merely by accepting a hatch: the controller, runtime traits and recipe modifiers must implement it. Verify registry identity, pattern factory, renderer and load-complete state in the client.
