# GTOHJS Development Resource Reindex

**Indexed:** 2026-08-01  
**Active project:** `[your workspace directory]\GTOHJS-Development-Project\GTOHJS`  
**Publication mirror:** `[your workspace directory]\Git\GTOHJS-git\GTOHJS`

## 1. Directory Responsibilities

| Path | Responsibility |
| --- | --- |
| `[your workspace directory]\GTOHJS-Development-Project\GTOHJS` | Sole active per3 source for daily development, incremental builds, and client verification |
| `[your workspace directory]\GTOHJS-Development-Project\Required-development-files\Developers-file` | User-supplied machines, recipes, and other development inputs; original files are read-only |
| `[your workspace directory]\GTOHJS-Development-Project\Required-development-files\gtohjs-things` | Internal GTOHJS reference documentation and reusable resources; not a build root |
| `[your workspace directory]\GTOHJS-Development-Project\Agent cache\Chatgpt` | GTOHJS-specific backups, logs, decompilation, deployment snapshots, and experimental cache |
| `[your workspace directory]\ME-Placement-Tool-for-gto-Development-Project\ME Placement Tool for gto` | Active source for the independent ME Placement Tool for gto mod |
| `[your workspace directory]\ME-Placement-Tool-for-gto-Development-Project\Agent cache\Chatgpt` | Logs, backups, and experimental cache specific to that independent mod |
| `[your workspace directory]\Required-development-files--Global` | Global read-only references for GTO, GTL, GTCEu, AE2, community projects, and upstream ME Placement Tool |
| `[your workspace directory]\Agent cache--Global\Chatgpt` | Reusable cross-project decompilation, ABI, source-index, and tool cache |
| `[your workspace directory]\Git\GTOHJS-git\GTOHJS` | Clean publication-source mirror, updated only when the user explicitly requests copying to Git |
| `[your workspace directory]\Git\GTOHJS-git\Release` | Formal JARs, version directories, and release archives requested by the user |

## 2. Global Read-Only References

- GTOCore: `[your workspace directory]\Required-development-files--Global\GregTech-Odyssey-file\GTOCore`
- Formal GTOCore JAR: `[your workspace directory]\Required-development-files--Global\GregTech-Odyssey-file\gtocore-forge-1.20.1-0.5.6-beta.jar`
- Formal GTOLib JAR: `[your workspace directory]\Required-development-files--Global\GregTech-Odyssey-file\gtolib-forge-1.20.1-26.7.4.jar`
- GTCEu / GTM: `[your workspace directory]\Required-development-files--Global\GregTech-Odyssey-file\GregTech-Modern`
- GTO AE2: `[your workspace directory]\Required-development-files--Global\GregTech-Odyssey-file\Applied-Energistics-2-gto`
- GTO community references: `[your workspace directory]\Required-development-files--Global\GregTech-Odyssey-file-from-public`
- GTL: `[your workspace directory]\Required-development-files--Global\GregTech-Leisure-file`
- Upstream ME Placement Tool: `[your workspace directory]\Required-development-files--Global\ME-Placement-Tool`

## 3. Workflow Boundaries

1. Before starting, read the root and target project's `AGENT.md` / `AGENTS.md`, then read the relevant documents under the active project's `docs` directory.
2. Search global read-only resources and project development inputs for evidence, but develop only in the active source tree.
3. By default, perform an incremental build, deployment, and command-line client test. When the user requests a clean build, produce it at the requested version and location.
4. Copy clean source to the Git publication mirror and put the JAR in `Release` only when explicitly requested by the user.
5. Compress the version directory and operate GitHub only when the user explicitly requests uploading a Release.

Fixed client directory: `[GTO client directory]`.

On 2026-08-01, verification and cleanup removed the old E-drive fix47 tree, old publication directory, and duplicate development materials. Archived copies in the former C-drive Codex workspace remain pending separate deletion authorization for that workspace; they are not active source.
