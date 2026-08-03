# GTOHJS fix53 客户端线圈上限与 MV 蒸汽配方 / Client Coil Limits and MV Steam Recipes

## 中文

### 线圈配置器修复

fix52 的服务端线圈公式和输入限幅正确，但客户端 `CoilTrait` 的成型状态与温度没有同步。配置器虽然从服务端收到了正确上限，显示值 supplier 随后又调用客户端的 `isFormed()/getTemperature()`，把并行数和线程数重新限制为 1。

fix53 将服务端下发的并行上限和线程上限分别缓存在客户端机器实例中：

- 服务端仍以真实结构和线圈温度计算运行上限。
- 客户端 UI 只使用配置器同步得到的缓存上限，不自行推断线圈温度。
- 初始打开 UI 和运行中的线圈变化都会同步“上限 + 实际设定值”。
- setter 与运行 getter 的服务端二次限幅保持不变，客户端缓存不能扩大真实能力。

### 通用蒸汽厂 MV 配方

`LargeSteamMultiblockMachine(holder, int)` 的第二个参数是基础可用 EU/t，不是并行数。原通用蒸汽厂传入 `8`，因此只接受 ULV 及以下配方。fix53 改为 `GTValues.V[MV] = 128 EU/t`，并使用 `.steamOverclock(GTValues.MV)` 设置正确的机器等级与提示：

```text
可用配方等级：ULV、LV、MV
最大基础配方功率：128 EU/t
HV 及以上配方：拒绝
```

GTO 原生运行判定使用 `baseEut << steamHatchMultiplier`，所以高级蒸汽仓可能把基础上限继续提高。fix53 额外在 `BaseSteamMultiblockMachine.getRealRecipe` 入口注入按机器 ID 判断的硬限制：仅当控制器为 `gtocore:universal_steam_factory` 时，原始配方输入功率不得超过 128 EU/t。其他蒸汽机器不经过该限制；大型蒸汽仓提供的原生蒸汽超频、动态最大并行数、15 种模式和结构仓室规则均保持不变。

## English

Fix52 calculated and enforced coil limits correctly on the server, but its client value suppliers recalculated the range from an unsynchronized client `CoilTrait`, reducing both displayed settings to 1. Fix53 caches the server-provided parallel and thread limits on the client. Runtime authority remains entirely server-side, including coil clamping and overflow protection.

The second `LargeSteamMultiblockMachine(holder, int)` argument is base recipe EU/t, not parallelism. The universal steam factory now uses `GTValues.V[MV]` and `.steamOverclock(GTValues.MV)`. Because native GTO checks `baseEut << steamHatchMultiplier`, fix53 also injects a machine-ID-guarded hard limit at `BaseSteamMultiblockMachine.getRealRecipe`: only this controller rejects original recipe input above 128 EU/t. ULV, LV and MV recipes remain available, while HV and higher remain unavailable regardless of steam hatch tier. Other steam controllers and the factory's native steam overclocking, dynamic parallel limit, 15 modes and hatch rules are unchanged.
