# GTOHJS fix64 自定义并行与线程 / Custom Parallelism and Threads

## 中文

fix64 只更改 `gtocore:hyperdimensional_smelter` 和 `gtocore:hyperdimensional_chemical_factory` 的并行与 CrossRecipe 线程配置。两台机器不再根据线圈温度、线圈等级或成型时的线圈公式限制玩家输入；左侧并行页和线程页始终提供完整的 GTOLib 数据范围：

```text
并行数：1 .. 9,007,199,254,740,991
线程数：1 .. 2,147,483,647
```

这里的两个最大值是 GTOLib 26.7.4 接口的硬数据类型边界，不是线圈或机器等级上限。`CrossRecipeTrait` 直接计算 `parallel * thread`，因此仍保留 `Long.MAX_VALUE` 乘积溢出保护：最后修改的一项优先保留，必要时自动下调另一项。这个保护不能取消，否则正数配置可能溢出为负数并破坏配方调度。

存档字段 `configuredParallel` 和 `configuredThread`、左侧图标、同步协议及“最后编辑项优先”规则保持不变，所以旧世界无需迁移。默认值仍是最大并行和 1 线程。线圈仍属于结构材料；超维度冶炼炉仍检查配方所需温度，但线圈不再决定并行或线程容量。

结构、配方模式、所有配方 1t、真空等级 4、激光供能、维护/消声要求以及禁止并行仓、加速仓、线程仓和超频仓的规则全部不变。机器介绍改为“完全自定义并行与多线程”，明确左侧设置、不受线圈限制和系统数据范围。

## English

Fix64 changes only the parallel and CrossRecipe-thread configuration of the Hyperdimensional Smelter and Hyperdimensional Chemical Factory. Coil temperature, coil tier and formation-derived coil formulas no longer clamp either player setting. The left-side configurators always expose the full GTOLib ranges: parallelism from 1 to `9,007,199,254,740,991`, and threads from 1 to `2,147,483,647`.

These maxima are hard GTOLib 26.7.4 data-type boundaries, not coil or machine-tier limits. `CrossRecipeTrait` directly multiplies parallelism by threads, so the `Long.MAX_VALUE` product-overflow guard remains mandatory. The last edited setting wins and the other setting is reduced only when needed to keep that product representable.

Saved field names, configurator icons, synchronization, defaults and old-world compatibility are unchanged. Coils remain structural materials, and the smelter still checks recipe temperature, but coils no longer determine capacity. Structures, recipe modes, one-tick processing, vacuum tier, laser power, maintenance/muffler requirements and the bans on amplification and overclock hatches are unchanged.
