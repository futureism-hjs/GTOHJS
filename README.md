# GTO HJS

> [!WARNING]
> 本项目包含由 AI 生成或在 AI 辅助下完成的代码、文档、材质与任务内容，可能存在错误、安全问题或与上游接口及许可不一致的情况。使用、修改或分发前请自行审查并充分测试。 Parts of this project were generated or produced with AI assistance and may contain errors, security issues, or incompatibilities with upstream APIs and licenses. Review and test them before use, modification, or redistribution.

Current release / 当前正式版本：`gtohjs-2.0-alpha-for-gtocore-0.5.6-beta.jar`

Compatibility / 兼容关系: `GTOHJS` and `GTOHJS-API` are separate repositories. GTOHJS versions before `3.0-alpha` do not require the API Mod. GTOHJS `3.0-alpha` and later require a matching `gtohjs_api` release from the separate API repository.

- [中文完整说明](README_ZH.md)
- [Complete English README](README_EN.md)
- [完整历史更新日志 / Full historical changelog](CHANGELOG.md)
- [2.0-per1 至 2.0-alpha 更新日志 / 2.0-per1 to 2.0-alpha changelog](CHANGELOG_2.0_PER1_TO_2.0_ALPHA.md)
- [本地依赖说明 / Local dependency guide](libs/README.md)
- [开发文档目录 / Development documentation](docs/README_ZH_EN.md)

GTO HJS 为 Minecraft 1.20.1 Forge 版 GregTech Odyssey 0.5.6-beta 扩展更多机器和配方。GTO HJS expands the Minecraft 1.20.1 Forge build of GregTech Odyssey 0.5.6-beta with additional machines and recipes.

## Complete content index / 完整内容索引

- 22 standalone `gtohjs` items / 22 个独立 `gtohjs` 物品：Recipe Editor、Custom Multiblock Structure Exporter、three preloaded AE component packs、Integral Bronze Framework，以及 16 种 World Fragments。
- 18 `gtocore` machine or part definitions / 18 个 `gtocore` 机器或仓室：Fragment World Collection Machine、Large Fragment World Collection Machine、Universal Steam Factory、One-Stop Rare Earth Processing Plant、four Hyperdimensional machines、Advanced Generator Array、Steam Array、Advanced Steam Array、Advanced Alchemy Cauldron、Large Petal Apothecary、ME Input Assembly、ME Stocking Input Assembly、ME Super Pattern Buffer、its Proxy and ME Super Wildcard Pattern Buffer。
- Three new recipe types / 3 个新配方类型：`gtceu:one_stop_rare_earth_processing`、`gtceu:large_petal_apothecary`、`gtceu:fragment_world_collection`。
- 764 recipes validated in the target pack / 当前目标整合包验证 764 条配方：254 Fragment World Collection、408 bulk Forge Hammer、71 Botania proxy、18 shaped crafting、4 chemical sludge、1 sludge electrolysis、3 rare-earth、2 ME assembly and 3 imported machine recipes。
- ME mechanisms / ME 机制：combined item/fluid input assemblies, configurable pattern grids up to 1,800 or 64 slots, bidirectional ME output, offline persistence/retry, scrolling five-row machine-mode pages, per-pattern private catalyst isolation and machine-level shared catalysts。
- Machine mechanisms / 机器机制：15-mode 1t Universal Steam Factory, custom parallelism/threads on advanced machines, fixed 524,288-parallel Hyperdimensional machines, 16/64-boiler Steam Arrays, chanced-input/output rewriting, Botania recipe proxying and runtime recipe-display synchronization。
- Developer tools / 开发工具：machine/crafting recipe draft generation, middle-click item/fluid amount editing, two-point multiblock scanning and Java structure export. The exporter intentionally has no preview page / 导出器当前不包含预览功能。
- Component packs / 元件包：123、42 and 21 item types respectively, `16,777,216` of every type, `20,000` power, independent external-storage UUIDs and in-place content migration。

`ME Placement Tool for gto` is distributed as a completely separate Mod. It is not a GTOHJS dependency, GTOHJS does not reference any of its runtime item IDs, and either Mod can be installed without the other. / `ME Placement Tool for gto` 是完全独立的 Mod，不是 GTOHJS 的依赖；GTOHJS 不引用其运行时物品 ID，两个 Mod 均可单独安装。

The current clean source bundle includes all development documents, registration templates, project rules and indexes. Third-party Mod JARs, Gradle caches, runtime logs and temporary reverse-engineering output are excluded from the clean source copy.

Source code is licensed under [LGPL-3.0-only](LICENSE). Original GTOHJS textures and quest content are licensed under [CC BY-NC-SA 4.0](LICENSE_ASSETS.md). Third-party assets retain their upstream licenses as documented in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
