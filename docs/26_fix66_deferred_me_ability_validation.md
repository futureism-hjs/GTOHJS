# GTOHJS fix66 ME ability 延后校验 / Deferred ME Ability Validation

**基线 / Baseline:** Minecraft 1.20.1, Forge 47.4.20, Java 21, GTCEu 26.7.3, GTOCore 0.5.6-beta, GTOLib 26.7.4.

## 中文

`GTAEMachines.<clinit>()V` 是创建 ME 部件 definition 的正确窗口，但 `.abilities(...)` 的候选方块绑定由后续 Registrate 注册阶段完成。在该静态初始化函数返回前立即读取 `PartAbility.getAllBlocks()` 会得到尚未完成绑定的能力表，导致已成功创建的 `gtocore:me_input_assembly` 被误判为注册失败。

fix66 将校验拆成两阶段：注册窗口内只检查 `GTRegistries.MACHINES` 中的 definition 对象身份；`FMLLoadCompleteEvent` 再检查 `IMPORT_ITEMS`、`IMPORT_FLUIDS` 和 `DUAL_INPUT` 三张能力表。这样仍能严格发现真实漏绑，同时不把正常的 Registrate 生命周期当成错误。机器创建窗口、控制器实现、材质与配方行为均未改变。

## English

`GTAEMachines.<clinit>()V` is the correct definition-creation window, but Registrate binds `.abilities(...)` candidates later. Reading `PartAbility.getAllBlocks()` before that static initializer returns observes an incomplete table and falsely rejects a valid definition.

Fix66 validates registry identity inside the early registration window and defers the three ability-membership checks to `FMLLoadCompleteEvent`. This preserves strict detection of a real missing binding without treating normal Registrate sequencing as a failure. No machine behavior, renderer, recipe handler or registration window changed.
