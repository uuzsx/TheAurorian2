package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.TheAurorian2;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SpreadingSnowyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class AurorianGrassBlock extends SpreadingSnowyBlock {

    public static final MapCodec<AurorianGrassBlock> CODEC = simpleCodec(AurorianGrassBlock::new);
    private static final ResourceKey<Block> AURORIAN_DIRT = ResourceKey.create(
            Registries.BLOCK, TheAurorian2.id("aurorian_dirt"));

    public AurorianGrassBlock(BlockBehaviour.Properties properties) {
        super(properties, AURORIAN_DIRT);
    }

    @Override
    protected MapCodec<? extends SpreadingSnowyBlock> codec() {
        return CODEC;
    }
}
