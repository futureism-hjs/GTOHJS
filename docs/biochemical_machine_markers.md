# Biochemical machine marker items

## Registered items

| Item ID | Chinese name | English name |
| --- | --- | --- |
| `gtohjs:biochemical_extraction_marker` | 生物提取机标记 | Biological Extraction Machine Marker |
| `gtohjs:biochemical_reaction_marker` | 生化反应室标记 | Biochemical Reaction Chamber Marker |
| `gtohjs:incubator_marker` | 大型培养缸标记 | Large Incubator Marker |

Names follow the corresponding 0.5.6 machine names. `GTOHJSItems.java` uses
the existing DeferredRegister and Ingredients creative-tab event. These are
plain stackable items with no additional item behavior. The acquisition task
now adds one-cell workbench recipes: each corresponding native machine x1
becomes its marker x1, consuming the machine. See
`51_acquisition_recipe_proposal.md` for the approved table and delivery evidence.
The existing Kotlin tooltip handler already handles HJS attribution.

The user supplied the three PNGs. Original bytes are preserved as
`src/main/encoded-assets/<item path>.png.b64`, decoded by the existing
`processResources` task into `assets/gtohjs/textures/item/`. Each generated
item model references that same path; no image editing or new asset pipeline
is involved. English and Chinese names use `item.gtohjs.<item path>`.

## Verification status

The later acquisition task completed a Java 21 online IDEA clean build and
deployed the 920,271-byte addon. The original source-only restrictions below
describe the initial marker-registration task, not this subsequent delivery.
Client loading, server-side conflict validation and use remain unverified.

The subsequent 2026-10-24 biochemical recipe task uses each corresponding item
as a one-count native nonconsumable input. It does not add acquisition recipes.
That task is authorized to clean-build and deploy without launching the client;
see `49_controller_inputs_and_biochemical_recipes.md` for its separate results.

Source registration, locale/model JSON and original image-byte equality are
checked locally. Build/package and client checks are pending. The user
explicitly prohibited this agent from building or deploying on 2026-10-24
because another AI is working in the project.
