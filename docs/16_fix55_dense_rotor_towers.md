# GTOHJS fix55 密实旋转高塔 / Dense Rotor Towers

## 中文

### 设计基线

fix55 取消 fix54 的低成本材料限制，并用四座高耸、封闭、密实的圆角塔替换开放骨架。设计直接参考
`gtocore:super_blast_smelter` 的实际 MBS：有效包围盒 `23 x 43 x 23`、6908 个结构块、888 个
`heatingCoils()`、48 个框架。参考机的圆润感来自严格对称的离散八边形、逐层收径、水平功能带和顶部再收束，
框架仅占约 0.69%。

所有 fix55 塔体都采用以下规则：

- 奇数宽、X/Z 对称的离散八边形主体，双层以上实心底座。
- 连续封闭外壳、实体中央核心和每 4–5 层一张实心隔板，不再出现贯通式空心塔。
- 外形逐层收径，顶部形成低框架占比的顶冠。
- 39 个 `H` 仓室点仍严格使用 `leap_forward_one_blast_furnace` 相对控制器投影。
- 最坏区块偏移下均为 `3 x 3` 区块，不超过既定 `4 x 4` 上限。

### 旋转方块

外周黄色节点使用 `gtocore:spacetime_compression_field_generator`。它是普通可放置方块，纹理为 32 帧、
`frametime=1` 的圆形转子动画；不依赖机器工作状态，因此停机、1t 配方间隙和 EMI 默认结构预览中都能持续旋转。
它已经被 GTOCore 原生多方块通过普通 `blocks(...)` 谓词使用，不需要新 BlockEntity 或 renderer。

`gtocore:rotating_transparent_surface` 只是物品，不能进入 pattern。`quantum_force_transformer_coil` 只在
ActiveBlock 运行态播放流动纹理，因此只作为锻炉和蒸汽炉的内部固定线圈，不承担外周持续旋转效果。

### 四台结构

| 机器 | 尺寸 W x H x D | 结构块 | 线圈 | 转子 | 框架 | 最大封闭空腔 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| 超维度锻炉 | 29 x 35 x 29 | 11387 | 756 个固定量子线圈 | 80 | 0 | 524 |
| 超维度蒸汽熔炉 | 27 x 37 x 27 | 11077 | 756 个固定量子线圈 | 80 | 0 | 324 |
| 超维度冶炼炉 | 29 x 47 x 29 | 15175 | 1512 个可替换线圈 | 120 | 32，0.21% | 680 |
| 超维度化工厂 | 31 x 43 x 31 | 16087 | 1508 个可替换线圈 | 120 | 0 | 748 |

锻炉使用超维度外壳、维度稳定外壳、维度桥接方块、硅岩合金外壳、铼强化聚能玻璃和量子操纵者线圈。
蒸汽炉保留青铜/蒸汽主体和火箱、管道、齿轮视觉，同时加入量子线圈与铼强化聚能玻璃。冶炼炉复用高温冶炼
外壳、热风口、极限进气、硅岩合金外壳、铼玻璃和 `heatingCoils()`。化工厂使用惰性外壳、强化基座、
维度桥接/超维度外壳、化学级玻璃、PTFE 管道和 `heatingCoils()`。材料成本不再作为结构生成器约束。

### 不变的运行契约

- 锻炉仍只运行土高炉配方，无能源，固定 524288 并行，结果为 1t。
- 蒸汽熔炉仍只运行熔炉配方，使用既有蒸汽仓能力，固定 524288 并行，结果为 1t。
- 冶炼炉仍运行电力高炉/合金冶炼炉配方，必须安装维护仓和唯一专用消声仓；消声仓现在位于顶冠
  `aisle=14,row=46,column=14`。
- 化工厂仍运行化学反应釜/大型化学反应釜/聚合反应配方，必须安装维护仓，真空等级保持 4。
- 冶炼炉和化工厂继续使用 fix53 的 GTO 化工复合体指数线圈容量、左侧并行/线程页、客户端上限同步和服务端限幅。
- 四台机器仍禁止超频、并行、加速和线程仓；既有能源、激光、I/O、催化剂及蒸汽仓边界没有扩大。
- 空格仍映射为 `Predicates.any()`；配方页、配方修饰器、controller、工作面 renderer、coremod 生命周期和 EMI
  集成均未改变。

### 生成和验证

历史外部开发脚本 `generate_hyperdimensional_redesign.js` 是四份 pattern 的设计源。它强制检查：尺寸、4 x 4 区块
上限、控制器、39 个仓位、单一六向连通体、最低高度/结构密度/线圈/转子数量、最大 1% 框架占比、逐层最低
方块数和最大封闭空腔。外部验证脚本 `validate_hyperdimensional_patterns.js` 独立重复这些数据约束；两份脚本均不随公开源码发布。

资源加载器还验证所有方块实际 registry key，避免错误 ID 被静默解析为空气。修改后必须完整重启客户端，
因为 `HyperdimensionalPatternResources` 会缓存 pattern。

## English

Fix55 removes the fix54 material-cost restriction and replaces all four open-frame structures with tall, sealed,
dense octagonal towers. The geometry follows the measured `gtocore:super_blast_smelter`: discrete 45-degree corner
cuts, staged radial contraction, horizontal process bands, a compact crown, many coils and very few frames.

The outer rotor nodes use `gtocore:spacetime_compression_field_generator`. Its 32-frame, one-tick interpolated
texture depicts a circular rotor and runs independently of machine state, so it remains visible while idle and in
the default EMI structure preview. It is an ordinary placeable block already used by GTOCore patterns and needs no
custom block entity or renderer. `rotating_transparent_surface` is only an item and is not usable in a pattern.

The forge is `29 x 35 x 29` with 11,387 blocks, 756 fixed quantum coils and 80 rotor nodes. The steam furnace is
`27 x 37 x 27` with 11,077 blocks, 756 fixed quantum coils and 80 rotors. The smelter is `29 x 47 x 29` with 15,175
blocks, 1,512 replaceable heating coils, 120 rotors and only 32 frames (0.21%). The chemical factory is
`31 x 43 x 31` with 16,087 blocks, 1,508 replaceable coils, 120 rotors and no frames. Every structure remains within
a worst-case `3 x 3` chunk span.

Runtime behavior is unchanged. Each controller retains the exact 39-position Leap Forward One hatch projection.
The smelter keeps mandatory maintenance and one dedicated top-crown muffler; the chemical factory keeps mandatory
maintenance and vacuum tier 4. Fix53 coil limits, left-side parallel/thread configurators and server authority remain
intact. No overclock, parallel, accelerate or thread hatch has been enabled. Pattern spaces still use
`Predicates.any()`, and recipe types, modifiers, controllers, renderers, coremod lifecycle and EMI integration are not
modified.

The canonical generator and the independent validator enforce minimum height, block density, coil and rotor counts,
maximum frame share, per-layer occupancy, one six-neighbor component and bounded enclosed cavities. The Java resource
loader also checks the resolved registry key for every referenced block. A full client restart is required after any
pattern update because pattern resources are cached.
