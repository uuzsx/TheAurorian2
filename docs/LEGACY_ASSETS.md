# Legacy asset migration

The legacy project at `D:\TheAurorian` is a read-only source of art and design material.

## Allowed sources

- `src/main/resources/assets/theaurorian/textures`
- `src/main/resources/assets/theaurorian/sounds`
- `src/main/resources/assets/theaurorian/geo`
- `src/main/resources/assets/theaurorian/animations`
- `src/main/resources/data/theaurorian/structure`

## Migration workflow

1. Select only assets required by the feature currently being implemented.
2. Copy them into the `theaurorian2` namespace; never reference the legacy project at runtime.
3. Rename identifiers when needed and update every model, animation and sound reference.
4. Check resource formats against Minecraft 26.1.2.
5. Verify texture dimensions, transparency, animation metadata, sound playback and model rendering.
6. Record the migrated asset below so unused or duplicate files do not accumulate.

## Migration register

| New identifier | Legacy source | Feature | Verified |
| --- | --- | --- | --- |
| `the_aurorian_2_logo.png` | `src/main/resources/the_aurorian_logo.png` | Mod list logo | Build verified |
| `assets/theaurorian2/textures/**` | `assets/theaurorian/textures/**` | Legacy texture archive (789 files) | Copy hashes verified; runtime use pending |
| `aurorian_rabbit`, `aurorian_pig`, `aurorian_sheep`, `aurorian_cow` | `common/entities/animal`, `client/model/entity`, `textures/entity` | Adult animal behavior, Java models, and textures | Build, dedicated server, and client resource loading verified |
