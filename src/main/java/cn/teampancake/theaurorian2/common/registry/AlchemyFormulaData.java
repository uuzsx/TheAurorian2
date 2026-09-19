package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

/** Data-driven legacy formulas; synchronized for the ingredient hints and potion preview. */
public final class AlchemyFormulaData {
    public record Formula(String formula) {
        private static final Codec<String> TEXT = Codec.STRING.validate(s ->
                !s.isEmpty() && s.length() <= 512 && s.matches("[0-9+!<>=*|&\\s-]+")
                        ? DataResult.success(s) : DataResult.error(() -> "Invalid alchemy formula"));
        public static final Codec<Formula> CODEC = TEXT.fieldOf("formula").codec().xmap(Formula::new, Formula::formula);
    }
    public static final DataMapType<Item, Formula> INGREDIENTS = DataMapType.builder(
            TheAurorian2.id("alchemy_table/ingredients"), Registries.ITEM, Formula.CODEC).synced(Formula.CODEC, false).build();
    public static final DataMapType<MobEffect, Formula> EFFECTS = DataMapType.builder(
            TheAurorian2.id("alchemy_table/usable_effects"), Registries.MOB_EFFECT, Formula.CODEC).synced(Formula.CODEC, false).build();
    public static final DataMapType<MobEffect, Formula> AMPLIFIERS = DataMapType.builder(
            TheAurorian2.id("alchemy_table/amplifier_effects"), Registries.MOB_EFFECT, Formula.CODEC).synced(Formula.CODEC, false).build();
    private AlchemyFormulaData() {}
}
