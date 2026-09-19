package cn.teampancake.theaurorian2.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.fish.Cod;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;

/** Vanilla schooling, panic, swimming, flopping and bucket persistence for both fish. */
public abstract class AurorianFishEntity extends Cod {
    protected AurorianFishEntity(EntityType<? extends AurorianFishEntity> type, Level level) {
        super(type, level);
    }

    public static <T extends AbstractFish> boolean checkSpawnRules(EntityType<T> type, LevelAccessor level,
            EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        // Keep vanilla's shallow-water lower bound, but allow inland lakes above sea level.
        // The biome tag excludes caves and sky islands; no water-column search is needed.
        return pos.getY() >= level.getSeaLevel() - 13
                && level.getFluidState(pos).is(FluidTags.WATER)
                && level.getFluidState(pos.below()).is(FluidTags.WATER)
                && level.getBlockState(pos.above()).is(Blocks.WATER);
    }
}
