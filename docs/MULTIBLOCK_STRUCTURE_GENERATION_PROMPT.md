# Multiblock Structure Generation Prompt

This project uses the user-maintained prompt at `E:\巨构提示词.txt` as the
long-term specification for generated Minecraft multiblock structures. Read
that source prompt before creating or revising any structure. Its current
output and environment requirements are:

- Deliver a Minecraft 1.20.1 `.litematic` file.
- The output may be placed in any convenient folder selected for the task.
- Use the GregTech Odyssey 0.5.6-beta reference environment.
- Use the original game directory on drive F:,
  `<path-to-GTO-0.5.6-beta-client>`.
- Treat the prompt's design rules as the default for scale, identity, detail,
  materials, complete-block constraints, controller and hatch markers, and
  structure verification. A task-specific user instruction takes priority.
- Generate one requested machine at a time and stop after that deliverable.

The prompt's older Sponge Schematic wording is superseded by the current
`.litematic` requirement above. Keep the original prompt path unchanged so it
remains the canonical source for future refinements.
