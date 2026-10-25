# HJS quest prose and horizontal branch revision

Source-location update (2026-10-25): the user designated project `chapters/`
as the canonical clean template, retaining all current customizations. Future
edits must happen there first and synchronize to the client afterward. Read
`AGENT/QUEST_WORKFLOW.md`. Initial 43-file direct-byte comparison found source
and client already identical; no chapter overwrite was necessary. The template
contains definitions only and does not reset or carry current-world progress.

Chapter-wide rule (2026-10-25, task 06): apply the user's examples to every
entry. Ordinary equipment, native-function combinations and obvious usage need
no prose or redundant subtitle. Only unique new features/non-obvious mechanics
justify short text. Reviewed all 75 entries and updated 48; consolidated port
instructions in the wireless shared entry. Preserve feature-only super buffers
and the three short marker subtitles. Restored the collector's full item title
after the earlier voltage-title edit accidentally shortened it to ULV.

Additional user correction: Advanced Generator Array is also title-only.

Follow-up (2026-10-25, task 05): stage categories refer to earliest ingredient
and manufacturing availability, not the finished machine's nominal voltage.
Ultimate Infinite Intake is now placed under EV per the user's correction;
its nominal machine tier and recipe are unchanged. Eight ordinary machines,
including the rare-earth plant, now have neither body nor subtitle. Keep only
unique/non-obvious mechanics in special-machine prose. Total remains 75.

Follow-up (2026-10-25, task 04): continue from the user's manually edited live
chapter. Remove all explanatory text/subtitles from the two basic input
assemblies. Limit three super-buffer entries to unique features: maximum
capacity (1800 ordinary, 64 wildcard), item/fluid output and deletion of excess
pattern-slot data when shrinking. Network-full output is cached, not asserted
to be automatically destroyed. Five text-only edits; user layout/state retained.

Follow-up (2026-10-25, task 03): shortened 16 special-machine descriptions.
The shared processing entry uses the user's three concise selection rules:
specific buffer configuration takes precedence; combined mode reads each
pattern's machine configuration; ordinary inputs follow the machine setting.
This wording was provided by the user, not a newly demonstrated runtime test.
Only description fields changed; 75 tasks, layout and task/reward state remain.

Date: 2026-10-25 (Asia/Shanghai)
Category: GTOHJS-related

## User-directed changes

This revision supersedes the prose and layout choices in document 53. The user
requested loose paragraphs in the original GTO narrative style, omission of
existing tooltip information, no troubleshooting, no machine structure writeups,
and only surprising exceptions rather than ordinary hatch/count limitations.
Simple items such as markers should use subtitles without body text.

The user also requested a root with horizontal category rows:
voltage progression, AE, magic, wireless grid, exploration/adventure.
A separate row retains the existing building/development tools.

## Result

- Retained the chapter identity and 75 tasks.
- Removed only the newly added “先读懂机器：供电、并行与线程” task.
- Added one reward-free voltage-category checkmark task.
- Retained the other 74 quest IDs and all their task conditions, task IDs,
  rewards, repeat settings and other non-presentation properties.
- Reorganized dependencies and coordinates as explicitly requested.
  Stage progression runs left to right. AE, magic, grid, adventure and tool
  children occupy horizontal category rows; their category dependencies do not
  require acquiring every previous item in that row.
- Narrative descriptions now use blank SNBT strings between paragraphs.
  There are 52 entries with prose and 103 blank paragraph separators.
- Three markers use only short subtitles. Stages, supplies, component packs
  and other simple entries also avoid unnecessary prose.
- Retained non-obvious behavior such as full-batch neutron power, low-voltage
  Distorter behavior, biochemical input visibility and maintenance filtering,
  destructive thermal form conversion, finite “infinite” storage, proxy output
  binding, shrinking-buffer data deletion and team-ledger sharing.

No machine logic, tooltip source, item recipes or other chapter was edited.

## Evidence and verification

Style references were the original chapter files lv.snbt,
wireless-energy.snbt and ore-processing.snbt, read without changes.
Tooltip duplication was checked against the existing language and tooltip owners,
with implementation facts reused from document 53 and its read ledger.

Static verification passed on the written live chapter:
newline-normalized SNBT parsing; 75 total quests; exactly one specified deletion
and one category addition; preservation of retained task conditions/rewards/
repeat settings; global hexadecimal-ID uniqueness; valid dependencies; no local
cycles or exact coordinate overlap; blank separators in every multi-paragraph
article; no troubleshooting or old fixed article headings; subtitle-only markers.

The full pre-revision chapter is retained in the parent project cache:
Agent cache/Chatgpt/quests-20261025/gto_hjs.before-layout-revision.snbt.
The first original 44-task backup is also retained separately.

No build, deployment, game launch/restart, remote operation or visual/gameplay
acceptance was performed. Exact in-game line wrapping and overview appearance
remain unverified by this static-only delivery.
