# GTOHJS 单方块、蒸汽与电力机器注册 / Single-block, Steam and Electric Machines

**基线 / Baseline:** Minecraft 1.20.1, Forge 47.4.20, GTCEu 26.7.3, GTOCore 0.5.6-beta, GTOLib 26.7.4, Java 21.

## 中文

### 1. 先确定注册窗口

GTO 机器不是普通 `DeferredRegister` 项目。单方块机器必须在目标 GTO 机器分组类的 `<clinit>()V` 返回前注册，通常是 `com.gtocore.common.data.GTOMachines`；如果原机器属于 GCYM 或其他分组，应注入对应分组。注册类可以放在 GTOHJS，但调用必须由 coremod 在正确的 `<clinit>` 窗口完成。

```javascript
// gtohjs_machine_registration.js
method.instructions.insertBefore(nodes[i], ASMAPI.buildMethodCall(
    'com/gtohjs/bootstrap/ExampleSingleMachineRegistration',
    'register', '()V', ASMAPI.MethodType.STATIC));
```

不要在 `FMLCommonSetupEvent`、服务器启动事件或普通 KubeJS 脚本中首次创建机器 definition。那时 Registrate/Forge 注册表可能已经冻结，模型、方块实体或 GTO definition 会缺失。

### 2. GTO 单方块模板

```java
public final class ExampleSingleMachineRegistration {
    private static final String PATH = "example_single_machine";
    private static final ResourceLocation ID =
            new ResourceLocation("gtocore", PATH);
    private static MachineDefinition definition;

    public static synchronized void register() {
        if (definition != null) return;
        MachineDefinition existing = GTRegistries.MACHINES.get(ID);
        if (existing != null) {
            definition = existing;
            return;
        }

        definition = MachineRegisterUtils.machine(
                PATH,
                "示例单方块机器",
                holder -> new SimpleTieredMachine(
                        holder, GTValues.LV, GTMachineUtils.defaultTankSizeFunction))
            .tier(GTValues.LV)
            .langValue("Example Single Machine")
            .nonYAxisRotation()
            .recipeType(GTRecipeTypes.LATHE_RECIPES)
            .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(
                    GTCEu.id("lathe"), GTRecipeTypes.LATHE_RECIPES))
            .recipeModifier(GTORecipeModifiers.UPGRADE_OVERCLOCK)
            .workableTieredHullRenderer(
                    new ResourceLocation("gtohjs", "block/machines/example_single_machine"))
            .register();

        if (GTRegistries.MACHINES.get(ID) != definition)
            throw new IllegalStateException("Machine registration failed: " + ID);
    }
}
```

普通配方机可使用上面的通用模板；单方块多方块部件应参考当前已验证的 `MEInputAssemblyRegistration`，并逐项核对 tier、ability、真实 recipe handler、renderer 和 tooltip。已删除的 `CustomLatheRegistration` 不再是有效模板。GTO 注册器通常使用 `gtocore` namespace；不要因为 Java 包名是 `com.gtohjs` 就把机器 ID 写成 `gtohjs:*`。

### 3. 蒸汽与电力的选择

| 类型 | 控制器实现 | 常用 builder 设置 | 关键注意事项 |
| --- | --- | --- | --- |
| 普通电力 | `ElectricMultiblockMachine` 或 `SimpleTieredMachine` | `recipeModifier(...)`、电力仓 predicate | 由 EU/t 和电压 tier 控制；仓室能力必须同时有运行时 trait |
| 低级蒸汽 | `SteamMultiblockMachine` | `.steamOverclock()` | 只开放低级蒸汽仓；通常要求排气仓；不要自动加入维护/并行/加速仓 |
| 高级蒸汽 | `LargeSteamMultiblockMachine` | `.steamOverclock(0)` 或原机对应参数 | 能处理大型蒸汽换算和并行；是否允许普通高级输入仓由 pattern 单独决定 |

电力机器可以使用 `PartAbility.IMPORT_ITEMS`、`EXPORT_ITEMS`、`IMPORT_FLUIDS`、`EXPORT_FLUIDS`；蒸汽机器必须区分 `STEAM`、`STEAM_IMPORT_ITEMS`、`STEAM_EXPORT_ITEMS` 与 GTO 的蒸汽流体能力。仅仅把仓室放进 pattern 并不会自动给控制器增加对应运行逻辑。

### 4. 材质、翻译和预览

纹理应复制到 GTOHJS 自己的 `assets/gtohjs/textures`，不要修改 GTOCore JAR。机器 renderer 可以引用 GTOCore 的已存在材质，也可以引用 GTOHJS 的副本。至少提供：

```json
{
  "block.gtocore.example_single_machine": "示例单方块机器",
  "item.gtocore.example_single_machine": "示例单方块机器",
  "machine.gtocore.example_single_machine": "示例单方块机器"
}
```

EMI/XEI 页面由 definition、recipe type 和 renderer 自动生成；不要手工改 EMI 代码。

### 5. 验收清单

1. coremod 日志显示目标 `<clinit>` 注入次数为预期值。
2. `GTRegistries.MACHINES.get(ID)` 与 builder 返回对象相同。
3. tier、recipe type、UI、renderer 均非空。
4. 客户端日志没有 `ExceptionInInitializerError`、拆分包错误或 registry freeze 错误。
5. 使用 `/give` 或 EMI 检查物品、UI 和配方页；不要只以编译成功作为验收。

## English

### Registration window

GTO machines are not plain Forge `DeferredRegister` entries. Register a single-block machine immediately before the `RETURN` of the target GTO machine-group `<clinit>()V` (normally `GTOMachines`; use the actual group for GCYM machines). Keep the Java registration class in GTOHJS, but inject its `register()` call from the coremod in that window. Do not first create a definition from common setup, server-start, or a KubeJS script.

### Template and design rules

Use `MachineRegisterUtils.machine(...)`, then set the tier, language, recipe type or abilities, real recipe handlers, renderer and tooltips. GTO-owned registrations normally use the `gtocore` namespace even when the implementation lives in `com.gtohjs`. Use `MEInputAssemblyRegistration` as the verified single-block part example; the removed Custom Lathe is no longer a valid template. Validate registry identity, not only the path.

Steam machines require steam-specific controller classes and `PartAbility` predicates. Electric machines require an electric controller and matching energy/input/output traits. A hatch accepted by a pattern is not automatically a runtime capability. Copy textures into GTOHJS resources instead of editing GTOCore. Let the registered definition generate EMI/XEI previews; never patch EMI for a machine-registration problem.

### Acceptance checklist

- Coremod reports the expected injection count.
- The registry entry and returned definition are the same object.
- Tier, UI, recipe type and renderer are non-null.
- The client reaches load complete without registry-freeze, split-package or initializer errors.
- Verify the actual item, screen and recipe page in the client.
