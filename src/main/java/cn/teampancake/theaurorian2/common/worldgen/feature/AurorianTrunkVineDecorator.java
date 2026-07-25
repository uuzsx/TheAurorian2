package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModTreeDecorators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public final class AurorianTrunkVineDecorator extends TreeDecorator {

    public static final MapCodec<AurorianTrunkVineDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.floatRange(0.0F, 1.0F)
                            .fieldOf("chance")
                            .forGetter(decorator -> decorator.chance),
                    Codec.floatRange(0.0F, 1.0F)
                            .fieldOf("density")
                            .forGetter(decorator -> decorator.density))
            .apply(instance, AurorianTrunkVineDecorator::new));

    private final float chance;
    private final float density;

    public AurorianTrunkVineDecorator(float chance, float density) {
        this.chance = chance;
        this.density = density;
    }

    @Override
    public void place(Context context) {
        RandomSource random = context.random();
        if (context.logs().isEmpty() || random.nextFloat() >= this.chance) {
            return;
        }

        boolean placed = false;
        for (BlockPos logPos : context.logs()) {
            for (Direction outward : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                if (random.nextFloat() < this.density && placeAt(context, logPos, outward)) {
                    placed = true;
                }
            }
        }

        if (!placed) {
            for (BlockPos logPos : context.logs()) {
                for (Direction outward : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                    if (placeAt(context, logPos, outward)) {
                        return;
                    }
                }
            }
        }
    }

    private static boolean placeAt(Context context, BlockPos logPos, Direction outward) {
        BlockPos vinePos = logPos.relative(outward);
        if (!context.isAir(vinePos)) {
            return false;
        }
        context.setBlock(
                vinePos,
                ModBlocks.AURORIAN_VINE.get().defaultBlockState()
                        .setValue(VineBlock.getPropertyForFace(outward.getOpposite()), true));
        return true;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeDecorators.TRUNK_VINES.get();
    }
}
