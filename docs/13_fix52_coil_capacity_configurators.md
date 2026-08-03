# GTOHJS fix52 线圈容量配置器 / Coil Capacity Configurators

## 中文

### 运行上限

超维度冶炼炉与超维度化工厂继续复用 `gtocore:chemical_complex` 的指数线圈容量，但并行和线程必须按 GTOLib 26.7.4 的实际 ABI 分开计算：

```text
raw = 2^min(60, floor(线圈温度 / 900))
并行上限 = min(9,007,199,254,740,991, raw)
线程上限 = min(2,147,483,647, raw)
```

并行接口为 `long`，最终上限是 `IParallelMachine.MAX_PARALLEL = 2^53 - 1`；线程接口 `ICrossRecipeMachine.getThread()` 为 `int`。不得再用一个 `int` 容量函数同时实现二者，否则永恒线圈的并行会被错误截断到 `Integer.MAX_VALUE`。

### 左侧配置栏

两台机器通过 `attachConfigurators(ConfiguratorPanel)` 在原生 Fancy UI 左侧配置栏追加两个独立页面：

- 并行数：`LongInputWidget`，图标为 `gtocore:infinite_parallel_hatch`。
- 线程数：`IntInputWidget`，图标为 `gtocore:max_thread_hatch`。

设定值由 `@SaveToDisk` 持久化。配置器的初始数据和增量更新必须由服务端同步当前线圈上限与实际设定值，不能相信客户端提供的范围。

### 限幅规则

所有入口都执行服务端限幅，包括按钮、文本输入、旧存档值和结构重新成型：

1. 小于 1 的值改为 1。
2. 高于当前线圈等级上限的值改为该线圈支持的最大值。
3. `CrossRecipeTrait` 使用 `parallel * thread` 计算可用工作量，因此二者乘积不得超过 `Long.MAX_VALUE`。最后编辑的一项优先保留，另一项在必要时自动下调。
4. 机器未成型时运行并行和线程返回 0；UI 使用 1 作为可编辑的安全下限。

默认配置为当前线圈支持的最大并行和 1 线程，与原生 `chemical_complex` 的高并行、低线程行为一致。切换为更低等级线圈时，运行值即使来自更高等级旧存档也会自动降至新线圈上限。

## English

The two hyperdimensional coil machines retain the `chemical_complex` exponential coil formula, but parallelism and threads follow separate GTOLib 26.7.4 ABI limits. Parallelism is a `long` capped at `2^53 - 1`; threads are an `int` capped at `Integer.MAX_VALUE`.

Each machine adds two native Fancy UI configurator tabs. The parallel tab uses a `LongInputWidget` and the `gtocore:infinite_parallel_hatch` item icon. The thread tab uses an `IntInputWidget` and the `gtocore:max_thread_hatch` icon. Values are persisted and the server synchronizes both the current coil limit and the effective selection.

Every input path clamps values to `1..currentCoilLimit`. Because GTOLib computes its work budget as `parallel * thread`, the pair is additionally kept within `Long.MAX_VALUE`; the last edited value wins and the other value is reduced when necessary. Runtime values are zero while the structure is unformed. New machines default to maximum coil parallelism and one thread.

