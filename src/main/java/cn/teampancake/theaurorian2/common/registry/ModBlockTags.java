package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModBlockTags {

    public static final TagKey<Block> SPIDER_MOTHER_BREAKABLE_TRAPS =
            TagKey.create(Registries.BLOCK, TheAurorian2.id("spider_mother_breakable_traps"));

    private ModBlockTags() {
    }
}
