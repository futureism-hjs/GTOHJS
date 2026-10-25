# Recipe package migration and performance work

Date: 2026-10-25 (Asia/Shanghai)
Category: GTOHJS-related
Target: active old GTO 0.5.6-beta.
Status: implemented, clean-built, deployed and entered a new world; user testing.

## Recipe-source contract

The 23 recipe files now use com.gtohjs.recipes. Class names, recipe IDs and
relative discovery order are preserved. Discovery, both editor templates and
copy-target hints move together. The game export directory remains
<version>/gtohjs/recipe. Old external drafts need their package declaration
updated before import. verifyRecipePackages rejects stale main Java/Kotlin
package declarations, avoiding silent omission by the new discovery path.

The shared api interfaces and CoreMod stages remain. Providers configure the
builder; actual builder/save calls remain emitted ASM in the verified native
window. Java providers must never call save() or add another registration path.

## Wildcard work and cache

Kotlin sealed states own pending/running/published/retry/detached lifetimes;
Java retains the existing native search adapter. No !! is used.

One server-level round-robin queue shares 2 ms / 128 visits per END tick.
Visits cover material/template pairs, blacklisted skips, task preparation and
cached-pattern restoration. The limit is cooperative: a single native search,
source decode, encoding or final publication cannot be preempted.

Configuration changes withdraw the published list and coalesce requests.
Revision/data-epoch checks cancel obsolete work and reject obsolete routes.
Only a complete pattern/route snapshot is published; AE is notified afterward.
Material/template traversal, first-match type selection, private/shared catalyst
isolation and rejection of ambiguous equivalent copies remain unchanged.

The shared LRU is limited to 32 signatures, 32768 entries and 32 MiB encoded
key/pattern payload. It stores definition bytes, source-slot numbers and resolved
type IDs, never machine/grid/action-source/inventory closures. Current-machine
wrappers are restored under the same budget. Once a job exceeds a cache bound,
it drops its encoded payload and continues producing only its local result.
The cache survives chunk unloads within a server session, never game restart.

Signatures explicitly read active pattern inventory, every private circuit/item/
fluid catalyst handler, shared handlers, both active blacklists, ordered available
types, selected type, output caps, capacity and data epoch. Native item-save NBT
is unsuitable: locked slots omit private catalysts/circuits and include unrelated
processing backlog. Output backlog, name and priority are excluded.

Execution waits for existing successful catalog/server-start validation.
Server tag/full datapack-sync events atomically advance the epoch; server tick
performs invalidation. Unload/removal/server-stop cancel work. Ordinary exceptions
clear results and retry at 20/40/80/160/200 ticks; Errors propagate.

No protected native index-completion contract is claimed or forcibly initialized,
and no index-observation Mixin is added. Empty results remain local, never shared.
A native index that completes late without another invalidation event still
requires runtime investigation, as explicitly agreed for R2.

## Review disposition

| Finding | Outcome |
| --- | --- |
| R1 | Cooperative rebuild, cancellation/coalescing, bounded session cache, per-material prefix-key reuse. |
| R2 | Existing lifecycle gates, reload invalidation, exception retry, no shared negative cache; native index timing unproven. |
| R3 | Collect other parts' configured keys once per medium/refresh; preserve all refresh triggers and actual-extractability SIMULATE calls. |
| R4 | Reorder hatch snapshot only on membership/priority changes; resolve terminal/tower once per transfer. Preserve priority/dimension/position and online/team checks. |
| R5 | Cache container references after formation, read live voltage; invalidate/unload clear references. All LuV/parallel-hatch rules remain. |
| R6 | ClassValue getter caches and SLF4J formatting; retain validation and log-file lifecycle. |
| R7 | Retain synchronized weak dose map: native hatch is final, no existing removal hook, no measured case for more injection. |
| R8 | Memoize original native conversion by recipe identity within a rebuild; preserve truncation, empty and NBT/AEKey semantics. |
| R9 | Reuse the material identity set in blacklist UI. |
| R10 | AE2 actually consumes patternPriority: retain field/UI/NBT. Share row scrolling; keep adapter mode-sync and distinct int/long limit wire formats. |
| R11 | Registration/load boundaries catch Exception; ABI/VM Errors fail at their original site. Unrelated migration/rollback handlers remain. |
| R12 | Production-primitive regression checks, package inspection and explicit evidence boundaries. |

No TPS percentage is asserted. The original report's 64-fold timing estimate is
not measured, enqueueTask is not a worker thread, and network cache quantity is
not proof of extractability.

## Verification and delivery

verifyOptimizationContracts runs from Gradle check without Minecraft bootstrap:
queue fairness/time budget, duplicate requests, cancellation/reentrant invalidation,
epochs, retry/complete publication, exact/equivalent source routes, LRU/count/payload
bounds and byte-array ownership. It tests production primitives directly.
Existing GT/crafting generated classes remain real migration regression samples.

Required build: Java 21, network enabled, IDEA MCP terminal, exactly
./gradlew clean build. Java/Kotlin bytecode target remains 17. Inspect the JAR for
new-package sources, both generated examples and state classes, and absence of
old-package/test classes. Back up/deploy only GTOHJS to the fixed F-drive client.
Codex launches the client; reaching its main menu is the user's acceptance gate.
Save entry, machine operation, in-world reload and profiling remain unperformed
unless separately evidenced below.

## 2026-10-25 verification evidence

- Java 21.0.5, IDEA MCP and network-enabled exact clean build: successful in
  29 seconds; all 13 tasks executed, including both verification tasks.
- 962730-byte artifact: all 23 primary recipe classes present under recipes,
  20 wildcard state/helper classes, Java 17 major 61; no old recipe package or
  contract-test classes packaged.
- Prior 922692-byte addon backed up at project-parent cache
  client-deployment-backup/20261025-141930-recipe-performance. Installed bytes
  equal the build and one active addon remains. Core/Seal were not modified.
- Codex launched the fixed client with Java 21.0.5; main-menu evidence pending.

The first startup exposed a pre-existing load-complete ordering assumption:
Hyperdimensional Biochemical Factory required the native preview cache before
native Data's background multiblock preinitialization had run. Its real pattern
factory had already built successfully. Reused the established other-machine
validation contract: keep actual factory/native-definition/renderer checks;
permit a lazy null preview cache and reject an existing empty cache. No global
native initialization or EMI path was added. The first failure/build logs and
artifact remain retained. The final Java 21 online IDEA clean build passed in
17 seconds, 13 tasks (12 executed, one test-compilation cache hit); contract checks
ran and passed. Final artifact: 962765 bytes, deployed byte-identically; the
original pre-task addon backup remains intact. Main-menu acceptance pending.

The second launch reached TitleScreen and validated 416 method-mode GT recipes,
then the user created a world and hit a separate Windows NIO initialization
failure (failed to create child event loop -> unable to establish loopback ->
AF_UNIX Invalid argument: connect). A standalone Java 21 Selector/Pipe probe
reproduced the failure in the direct runner and passed through IDEA MCP.
This is the previously documented launch-context issue, not a recipe/machine
exception. The helper now refuses launch unless that preflight succeeds, and
the next client was launched through IDEA MCP with the same installed artifact.
The existing newly created world is retained. IDEA-launched PID 20004 passed
preflight and logged the player in at 14:29:42 after server-side recipe checks.
The client remains open for the user's tests. This verifies world entry, not
individual machine behavior or measured performance.

