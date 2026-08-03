# GTOHJS fix60 配方模式与耗时 / Recipe Modes and Duration

## 中文

### 超维度化工厂

超维度化工厂只注册以下两个机器模式：

```text
gtceu:large_chemical_reactor
gtceu:polymerization_reactor
```

不再单独注册 `gtceu:chemical_reactor`。这不会丢失普通化学反应釜配方，因为 GTOCore 的 `RecipeTypeModify.init()` 明确执行：

```java
LARGE_CHEMICAL_RECIPES.getProxyRecipes().add(CHEMICAL_RECIPES);
```

因此大型化学反应釜模式同时读取普通化学反应釜配方，左侧机器模式页从三项精简为两项。结构、仓室、线圈公式、并行/线程配置、真空等级和 1t 行为不变。

### 通用蒸汽厂

通用蒸汽厂继续保留 15 种配方模式和 MV 及以下功率限制，但所有通过检查的配方最终耗时固定为 1t。

`BaseSteamMultiblockMachine.getRealRecipe` 自己执行并行、耗时倍率和蒸汽仓超频，并且不会调用机器 definition 上的普通 recipe modifier。fix60 因此扩展已有的按机器 ID 过滤的 coremod：

1. 入口仍拒绝原始 EU/t 高于 128 的配方。
2. 原生方法完成并行、蒸汽耗时倍率与超频。
3. 每个原生非空返回值在 `ARETURN` 前调用 `lockRecipeDuration`，只对 `gtocore:universal_steam_factory` 写入 `duration=1`。
4. 其他蒸汽多方块的返回配方原样通过。

## English

The Hyperdimensional Chemical Factory now exposes only large-chemical-reactor and polymerization modes. The standalone chemical-reactor mode is redundant because GTOCore explicitly adds `CHEMICAL_RECIPES` as a proxy of `LARGE_CHEMICAL_RECIPES`; ordinary chemical recipes therefore remain available through the large-chemical mode.

The Universal Steam Factory retains its fifteen modes and MV-or-lower input limit. Every accepted recipe is locked to one tick after native parallel calculation, steam duration scaling and steam-hatch overclocking. The existing machine-ID-guarded `BaseSteamMultiblockMachine.getRealRecipe` transform applies this final lock at each native non-null return. Other steam multiblocks are unchanged.
