# 固定并行运行链与 Coremod 架构 / Fixed Parallel Runtime and Coremod Architecture

> 适用基线 / Applies to: Minecraft 1.20.1, Forge 47.4.20, GTCEu 26.7.3, GTOCore 0.5.6-beta, GTOLib 26.7.4, GTOHJS 2.1-alpha fix1.

## 中文

### 1. UI 并行值不等于实际配方并行

`NoEnergyCustomParallelMultiblockMachine` 的并行 trait 只保存、同步并显示玩家配置，并通过 `getParallel()` 暴露数值。它不会自动修改 `GTRecipe`。无能源控制器必须在 `getRealRecipe(...)` 中显式调用：

```java
GTRecipe modified = ParallelLogic.accurateParallel(this, unit, recipe, getParallel());
if (modified != null) {
    modified.duration = 1;
}
return modified;
```

超维度锻炉此前只把最小值和最大值都设置为 `524288`，因此左侧 UI 正确显示固定并行，但运行配方的 `parallels` 仍为 1。fix1 仿照 GTOCore `AdvancedPrimitiveBlastFurnaceMachine` 使用上面的真实执行链。不要只写 `recipe.parallels = 524288`，也不要手工复制输入输出数量；`accurateParallel` 会根据配方内容、可用输入和输出容量生成一致的并行配方。

`HyperdimensionalForgeRegistration` 里的 `ONE_TICK` definition modifier 只锁定耗时，不提供并行。运行控制器仍在 `getRealRecipe(...)` 中再次把最终耗时固定为 1t，以保证并行扩展后的配方也遵守合同。

### 2. 蒸汽控制器的不同合同

超维度蒸汽熔炉继承 GTOCore `BaseSteamMultiblockMachine`。该父类已经在 `getRealRecipe(...)` 中完成以下工作：

1. 检查配方电压是否不高于机器蒸汽电压；
2. 调用 `ParallelLogic.accurateParallel(..., maxParallels)`；
3. 应用耗时倍率和可用的蒸汽超频；
4. 返回真实并行配方。

因此子类必须继续调用 `super.getRealRecipe(...)`，不能再调用一次 `accurateParallel`，否则输入和输出会被二次放大。`524288` 是允许上限，不是无条件强制值；实际并行仍受现有输入数量和输出容量限制。验证时应使用能够容纳测试数量的 ME 输入/输出仓，提供 N 份输入并确认运行配方的 `parallels == N`。只看左侧配置文本、只放一份输入，或使用装不下输出的普通总线，都不能证明并行失效。

### 3. 可任意填充的结构内部位置

高级炼金锅的 14 个中空位置使用：

```java
.where(' ', Predicates.any())
```

GTCEu 的 `FactoryBlockPattern.where` 遇到 `isAny()` 会跳过该字符，不把这些坐标放入结构 predicate。结果是这些位置可以为空气、流体或任意方块；放置和移除不会触发结构重检，仓室也不会在这些位置附着到控制器。资源 pattern 和 14 个字符的数量断言保持不变。

需要强制空气时才使用 `Predicates.air()`。不要用普通“始终为真”的自定义 predicate 代替 `Predicates.any()`，因为它仍可能进入结构监听和部件收集流程。

### 4. 当前代码职责边界

| 层 | 职责 | 维护规则 |
| --- | --- | --- |
| `META-INF/coremods.json` + Coremod JS | 在 GTO 静态初始化和原生配方窗口插入最小方法调用或 builder 字节码 | 保持薄层；目标方法、描述符和命中次数必须校验 |
| `bootstrap/*.java` | 注册机器、配方页、物品及最终状态验证 | 新功能优先复用已验证 registration 模板 |
| `machine/*.java` | 配方运行、并行、线程、仓室和持久状态 | 运行行为放在 Java，不塞入 JS |
| Mixin | 只能处理已验证且普通注册窗口无法覆盖的实例行为 | 不与 Coremod 重复修改同一合同 |
| resources | pattern、语言、模型、纹理和元数据 | ID 与 Java definition 保持一致 |

Forge 1.20.1 的 `META-INF/coremods.json` 直接加载 Nashorn Coremod JavaScript。把该文件机械改成 `.java` 或 `.kt` 不会被 Forge 识别。完全迁移需要实现并注册新的 ModLauncher transformation service，还会改变加载顺序、类加载边界和发布面，风险远大于收益。因此当前优化策略是保留薄 JS 注入层，把注册状态、校验和运行业务放在 Java。本次只删除了 Coremod 中一份完全重复的 `appendRecipePowerAndDuration` 定义，不改变字节码输出。

### 5. 文档优先维护流程

后续开发先阅读 `docs/README_ZH_EN.md`、与目标功能对应的文档以及 `GTOHJS_REGISTRATION_TEMPLATES_ZH.md`。以下情况可以直接依据文档和现有成功模板修改：

- 同一依赖版本下调整机器参数、提示、配方数量或已知结构谓词；
- 复用已有注册窗口、控制器父类和配方构建入口；
- 修复已记录的常见错误且验证结果符合文档。

只有以下情况才定向复查 GTOCore、GTOLib 或 GTCEu 源码：

- 文档没有覆盖目标行为，或文档与活动源码冲突；
- 依赖版本、方法描述符、字段类型或注册阶段发生变化；
- 新增核心运行机制、Coremod/Mixin 目标或从未使用过的仓室；
- 按文档实施后编译、启动或游戏内行为验证失败。

复查时只读取相关类和直接调用链，并把最终结论、适用版本、模板和验证边界写回文档。修改已经文档化的行为时，源码和中英文文档必须在同一任务同步更新。

### 6. 最低验证

1. `node --check src/main/resources/coremods/gtohjs_machine_registration.js`。
2. Java 21 执行非清洁构建；Coremod 注入命中次数和注册验证必须通过。
3. 固定客户端启动后，确认 GTOHJS 无 `ERROR/FATAL` 且相关机器 pattern 构建成功。
4. 锻炉用可容纳 N 份输入/输出的仓室验证实际消耗和产出均为 N，且耗时 1t。
5. 蒸汽熔炉使用同样方法验证其父类并行链；不要把 I/O 限制误判成固定 1 并行。
6. 在炼金锅 14 个内部位置放置并移除普通方块，机器应保持成型；这些方块不得贡献仓室能力。

## English

### Runtime parallelism

The parallel trait on `NoEnergyCustomParallelMultiblockMachine` stores, synchronizes, and displays a configured value; it does not transform a recipe by itself. A no-energy controller must call `ParallelLogic.accurateParallel(this, unit, recipe, getParallel())` from `getRealRecipe(...)`. The Hyperdimensional Forge previously fixed both UI bounds at 524288 without making that call, so its displayed capacity was correct while the running recipe remained at one parallel. Fix1 follows GTOCore's verified `AdvancedPrimitiveBlastFurnaceMachine` path and then locks the transformed recipe to one tick.

The Hyperdimensional Steam Furnace has a different contract. Its GTOCore `BaseSteamMultiblockMachine` parent already checks voltage, calls `accurateParallel` with `maxParallels`, applies duration and steam-overclock rules, and returns the transformed recipe. The child must call `super` exactly once. Calling `accurateParallel` again would scale inputs and outputs twice. Its 524288 value is a ceiling; available inputs and output capacity still determine the effective result.

### Ignored cauldron positions

The Advanced Alchemy Cauldron maps its fourteen internal spaces to `Predicates.any()`. GTCEu omits `isAny()` symbols from the pattern predicate, so air, fluids, or arbitrary blocks are accepted there without formation monitoring or part attachment. Use `Predicates.air()` only when air is mandatory, and do not replace `any()` with a normal always-true predicate.

### Architecture and maintenance policy

Forge 1.20.1 loads the script referenced by `META-INF/coremods.json` through the Nashorn Coremod API. Java or Kotlin files are not drop-in replacements. A full migration would require a custom ModLauncher transformation service and would expand loading-order and compatibility risk. Keep JavaScript as a thin, validated injection layer; keep registration, validation, and runtime behavior in Java. Fix1 removes one duplicate JS helper definition without changing generated bytecode.

Future work must consult this documentation index, the relevant feature document, and the verified registration templates before reopening upstream source. Re-audit only when documentation is missing or contradictory, a dependency/ABI changes, a new core mechanism is required, or documented implementation fails verification. Read only the directly relevant classes and update the bilingual documentation whenever the documented contract changes.
