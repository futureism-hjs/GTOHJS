# per5 Special Parallelism and Steam Arrays / 特殊并行与蒸汽阵列

> [!WARNING]
> 本文包含 AI 辅助整理与实现内容。注册窗口、配方 ABI、仓室集合和运行行为以 GTOCore 0.5.6-beta、GTCEu 26.7.3、目标客户端日志及游戏内验证为准。

## 中文

### 大型碎片世界采集器

`gtocore:large_fragment_world_collection_machine` 继续使用原有碎片世界采集配方页、256 倍耗能和 0.25 倍耗时，但取消固定 64 并行。控制器改为 `CustomParallelMultiblockMachine`，运行时使用 `GTORecipeModifiers.PARALLEL`，左侧标签页允许填写：

```text
1 .. 9,007,199,254,740,991
```

该上限是 `IParallelMachine.MAX_PARALLEL` 的 `long` 数据边界。实际单次执行量仍会被输入数量、输出容量和可用电压限制。机器介绍使用 GTO 标准“特殊并行”属性，并额外说明左侧配置入口。

本次从外部配方草稿目录注册两条工作台配方：

- `gtohjs:large_fragment_world_collection_machine`：IV 电路位使用 `CustomTags.IV_CIRCUITS`，因此接受任意 IV 等级电路。
- `gtohjs:ulv_fragment_world_collection_machine`：按草稿使用橡木原木和泥土。

旧的 `gtohjs:fragment_world_collection_machine` 高阶工作台配方保留；两个配方 ID 不冲突。

### 蒸汽阵列

| 机器 | ID | 内部上限 | 可放锅炉 |
| --- | --- | ---: | --- |
| 蒸汽阵列 | `gtocore:steam_array` | 16 | LP 固体、LP 液体 |
| 进阶蒸汽阵列 | `gtocore:advanced_steam_array` | 64 | LP/HP 固体、液体、太阳能 |

两台控制器继承 `StorageMultiblockMachine` 并实现 `IArrayMachine`。右下角单槽由 GTO 的存储型多方块 UI 提供，槽内同种锅炉的堆叠数量就是参与工作的锅炉数。控制器 definition 固定使用 `DUMMY_RECIPES`；放入锅炉后，`IArrayMachine.recipeTypes()` 从该锅炉 definition 动态取得 `STEAM_BOILER_RECIPES`。固体和液体锅炉仍会按输入内容过滤，避免同一配方页造成燃料类型串用。

运行行为：

1. 不存在预热或冷却过程；有效锅炉配方开始后，在第一个 10t 蒸汽节拍直接按额定值产汽。
2. 每 10t 为每台实际工作的锅炉消耗 1 mB 水并输出蒸汽。
3. 额定输出为原锅炉当前配置基础产量的 1.5 倍。原锅炉满温时每 10t 输出 `baseOutput / 2`，因此阵列每 10t 使用 `baseOutput * 3 / 4`。
4. HP 固体和液体锅炉沿用原版高压锅炉的 0.5 倍燃料配方耗时。

太阳能锅炉使用一个空的 10t 动态节拍配方；水消耗和蒸汽输出统一在控制器的 tick 逻辑中完成，避免配方输出和控制器输出重复。进阶阵列在模型集热管位置调用 `GTUtil.canSeeSunClearly`，日照失效会停止配方。

当前阵列刻意不复刻单方块锅炉的断水爆炸、固体燃料灰烬和原生温度曲线，并完全取消温度、预热与冷却状态。

工作台配方由 `CustomCraftingRecipeRegistration` 在既有原生配方加载窗口中注册：

- `gtohjs:steam_array`：青铜板、青铜普通流体管和 `gtohjs:integral_bronze_framework`。
- `gtohjs:advanced_steam_array`：钢板、钢大型流体管和 `gtocore:steam_array`。

### 结构和仓室

两份 Litematic 都转换为 `3 x 3 x 3` pattern，并使用 `ArrayMachineRenderer` 复用发电阵列的内部机器展示效果。

控制器外观分别复用 `gtceu:bronze_large_boiler` 与 `gtceu:steel_large_boiler` 的基础外壳纹理和工作面贴图，同时保留阵列内部锅炉展示效果。

- 蒸汽阵列以 `gtceu:steam_machine_casing` 位置开放仓室。
- 进阶蒸汽阵列以 `gtceu:solid_machine_casing` 位置开放仓室，并保留固定太阳能集热管点位。
- 只接受 `IMPORT_ITEMS`、`EXPORT_ITEMS`、`IMPORT_FLUIDS` 和 `EXPORT_FLUIDS` 对应方块。
- 水输入和蒸汽输出各至少需要一个流体仓。
- `IMPORT_FLUIDS` 集合中的 `gtocore:heat_hatch` 与 `gtocore:advanced_heat_hatch` 被显式排除；能源、激光、维护、并行、加速、线程、超频等仓室没有开放。

### 生命周期与验证

两台机器在 `GTOMachines.<clinit>` 返回前由既有 Coremod 注册窗口调用。`FMLLoadCompleteEvent` 会验证 registry identity、`DUMMY_RECIPES`、pattern supplier、实际 pattern 构建和 `ArrayMachineRenderer`。Coremod 修改后必须先执行：

```powershell
node --check src\main\resources\coremods\gtohjs_machine_registration.js
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
.\gradlew.bat clean build --stacktrace
```

客户端验收至少包括：两台控制器和结构预览可见、槽位数量限制正确、非法锅炉无法放入、固体/液体燃料不串用、首个 10t 节拍即可输出 1.5 倍额定蒸汽、太阳能失去日照后停机、导热仓不能成型以及日志中无 GTOHJS `ERROR/FATAL`。

## English

The Large Fragment World Collection Machine now uses `CustomParallelMultiblockMachine` and `GTORecipeModifiers.PARALLEL`. Its left configurator accepts `1..9,007,199,254,740,991`; effective work is still bounded by voltage and available I/O. The new large-machine crafting recipe uses `CustomTags.IV_CIRCUITS`, while the new ULV recipe follows the supplied oak-log and dirt draft.

`gtocore:steam_array` stores up to 16 LP solid/liquid boilers. `gtocore:advanced_steam_array` stores up to 64 LP/HP solid, liquid or solar boilers. Both derive `STEAM_BOILER_RECIPES` dynamically from the stored machine through `IArrayMachine`. They have no warmup, cooling or persisted temperature state; nominal output is available on the first ten-tick steam cadence after a valid recipe starts.

At full temperature each active boiler consumes 1 mB of water and emits `baseOutput * 3 / 4` steam every ten ticks, which is 1.5 times the native boiler's full-temperature `baseOutput / 2` burst. HP fuel recipes retain their 0.5 duration multiplier. Solar boilers use an empty ten-tick cadence recipe and require `GTUtil.canSeeSunClearly` at the model's collector position, so water and steam are handled exactly once by the controller.

Their shaped crafting recipes are registered through `CustomCraftingRecipeRegistration` in the established native recipe-loading window: `gtohjs:steam_array` uses bronze plates, normal bronze fluid pipes and the Integral Bronze Framework; `gtohjs:advanced_steam_array` uses steel plates, large steel fluid pipes and the Steam Array.

Both patterns are 3x3x3 and use `ArrayMachineRenderer`. Only item/fluid import and export parts are accepted. Normal and advanced heat hatches are explicitly removed from the shared fluid-input ability set; energy, laser, maintenance and amplification hatches are not accepted. Load-complete validation builds each pattern and verifies its recipe type and renderer.

The Steam Array reuses the controller textures of `gtceu:bronze_large_boiler`, while the Advanced Steam Array reuses those of `gtceu:steel_large_boiler`. `ArrayMachineRenderer` remains in place so the stored-boiler display is preserved.
