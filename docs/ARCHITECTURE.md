# Architecture

## Source boundaries

- `cn.teampancake.theaurorian2`: mod entry point and shared identifiers only.
- `common`: server-authoritative gameplay, registries, world generation, data and networking.
- `client`: rendering, screens, models, particles and client-only event handlers.
- `data`: data generators and bootstrap definitions.
- `compat`: optional integrations. Each integration must remain isolated from the core mod.
- `test`: GameTests and deterministic regression fixtures.

## Design rules

1. Registries are split by Minecraft registry type. The mod entry point only wires registrars.
2. Persistent gameplay state has an explicit codec and migration strategy.
3. Client effects are derived from synchronized state; clients do not decide gameplay outcomes.
4. World generation uses data-driven bootstrap definitions unless code is required by the algorithm.
5. Large mechanics are modeled as small state machines instead of tick-event condition chains.
6. Compatibility code never becomes a required dependency of core gameplay.
7. A feature is complete only when code, assets, data, localization and verification are present.

## Initial milestones

1. Bootstrap: reproducible build, CI, empty client and dedicated-server startup.
2. Foundation: registries, data generation, networking and GameTest conventions.
3. Dimension slice: portal, one biome, terrain, one structure and return path.
4. Progression slice: first material tier, crafting loop, creature and advancement.
5. Expansion: additional biomes, structures, bosses, world events and optional integrations.
