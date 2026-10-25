# Build Dependencies

> [!WARNING]
> This project contains AI-generated or AI-assisted code and documentation. Dependency names and compatibility constraints must be independently reviewed before building or redistributing the project.

The following eight pinned JARs are tracked in this repository so GitHub Actions and contributors can run the same build without local file setup. They are compile/deobfuscation dependencies and are not copied into the GTOHJS release ZIP; the target modpack supplies runtime dependencies. Keep these exact filenames aligned with `build.gradle`:

- `appliedenergistics2-forge-1.20.1-15.267.4.jar`
- `RecipeSearch-1.3.jar`
- `gtmthings-forge-1.20.1-26.7.1.jar`
- `configuration-forge-1.20.1-3.1.0.jar`
- `ldlib-forge-1.20.1-1.0.50.jar`
- `gtceu-forge-1.20.1-26.7.3.jar`
- `gtocore-forge-1.20.1-0.5.6-beta.jar`
- `gtolib-forge-1.20.1-26.7.4.jar`

The build uses these eight files as local compile-only or deobfuscation dependencies. GTOHJS does not directly import GTMThings classes, but GTOCore's public machine ABI inherits `IBindable`, so javac must be able to resolve the matching GTMThings API. Configuration 3.1.0 supplies the same annotation-driven in-game config UI used by GTOCore. Runtime copies are supplied by the GTO installation. Do not substitute nearby versions without auditing GTOCore API and Coremod bytecode compatibility.

The project license file describes GTOHJS source and original assets. Each bundled dependency remains subject to its upstream license and terms. In particular, `gtceu-1.20.1-1.8.0.jar` is not required by the current build and must not be added.
