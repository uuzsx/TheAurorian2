package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.effect.AurorianMobEffect;
import cn.teampancake.theaurorian2.common.effect.FrostbiteMobEffect;
import cn.teampancake.theaurorian2.common.enchantment.FreezeAspectMobEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMobEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, TheAurorian2.MOD_ID);

    public static final DeferredHolder<MobEffect, FreezeAspectMobEffect> FREEZE_ASPECT =
            MOB_EFFECTS.register("freeze_aspect", FreezeAspectMobEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> STUN = register("stun", () ->
            harmful(0x8B0000)
                    .addFixedModifier(Attributes.MOVEMENT_SPEED, "stun_movement_speed", -0.80D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addFixedModifier(Attributes.ATTACK_DAMAGE, "stun_attack_damage", -0.90D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addFixedModifier(Attributes.JUMP_STRENGTH, "stun_jump_strength", -0.90D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addFixedModifier(Attributes.BLOCK_BREAK_SPEED, "stun_block_break_speed", -0.95D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addFixedModifier(Attributes.BLOCK_INTERACTION_RANGE, "stun_block_range", -0.70D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addFixedModifier(Attributes.ENTITY_INTERACTION_RANGE, "stun_entity_range", -0.70D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addFixedModifier(Attributes.ATTACK_SPEED, "stun_attack_speed", -0.70D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final DeferredHolder<MobEffect, MobEffect> PARALYSIS = register("paralysis", () ->
            harmful(0xC09C72)
                    .addFixedModifier(Attributes.MOVEMENT_SPEED, "paralysis_movement_speed", -1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addFixedModifier(Attributes.JUMP_STRENGTH, "paralysis_jump_strength", -1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final DeferredHolder<MobEffect, MobEffect> OVERHEATING = register("overheating", () ->
            harmful(0xDB5F39)
                    .addFixedModifier(Attributes.MOVEMENT_SPEED, "overheating_movement_speed", -0.05D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addFixedModifier(Attributes.ATTACK_SPEED, "overheating_attack_speed", -0.20D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final DeferredHolder<MobEffect, MobEffect> PRESSURE = register("pressure", () -> harmful(0x714BDB));
    public static final DeferredHolder<MobEffect, MobEffect> DEAFNESS = register("deafness", () -> harmful(0x886671));
    public static final DeferredHolder<MobEffect, MobEffect> EIDOLON_POISON = register("eidolon_poison", () ->
            harmful(0x36D6BE).setBlendDuration(22));
    public static final DeferredHolder<MobEffect, MobEffect> CRYSTALLIZATION = register("crystallization", () -> harmful(0x17D1C7));
    public static final DeferredHolder<MobEffect, MobEffect> SHADOWED_SIGHT = register("shadowed_sight", () -> harmful(0x2B2B2B));
    public static final DeferredHolder<MobEffect, MobEffect> TREMOR = register("tremor", () -> harmful(0x81663B));
    public static final DeferredHolder<MobEffect, FrostbiteMobEffect> FROSTBITE =
            MOB_EFFECTS.register("frostbite", FrostbiteMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> LACERATION = register("laceration", () -> harmful(0xB83246));
    public static final DeferredHolder<MobEffect, MobEffect> CORRUPTION = register("corruption", () -> harmful(0x570E20));
    public static final DeferredHolder<MobEffect, MobEffect> INCANTATION = register("incantation", () -> harmful(0x7053A6));
    public static final DeferredHolder<MobEffect, MobEffect> VULNERABILITY = register("vulnerability", () -> harmful(0xC06C7A));
    public static final DeferredHolder<MobEffect, MobEffect> FORBIDDEN_CURSE = register("forbidden_curse", () -> harmful(0x4B6584));
    public static final DeferredHolder<MobEffect, MobEffect> CONFUSION = register("confusion", () -> harmful(0xB05AC8));
    public static final DeferredHolder<MobEffect, MobEffect> HOLINESS = register("holiness", () ->
            new AurorianMobEffect(MobEffectCategory.BENEFICIAL, 0xFFFFEB)
                    .addFixedModifier(Attributes.ATTACK_DAMAGE, "holiness_attack_damage", 0.10D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addFixedModifier(Attributes.MOVEMENT_SPEED, "holiness_movement_speed", 0.05D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    private ModMobEffects() {
    }

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }

    private static AurorianMobEffect harmful(int color) {
        return new AurorianMobEffect(MobEffectCategory.HARMFUL, color);
    }

    private static <T extends MobEffect> DeferredHolder<MobEffect, T> register(
            String name, java.util.function.Supplier<T> supplier) {
        return MOB_EFFECTS.register(name, supplier);
    }
}
