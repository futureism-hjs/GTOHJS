# GTOHJS fix57 封闭后墙与开放信号阵列 / Sealed Rear and Open Signal Array

## 中文

### 变更边界

fix57 只重构 `gtocore:hyperdimensional_chemical_factory` 的结构资源和对应方块谓词，不改变控制器、三种配方类型、线圈并行/线程公式、真空等级、仓室能力、配方修饰器或 EMI 代码。机器仍使用 GTO 原生 definition/pattern 预览链。

### 后墙封闭

结构坐标继续以 litematic local 坐标描述：控制器侧为 `z=0`，最外层背面为 `z=38`；pattern 反向序列化，因此 `z=38` 对应 `aisle=0`。

- 低层厂房后墙新增 211 个强化基座：`z=38,y=1..4,x=1..47`，以及 `z=38,y=5,x=13..35`。
- 上塔原 U 形开口改为 `x=20..28,y=22..29,z=21..23` 的 216 点密实服务区。`z=23` 是带化学玻璃、管道和齿轮箱节点的封闭后盖，`z=21..22` 填入工艺设备，避免封墙后留下隐藏空腔。
- 两组坐标均由生成器和独立验证器逐点检查，正式结果缺口为 0。

### 开放信号阵列

fix56 在 `y=30` 补出的实心屋面已完全取消。fix57 从 pristine litematic 的原始 U 形轮廓出发，构造开放式信号发射器：

- `y=30`：外部服务环和两条径向馈线，四个象限保持露空。
- `y=31`：第一层钢制信号环和十字馈线。
- `y=32`：四个过渡节点与中央钨钢主管。
- `y=33`：收束后的钛制信号环和馈线。
- `y=34`：第二组过渡节点。
- `y=35`：钨钢顶部信号环。
- `y=36`：五点式钨钢发射端。

生成器固定检查 local `(20,30,12)`、`(28,30,12)`、`(20,30,19)`、`(28,30,19)` 仍为空气，防止开放结构再次退化成实心屋面。最高受检层为 `y=36`。

### 材质和 pattern 符号

`gtocore:spacetime_compression_field_generator` 被完全移除。它不能出现在输入 palette、输出 palette、实际体素、pattern 或 Java predicate 中；启动验证仍检查 `R` 数量严格为 0。

| 符号 | 方块 | 用途 |
| --- | --- | --- |
| `G` | `gtceu:ptfe_pipe_casing` | 化学主管与馈线 |
| `K` | `gtceu:tungstensteel_pipe_casing` | 高压主立管和发射端 |
| `L` | `gtceu:steel_gearbox` | 低压节点 |
| `M` | `gtceu:stainless_steel_gearbox` | 分隔/中继节点 |
| `N` | `gtceu:titanium_gearbox` | 钛管路节点 |
| `O` | `gtceu:tungstensteel_gearbox` | 顶部和高压节点 |
| `P` | `gtceu:titanium_pipe_casing` | 高纯度支路 |
| `Q` | `gtceu:steel_pipe_casing` | 普通回流与服务管路 |

这些方块均在固定 beta 内嵌 GTCEu 26.7.3 中存在，并分别有 blockstate 和 block model。普通连续线路主要使用管道机壳，齿轮箱只作为交汇和分层节点。

### 结构基线

| 项目 | fix57 结果 |
| --- | ---: |
| pattern 尺寸 | `49 x 37 x 39` |
| 最坏区块跨度 | `4 x 4` |
| 受检非空位置 | 17,425 |
| 可替换线圈 `E` | 1,362 |
| 仓室候选 `H` | 39 |
| 框架 `I` | 16 |
| 禁用旋转方块 | 0 |
| 六向连通体 | 1 个，17,425 块 |
| 最大封闭空气腔 | 0 |

空格仍映射为 `Predicates.any()`，所以结构周围和开放信号阵列中的视觉空气位可以放任意方块，不进入结构监听。

### 生成与验证

历史生成流程唯一允许的输入是外部未加工备份：

```text
hyperdimensional_chemical_factory3_original.litematic
```

另一份同名开发素材已是上一版加工结果，不能再次作为输入。相关工具不随公开源码发布；当时使用的等价命令为：

```powershell
python -X utf8 enhance_chemical_factory3.py `
  hyperdimensional_chemical_factory3_original.litematic `
  --litematic-output hyperdimensional_chemical_factory3_completed.litematic `
  --pattern-output src\main\resources\data\gtohjs\structures\hyperdimensional_chemical_factory.pattern `
  --report hyperdimensional_chemical_factory3_report.json
node validate_hyperdimensional_patterns.js
python -X utf8 validate_fix57_litematic_roundtrip.py <completed> <pattern> --report <report>
```

每次修改必须重启客户端，因为 `HyperdimensionalPatternResources` 会缓存 pattern。

### fix57 验收

- Java 21 执行 `clean build reobfJar` 成功，正式产物为 `gtohjs-1.0-for-gtocore-0.5.6-beta_fix57.jar`。
- 完成版 litematic 连续两轮 typed-NBT 重写后语义树、解压字节和体素网格一致；独立解析器与正式 pattern 逐字符重建一致。
- 低层后墙 211 点、上塔服务区 216 点均为 0 缺口；四个屋顶开放样点仍为空；禁用方块在 palette、体素、pattern 映射和旧 `R` 符号中均为 0。
- 固定 beta 客户端使用 Java 21 命令行启动。运行日志确认 `dimensions=49x37x39`、`sealedRear=true`、`openSignalArray=true`、`forbiddenRotors=0`，加载期 `patternBuilt=true`。
- 快速进入“新的世界”成功；EMI 烘焙并重载 84,935 条配方。GTOHJS 和 EMI 定向 `ERROR/FATAL` 均为 0，崩溃标记为 0。
- 该次客户端验收已完成；世界内结构和原生多方块预览检查列为后续人工验收项。原始外部测试报告未包含在公开源码中。

## English

Fix57 changes only the Hyperdimensional Chemical Factory structure and its block predicates. Controller behavior, recipe types, coil limits, vacuum tier, hatch contract, recipe modifiers and EMI code are unchanged.

The low factory rear is sealed at local `z=38` with 211 required blocks. The upper U-shaped opening is also closed and filled as a 216-position process bay over `x=20..28,y=22..29,z=21..23`, preventing a hidden cavity behind the new rear face. Both regions are checked coordinate by coordinate.

The former solid `y=30` roof is removed. A connected open signal array now rises through `y=36`: a broad service ring and radial feeds at `y=30`, successively smaller steel, titanium and tungstensteel rings, transition nodes, a central pressure feed, and a five-block transmitter tip. Four fixed samples on the service deck must remain air, so future regeneration cannot silently turn it back into a solid slab.

`gtocore:spacetime_compression_field_generator` is forbidden in the source palette, output palette, voxel grid, pattern and Java mapping. The structure instead uses PTFE/steel/titanium/tungstensteel pipe casings and steel/stainless/titanium/tungstensteel gearboxes. The final pattern is `49 x 37 x 39`, contains 17,425 monitored blocks, 1,362 coils, 39 hatch candidates and 16 frames, forms one six-neighbor component, has no enclosed air cavity, and still fits within a worst-case `4 x 4` chunk span.

The historical workflow always regenerated from the unprocessed external artifact `hyperdimensional_chemical_factory3_original.litematic`; a similarly named development copy had already been processed. The external tooling is not included in the public source tree. Spaces remain `Predicates.any()`, and a full client restart is required after pattern changes.

The Java 21 clean build, two typed-NBT roundtrips, independent parser, exact pattern reconstruction and runtime client validation all passed. The fixed-beta client registered the `49 x 37 x 39` pattern with `sealedRear=true`, `openSignalArray=true`, `forbiddenRotors=0` and `patternBuilt=true`, entered the test world, and reloaded 84,935 EMI recipes with zero targeted GTOHJS/EMI errors or crash markers. This completed the automated client acceptance; hands-on inspection remained a separate manual acceptance item.
