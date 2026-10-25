# Ultra-high-amperage wireless terminal templates

Date: 2026-10-25 (Asia/Shanghai)
Category: GTOHJS-related

## Cause and implementation

Native 0.5.6 wireless hatches use energy abilities at 2/4/16/64A, and laser
abilities at 256A through 16777216A. The terminal enumerates each family's
registered ability blocks before parsing their IDs. Its previous laser parser
only recognized native `<tier>_<amps>a_laser_source_hatch` / target names,
so wireless 256A+ items were missing from every template slot's whitelist.

AdaptiveTemplateRegistry now applies its existing wireless ID parser to both
matching energy and laser families. The native laser ID fallback remains.
Ability registration continues to determine the family; no additional registry,
slot, energy path, machine ID or inventory persistence format is introduced.
Strict numeric suffix validation and the creative-item exclusion remain.

| Native templates | Terminal slot, left to right |
| --- | --- |
| Wireless output, 2/4/16/64A | 1: Power output |
| Wireless input, 2/4/16/64A | 2: Energy input |
| Wireless output, 256A through 16777216A; native laser source | 3: Laser source |
| Wireless input, 256A through 16777216A; native laser target | 4: Laser target |

The reported `gtocore:max_wireless_output_hatch_16777216a` belongs in slot 3.
The MAX tier and amperage are parsed without changing existing power/capacity
limits. Runtime whitelist export remains `/gtohjs dumptemplates`.

## Verification and deployment

Required: Java 21, network-enabled IDEA MCP `./gradlew clean build`, then
backup and addon-only deployment to the fixed F-drive 0.5.6 client. Preserve
the running client; the replacement code takes effect after the user's later
restart. Source/build/package evidence does not establish in-game insertion.
Workspace progress: `Documentation/progress_2026-10-25_15.md`.

Completed: Java 21.0.5 online IDEA clean build passed in 53 seconds, with
existing recipe-package and optimization-contract checks passing. Of 13
actionable tasks, 12 executed and one test compilation came from cache.
Packaged bytecode was inspected. Compared with the previous deployed addon,
only AdaptiveTemplateRegistry.class and the manifest differ; resource entries
are identical. Resource indexes were therefore left untouched.

The 962837-byte addon was deployed alone to the fixed client's mods folder.
Its bytes equal the build. The prior 962816-byte addon is preserved under
project-parent cache `client-deployment-backup/20261025-ultra-high-templates/`;
build log is `ultra-high-templates-20261025/build.log`. One active addon remains.
Client PID 33192 was preserved. No restart, Core/Seal modification, Git action
or in-game template insertion test was performed. Restart/insertion remains
user-owned; current-process behavior is unchanged.
