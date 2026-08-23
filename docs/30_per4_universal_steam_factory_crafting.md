# GTOHJS per4 Universal Steam Factory Crafting Recipe

Per4 imports the external recipe-editor draft as the shaped crafting recipe `gtohjs:universal_steam_factory` without modifying the draft file. It outputs `gtocore:universal_steam_factory` with this pattern:

```text
ABA
CDC
EEE
```

- `A`: long bronze rod.
- `B`: bronze plate.
- `C`: large bronze fluid pipe.
- `D`: bronze gear.
- `E`: double bronze plate.

Registration reuses the verified `CustomCraftingRecipeRegistration` and `VanillaRecipeHelper.addShapedRecipe` lifecycle. The output item is validated by registry ID and included in load-complete non-empty-output validation and native-recipe-table diagnostic logging.

At the user's request, this release did not run a client test. Acceptance was limited to the coremod JavaScript syntax check, Java 21 `clean build`, release-JAR version-metadata validation, public-release-tree allowlist audit, and README forbidden-word scan.
