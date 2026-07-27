package cn.teampancake.theaurorian2.common.enchantment;

import cn.teampancake.theaurorian2.common.registry.ModMobEffects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record FreezeAspectEffect(int placeholder) implements EnchantmentEntityEffect {

    public static final MapCodec<FreezeAspectEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("placeholder", 0).forGetter(FreezeAspectEffect::placeholder))
            .apply(instance, FreezeAspectEffect::new));

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 origin) {
        if (!(entity instanceof LivingEntity livingEntity) || !livingEntity.canFreeze()) {
            return;
        }

        int durationTicks = enchantmentLevel * 4 * 20;
        livingEntity.addEffect(new MobEffectInstance(
                ModMobEffects.FREEZE_ASPECT,
                durationTicks,
                enchantmentLevel - 1,
                false,
                false,
                false));
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
