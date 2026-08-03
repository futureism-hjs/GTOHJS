# GTOHJS fix58 三个用户模型导入 / Three User-authored Structure Imports

> fix59 已替换本文中的超维度化工厂结构与仓位段；锻炉和蒸汽熔炉基线仍然有效。当前化工厂事实见 `20_fix59_explicit_diamond_hatches.md`。
> Fix59 supersedes the chemical-factory structure and hatch sections below. The forge and steam-furnace baselines remain valid; see `20_fix59_explicit_diamond_hatches.md` for the current chemical factory.

## 中文

### 变更边界

fix58 只替换以下三个多方块的结构资源和对应方块 predicate：

| 机器 | 只读模型源 | 正式尺寸 | 最坏区块跨度 |
| --- | --- | ---: | ---: |
| 超维度锻炉 | 外部开发素材 `超维度锻炉.litematic` | `15 x 43 x 15` | `2 x 2` |
| 超维度蒸汽熔炉 | 外部开发素材 `超维度蒸汽熔炉.litematic` | `15 x 43 x 15` | `2 x 2` |
| 超维度化工厂 | 外部开发素材 `超维度化工厂最终版1.litematic` | `49 x 34 x 39` | `4 x 4` |

配方类型、控制器、1t 修饰器、蒸汽消耗、线圈并行/线程公式、左侧配置页、真空等级、仓室能力、renderer 和 EMI 注册链均不改变。超维度冶炼炉也不在本次修改范围内。

### 坐标方向

三个 region 的 signed X 都为负，控制器都在 local 最小 Z 端和底部工作面。导入器使用与 fix56/fix57 已验证化工厂相同的序列化规则：

- aisle：local Z 从最大到最小，远端先、控制器端最后；
- row：local Y 从最小到最大，严格底到顶，禁止 Y 翻转；
- column：negative signed X 使用 local X 从最大到最小；
- 锻炉和蒸汽炉控制器为 `(column=7,row=1,aisle=14)`；
- 化工厂控制器为 `(column=24,row=2,aisle=38)`。

pattern 空格继续映射 `Predicates.any()`。这些视觉空气位不要求为空，也不会被加入结构监听。

### 仓位规范化

三台机器继续使用 `gtocore:leap_forward_one_blast_furnace` 的 39 点仓位投影。锻炉和蒸汽炉的 39 点在源模型中全部已经是对应机器外壳。化工厂的 39 点来自 9 个惰性机壳、13 个 PTFE 管道、4 个强化基座和 13 个空气位；导入时统一覆盖为 `H`，实际搭建时必须使用惰性机壳或原有合同允许的仓室。

该规范化只服务于既有能力合同，不会开放超频、并行、加速或线程仓。化工厂仍强制一个维护仓，并保留普通能源/激光、物品/流体 I/O 和催化剂仓边界。

### 材质与精确结构

超维度锻炉使用钢制机器外壳、固体钢机壳、钢火箱和钢框架；超维度蒸汽熔炉使用青铜机器外壳、蒸汽机器外壳、青铜火箱和青铜框架。两者覆盖 `H/S` 后均为 1,957 个非空位置、7,718 个忽略空格和一个六向连通体。

化工厂使用强化基座、惰性机壳、PTFE/钨钢管道、星金线圈、钠泉框架、不锈钢框架、强化厂房外壳、化学玻璃、承压外壳和 HV 机壳。新增字符 `T` 精确映射 `gtceu:naquadah_frame`。覆盖 `H/S` 后为 14,469 个非空位置、50,505 个忽略空格、922 个可替换线圈和一个六向连通体；禁用的 `gtocore:spacetime_compression_field_generator` 仍为 0。

### 生成与验证

历史正式导入入口使用不随公开源码发布的外部工具：

```powershell
python -X utf8 import_fix58_multiblock_structures.py --apply
node validate_hyperdimensional_patterns.js
```

导入器硬校验源文件、region position、signed size、完整 palette/state 计数、控制器坐标、39 个仓位来源、最终字符计数、六向连通性和封闭空间拓扑。它先输出外部审计副本，只有 `--apply` 才替换正式资源。

历史外部脚本 `generate_hyperdimensional_redesign.js` 从 fix58 起只允许生成未变化的超维度冶炼炉，不能再覆盖这三个用户模型。修改 pattern 后必须完整重启客户端，因为 `HyperdimensionalPatternResources` 会缓存结构。

## English

Fix58 replaces only the structure resources and block predicates of the Hyperdimensional Forge, Hyperdimensional Steam Furnace and Hyperdimensional Chemical Factory. Their source Litematics remain read-only. Runtime controllers, recipe types, one-tick modifiers, steam behavior, coil parallel/thread limits, configurators, vacuum tier, hatch capabilities, renderers and EMI lifecycle are unchanged. The Hyperdimensional Smelter is outside this change.

The forge and steam furnace are both `15 x 43 x 15`; the final chemical factory is `49 x 34 x 39`. Aisles are emitted from the far end to the controller, rows remain bottom-to-top, and negative signed X follows the verified fix56/fix57 column orientation. Pattern spaces remain `Predicates.any()`.

All three structures retain the exact 39-position Leap Forward One hatch projection. Every projected position in the forge and steam source is already its base casing. The chemical source has nine inert casings, thirteen PTFE pipes, four reinforced base blocks and thirteen air cells at those coordinates; the importer normalizes all 39 to `H`. This preserves the established hatch contract and does not enable overclock, parallel, accelerate or thread hatches.

After normalization, each forge/steam pattern has 1,957 monitored positions and one six-neighbor component. The chemical factory has 14,469 monitored positions, 922 replaceable coils and one component. Its `T` symbol maps to `gtceu:naquadah_frame`; forbidden spacetime compression field generators remain absent.

The historical external importer was `import_fix58_multiblock_structures.py --apply`. It validated the source metadata, dimensions, full state counts, controller, hatch sources, exact pattern counts and topology before writing resources. The independent JavaScript validator then checked dimensions, symbols, controller/hatch coordinates, counts and connectivity. Those tools are not included in the public source tree. A full client restart is mandatory after applying a pattern change.
