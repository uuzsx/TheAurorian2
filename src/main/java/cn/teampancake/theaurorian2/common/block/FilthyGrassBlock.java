package cn.teampancake.theaurorian2.common.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SpreadingSnowyBlock;
import com.mojang.serialization.MapCodec;

/** Grass block variant used by the Equinox Flower Island surface. */
public final class FilthyGrassBlock extends AurorianGrassBlock {

    public static final MapCodec<FilthyGrassBlock> CODEC = simpleCodec(FilthyGrassBlock::new);

    public FilthyGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends SpreadingSnowyBlock> codec() {
        return CODEC;
    }
}
