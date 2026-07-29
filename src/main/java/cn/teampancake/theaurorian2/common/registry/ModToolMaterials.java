package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public final class ModToolMaterials {

    private static final TagKey<Item> AURORIAN_PLANKS = TagKey.create(
            Registries.ITEM, TheAurorian2.id("aurorian_planks"));
    private static final TagKey<Item> AURORIAN_STONE_TOOL_MATERIALS = TagKey.create(
            Registries.ITEM, TheAurorian2.id("aurorian_stone_tool_materials"));

    public static final ToolMaterial WOOD = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 59, 2.0F, 0.0F, 15, AURORIAN_PLANKS);
    public static final ToolMaterial STONE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_STONE_TOOL, 131, 4.0F, 1.0F, 5, AURORIAN_STONE_TOOL_MATERIALS);

    private ModToolMaterials() {
    }
}
