# GTOHJS Method-Mode Recipe Injection Task

请在当前 GTOHJS 项目中分析并实现“方法模式”的配方注入结构。

## 项目范围
开始前请读取当前目录路径上的 `AGENT.md` / `AGENTS.md`、项目文件索引、读取索引，以及以下现有资料：

- `docs/08_recipe_registration.md`
- `GTOHJS_REGISTRATION_TEMPLATES_ZH.md`
- `src/main/resources/coremods/gtohjs_machine_registration.js`
- 当前已有的 Java 配方注册类

## 目标

当前配方注册需要在 CoreMod JS 中为每个配方手写大量 ASM 构建代码。希望改成：

```text
参数 Java 类
    ↓
统一配方构建与注入方法类
    ↓
CoreMod 注入 Data.commonInit()
    ↓
运行时执行 RecipeBuilder.save()
```

“方法模式”的具体含义：

- 提供一个统一的方法类，预先写好配方注入的通用格式；
- 其他 Java 类只填写配方参数；
- 不使用 Datagen；
- 不生成额外 Java 源文件；
- 参数类直接参与正常 Java 编译和 JAR 构建；
- 统一方法类负责生成 ASM 指令并接入现有 CoreMod 注入流程。

## 必须保留的 GTO 生命周期

当前配方必须继续按照以下顺序运行：

```text
Data.commonInit()
    ↓
RecipeFilter.init()
    ↓
RecipeType.recipeBuilder(...)
    ↓
输入、输出、流体、EUt、duration
    ↓
RecipeBuilder.save()
    ↓
RecipeBuilder.finish()
```

`RecipeBuilder.save()` 不能在普通 Java 包装方法中直接执行。统一方法类应当生成对应的 ASM 指令，使真正的 `save()` 仍然被注入到 `Data.commonInit()` 中 `RecipeFilter.init()` 之后。

不得因为改造而产生 `gtceu:default` DUMMY 配方，也不得绕过现有 GTO 原生配方注册窗口。

## 推荐结构

请结合现有实现设计并实现以下职责。实际类名可以根据项目风格调整，但不得重复创建已经存在的配方保存路径。

### 1. 配方参数类

用于声明配方参数，例如：

- 原始配方 ID；
- RecipeType 或 RecipeType 字段引用；
- 输入物品和数量；
- 输出物品和数量；
- 输入流体和数量；
- 输出流体和数量；
- EUt；
- duration；
- 必要的条件或校验信息。

参数类不应手写 `InsnList`、`FieldInsnNode` 或 `MethodInsnNode`。

目标使用形式可以类似：

```java
RecipeSpec.builder("my_assembler_recipe")
        .recipeType("ASSEMBLER_RECIPES")
        .inputItem("gtceu:steel_plate", 2)
        .outputItem("gtohjs:my_machine", 1)
        .eut(30L)
        .duration(200)
        .build();
```

### 2. 统一配方构建方法类

统一方法类负责：

- 将参数转换为 ASM 指令；
- 构造 `ResourceLocation`；
- 获取 RecipeType；
- 调用 `recipeBuilder(...)`；
- 写入物品、流体、EUt 和 duration；
- 生成 `save()` 调用；
- 调用现有或统一的 Java 接收/校验方法。

统一方法类不能在 CoreMod 转换阶段直接执行真正的 `RecipeBuilder.save()`。它只能构造将来注入到目标方法中的字节码。

### 3. CoreMod 注入接口

尽量复用当前 `gtohjs_machine_registration.js` 的注入入口：

- 定位 `com.gtocore.data.Data.commonInit()`；
- 查找唯一的 `RecipeFilter.init()`；
- 在其后插入所有方法模式配方生成的 ASM；
- 查找唯一的 `RecipeBuilder.finish()`；
- 在其后执行最终配方验证；
- 目标缺失、重复或结构变化时必须抛出错误，不能静默跳过。

CoreMod JS 应尽量只保留目标定位和统一入口，不再为每个普通配方重复手写完整 ASM 构建函数。

### 4. 统一验证

验证逻辑至少应检查：

- 配方结果不为 `null`；
- 配方 ID 不是 `gtceu:default`；
- 使用 `RecipeBuilder.getTypeID(rawId, recipeType)` 获取最终 ID；
- RecipeType 正确；
- EUt 和 duration 正确；
- 输入输出和流体内容正确；
- `RecipeBuilder.finish()` 后最终配方表仍保留同一个配方对象。

## 类加载与 ABI 要求

实现前必须确认并避免以下问题：

- 参数类静态初始化提前触发 GTORecipeTypes 或注册表初始化；
- CoreMod 转换阶段提前调用真实的 RecipeBuilder；
- 注册表被提前读取或触发冻结/解冻错误；
- 客户端专用类被服务端或转换阶段加载；
- RecipeType 对象被过早保存导致 GTO 静态初始化顺序变化。

如果保存 RecipeType 对象存在提前初始化风险，应保存字段名、类名或其他延迟解析信息，并在实际注入的 ASM 中使用 `GETSTATIC`。

必须保留当前已验证的：

- `ChemicalHelper` 材料物品处理；
- `GTMaterials` 和 `GTOMaterials` 的现有来源区分；
- 普通物品 ID 的现有字符串处理方式；
- EUt 为 `long` 的 ASM 描述符；
- 超出 `SIPUSH` 范围时的正确常量指令；
- raw ID 与最终 type-prefixed ID 的区分。

## 迁移策略

不要一次性重写全部配方。

1. 先审查当前配方注入和已有 Java 接收类。
2. 设计统一参数对象和构建方法。
3. 选择一个简单的现有固定配方进行迁移。
4. 对比迁移前后的最终 ID、RecipeType、输入、输出、EUt、duration 和注册数量。
5. 验证通过后，再迁移其他普通固定配方。
6. 动态材料配方、特殊代理配方或具有独立生命周期的配方可以暂时保留专用路径。
7. 删除重复逻辑前，确认没有其他调用方。

不得为方法模式创建与当前 Java 注册类完全重复的第二套 `save()` 路径。

## 明确排除

本任务不实现以下方案：

```text
参数 Java
    ↓
Datagen
    ↓
生成 Java 源文件
```

不新增 Datagen 任务、不生成 `build/generated` Java、不把 Java 源文件作为中间产物写入项目源码目录。

## 项目执行规则

- 只修改活动 GTOHJS 项目，不修改 GTOCore、GTOLib、GTCEu、EMI 或 Git 镜像；
- 所有文件修改使用 Codex 客户端补丁机制；
- 构建必须使用 Java 21；
- Gradle 构建必须联网；
- 默认使用精确命令 `./gradlew clean build`；
- 网络或依赖解析失败时立即停止并报告；
- 构建成功后按项目规则备份并部署 JAR 到固定客户端；
- 修改行为后更新英文开发文档和读取索引。

## 最终输出

请说明：

1. 当前配方注入结构的问题；
2. 方法模式是否能在当前 GTO 生命周期中安全实现；
3. 计划新增或修改的文件；
4. Java 参数类、统一方法类和 CoreMod 的职责边界；
5. 如何避免提前触发注册表和 RecipeType 初始化；
6. 第一个迁移配方的前后验证结果；
7. 构建、部署和客户端验证结果；
8. 尚未迁移的特殊配方及原因。

如果发现方法模式无法安全实现，不要强行重写。请保留当前已验证路径，并提出最小化的结构改进方案。
