# 配方页（RecipeType）注册 / Recipe Type Page Registration

## 中文

“新配方页”就是新的 GTO `RecipeType`：它定义机器 UI 的物品/流体槽位、进度条、声音、配方分组和 EMI 分类。不要手写 EMI 页面。

### Java 模板

```java
public final class ExampleRecipeTypeRegistration {
    public static final String PATH = "example_process";
    public static final ResourceLocation ID = GTCEu.id(PATH);
    private static RecipeType definition;

    public static synchronized void register() {
        if (definition != null) return;
        GTRecipeType existing = GTRegistries.RECIPE_TYPES.get(ID);
        if (existing != null) {
            if (!(existing instanceof RecipeType type))
                throw new IllegalStateException("Wrong recipe type class: " + ID);
            definition = type;
            return;
        }
        definition = RecipeTypeRegisterUtils.register(
                PATH, "示例处理", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(6, 18, 9, 3)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE,
                    FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);
        if (definition == null || !ID.equals(definition.registryName) ||
                definition.getRecipeUI() == null)
            throw new IllegalStateException("Recipe type registration failed: " + ID);
    }

    public static RecipeType definition() {
        if (definition == null) throw new IllegalStateException("Not registered: " + ID);
        return definition;
    }
}
```

`setMaxIOSize(itemIn, itemOut, fluidIn, fluidOut)` 决定配方页和机器 UI 的最大槽位。通用稀土页当前是 6/18/9/3；“每行 3 个”属于 UI 布局实现，不能用错误的槽位顺序代替。

### 注册窗口和机器引用

在 `com.gtocore.common.data.GTORecipeTypes.<clinit>()V` 的 `RETURN` 前注入 `register()`。之后机器 builder 使用同一个 `RecipeType` 对象：

```java
.recipeTypes(ExampleRecipeTypeRegistration.definition())
```

配方页注册必须先于机器和配方引用，但不能在 mod 构造函数中主动读取 `GTORecipeTypes` 静态字段，否则可能触发 `Registry ... cannot be set to unfrozen state`。构造函数只做资源/物品注册；状态验证放在 GTO 注册窗口和 `FMLLoadCompleteEvent`。

### 翻译

```json
{
  "gtceu.example_process": "示例处理",
  "gtohjs.machine.example_multiblock": "示例多方块"
}
```

## English

A new recipe page is a GTO `RecipeType`, not a hand-written EMI page. It owns the recipe group, item/fluid slot limits, progress texture, direction and sound. Register it before the GTO recipe-type registry freezes, by injecting `register()` before the `RETURN` of `GTORecipeTypes.<clinit>()V`. Machines and recipes must reference the same `RecipeType` object.

`setMaxIOSize(itemInputs, itemOutputs, fluidInputs, fluidOutputs)` controls both the recipe page and machine UI. The current rare-earth page uses 6/18/9/3. Do not force GTO recipe-type static initialization from the mod constructor; defer validation to the native registration window and load-complete event. Add translations for the recipe type and machine, then let GTO/GTCEu generate the EMI view.

## 外部 RecipeType 代理 / External RecipeType Proxies

当新 GTO 配方页需要直接运行原版或其他模组的 `RecipeType` 时，可以把外部类型传给 `RecipeType` 构造器。机器搜索数据库会从当前 `RecipeManager` 转换代理配方。不要假设通用 `GTRecipeType.toGTrecipe()` 能保留模组自定义字段：例如 Botania 花药台的种子位于 `getReagent()`，不在 `getIngredients()` 中。

专用代理类型必须同时处理两条路径：

1. override `toGTrecipe()`，完整复制自定义输入并设置 GTO 的功率、时间和扩展；
2. override `buildRepresentativeRecipes()`，把转换结果加入该类型的 main category，否则机器搜索可能成功，但标准 GT EMI 分类没有可显示的 recipe definition。

GTOCore 0.5.6 / GTOLib 26.7.4 在首次装载后会复用 `GTORecipes` 的 RecipeManager 缓存，普通 `/reload` 不能可靠替换代理配方数据库。代理类型仍应在客户端 `RecipesUpdatedEvent` 中从事件携带的 `RecipeManager` 重建分类，但新增或修改外部配方后必须完整重启客户端/服务器，不能把热重载作为验收路径。fix62 的大型花药台是当前模板。

For an external vanilla/mod RecipeType proxy, override both conversion and representative-category construction. Generic GT conversion may omit mod-specific fields, and proxy search entries are not automatically inserted into the GT category consumed by the client recipe cache. GTOCore 0.5.6 reuses its RecipeManager cache after the first load, so client synchronization may rebuild the current category, but adding or changing external recipes requires a full client/server restart rather than `/reload`.
