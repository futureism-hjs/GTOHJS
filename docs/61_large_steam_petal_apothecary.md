# Large Steam Petal Apothecary

## Confirmed design

The user confirmed both interview rounds and clarified the controller overlay
on 2026-11-07: its artwork must occupy 14 x 14 pixels. Native GT overlays use
a 16 x 16 PNG with a one-pixel transparent border. Both idle and active
artwork must remain inside pixel coordinates (1,1) through (14,14).

Register `gtocore:large_steam_petal_apothecary` through the existing native
machine list. Reuse `LargeSteamMultiblockMachine` at base 32 EU/t and only
the existing `gtceu:large_petal_apothecary` proxy recipe type. Keep native
difficulty parallel limits, duration scaling and steam-hatch OC. Do not copy
the Universal Steam Factory's machine-specific 1t/MV overrides. Preserve the
existing electrical/mana machines and shared recipe conversion.

The supplied schematic contributes its 9 x 7 x 7, 161-block machine; exclude
the grass/dirt layer and replace the steam-cracker controller marker. Keep
126 steam casings, 7 lime glass, 20 bronze pipes, 5 bronze gearboxes and
2 bronze casings before service substitutions. Empty cells allow decoration.
Default pattern axes are LEFT/UP/FRONT, so reverse both source X and Z;
rows remain bottom-up. Source controller (2,1,0) becomes pattern (6,1,6).

Every steam casing accepts the same service predicate: exactly one steam
energy hatch, exactly one unobstructed steam vent, a shared total of 1-5 item
inputs and exactly one item output. Steam and ordinary buses share those
limits. No recipe-fluid, electrical, mana, maintenance or performance parts.
Five four-slot bronze input buses cover the recipe type's 17 input slots
when native unpainted/non-distinct handlers are merged.

Craft one controller at a workbench with PLP/LAL/PLP: P=bronze plate,
L=Botania livingrock, A=default Botania apothecary. Use the existing custom
crafting lifecycle, material tags and post-load recipe validation.

The actual controller-front overlay uses a bronze frame and botanical mana
flower/runes. Idle is dark jade; working is bright mana teal. Store PNG
source as text through the existing encoded-assets/processResources flow.
No new renderer or resource generation framework is required.

## Implementation and validation

Implemented using the existing registration list, pattern loader, combined
ability predicates, crafting registration/output validation and PNG decoding.
Added four language resources and the bilingual changelog entry.

On 2026-11-07 the Java 21.0.5 online IDEA MCP `./gradlew clean build` passed
in 35 seconds: 16 actionable tasks, 14 executed and two from cache. Existing
mana-conversion checks passed 72,013 assertions; optimization contracts and
73,813 steam-budget checks passed. These are regression checks, not gameplay
acceptance of this controller. No new processing implementation was introduced.

Static verification compared all 441 pattern cells against the input NBT,
including native-axis orientation and controller position. Four JSON language
files parsed successfully. Both packaged PNGs are 16 x 16 RGBA with exact
alpha bounds (1,1,15,15), giving 14 x 14 artwork and a transparent outer border.
Whitespace checks and final JAR class/pattern/texture/language checks passed.

Artifact: `build/libs/gtohjs-5.7-spe1-for-gtocore-0.5.6-beta.jar`, 1,013,378
bytes at this check. It includes the current development worktree's other
existing changes; this task did not remove or publish them.

Runtime formation, exhaust blockage, complete ingredient/reagent consumption,
native parallel/OC behavior, workbench use and in-game overlay appearance
remain untested. No deployment, client launch or publication was performed.

## Asset production

Used the built-in imagegen tool for original idle art and an active-state edit.
The high-resolution concepts were technically normalized to hard cutout alpha,
cropped using the same idle bounds for alignment, reduced with nearest-neighbor
sampling to 14 x 14, and centered in a transparent 16 x 16 native canvas.
The authoritative saved sources are the two PNG-base64 files under
`src/main/encoded-assets/large_steam_petal_apothecary_front*.png.b64`; the existing
Gradle map emits the actual `overlay_front.png` and `overlay_front_active.png`.
The user corrected the initial concept-scale assumption before final packing.

Idle generation prompt:

> Create ONE production Minecraft GregTech machine controller front overlay sprite, NOT a mockup or a rendered block. Square isolated sprite on genuine transparent background, front-on orthographic. Pixel art following a strict logical 32 by 32 pixel grid with large crisp nearest-neighbor pixel clusters, NO anti-aliasing. Fill square canvas with a thin aged-bronze octagonal bezel inset from edges, four tiny bronze corner fasteners. Center: a simple recognizable four-petal magical botanical flower above a tiny shallow apothecary bowl, enclosed by a broken circular mana rune ring. Color palette: aged copper/bronze (#79522e, #bc8b4a), very dark mana green (#143c35), muted jade (#347963) and small pale sage highlights. This is the IDLE state: jade glyphs are dim, no light spill, no bright glow. Mostly transparent spaces between ring, glyph, bowl and bezel allow underlying machine casing to show. Symmetrical, readable at 32px game scale. No letters, no numbers, no readable text, no labels, no watermarks, no background, no perspective, no scene, no steam/clouds. The circular rune ring and flower must read as botanical magic, not electricity.

Active editing prompt:

> Edit this Minecraft GregTech controller overlay sprite into its ACTIVE WORKING state. Preserve the exact canvas framing, pixel geometry, bronze bezel and corner fasteners, flower, bowl and rune-ring positions. ONLY brighten the green flower and green rune ring from dark jade to luminous vivid mana teal and pale cyan-green highlights. No red pixels. Keep the bronze frame unchanged. Preserve all genuinely transparent holes and transparent background. No soft halo or glow spill outside the hard-edged sprite. Maintain crisp logical 32px pixel-art readability, front-on square, same exact silhouette and placement. No new symbols, text, background, perspective, animation frames or labels.
