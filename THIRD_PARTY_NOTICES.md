# Third-Party Asset Notices

Original GTOHJS textures and quest content are licensed under `CC-BY-NC-SA-4.0` as described in `LICENSE_ASSETS.md`. GTOHJS also includes assets copied or adapted from other mods. Those files are excluded from the GTOHJS content license and remain governed by their upstream terms. This notice records their provenance; it does not replace or expand the upstream license terms.

## ExtendedAE recipe editor icon

- GTOHJS asset: `assets/gtohjs/textures/item/recipe_editor.png`
- Upstream asset: `assets/expatternprovider/textures/item/pattern_modifier.png`
- Upstream project: [ExtendedAE](https://github.com/GlodBlock/ExtendedAE), version 1.4.17
- Relationship: byte-for-byte copy. GTOCore 0.5.6-beta also points its recipe editor item model at this upstream asset.
- License record: the inspected ExtendedAE Forge artifact declares `LGPL-3.0` in `META-INF/mods.toml`.

## GTOCore integral bronze framework

- GTOHJS asset: `assets/gtohjs/textures/block/casings/integral_bronze_framework.png`
- Upstream asset: `assets/gtocore/textures/block/casings/integral_framework/ulv.png`
- Upstream project: [GTOCore](https://github.com/GregTech-Odyssey/GTOCore), version 0.5.6-beta
- Relationship: recolored/adapted texture for the GTOHJS integral bronze framework.
- License record: the inspected GTOCore source repository includes the GNU Lesser General Public License version 3 text in its `LICENSE` file.

## GTLCore world fragments and collector overlays

- GTOHJS assets: `assets/gtohjs/textures/item/world_fragments_*.png` and `assets/gtohjs/textures/block/machines/fragment_world_collection_machine/*`
- Upstream assets: the corresponding `assets/gtlcore/textures/item/world_fragments_*` and `assets/gtceu/textures/block/machines/fragment_world_collection_machine/*` files
- Upstream project: [GTLCore](https://github.com/nutant233/GTLCore), version `1.2.2.9-fix4`, distributed with GregTech Leisure 1.4.5.1
- Relationship: texture copies. The GTOHJS item model JSON files only change the texture namespace from `gtlcore` to `gtohjs`.
- License record: the inspected GTLCore artifact declares `LGPLv3.0` in `META-INF/mods.toml`.

Copyright remains with the respective upstream contributors.
