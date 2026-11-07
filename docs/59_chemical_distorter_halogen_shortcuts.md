# Deep Chemical Distorter: bromine and iodine shortcuts

Date: 2026-11-06 (Asia/Shanghai)
Category: GTOHJS-related
Status: Implemented; Java 21 online clean build passed on retry; client checks pending.

## Approved design

Two independent HV recipes replace the external salt-water mixing, brine
concentration/heating and extraction steps. They use existing materials and
the existing `gtceu:chemical_distort` recipe type. No prior bromine/iodine
production, catalyst, research or additional coil requirement is introduced.
Both recipes use 480 EU/t and 600 ticks (30 seconds) before native overclocking
and parallelism. The existing voltage-dependent parallel rules remain intact.

All fluid amounts below are mB. Dust amounts are item counts.

| Raw recipe ID (gtohjs namespace) | Inputs | Outputs |
| --- | --- | --- |
| `brine_shortcut/bromine` | Salt dust 50; Water 25000; Chlorine 3000; Steam 6000 | Bromine 4000; Hot Alkaline Debrominated Brine 2000; Acidic Bromine Exhaust 4000 |
| `brine_shortcut/iodine` | Salt dust 200; Water 100000; Hydrochloric Acid 2000; Oxygen 1000 | Iodine dust 1; Water 1000; Hot Alkaline Debrominated Brine 4000 |

Final IDs are computed by the existing `RecipeBuilder.getTypeID` path, rather
than assuming a final prefix. Keep the recipe IDs stable after introduction.

## Cost derivation and optional recycling

The baseline salt-water mixing recipe consumes two salt dust and 1000 mB water
per 1000 mB salt water. Concentrating 20000 mB salt water produces 1000 mB raw
brine. Thus each 1000 mB raw brine represents 40 salt dust and 20000 mB water.
The approved shortcuts add 25 percent to these salt/water quantities without
increasing product or tail-fluid yields. This is a convenience loss, not an
extra recoverable material output.

For bromine, one brine chlorination followed by two filtration batches and four
condensation/concentration/distillation batches gives 4000 mB bromine, 2000 mB
alkaline tail fluid and 4000 mB acidic exhaust. Chlorine input is 5000 mB with
2000 mB returned by distillation: internal reuse reduces external input to
3000 mB. Steam input totals 6000 mB. Condensation water is returned unchanged
and handled internally. Salt/water increase from 40/20000 to 50/25000.

For iodine, two acidification batches and one oxidation batch consume 4000 mB
hot brine, 2000 mB hydrochloric acid and 1000 mB oxygen. They yield one iodine
dust, 1000 mB water and 4000 mB alkaline tail fluid. Salt/water increase from
160/80000 to 200/100000.

Both shortcuts retain native recoverable alkaline tail fluid. The existing
potassium neutralization, raw-brine mixing and centrifugation recipes remain
available. Acidic bromine exhaust also remains available to the original
recovery route; that route produces a bromine-chain intermediate and therefore
still requires part of the original extraction equipment. Recovery is optional
and does not automatically feed the new shortcuts. Native recipes are unchanged.

## Implementation and input capacity

`ChemicalDistortRecipes` remains the single owner. Its native MODULE=3 source
guard stays pinned to 21; two independent halogen specs bring the dedicated
type total to 23. Both use the existing late method-mode catalog and literal
ASM builder/save loop. No new registration mechanism or EMI patch is added.
Acceptance checks cover exact deterministic IO, type, final ID, EU/t, duration
and absence of extra conditions/data/extensions. The catalog checks retention
in both final recipe tables. Original polymer transformation checks remain.

The recipe type permits nine entries in each IO direction. Both shortcuts fit.
Iodine needs 100000 mB water and 200 salt dust, exceeding one ordinary HV
single-fluid hatch (64000 mB) and one ordinary item stack. Supply water using
multiple normal hatches in the same shared input group, and salt across slots.
Distinct or differently grouped inputs must not split the recipe bundle.
The reference controller combines ordinary nondistinct handlers; the pinned
GTCEu JAR's RecipeHandlerUnit bytecode passes the same remaining-fluid list
through its fluid handlers. Native tank handling reduces the remainder.
No tank, machine, IO or scheduler implementation is changed by this task.
Physical multi-hatch matching and consumption still require client acceptance.

## Verification and scope

- Checked the current local 0.5.6 BrineRecipes quantities and material fields.
- Reused the existing acquisition provider's exact-content validation pattern.
- Preserved pre-existing unrelated mana-converter worktree changes and version
  `5.7-spe1-for-gtocore-0.5.6-beta`; they are outside this recipe task.
- Java 21.0.5 online IDEA MCP `./gradlew clean build`: attempted. The default
  user-level wrapper distribution waited on an exclusive file lock before any
  Gradle task output. Only this task's stalled startup processes were stopped.
  Reusing the existing project-local Gradle home reached Gradle 8.8, then failed
  in 12 seconds while applying ForgeGradle 6.0.54 at `build.gradle:5`:
  `Failed to validate certificate for host 'https://maven.minecraftforge.net/'`.
  No compile/check task or successful artifact is established by this attempt.
  Per workspace instructions, stop on network/dependency failure; certificate
  checks were not disabled and offline mode was not used.
- `git diff --check` passed for the worktree. This is whitespace validation,
  not compilation, recipe registration or gameplay acceptance.
- Runtime final-table validation and actual HV production/recycling: pending.
- No client deployment/launch or Git publication is claimed by this record.

### Successful retry (2026-11-06, Asia/Shanghai)

The user requested another build attempt. Java 21.0.5 online IDEA MCP
`./gradlew clean build` passed in 19 seconds: 15 actionable tasks, 10 executed
and five restored from Gradle's build cache. Certificate validation remained
enabled; no offline flag or dependency substitution was used. The same existing
project-local Gradle home was reused. The earlier certificate blocker is resolved
for this attempt; its record above is historical.

The existing `verifyManaConversion` passed 72013 assertions and
`verifyOptimizationContracts` passed its fairness/budget, routing, cache bounds,
cancellation/epochs/retry/publication checks. These are existing project checks,
not halogen recipe runtime acceptance.

Artifact: `build/libs/gtohjs-5.7-spe1-for-gtocore-0.5.6-beta.jar`, 982776 bytes.
The JAR contains `ChemicalDistortRecipes` and its `HalogenSpec` class;
`git diff --check` passed. The artifact also includes the already-present
mana-converter worktree changes. No deployment or client launch was performed
for this build-only retry. Both final recipe definitions and actual HV
multi-hatch consumption, outputs and recycling remain client acceptance work.

### Isolated GitHub publication (2026-11-06)

At the user's explicit request, prepared an independent branch directly from
remote main `3d1502d60dc17c540039a25df0f682c4a195cc50`. It contains only
`ChemicalDistortRecipes.java`, document 47's cross-reference, document 59's
public design/verification record and the corresponding documentation-index
entry. The three local-only changelog commits and unrelated mana-converter
work were excluded. Main and the original development worktree were preserved.

The isolated branch passed its own Java 21 online IDEA MCP
`./gradlew clean build` in 14 seconds (13 tasks: nine executed, four from cache).
The existing optimization contract check passed. Published commit:
`7c5e9b39b1f866d2996698971ab2018c02cc1815` on
`codex/chemical-distorter-halogen-shortcuts` in `futureism-hjs/GTOHJS`.
GitHub confirmed both author and committer as `futureism-hjs`, including the
account avatar. Author/committer email is the account's ID-based noreply
address. The user-supplied token was used only in process-local authentication;
no credential was written into a commit, remote URL or Git configuration.
No merge, release publication, client deployment or launch was performed.

### Main integration verified (2026-11-07)

The user subsequently requested merging the isolated change into main.
The remote main was still the branch's direct parent, so a normal fast-forward
push integrated the same tested commit without rewriting its author/committer.
On continuation, `git ls-remote` confirmed both remote main and the retained
feature branch point to `7c5e9b39b1f866d2996698971ab2018c02cc1815`.
No unrelated local commits or working-tree changes were included. The original
local main/worktree was not reset or merged; no client or release operation ran.
