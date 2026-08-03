# GTOHJS 开发资源重新索引 / Development Resource Reindex

**索引日期 / Indexed:** 2026-08-01  
**活动工程 / Active project:** `E:\program\java\GregTech-Odyssey\GTOHJS-Development-Project\GTOHJS`  
**发布镜像 / Publication mirror:** `E:\program\java\GregTech-Odyssey\Git\GTOHJS-git\GTOHJS`

## 中文

### 1. 目录职责

| 路径 | 职责 |
| --- | --- |
| `E:\program\java\GregTech-Odyssey\GTOHJS-Development-Project\GTOHJS` | 日常开发、非清洁构建和客户端验证使用的唯一 per3 活动源码 |
| `E:\program\java\GregTech-Odyssey\GTOHJS-Development-Project\Required-development-files\Developers-file` | 用户提供的机器、配方和其他开发输入；原文件只读 |
| `E:\program\java\GregTech-Odyssey\GTOHJS-Development-Project\Required-development-files\gtohjs-things` | GTOHJS 内部参考文档和可复用资源；不作为构建根目录 |
| `E:\program\java\GregTech-Odyssey\GTOHJS-Development-Project\Agent cache\Chatgpt` | GTOHJS 专属备份、日志、反编译、部署快照和实验缓存 |
| `E:\program\java\GregTech-Odyssey\ME-Placement-Tool-for-gto-Development-Project\ME Placement Tool for gto` | 独立 ME Placement Tool for gto 活动源码 |
| `E:\program\java\GregTech-Odyssey\ME-Placement-Tool-for-gto-Development-Project\Agent cache\Chatgpt` | 该独立 Mod 的专属日志、备份和实验缓存 |
| `E:\program\java\GregTech-Odyssey\Required-development-files--Global` | GTO、GTL、GTCEu、AE2、社区工程和上游 ME Placement Tool 的全局只读参考 |
| `E:\program\java\GregTech-Odyssey\Agent cache--Global\Chatgpt` | 可跨项目复用的反编译、ABI、源码索引和工具缓存 |
| `E:\program\java\GregTech-Odyssey\Git\GTOHJS-git\GTOHJS` | 用户明确要求复制到 Git 时才更新的清洁发布源码镜像 |
| `E:\program\java\GregTech-Odyssey\Git\GTOHJS-git\Release` | 正式 JAR、版本目录和用户要求生成的发布压缩包 |

### 2. 全局只读参考

- GTOCore：`E:\program\java\GregTech-Odyssey\Required-development-files--Global\GregTech-Odyssey-file\GTOCore`
- 正式 GTOCore JAR：`E:\program\java\GregTech-Odyssey\Required-development-files--Global\GregTech-Odyssey-file\gtocore-forge-1.20.1-0.5.6-beta.jar`
- 正式 GTOlib JAR：`E:\program\java\GregTech-Odyssey\Required-development-files--Global\GregTech-Odyssey-file\gtolib-forge-1.20.1-26.7.4.jar`
- GTCEu / GTM：`E:\program\java\GregTech-Odyssey\Required-development-files--Global\GregTech-Odyssey-file\GregTech-Modern`
- GTO AE2：`E:\program\java\GregTech-Odyssey\Required-development-files--Global\GregTech-Odyssey-file\Applied-Energistics-2-gto`
- GTO 社区参考：`E:\program\java\GregTech-Odyssey\Required-development-files--Global\GregTech-Odyssey-file-from-public`
- GTL：`E:\program\java\GregTech-Odyssey\Required-development-files--Global\GregTech-Leisure-file`
- 上游 ME Placement Tool：`E:\program\java\GregTech-Odyssey\Required-development-files--Global\ME-Placement-Tool`

### 3. 工作流边界

1. 开始前读取总根目录和目标项目的 `AGENT.md` / `AGENTS.md`，再读取活动工程 `docs` 中的相关文档。
2. 在全局只读资源和项目开发输入中查找资料，只在活动源码中开发。
3. 默认执行非清洁构建、部署和命令行客户端测试；用户要求清洁构建时按指定版本和目录产出。
4. 只有用户明确要求时，才把清洁源码复制到 Git 发布镜像并把 JAR 放入 `Release`。
5. 只有用户明确要求上传 Release 时，才压缩版本目录并操作 GitHub。

固定客户端目录：`F:\Minecraft\PCL启动器\.minecraft\versions\GregTech.Odyssey-0.5.6-beta`。

2026-08-01 已完成 E 盘旧 fix47、旧发布目录和重复开发资料清理。原 C 盘 Codex 工作区中的已归档副本暂时保留，等待针对该工作区的单独删除授权；它们不是活动源码。

## English

Daily development, incremental builds, deployment and client verification use the sole active tree at `GTOHJS-Development-Project/GTOHJS`. Global GTO, GTL, GTCEu, AE2 and upstream sources under `Required-development-files--Global` are read-only. Project-specific logs, backups and analysis artifacts belong in the project `Agent cache/Chatgpt`; reusable cross-project analysis belongs in `Agent cache--Global/Chatgpt`.

`Git/GTOHJS-git/GTOHJS` is the clean publication source mirror and `Git/GTOHJS-git/Release` is the release-artifact root. Neither location is updated, initialized, committed, pushed or uploaded unless the user explicitly requests that publication step.

The verified E-drive legacy copies were removed on 2026-08-01. Archived duplicates in the former C-drive Codex workspace remain pending separate deletion approval and are not active source trees.
