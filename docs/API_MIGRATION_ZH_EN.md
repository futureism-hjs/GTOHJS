# GTOHJS 3.0 API 迁移副本 / API migration copy

`GTOHJS-must-api` 是 `GTOHJS` 的独立副本，版本为 `3.0-alpha-for-gtocore-0.5.6-beta`。活动源码目录 `GTOHJS` 没有被修改。

This directory is an isolated copy of `GTOHJS` at `3.0-alpha-for-gtocore-0.5.6-beta`; the active `GTOHJS` source tree remains unchanged.

本副本新增 `gtohjs_api` 的必需依赖，并通过 `LegacyGTOHJSApiProvider` 发布现有机器、部件和配方页的元数据。实际机器和配方仍由原先已经验证的 GTOHJS Coremod 模板注册；这样不会把 GTOLib `save()` 调用移到错误的 Forge 事件中。

The copy requires `gtohjs_api` and publishes metadata through `LegacyGTOHJSApiProvider`. Actual machines and recipes continue to use the proven Coremod templates, so GTOLib `save()` is not moved outside GTO's native recipe window.

API JAR 放在本副本 `libs` 目录用于本地编译；发布时请把 API Mod 一起安装。API 与内容 Mod 使用不同 Mod ID，可以同时加载。

> AI 生成警告 / AI-generated notice: this migration document and compatibility provider were prepared with AI assistance and require review against the target pack.
