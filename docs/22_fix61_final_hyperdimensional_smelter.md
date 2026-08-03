# GTOHJS fix61 最终版超维度冶炼炉 / Final Hyperdimensional Smelter

## 中文

### 结构来源与方向

fix61 只替换超维度冶炼炉的结构、结构方块 predicate、仓位布局和控制器外壳外观。只读模型源为：

```text
外部开发素材 `超维度冶炼炉最终版.litematic`
```

模型是单 region `Unnamed`，position `(48,0,0)`，signed size `(-49,34,39)`，正式尺寸为 `49 x 34 x 39`。控制器 local 坐标是 `(24,2,0)`，导入后的 pattern 坐标是 `(aisle=38,row=2,column=24)`。

序列化继续使用已经过客户端验证的方向：aisle 按 local Z 从最大到最小，row 按 local Y 从最小到最大，negative signed X 的 column 按 local X 从最大到最小。模型空气仍映射 `Predicates.any()`，空气位置可放任意方块，也不进入结构监听。

### 外壳与仓位

控制器 appearance block、27 个仓位的基础外壳和工作状态 renderer 均改为 `gtocore:naquadah_alloy_casing` 对应的 `gtocore:block/casings/hyper_mechanical_casing`。模型中原有的高温冶炼外壳仍是结构材料，不被批量替换。

仓位严格复用超维度化工厂的控制器正面服务面：

```text
aisle = 38
row = 1..4
column = 21..27
排除控制器 (38,2,24)
总计 27 个 H
```

`H` 只允许硅岩合金机械外壳、普通能源输入、激光输入、物品/流体输入输出和恰好一个维护仓。并行仓、加速仓、线程仓和超频仓仍不允许。

模型顶部的 5 个 `gtocore:me_muffler_hatch` 是消声仓候选点：

```text
(23,33,24)
(24,33,23)
(24,33,24)
(24,33,25)
(25,33,24)
```

这 5 点使用 `naquadah_alloy_casing OR MUFFLER` predicate，并由全局 `setExactLimit(1)` 要求恰好安装一个消声仓；其余四点使用硅岩合金机械外壳。

### 精确结构基线

正式 pattern 的精确符号数量为：

| 内容 | 数量 |
| --- | ---: |
| 忽略空气 | 50,370 |
| 高温冶炼外壳 | 9,297 |
| 硅岩合金机械外壳（非仓位） | 2,124 |
| PTFE 管道外壳 | 770 |
| 可替换线圈 | 825 |
| 硅岩框架 | 750 |
| 钨钢管道外壳 | 247 |
| 极限引擎进气外壳 | 241 |
| 散热片 | 205 |
| 引擎进气外壳 | 112 |
| 仓位 `H` | 27 |
| 消声候选点 `M` | 5 |
| 控制器 `S` | 1 |

受检位置总数为 14,604，构成一个六向连通体；最大封闭空间为 3,200 格。任意区块对齐下的最坏占地仍为 `4 x 4` 区块。

### 不变运行机制

以下行为不变：电力高炉与合金冶炼炉两个配方模式、永恒线圈温度驱动的并行/线程上限、玩家左侧并行/线程配置页、两项采用相同上限、所有配方 1t、激光供能以及无增幅仓/无超频仓限制。GTOCore、GTOLib 和 EMI 文件均未修改。

### 生成与验证

```powershell
python -m py_compile import_fix61_hyperdimensional_smelter.py
python -X utf8 import_fix61_hyperdimensional_smelter.py --apply
node validate_hyperdimensional_patterns.js
```

以上命令依赖不随公开源码发布的历史外部工具。导入器硬校验源文件、region metadata、完整 palette、控制器、27 个服务仓位、5 个 ME 消声标记、精确符号数量、六向连通性和最大封闭空间。`--apply` 只覆盖正式冶炼炉 pattern。

## English

Fix61 replaces only the Hyperdimensional Smelter structure, block predicates, hatch layout and controller casing appearance. Its read-only external source was `超维度冶炼炉最终版.litematic`.

The single region has position `(48,0,0)`, signed size `(-49,34,39)` and final dimensions `49 x 34 x 39`. The controller maps from local `(24,2,0)` to pattern `(aisle=38,row=2,column=24)`. Aisles remain far-end-to-controller, rows remain bottom-to-top, and spaces remain `Predicates.any()`.

The controller appearance, service-face base casing and casing renderer now use `gtocore:naquadah_alloy_casing` and its canonical `gtocore:block/casings/hyper_mechanical_casing` texture. High-temperature smelting casings present in the source model remain distinct required structure materials.

The exact 27-position service face matches the Hyperdimensional Chemical Factory: aisle 38, rows 1 through 4, columns 21 through 27, excluding the controller. These positions accept normal energy or laser input, item/fluid I/O and exactly one maintenance hatch. Parallel, accelerate, thread and overclock hatches remain excluded.

Five ME muffler markers form a cross on the top crown. Each candidate accepts either naquadah-alloy casing or a muffler ability, while the global exact limit requires one muffler in total. The other four candidates are filled with casing.

The final pattern contains 14,604 monitored positions, 825 replaceable coils, 27 service hatches and five muffler candidates. It is one six-neighbor component, its largest enclosed space is 3,200 cells, and its worst-case footprint remains within four by four chunks.

Recipe modes, coil-derived parallel/thread limits, left-side configurators, one-tick processing, laser support and the ban on amplification/overclock hatches are unchanged. GTOCore, GTOLib and EMI files are read-only and unmodified.
