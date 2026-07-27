# Enchantment framework

The Aurorian 2 enchantments use Minecraft's data-driven enchantment registry.

- Stable IDs are declared in `ModEnchantments`.
- Runtime code must resolve optional holders through `EnchantmentAccess`.
- Custom entity, value, and location effect codecs belong in `ModEnchantmentEffectTypes`.
- Enchantment definitions will live in `data/theaurorian2/enchantment`.
- Enchanting-table, loot, trade, curse, and treasure availability will be explicit tags.

Declaring a key does not create an enchantment or an enchanted book. Until its data file is added,
the key remains inactive and `EnchantmentAccess` returns level `0`.

## Legacy design inventory

The following IDs are reserved while their mechanics and balance are redesigned:

`amnesia_curse`, `arrow_rain`, `aurora`, `clear_mind`, `cobweb_crossing`,
`experience_ore`, `freeze_aspect`, `guardian`, `impale`, `legendary_hero`,
`lightning_damage`, `lightning_resistance`, `molten_core`, `moonlight`,
`night_walker`, `overload`, `reflect_aura`, `roundabout_throw`, `savage`,
`slimes_hater`, `soul_slash`, `source_of_terra`, `spring_of_life`,
`sunder_armor_slash`, `virtualization`, and `wind_runner`.
