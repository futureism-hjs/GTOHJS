# Local Build Dependencies

> [!WARNING]
> This project contains AI-generated or AI-assisted code and documentation. Dependency names and compatibility constraints must be independently reviewed before building or redistributing the project.

Third-party mod JARs are intentionally excluded from this repository. Obtain them from a legal GTO 0.5.6-beta development installation and place them in this directory. Use the six filenames below and the exact additional API filename declared in `build.gradle`:

- `appliedenergistics2-forge-1.20.1-15.267.4.jar`
- `RecipeSearch-1.3.jar`
- `gtmthings-forge-1.20.1-26.7.1.jar`
- `ldlib-forge-1.20.1-1.0.50.jar`
- `gtceu-forge-1.20.1-26.7.3.jar`
- `gtocore-forge-1.20.1-0.5.6-beta.jar`
- The additional GTO runtime API JAR declared in `build.gradle`.

The build uses these seven files as local compile-only or deobfuscation dependencies. GTOHJS does not directly import GTMThings classes, but GTOCore's public machine ABI inherits `IBindable`, so javac must be able to resolve the matching GTMThings API. The runtime copy is supplied by the GTO installation. Do not substitute nearby versions without auditing GTOCore API and Coremod bytecode compatibility.

All third-party JARs in this directory are local-only and must remain untracked. In particular, `gtceu-1.20.1-1.8.0.jar` is not required by the current build and should not be added.
