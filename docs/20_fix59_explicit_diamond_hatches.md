# GTOHJS fix59 钻石标记仓位 / Explicit Diamond-marked Hatches

> fix60 只调整运行时配方模式：移除独立的普通化学反应釜模式，保留大型化学反应釜与聚合反应；本文结构与仓位基线仍然有效。
> Fix60 changes runtime recipe modes only: the standalone chemical-reactor mode is removed, while large chemical and polymerization modes remain. This structure/hatch baseline is still current.

## 中文

### 新结构来源

fix59 只再次替换超维度化工厂结构。锻炉、蒸汽熔炉和冶炼炉继续使用 fix58 已验证资源。

```text
真实结构：外部开发素材 `超维度化工厂最终版完全体.litematic`
仓位标记：外部开发素材 `超维度化工厂最终版仓室位置.litematic`
```

两份文件均为单 region `Unnamed`，position `(48,0,0)`、signed size `(-49,34,39)`。除 27 个仓位标记外，两份体素网格完全相同：仓位文件把完全体中的 27 个 `gtceu:inert_machine_casing` 替换为 `minecraft:diamond_block`。

### 仓位合同

钻石块只作为离线定位标记，不进入正式结构，也不是可安装仓室。导入时从仓位文件取得 27 个坐标，再从完全体取得其余全部材质；正式 pattern 在这些坐标写入 `H`。27 个位置全部位于控制器端 `aisle=38`：

- row `1..4`；
- column `21..27`；
- 排除控制器 `(aisle=38,row=2,column=24)`。

因此正式服务面为 `7 x 4 - 1 = 27` 个 `H`。旧的 Leap Forward One 39 点投影不再用于化工厂，但锻炉、蒸汽熔炉和冶炼炉仍保持各自原有仓位。

化工厂 `H` predicate 的能力集合不变：惰性机壳、普通能源/激光输入、物品/流体输入输出、催化剂仓以及恰好一个维护仓。超频、并行、加速和线程仓仍不允许。

### 真实结构基线

完全体精确 palette：

| 方块 | 数量 |
| --- | ---: |
| `minecraft:air` | 50,463 |
| `gtocore:strengthen_the_base_block` | 9,350 |
| `gtceu:inert_machine_casing` | 2,178 |
| `gtceu:ptfe_pipe_casing` | 792 |
| `gtocore:starmetal_coil_block` | 922 |
| `gtceu:naquadah_frame` | 734 |
| `gtocore:naquadah_reinforced_plant_casing` | 196 |
| `gtocore:chemical_grade_glass` | 176 |
| `gtocore:pressure_containment_casing` | 104 |
| `gtceu:hv_machine_casing` | 22 |
| `gtceu:tungstensteel_pipe_casing` | 20 |
| `gtceu:stainless_steel_frame` | 16 |
| 控制器 | 1 |

覆盖控制器和仓位后，正式 pattern 有 14,511 个受检位置：`H=27`、可替换线圈 `E=922`、控制器 `S=1`。50,463 个模型空气仍写为空格并映射 `Predicates.any()`。

与 fix58 完全体相比，新模型另有 55 个真实材质变化：54 个空气位改为强化基座，1 个空气位改为惰性机械外壳。生成后的全部 14,511 个受检位置构成一个六向连通体；最大封闭空间为 3,200 格。钻石块数量必须始终为 0，因为它们只存在于离线覆盖层，绝不会映射为正式 predicate。

### 生成与验证

fix59 入口只重建化工厂，避免依赖已经从 `机器` 目录移除的旧锻炉/蒸汽炉源文件：

```powershell
python -m py_compile import_fix59_chemical_factory.py
python -X utf8 import_fix59_chemical_factory.py --apply
node validate_hyperdimensional_patterns.js
```

以上命令依赖不随公开源码发布的历史外部工具。导入器硬校验两份模型的 region、尺寸、完整 palette、逐体素差异、27 个钻石坐标、控制器、字符计数、六向连通性和最大封闭空间。它先写入外部审计副本，只有 `--apply` 才覆盖正式资源。

### 不变行为

配方类型、线圈并行/线程公式、左侧配置页、1t 处理、真空等级 4、强制维护仓、能源/激光与 I/O 能力、renderer 和 GTO 原生 EMI 预览链均不改变。GTOCore、GTOLib 和 EMI 文件仍只读。

## English

Fix59 changes only the Hyperdimensional Chemical Factory. The complete Litematic is the authoritative material structure, while the second Litematic is a coordinate overlay: exactly 27 inert casings are replaced by diamond blocks and every other voxel is identical.

Diamond blocks are offline markers only. The importer takes all materials from the complete model and writes `H` at the marked coordinates. All 27 positions are on controller aisle 38, rows 1 through 4, columns 21 through 27, excluding the controller at row 2/column 24. The former 39-position Leap Forward One projection is no longer applied to this machine.

The `H` capability predicate is unchanged: inert casing, normal energy or laser input, item/fluid I/O, catalyst hatches and exactly one maintenance hatch. Overclock, parallel, accelerate and thread hatches remain forbidden. Runtime recipe types, coil parallel/thread calculation, configurators, one-tick processing, vacuum tier, renderer and native EMI preview are unchanged.

The final pattern remains `49 x 34 x 39`, contains 14,511 monitored positions, 27 explicit hatch positions and 922 replaceable coils. Model air remains `Predicates.any()`. GTOCore, GTOLib and EMI sources are not modified.

Compared with the fix58 complete model, the new material model also changes 54 air cells to reinforced base blocks and one air cell to inert casing. All 14,511 monitored positions form one six-neighbor component, and the largest enclosed space contains 3,200 cells. Diamond blocks must never enter the canonical pattern or any runtime predicate.

The historical workflow used the external `import_fix59_chemical_factory.py` importer followed by `validate_hyperdimensional_patterns.js`. The importer validated both source models, their exact voxel differences, the 27 markers, controller orientation, symbol counts, connectivity and enclosed-space baseline before replacing only the chemical-factory resource. Those tools are not included in the public source tree.
