package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.enchantment.FreezeAspectEffect;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Codec registries used by effects that cannot be expressed with vanilla enchantment components. */
public final class ModEnchantmentEffectTypes {

    public static final DeferredRegister<MapCodec<? extends EnchantmentEntityEffect>> ENTITY_EFFECTS =
            DeferredRegister.create(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, TheAurorian2.MOD_ID);
    public static final DeferredRegister<MapCodec<? extends EnchantmentValueEffect>> VALUE_EFFECTS =
            DeferredRegister.create(Registries.ENCHANTMENT_VALUE_EFFECT_TYPE, TheAurorian2.MOD_ID);
    public static final DeferredRegister<MapCodec<? extends EnchantmentLocationBasedEffect>> LOCATION_EFFECTS =
            DeferredRegister.create(Registries.ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE, TheAurorian2.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<FreezeAspectEffect>> FREEZE_ASPECT =
            ENTITY_EFFECTS.register("freeze_aspect", () -> FreezeAspectEffect.CODEC);

    private ModEnchantmentEffectTypes() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_EFFECTS.register(modEventBus);
        VALUE_EFFECTS.register(modEventBus);
        LOCATION_EFFECTS.register(modEventBus);
    }
}
