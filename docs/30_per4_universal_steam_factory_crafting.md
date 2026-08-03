# GTOHJS per4 通用蒸汽厂工作台配方 / Universal Steam Factory Crafting Recipe

## 中文

per4 从外部配方编辑器草稿只读导入 `gtohjs:universal_steam_factory` 工作台有序配方，不修改草稿文件。配方输出 `gtocore:universal_steam_factory`，形状为：

```text
ABA
CDC
EEE
```

- `A`：青铜长杆。
- `B`：青铜板。
- `C`：大型青铜流体管。
- `D`：青铜齿轮。
- `E`：双层青铜板。

注册复用已验证的 `CustomCraftingRecipeRegistration` 和 `VanillaRecipeHelper.addShapedRecipe` 生命周期。输出物品会经过注册表 ID 校验，并加入加载完成阶段的非空输出校验与原生配方表诊断日志。

按用户要求，本版本不启动客户端测试。验收范围为 Coremod JavaScript 语法检查、Java 21 `clean build`、正式 JAR 版本元数据、公开发布树白名单以及 README 禁词检查。

## English

Per4 imports the external recipe-editor draft as the shaped recipe `gtohjs:universal_steam_factory` without modifying the draft. It outputs `gtocore:universal_steam_factory` from long bronze rods, a bronze plate, large bronze fluid pipes, a bronze gear and double bronze plates using the `ABA / CDC / EEE` pattern.

Registration reuses the validated `CustomCraftingRecipeRegistration` and `VanillaRecipeHelper.addShapedRecipe` lifecycle. The output is registry-checked and included in load-complete output validation and native-map diagnostics.

At the user's request, no client test is run for this release. Acceptance is limited to the Coremod JavaScript syntax check, Java 21 clean build, release-JAR metadata validation, public-tree allowlist audit and README forbidden-word scan.
