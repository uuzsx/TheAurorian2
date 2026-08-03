package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class ModEntityTypeTags {

    public static final TagKey<EntityType<?>> PHANTOM_BLOSSOM_EXECUTION_IMMUNE =
            TagKey.create(Registries.ENTITY_TYPE, TheAurorian2.id("phantom_blossom_execution_immune"));

    private ModEntityTypeTags() {
    }
}
