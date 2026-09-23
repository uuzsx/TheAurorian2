# The Aurorian 2 Engineering Contract

These rules apply to the entire repository. They are persistent project requirements, not optional cleanup preferences.

## Priority

1. Preserve the requested behavior, appearance, mechanics, and player experience.
2. Preserve correctness, save compatibility, and client/server safety.
3. Prefer compatibility and efficient runtime behavior.
4. Keep implementation and assets free of accidental duplication and noise.

Never change, omit, weaken, or approximate a requested result merely to reduce code or file size. A complete implementation may be large when the feature genuinely requires it.

## Daily Development Workflow

- For feature development and bug fixes, follow this sequence: define player-visible acceptance criteria, inspect the current implementation and existing changes, implement the complete behavior, run the applicable verification below, and report results and remaining risks.
- For bugs, establish reproducible steps or other concrete failure evidence before choosing a fix, then verify the affected behavior after the change.
- Read `docs/DEVELOPMENT_WORKFLOW.md` when it is available locally for the detailed process and reusable task template. It is local-only; do not add or upload it. If it is absent in another checkout, follow the sequence and requirements in this file.
- Keep small tasks lightweight: record scope and verification in the conversation instead of requiring a separate document. Routine steps within the authorized task do not require separate approval; clarify missing information when it materially changes the requested result.

## Legacy Source Reference

- The user-designated legacy source is `D:/TheAurorian-NeoForge-1.21`.
- Its current source root is nested at `D:/TheAurorian-NeoForge-1.21/TheAurorian-NeoForge-1.21`.
- Use this source when reviewing or porting legacy features. `D:/TheAurorian` is outdated and must not be treated as the latest legacy implementation.

## Story Canon

- `docs/LORE_CANON.md` is the authoritative narrative reference for The Aurorian 2.
- Read it before writing or changing lore-sensitive names, dialogue, quests, advancements, item or boss descriptions, structures, inscriptions, environmental storytelling, faction behavior, sky events, or story progression.
- Preserve the distinction between confirmed canon, in-world propaganda, deliberately hidden truth, and unresolved design questions.
- Do not turn an unresolved detail into canon without explicit user approval.
- In-world text may contradict the truth when the canon document identifies it as Aurorian Church propaganda; implementation notes and internal documentation must not present that propaganda as objective fact.

## Required Design Check

Before implementing a feature, explicitly consider:

- Does this implementation achieve the user's actual purpose and all stated details?
- Can an established project or NeoForge API provide the result without changing behavior?
- Does it affect vanilla global state, other dimensions, registries, networking, saves, or other mods?
- Does it add per-tick work, broad searches, repeated allocations, unnecessary packets, or always-ticking block entities?
- Is duplication real and safely removable, or would consolidation make behavior harder to verify?
- What build, resource, client, server, worldgen, and save checks match the risk?

## Meaning Of Lean And Optimized

- "Lean" means removing redundant code, duplicate logic, meaningless whitespace, unused resources, and unnecessary runtime work.
- "Optimized" means improving performance, stability, and compatibility without altering the requested behavior.
- Line count alone is not an optimization target.
- Do not introduce abstractions until they remove proven duplication or isolate a real compatibility boundary.
- Do not combine implementations whose small differences are part of their intended behavior or appearance.

## Compatibility

- Prefer NeoForge events, registries, tags, data attachments, data maps, loot modifiers, biome modifiers, and data-driven resources over invasive hooks.
- Use a Mixin only when the public API cannot implement the requirement correctly. Keep its target narrow, document the reason, and avoid cancelling or replacing an entire vanilla method when a smaller injection is sufficient.
- Scope Aurorian rules to the Aurorian dimension, biome, entity, item, or effect whenever global behavior is not explicitly required.
- Never load client-only classes from common or dedicated-server code.
- Treat the server as authoritative. Synchronize only changed state and only when needed.
- Use tags for cross-mod material and recipe compatibility instead of enumerating foreign mod IDs.
- Keep published registry IDs stable. A rename requires an explicit migration strategy for existing saves.
- Do not overwrite vanilla or another mod's data when additive namespaced data can achieve the same result.

## Performance

- Avoid broad per-tick block or entity scans. Use events, cached state, scheduled work, or bounded searches.
- Do not perform disk access, resource parsing, registry discovery, or large allocation work in render or tick loops.
- Tick block entities only while their behavior requires it.
- Keep worldgen bounded and deterministic. Never add unbounded ground searches or repeated chunk-scale retries.
- Avoid per-frame object churn in renderers. Respect culling and cap particles or decorative effects.
- Do not send full state repeatedly when a small delta or state transition is sufficient.
- Measure suspected hot paths before changing behavior for performance.

## Models And Resources

- Parse and validate every imported JSON or Geo model before implementation.
- Remove meaningless formatting or generated whitespace when it does not change the token stream.
- Generated JSON must use `scripts/json_utils.ps1` and its validated compact `Write-Json` output where applicable.
- Formatting cleanup must preserve every semantic value and structure.
- Model bones, pivots, UVs, negative dimensions, mirroring, rotations, animations, render transforms, material assignments, and user-specified coordinates are behavioral contracts. Never alter them merely to make a file smaller or code more uniform.
- Retain intentional variants. Remove a resource only after proving it has no direct, conventional, generated, or dynamic reference.
- Do not convert a model format or share a parent model unless rendered output and all transforms remain identical.

## Approved Model Presentation And Hello Defaults

- The user-approved `stupid_cat` model is the default reference for future model size, inventory presentation, and hello interaction, unless the user specifies otherwise. Use `exports/skin_doll/final/stupid_cat.bbmodel` locally and the packaged `geckolib/models/block/stupid_cat.geo.json` as the geometry reference.
- Keep the approved doll proportions and uniform physical pixel size across head, body, arms, and legs; do not stretch the original skin proportions to create smaller limbs. Match this size for subsequent dolls.
- Inventory icons use front lighting (`gui_light: front`) and must appear centered with no clipping. The approved reference GUI transform is rotation `[15,150,0]`, translation `[0,-7,0]`, scale `[0.85,0.85,0.85]`. Match the visual result; models with different bounds or pivots may require different transform values. Verify the actual creative inventory display.
- Hello interaction follows `StupidCatBlockEntity`: right-click plays `wave_hello` once, then returns to `idle_blink`; the right hand leads and the left hand moves subtly. The reference duration is 3.2 seconds (64 ticks); suppress repeated triggers during playback and synchronize from the server. Blink surfaces remain coplanar with the eyes, with correct backface culling.
- The cat doll plays one vanilla classic cat meow when hello starts, with no sound stacking. Preserve character-appropriate audio for future models rather than automatically giving unrelated objects a cat voice.
- Apply these defaults to future work, preserving explicitly requested object dimensions and mechanics (for example, furniture dimensions or chest opening animations). This does not request a retroactive rewrite of existing models.

## Verification

- Preserve unrelated working-tree changes and never include `logs/` in a commit.
- Treat root `docs/` and `exports/` as local-only personal notes and experiments. Preserve their local files; never add or force-add their contents to Git or upload them to GitHub. Local references such as `docs/LORE_CANON.md` remain applicable. Removing previously tracked copies from the Git index is allowed without deleting local files.
- Review the exact changed-file set before staging.
- Parse all changed JSON and verify whitespace-only rewrites preserve their token streams.
- Run `gradlew.bat build` for implementation changes.
- Start a client and complete resource loading for model, texture, HUD, shader, or other rendering changes.
- Start a dedicated server for common-code, registry, networking, effect, or worldgen changes when relevant.
- Test existing-save behavior whenever registry IDs, persistent data, dimensions, or worldgen contracts are touched.
- Report what was verified and any residual risk. Do not claim behavior-neutral optimization without evidence.
