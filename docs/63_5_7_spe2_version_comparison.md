# GTOHJS5.7-spe2 comparison and deployment

Current version: `5.7-spe2-for-gtocore-0.5.6-beta`.
Release-note baseline: the recorded spe1 source commit
`d959f5fec8e6f1f5c56362d8a3433c7b5fb52d3a`, whose gradle.properties
identifies `5.7-spe1-for-gtocore-0.5.6-beta`.
The earlier rolling local-JAR comparison is retained separately below.

## Comparison policy

Compare decompressed JAR entry payloads directly, not ZIP timestamps or
container ordering. No hash audit is required. Classify added, removed and
changed entries; separately identify version metadata versus behavior/assets.
Runtime acceptance is separate from byte comparisons and the Gradle build.

The spe1 source baseline contains the Mana Converter and its provenance
correction, plus existing chemical halogen/polymer shortcuts. It does not
contain the new ore/apothecary controllers or the eleven mineral/biochemical
recipes. Use the source delta for release categories; do not infer feature
ownership from a rolling local JAR whose filename was reused during development.

## Spe1-to-spe2 release changes

| Feature | Spe1 source baseline | Spe2 |
| --- | --- | --- |
| Mineral shortcuts | Existing halogen/polymer routes | Six additional mineral routes |
| Exclusive biochemical batches | Existing derived recipe families | Five native-derived batches with T2 filtering |
| Biochemical item inputs | Six types | Nine types |
| Large Steam Petal Apothecary | Absent | Controller, workbench recipe and14x14 idle/active artwork |
| Basic steam ore plant | Absent | Source-shaped controller, native ore recipes,2000 ceiling and steam-aware startup |
| Structure-source labels | Per-character fixed/moving color behavior | Stationary whole-line cycling color |
| Copy-ready commit line | No explicit requirement | Required after code-changing tasks |
| Mana Converter and attribution fix | Already recorded | Retained, not repeated as additions |

The bilingual spe2 changelog follows these Added/Changed groups. The spe1
section is restored to the recorded source version's text. The source review
does not establish post-restart gameplay acceptance.

## Earlier rolling local-JAR comparison (not the release-note baseline)

This compares the previously deployed development JAR still named spe1
(1018129 bytes,448 entries) against the spe2 rebuild. The development JAR
already contained the new features above.

| Measure | Previous5.7-spe1 | Current5.7-spe2 |
| --- | ---: | ---: |
| JAR bytes | 1,018,129 | 1,018,131 |
| Entry names | 448 | 448 |
| Added/removed entries | Baseline | 0/0 |
| Identical entry payloads | Baseline | 446 |
| Identical class payloads | Baseline | 285 (all classes) |

Only two entries changed:

- `META-INF/MANIFEST.MF`: Implementation-Version changed from spe1 to spe2;
  Implementation-Timestamp changed to the current build time.
- `META-INF/mods.toml`: mod version changed from spe1 to spe2.

There are no class, machine, recipe, structure, texture or language changes
relative to the actual previously deployed artifact. The two-byte ZIP size
difference is packaging/metadata, not a gameplay change.
This statement applies only to those two rolling build artifacts. It does
not mean that the spe2 source has no additions over the spe1 source version.

## Verification and deployment

Java21.0.5 online IDEA MCP `./gradlew clean build` passed on2026-11-07 in22s,
with16 tasks (13 executed,3 from normal build cache). Mana72013 assertions,
steam73813 checks and existing optimization contracts passed.
Version consistency was checked in gradle.properties, the newest changelog
heading, output filename, packaged mod metadata and manifest.

The1018131-byte `gtohjs-5.7-spe2-for-gtocore-0.5.6-beta.jar` was deployed
to the fixed0.5.6-beta client. The previous JAR and exact comparison baseline
are backed up outside the client mods folder. One active addon remains and
its bytes equal the immediately staged clean-build artifact.

The client was stopped. Relevant generated indexes were already absent after
the prior safe-exit refresh completed; they will regenerate normally.
No client launch, gameplay validation or Git publication was performed.
