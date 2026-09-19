# Super Pattern Proxy Output and Universal Steam Buses

## Late-bound proxy output

`gtocore:me_super_pattern_buffer_proxy` always contributes its GTOHJS output handler when a multiblock collects recipe handlers. The handler resolves the proxy's current target for every output simulation and execution. It also delegates the target's item and fluid infinite-output markers while the handler unit is built.

This ordering matters because a player can bind the proxy after the multiblock controller has already collected its parts. The old registration-time target check omitted the output handler in that case, leaving a correctly bound proxy with no output path and causing ordinary recipes to report blocked output.

When the current target is `gtocore:me_super_pattern_buffer`, item and fluid outputs use that buffer's shared persistent ME output storage. When the proxy is unbound or targets an ordinary GTO pattern buffer, the handler rejects output. Nothing is treated as infinite merely because an earlier binding existed.

GTCEu caches each handler unit's infinite-output markers during construction and uses them for output capacity and parallel checks. A bound Super Pattern Buffer's own handler advertises both markers. The proxy must advertise the same markers or ordinary recipes, such as a Universal Steam Factory lathe recipe, are rejected as output-blocked before the proxy receives the simulated output. GTOCore requests a controller recheck after a successful proxy bind, rebuilding the unit with the current target markers.

## Universal Steam Factory item buses

The Universal Steam Factory already uses the broad ordinary item import and export abilities in its structure predicate. In the active GTOCore ABI, these abilities expand across every registered ordinary item-bus tier. The factory therefore accepts ULV through the highest registered item input and output bus tiers, including MV and above.

The existing structure limits remain one ordinary item input bus and four ordinary item output buses. Steam-specific item buses, steam fluid hatches, the steam source, and the vent retain their existing independent limits.

## Verification

The source change requires a Java 21, network-enabled `./gradlew clean build` and deployment of the resulting JAR to the fixed GTO client. In-world acceptance should verify all three states:

1. Bind a Super Pattern Buffer Proxy after its multiblock is formed, then run a non-buffer pattern whose products reach the Super Pattern Buffer's connected AE network.
2. Disconnect the proxy and confirm that the same pattern reports blocked output.
3. Form the Universal Steam Factory with MV-or-higher ordinary item input and output buses, then confirm its existing one-input/four-output limits still apply.

### 2026-09-11 Build and deployment

The first Java 21, network-enabled clean Gradle build completed successfully. The produced `gtohjs-5.0per1-for-gtocore-0.5.6-beta.jar` was deployed to the fixed client after the prior deployed JAR was backed up under the project cache. Client acceptance then established that a bound proxy can service pattern jobs but ordinary lathe recipes are still output-blocked. The infinite-output-marker delegation above addresses that failed acceptance case.

The replacement Java 21, network-enabled clean Gradle build completed successfully on 2026-09-11 with all nine actionable tasks executed. Its `669955`-byte JAR replaced the client artifact after a second deployment backup. Client startup is user-owned: Codex must not launch or restart the Minecraft client. In-world acceptance therefore begins only after the user launches the fixed PCL version and tests the three states above.

### Client-start boundary

The fixed client directory contains the deployed replacement JAR. A Codex-side launch attempt is not part of verification and must not be repeated. Before user launch, no client log can be treated as evidence for this replacement artifact; after launch, inspect the user's resulting `logs/latest.log` for GTOHJS loading and mixin errors before claiming startup validation.
