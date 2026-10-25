# Item attribution coverage and Infinite Wireless Energy Unit name

Date: 2026-10-25 (Asia/Shanghai)
Category: GTOHJS-related
Status: audited, clean-built and deployed; current client preserved, visual check after user restart.

## Audit and change

The current 64 HJS-added item registrations comprise 26 gtohjs-namespace items
(including block items), 37 gtocore-namespace machine items and one gtocore
data stick. Existing namespace handling, the eight GTL machine identities and
five adaptive machine identities already cover the other 62 items.

The two omitted identities are now added to the existing Kotlin tooltip owner:

| Item ID | Display name |
| --- | --- |
| gtocore:me_super_wildcard_pattern_buffer | ME Super Wildcard Pattern Buffer |
| gtocore:net_data_stick | Network Configuration Data Stick |

GTOHJSItemTooltipHandler remains the only general attribution callback. Its
existing translated/colored Added by GTO HJS line and registration are reused.
The local set is renamed to GTOCORE_ITEM_PATHS because it includes a non-machine
item. No second event handler or new translation mechanism is introduced.
Native fusion machines decorated by HJS remain native content, not new HJS items.

## Naming

The exact Chinese display name is 无限无线能量单元; English is Infinite Wireless
Energy Unit. Both the static block-name key and shared animated-name format
are corrected. The shared InfiniteEnergyPresentation.unitName owner continues
to provide item, placed-block and tower-count names, preserving the animated
infinity prefix. The registry ID stays gtohjs:infinite_wireless_energy_storage_unit.
The matching quest/article titles use the same name; quest IDs and progress stay.

## Verification and client ownership

This is a targeted tooltip/text correction. Verify the complete source inventory,
JSON validity, dynamic/static name agreement and synchronized chapter contents;
reuse the required Java 21 network-enabled IDEA clean build and package checks.
No new unit framework or implementation-mirroring tests are needed.

The user is testing the existing running client and forbids closing it. Preserve
that process. New compiled tooltip code takes effect on the user's later restart;
do not claim that the current JVM adopted the replacement JAR. Native resource
index refresh, if needed, must wait until all matching client JVMs have exited.

## Verification and deployment evidence

All 64 current item identities are covered after the two additions. Both locale
JSON files parse and their static/dynamic names agree exactly. The chapter
changed only its two matching title strings; source/client bytes match and the
prior definition is backed up. Java 21.0.5 / IDEA MCP / network-enabled exact
clean build passed in 23 seconds, all 13 tasks executed, including the existing
production contract checks. Package checks confirm both attribution paths and
the corrected name keys, with no old recipe-package classes.

Artifact: 962816 bytes. The prior 962765-byte addon and matching HJS/aggregate
resource indexes are preserved under project-parent cache
client-deployment-backup/20261025-144404-attribution-unit-name. Installed bytes
equal the build; no Core/Seal changes. PID 20004 remains in the user's singleplayer
world. The existing refresh-after-client-exit helper is reused; resource indexes
are untouched while any matching client is running. New code/names are pending
the user's later restart; current-process visual acceptance is not claimed.
