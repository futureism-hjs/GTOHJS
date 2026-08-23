# GTOHJS fix50 Hyperdimensional Update

## Runtime formula

The hyperdimensional smelter and hyperdimensional chemical factory continue to use GTOLib `CoilCrossRecipeMultiblockMachine`'s native parallel and CrossRecipe thread paths, but their capacity formula changes to:

```text
parallelism = threads = max(1, floor(coil temperature / 900)^2)
```

The constructor's parallel function and `getThread()` in `HyperdimensionalCoilMachine` call the same `coilCapacity`, preventing the tooltip, parallelism, and threads from using different formulas. All recipes remain forced to one tick, and overclock hatches remain rejected both during structure matching and runtime.

## New models and hatches

| Machine | Litematic | Effective structure | New constraint |
| --- | --- | --- | --- |
| `gtocore:hyperdimensional_smelter` | External development asset `超维度冶炼炉2.litematic` | `15 x 43 x 15`; controller normalized to `aisle=14,row=1,col=7` | The draft `gtocore:me_muffler_hatch` position is normalized to the sole `M=7,41,7`; exactly one `MUFFLER` ability must be installed there, and the 39 H positions no longer accept mufflers |
| `gtocore:hyperdimensional_chemical_factory` | External development asset `超维度化工厂2.litematic` | Source selection is `15 x 43 x 16`, trimmed to `15 x 43 x 15` after removing the all-air boundary; controller normalized to `14,1,7` | Filter symbols and filter-tier runtime are completely removed; exactly one maintenance hatch must be installed among the 39 H positions; vacuum tier remains 4 |

The generator no longer assumes that the controller is always at minimum Z. It first trims all-air boundary aisles, then chooses far-end-to-controller-end aisle order according to which end of the effective Z axis contains the controller. Rows always follow `minY -> maxY`, and X always follows the Litematic's local LEFT direction. The 39 leap-forward-one hatch positions are mirrored or shifted automatically with the controller end.

## Machine and item tooltips

The tooltip for each of the four hyperdimensional controllers begins with five fixed localized lines: aqua "超越维度的力量", dark blue "通过线圈的磁场不断压缩空间，超越维度", purple "将宇宙的能量全部汇聚到一点", green "使用高强度材料搭建", and gold "在有限的电压等级下发挥无限的潜力". The two coil machines then show the localized "特殊多线程" label and the squared formula instead of calling the old `coilParallelTooltips()`.

At the end of the tooltip list, Forge `ItemTooltipEvent` appends the localized footer "由 GTO HJS 添加" to every `gtohjs:*` item and the eight `gtocore:*` machine items registered by GTOHJS. The event only appends display text; it does not modify the original item, block, or GTO registry.

## Verification requirements

1. Run `clean compileJava processResources jar reobfJar` with Java 21.
2. The structure validator must return four `15x43x15` structures, controller `14,1,7`, and H=39; the smelter must report `M=7,41,7`, and the chemical factory must report filters=0.
3. The client must log `REGISTERED` and `patternBuilt=true` for all four machines. The smelter log must include the dedicated muffler constraint, and the chemical factory log must include `maintenanceHatch=true` and `cleanroomFilters=false`.
4. Manually confirm in game that the source footer is last, the five colored lines are in the correct order, and structure errors for the muffler and maintenance hatches point to the correct positions.
