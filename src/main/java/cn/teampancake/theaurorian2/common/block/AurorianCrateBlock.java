package cn.teampancake.theaurorian2.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Breakable loot crate; creative breaking uses the same loot as survival. */
public final class AurorianCrateBlock extends Block {
    public static final MapCodec<AurorianCrateBlock> CODEC = simpleCodec(AurorianCrateBlock::new);

    public AurorianCrateBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel && player.isCreative()) {
            // Vanilla skips playerDestroy in creative, so emit its normal loot once here.
            Block.dropResources(state, level, pos, null, player, player.getMainHandItem());
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
