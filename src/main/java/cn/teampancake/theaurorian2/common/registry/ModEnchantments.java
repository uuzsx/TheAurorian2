package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Stable keys for Aurorian enchantments.
 *
 * <p>Enchantments are data-driven, so these keys do not register gameplay content by themselves.
 * A key becomes available only after a matching file is added under
 * {@code data/theaurorian2/enchantment}.</p>
 */
public final class ModEnchantments {

    public static final ResourceKey<Enchantment> AMNESIA_CURSE = key("amnesia_curse");
    public static final ResourceKey<Enchantment> ARROW_RAIN = key("arrow_rain");
    public static final ResourceKey<Enchantment> AURORA = key("aurora");
    public static final ResourceKey<Enchantment> CLEAR_MIND = key("clear_mind");
    public static final ResourceKey<Enchantment> COBWEB_CROSSING = key("cobweb_crossing");
    public static final ResourceKey<Enchantment> EXPERIENCE_ORE = key("experience_ore");
    public static final ResourceKey<Enchantment> FREEZE_ASPECT = key("freeze_aspect");
    public static final ResourceKey<Enchantment> GUARDIAN = key("guardian");
    public static final ResourceKey<Enchantment> IMPALE = key("impale");
    public static final ResourceKey<Enchantment> LEGENDARY_HERO = key("legendary_hero");
    public static final ResourceKey<Enchantment> LIGHTNING_DAMAGE = key("lightning_damage");
    public static final ResourceKey<Enchantment> LIGHTNING_RESISTANCE = key("lightning_resistance");
    public static final ResourceKey<Enchantment> MOLTEN_CORE = key("molten_core");
    public static final ResourceKey<Enchantment> MOONLIGHT = key("moonlight");
    public static final ResourceKey<Enchantment> NIGHT_WALKER = key("night_walker");
    public static final ResourceKey<Enchantment> OVERLOAD = key("overload");
    public static final ResourceKey<Enchantment> REFLECT_AURA = key("reflect_aura");
    public static final ResourceKey<Enchantment> ROUNDABOUT_THROW = key("roundabout_throw");
    public static final ResourceKey<Enchantment> SAVAGE = key("savage");
    public static final ResourceKey<Enchantment> SLIMES_HATER = key("slimes_hater");
    public static final ResourceKey<Enchantment> SOUL_SLASH = key("soul_slash");
    public static final ResourceKey<Enchantment> SOURCE_OF_TERRA = key("source_of_terra");
    public static final ResourceKey<Enchantment> SPRING_OF_LIFE = key("spring_of_life");
    public static final ResourceKey<Enchantment> SUNDER_ARMOR_SLASH = key("sunder_armor_slash");
    public static final ResourceKey<Enchantment> VIRTUALIZATION = key("virtualization");
    public static final ResourceKey<Enchantment> WIND_RUNNER = key("wind_runner");

    public static final List<ResourceKey<Enchantment>> ALL = List.of(
            AMNESIA_CURSE,
            ARROW_RAIN,
            AURORA,
            CLEAR_MIND,
            COBWEB_CROSSING,
            EXPERIENCE_ORE,
            FREEZE_ASPECT,
            GUARDIAN,
            IMPALE,
            LEGENDARY_HERO,
            LIGHTNING_DAMAGE,
            LIGHTNING_RESISTANCE,
            MOLTEN_CORE,
            MOONLIGHT,
            NIGHT_WALKER,
            OVERLOAD,
            REFLECT_AURA,
            ROUNDABOUT_THROW,
            SAVAGE,
            SLIMES_HATER,
            SOUL_SLASH,
            SOURCE_OF_TERRA,
            SPRING_OF_LIFE,
            SUNDER_ARMOR_SLASH,
            VIRTUALIZATION,
            WIND_RUNNER);

    private ModEnchantments() {
    }

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, TheAurorian2.id(name));
    }
}
