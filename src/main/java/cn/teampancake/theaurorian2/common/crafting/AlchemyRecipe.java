package cn.teampancake.theaurorian2.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import cn.teampancake.theaurorian2.common.registry.ModAlchemy;

/** Three unordered ingredients and one water bottle. Results never escape by reference. */
public record AlchemyRecipe(List<Ingredient> ingredients, ItemStackTemplate result, int time) implements Recipe<AlchemyRecipe.Input> {
    public static final MapCodec<AlchemyRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.listOf(3, 3).fieldOf("ingredients").forGetter(AlchemyRecipe::ingredients),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(AlchemyRecipe::result),
            Codec.intRange(1, 72000).optionalFieldOf("time", 140).forGetter(AlchemyRecipe::time)
    ).apply(i, AlchemyRecipe::new));
    public static final RecipeSerializer<AlchemyRecipe> SERIALIZER = new RecipeSerializer<>(CODEC,
            ByteBufCodecs.fromCodecWithRegistries(CODEC.codec()));
    public AlchemyRecipe { ingredients = List.copyOf(ingredients); }
    @Override public boolean matches(Input input, Level level) {
        if (!input.getItem(3).is(Items.POTION) || !input.getItem(3)
                .getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) return false;
        for (int a = 0; a < 3; a++) for (int b = 0; b < 3; b++) {
            if (a != b && ingredients.get(0).test(input.getItem(a))
                    && ingredients.get(1).test(input.getItem(b))
                    && ingredients.get(2).test(input.getItem(3 - a - b))) return true;
        }
        return false;
    }
    @Override public ItemStack assemble(Input input) { return result.create(); }
    @Override public boolean showNotification() { return false; }
    @Override public String group() { return ""; }
    @Override public RecipeSerializer<AlchemyRecipe> getSerializer() { return SERIALIZER; }
    @Override public RecipeType<AlchemyRecipe> getType() { return ModAlchemy.RECIPE_TYPE.get(); }
    @Override public PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }
    @Override public RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }
    public record Input(List<ItemStack> items) implements RecipeInput {
        @Override public ItemStack getItem(int slot) { return items.get(slot); }
        @Override public int size() { return 4; }
    }
}
