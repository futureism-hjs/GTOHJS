# Hyperdimensional Biochemical Factory

## Current derived recipes and maintenance filtering

The 2026-10-24 change adds the extraction, reaction and incubation recipe
families to this existing independent type, with half duration, unchanged EU/t
and one corresponding nonconsumable marker. Reaction/incubation radiation is
removed; incubation filtering uses cleaning/sterile/absolute-clean maintenance
at T1/T2/T3. Ordinary maintenance is tier zero. Native coil runtime and the
structure remain intact. See `49_controller_inputs_and_biochemical_recipes.md`
for the complete contract and verification boundary.

## Scope

This change registers the user-authored `超维度生化工厂.litematic` as a new GTO multiblock machine. The controller marker in the supplied model is `gtocore:biochemical_extraction`, but the formal machine controller is the newly registered `gtocore:hyperdimensional_biochemical_factory`.

The machine has its own RecipeType, `gtceu:hyperdimensional_biochemical_processing`. It does not reuse `biochemical_extraction`, `biochemical_reaction`, `chemical_complex`, or the existing hyperdimensional chemical-factory recipe maps.

## Recipe type

`HyperdimensionalBiochemicalRecipeTypeRegistration` follows the verified one-stop rare-earth registration path:

- Registry ID: `gtceu:hyperdimensional_biochemical_processing`
- Group: `GTRecipeTypes.MULTIBLOCK`
- EU direction: input
- Item I/O: 6 inputs, 18 outputs
- Fluid I/O: 9 inputs, 3 outputs
- Progress bar: multiple-arrow, left-to-right
- Sound: chemical

The recipe type is injected before `GTORecipeTypes.<clinit>` returns. The machine references the exact same `RecipeType` object. Registration validates the namespace, group, I/O limits, recipe UI, and registry identity.

## Controller and runtime

The controller uses GTOLib `CoilCrossRecipeMultiblockMachine::createCoilParallel`, matching `gtocore:chemical_complex`. This preserves the native CrossRecipe runtime:

- coil temperature determines the maximum parallel value using `2^min(60, floor(temperature / 900))`;
- the configured parallel value is initialized and clamped by GTOLib;
- no Thread Hatch means one thread;
- Thread Hatches are consumed by the native CrossRecipe trait;
- independent multithread scheduling requires a Wireless Energy Hatch;
- catalyst, energy/laser, item/fluid, maintenance, thread, overclock, and accelerate behavior remains controlled by the existing GTO/GTOLib traits.

No second parallel or threading implementation is added.

## Structure and hatches

The formal pattern is `data/gtohjs/structures/hyperdimensional_biochemical_factory.pattern` with dimensions `43 x 22 x 49`. Aisles are emitted from local Z maximum to minimum, rows from Y minimum to maximum, and columns in reverse local-X order because the source region has signed size `(-43,22,49)`.

The controller marker is at full-model local `(21,1,0)`. The controller-body projection is `13 x 6 x 13`, with its marker at `(6,0,0)`; translating it by `(+15,+1,0)` gives an exact voxel-for-voxel match. Only the 190 `gtceu:clean_machine_casing` blocks in that translated body projection become `H` hatch positions. The other 2,034 clean-machine-casing blocks in the full model remain fixed `A` casing.

Pattern symbols are:

- `A`: `gtceu:clean_machine_casing` (fixed casing outside the service projection)
- `B`: oxidation-resistant Hastelloy-N mechanical casing
- `C`: biological mechanical casing
- `D`: LuV hermetic casing
- `E`: high-power casing
- `F`: filter casing
- `G`: chemical corrosion-resistant pipe casing
- `H`: clean-machine-casing service positions
- `I`: plascrete
- `J`: inert machine casing
- `K`: cleanroom glass
- `L`: PTFE pipe casing
- `M`: Hastelloy-N frame
- `N`: glass
- `O`: sterilizing-filter casing
- `P`: cupronickel heating coils
- `Q`: sponge
- `S`: formal controller
- space: `Predicates.any()`, intentionally ignored by structure monitoring

The H predicate accepts the clean casing, GTO automatic item/fluid and energy/laser abilities, at most two catalyst hatches, and exactly one maintenance hatch. The machine definition uses clean-machine-casing as its appearance block and the existing clean-stainless/general0 casing renderer.

## Validation

Load-complete validation checks the RecipeType object, machine registry identity, one pattern supplier, dimensions, 190 H symbols, 432 coil symbols, renderer, and world/XEI preview flags. The source Litematic files remain read-only inputs; only the text pattern is packaged in the mod.
