package cn.teampancake.theaurorian2.common.effect;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/** A mob effect whose attribute values do not scale with the amplifier. */
public class AurorianMobEffect extends MobEffect {

    public AurorianMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public AurorianMobEffect addFixedModifier(
            Holder<Attribute> attribute,
            String modifierName,
            double amount,
            AttributeModifier.Operation operation) {
        this.addAttributeModifier(
                attribute,
                TheAurorian2.id(modifierName),
                operation,
                amplifier -> amount);
        return this;
    }
}
