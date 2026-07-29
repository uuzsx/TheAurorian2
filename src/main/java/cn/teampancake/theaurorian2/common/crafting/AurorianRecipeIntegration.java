package cn.teampancake.theaurorian2.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;

import java.util.Map;
import java.util.Set;

public final class AurorianRecipeIntegration {

    private static final Set<String> VANILLA_STICK_INGREDIENTS = Set.of(
            "minecraft:stick",
            "#c:rods/wooden");
    private static final String EQUIPMENT_STICKS = "#theaurorian2:vanilla_equipment_sticks";
    private static final Set<Identifier> STICK_COMPATIBLE_RECIPES = Set.of(
            vanillaRecipe("arrow"),
            vanillaRecipe("brush"),
            vanillaRecipe("crossbow"),
            vanillaRecipe("fishing_rod"),
            vanillaRecipe("copper_axe"),
            vanillaRecipe("copper_hoe"),
            vanillaRecipe("copper_pickaxe"),
            vanillaRecipe("copper_shovel"),
            vanillaRecipe("copper_spear"),
            vanillaRecipe("copper_sword"),
            vanillaRecipe("diamond_axe"),
            vanillaRecipe("diamond_hoe"),
            vanillaRecipe("diamond_pickaxe"),
            vanillaRecipe("diamond_shovel"),
            vanillaRecipe("diamond_spear"),
            vanillaRecipe("diamond_sword"),
            vanillaRecipe("golden_axe"),
            vanillaRecipe("golden_hoe"),
            vanillaRecipe("golden_pickaxe"),
            vanillaRecipe("golden_shovel"),
            vanillaRecipe("golden_spear"),
            vanillaRecipe("golden_sword"),
            vanillaRecipe("iron_axe"),
            vanillaRecipe("iron_hoe"),
            vanillaRecipe("iron_pickaxe"),
            vanillaRecipe("iron_shovel"),
            vanillaRecipe("iron_spear"),
            vanillaRecipe("iron_sword"),
            vanillaRecipe("stone_axe"),
            vanillaRecipe("stone_hoe"),
            vanillaRecipe("stone_pickaxe"),
            vanillaRecipe("stone_shovel"),
            vanillaRecipe("stone_spear"),
            vanillaRecipe("stone_sword"),
            vanillaRecipe("wooden_axe"),
            vanillaRecipe("wooden_hoe"),
            vanillaRecipe("wooden_pickaxe"),
            vanillaRecipe("wooden_shovel"),
            vanillaRecipe("wooden_spear"),
            vanillaRecipe("wooden_sword"));

    private AurorianRecipeIntegration() {
    }

    public static void onModifyRecipeJsons(ModifyRecipeJsonsEvent event) {
        for (Identifier recipeId : STICK_COMPATIBLE_RECIPES) {
            JsonElement recipe = event.getRecipeJsons().get(recipeId);
            if (recipe == null || !recipe.isJsonObject()) {
                continue;
            }

            JsonElement keyElement = recipe.getAsJsonObject().get("key");
            if (keyElement == null || !keyElement.isJsonObject()) {
                continue;
            }

            JsonObject key = keyElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> ingredient : key.entrySet()) {
                JsonElement value = ingredient.getValue();
                if (value.isJsonPrimitive()
                        && value.getAsJsonPrimitive().isString()
                        && VANILLA_STICK_INGREDIENTS.contains(value.getAsString())) {
                    ingredient.setValue(new JsonPrimitive(EQUIPMENT_STICKS));
                }
            }
        }
    }

    private static Identifier vanillaRecipe(String path) {
        return Identifier.fromNamespaceAndPath("minecraft", path);
    }
}
