# GTO HJS

> [!WARNING]
> This project contains code, documentation, textures and quest content generated or produced with AI assistance. They may contain errors, security issues, or incompatibilities with upstream APIs and licenses. Review and test them before use, modification, or redistribution; no accuracy, completeness, or fitness is guaranteed.

[中文](README_ZH.md) | [Changelog](CHANGELOG.md)

GTO HJS is a compatibility extension for the Minecraft 1.20.1 Forge build of GregTech Odyssey 0.5.6-beta. It adds machines, multiblock parts, recipe types and recipes inside GTOCore and GTOLib's native registration windows without modifying GTOCore, GTOLib or EMI files.

The current pre-release is `1.0-pre1-for-gtocore-0.5.6-beta`. Pre1 packages the feature set validated during the fix67 development cycle as a standalone GitHub source repository; the version cleanup does not change machine, recipe or runtime behavior.

## Runtime and development dependencies

| Component | Version or range |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 47.4.20; manifest range `[47.4.20,48)`, mod-loader range `[47,)` |
| Java | JDK 21 by default; Java 17 bytecode target |
| GTCEu | 26.7.3; manifest range `[26.7.3,26.8)` |
| GTOCore | 0.5.6-beta; manifest range `[0.5.6-beta,0.5.7)` |
| AE2 | The target pack uses 15.267.4; the ME assemblies and their recipes require AE2 content |

The additional local compile APIs are RecipeSearch 1.3, AE2 15.267.4 and GTMThings 26.7.1. GTMThings is an indirect compile-time ABI dependency because a GTOCore machine superclass inherits its `IBindable` interface; the target modpack supplies the runtime copy. Features such as the Large Petal Apothecary rely on the pack's existing Botania, AppBot and GTO integration mods. Do not use this table to replace the dependency set locked by the modpack.

Third-party mod JARs are not redistributed in this repository. Before the first build, place legally obtained dependencies in `libs` exactly as described in [libs/README.md](libs/README.md).

## Build

Use Java 21 and an online Gradle build by default:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
.\gradlew.bat clean build --stacktrace
```

When the Coremod changes, also check its JavaScript syntax:

```powershell
node --check src\main\resources\coremods\gtohjs_machine_registration.js
```

The release artifact is written to:

```text
build\libs\gtohjs-1.0-pre1-for-gtocore-0.5.6-beta.jar
```

Stop the build and wait for manual dependency handling if a network download fails. Do not package against an unknown partial dependency state.

## Install

After closing the client, place the release JAR in the `mods` directory of a Minecraft 1.20.1 Forge GTO 0.5.6-beta instance. Remove older GTOHJS JARs so exactly one version is loaded, and verify the dependency versions above before startup.

## Feature overview

The current source includes:

- Universal Steam Factory with 15 modes, MV-and-below recipe acceptance and a final 1t duration lock.
- Hyperdimensional Forge, Steam Furnace, Smelter and Chemical Factory.
- One-stop Rare Earth Processing Plant with its own recipe type.
- Advanced Generator Array, Advanced Alchemy Cauldron and Large Petal Apothecary.
- A Botania petal-apothecary proxy for the Large Petal Apothecary, with no mana output from proxy recipes.
- ME Input Assembly and ME Stocking Input Assembly.
- Native GTO recipes, shaped crafting recipes, bulk forge-hammer recipes and imported chemical/rare-earth recipes.

## Public documentation scope

The public pre-release includes only installation, build, feature-summary and licensing information. Detailed development documentation, registration templates, internal lifecycle analysis and historical validation records are not currently published.

## License

The source code is licensed under [LGPL-3.0-only](LICENSE). Original textures and quest content owned by GTOHJS contributors are licensed under [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0)](LICENSE_ASSETS.md). Third-party assets are not relicensed; their provenance and upstream terms are documented in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
