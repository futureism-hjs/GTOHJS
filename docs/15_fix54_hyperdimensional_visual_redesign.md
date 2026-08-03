# GTOHJS fix54 超维度机器外观重设计 / Hyperdimensional Visual Redesign

> 历史基线：fix54 的开放骨架与低成本约束已被 fix55 取代。当前实现见
> `16_fix55_dense_rotor_towers.md`。
>
> Historical baseline: fix55 supersedes the open-frame and cost-limited fix54
> structures. See `16_fix55_dense_rotor_towers.md` for the current implementation.

## 中文

### 参考机审计结论

fix54 只读分析了 GTOCore 的六台代表机器及其 MBS 结构：

| 参考机器 | 尺寸 W x H x D | 可复用的设计语言 |
| --- | ---: | --- |
| `quantum_force_transformer` | 37 x 16 x 31 | 低矮宽体、重复舱段、仓室集中在服务面 |
| `magnetic_confinement_dimensionality_shock_device` | 23 x 23 x 32 | 轴向套筒、同心约束环、连续中心管线 |
| `hyperdimensional_plasma_fusion_core` | 63 x 83 x 47 | 透明核心井、间隔支撑环、底部服务带 |
| `luv_kuangbiao_one_giant_nuclear_fusion_reactor` | 39 x 17 x 39 | 低矮同心反应堆、径向支撑和少量功能色 |
| `chemical_complex` | 29 x 23 x 13 | 非对称反应罐、PTFE 管廊、不锈钢承重骨架 |
| `super_blast_smelter` | 24 x 43 x 23 | 分级热工层、可见线圈井、顶部排气冠 |

GTO 的科技感主要来自可读的功能分区、重复节奏、透明核心、框架深度和少量高饱和功能色。它不是把昂贵纹理均匀铺满外墙。`sps_casing`、超维度外壳、时空核心、重夸克块、铼能量玻璃、ABS 外壳和聚变 MK4 外壳因此没有进入新结构。专用动态 renderer 也没有复用，因为它们带具体控制器类型检查和固定尺寸。

### fix54 四台结构

| 机器 | 尺寸 W x H x D | 任意区块偏移下最大跨度 | 结构块 | 造型 |
| --- | ---: | ---: | ---: | --- |
| 超维度锻炉 | 31 x 15 x 25 | 3 x 3 区块 | 1111 | 横向三联锻压舱、对置压臂、砧座和顶部传动桥 |
| 超维度蒸汽熔炉 | 25 x 15 x 33 | 3 x 3 区块 | 1168 | 四道轴向涡轮环、三段锅炉、外露蒸汽主管和端盖 |
| 超维度冶炼炉 | 25 x 21 x 25 | 3 x 3 区块 | 1118 | 透明线圈井、双轴磁约束环、赤道散热环和排气冠 |
| 超维度化工厂 | 25 x 19 x 25 | 3 x 3 区块 | 1243 | 高低错层双反应罐、PTFE 管廊和两座咬合龙门架 |

四台均远小于用户要求的 4 x 4 区块上限。这里按结构可能相对区块边界任意偏移的最坏情况计算，不能简单用 `ceil(width / 16)`。

### 材质与成本

超维度锻炉使用钢制机器外壳、坚固钢壳、钢火箱、钢框架、热风口、少量层压玻璃和钢管道。三个舱段共享传动桥，不使用量子操纵者的 SPS、时空或超维度材料。

超维度蒸汽熔炉使用青铜机器外壳、蒸汽机器外壳、青铜火箱、框架、管道和 48 个齿轮箱节点。齿轮箱只标记四道涡轮环的功能节点，不再整圈铺设；192 块层压玻璃形成锅炉观察段。

超维度冶炼炉使用高温冶炼外壳、热风口、普通/极端发动机进气、钢框架、126 块层压玻璃和 148 个可替换线圈位。旧结构的 355 个硅岩框架和 780 个线圈被移除。唯一 `M` 消声仓位位于远端外露排气点 `aisle=0,row=10,column=12`。

超维度化工厂使用惰性 PTFE 外壳、PTFE 管道、227 个 PTFE 反应罐支撑、263 个不锈钢厂房框架、238 块化学级玻璃和 143 个可替换线圈位。普通承重结构由不锈钢承担，PTFE 只用于工艺区域；强化基座仅 31 个。

### 不变的机器契约

- 每台机器仍有且仅有 39 个 `H` 仓室候选位；相对控制器坐标与 `leap_forward_one_blast_furnace` 保持一致。
- 锻炉仍只接受既有物品输入/输出规则；蒸汽熔炉仍要求一个蒸汽仓并保持既有输入/输出规则。
- 冶炼炉继续要求维护仓和专用消声仓，并支持既有能源/激光及 I/O；化工厂继续强制维护仓并支持既有能源/激光、I/O 和催化剂仓。
- 两台线圈机器继续使用 fix53 的线圈并行/线程公式、左侧配置页和服务端限幅。
- 超频、并行、加速和线程仓没有因外观重画而开放。
- 所有空格继续映射为 `Predicates.any()`，不会因为在视觉空位放方块而触发结构重检。
- 配方类型、配方修饰器、机器 controller、工作面 renderer 和 EMI 注册链均未修改。

### 结构资源流程

历史外部开发脚本 `generate_hyperdimensional_redesign.js` 是 fix54 四份 `.pattern` 的设计源。脚本先构造体素模型，再断言：

1. 尺寸和占地不超过 64 x 64。
2. 控制器唯一且在控制器端底层中心。
3. 39 个仓室点精确匹配既有相对坐标。
4. 冶炼炉恰好一个专用消声位，其他机器没有 `M`。
5. 线圈存在，全部字符都有 Java predicate，昂贵玻璃不超过预算。
6. 结构块数不超过每台预算，主六向连通体占比至少 95%。fix54 四台实际均为单一连通体，主连通占比 100%。

历史生成流程先以 `node generate_hyperdimensional_redesign.js --apply` 写入正式资源，再运行外部验证脚本 `validate_hyperdimensional_patterns.js`，随后使用 Java 21 完整构建并重启客户端。这些脚本不随公开源码发布；不要手工编辑长 `.pattern`，否则容易重新引入 Y 轴翻转、行宽、符号映射和仓室投影错误。

历史外部预览素材 `hyperdimensional_redesign_preview.png` 用于快速检查轮廓和 palette；最终方向、仓室可达性和成型结果仍以 GTO 原生世界/EMI 预览为准。

## English

Fix54 redesigns the four hyperdimensional structures after auditing six GTOCore reference multiblocks. It borrows their geometry rather than their endgame material lists: repeated bays from the Quantum Force Transformer, axial confinement rings from the dimensional shock device, a visible core well from the plasma fusion core, a low radial silhouette from Kuangbiao One, asymmetric process modules from the Chemical Complex, and the thermal hierarchy/exhaust crown from the Super Blast Smelter.

The forge is now a `31 x 15 x 25` three-cell press line. The steam furnace is a `25 x 15 x 33` four-ring axial boiler. The smelter is a `25 x 21 x 25` magnetic coil vessel. The chemical factory is a `25 x 19 x 25` pair of staggered reactors under interlocking gantries. Every design spans at most `3 x 3` chunks even at the worst possible chunk offset, stays within the requested `4 x 4` limit, and forms one six-neighbor connected component.

Cost is controlled with steel, bronze, inert/PTFE casings, ordinary frames, pipes and narrow observation bands. The new patterns avoid bulk SPS casing, dimensionally transcendent casing, spacetime cores, rhenium energy glass, heavy-quark blocks, ABS casing and high-tier fusion casing. The smelter uses 126 laminated glass blocks and 148 replaceable coils instead of the previous 355 Naquadah frames and 780 coils. The chemical factory uses 238 chemical-grade glass blocks and separates its stainless structural gantries from its PTFE process supports.

Runtime contracts are unchanged. Each controller retains exactly 39 hatch-capable positions relative to the controller, matching the established Leap Forward One projection. The smelter retains one dedicated muffler position. Existing steam, maintenance, energy/laser, I/O and catalyst rules remain intact, as do the fix53 coil limit configurators. Spaces still use `Predicates.any()`. No recipe type, recipe modifier, controller, casing renderer or EMI integration code was changed.

The historical external generator was `generate_hyperdimensional_redesign.js`. It validated dimensions, controller and hatch coordinates, symbol mappings, coil/muffler requirements, material budgets and six-neighbor connectivity before writing resources. That tooling is not included in the public source tree. The generated resources were then validated, built with Java 21, and tested after a full client restart because `HyperdimensionalPatternResources` caches loaded patterns.
