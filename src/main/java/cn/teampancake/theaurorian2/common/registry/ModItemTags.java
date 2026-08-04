package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {

    public static final TagKey<Item> ACCESSORIES_AND_ARTIFACTS =
            TagKey.create(Registries.ITEM, TheAurorian2.id("accessories_and_artifacts"));

    private ModItemTags() {
    }
}
