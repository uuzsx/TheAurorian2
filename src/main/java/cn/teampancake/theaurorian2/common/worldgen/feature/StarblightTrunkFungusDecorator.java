package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModTreeDecorators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public final class StarblightTrunkFungusDecorator extends TreeDecorator {

    public static final MapCodec<StarblightTrunkFungusDecorator> CODEC = Codec.floatRange(0, 1)
            .fieldOf("chance").xmap(StarblightTrunkFungusDecorator::new, decorator -> decorator.chance);
    private final float chance;

    public StarblightTrunkFungusDecorator(float chance) {
        this.chance = chance;
    }

    @Override
    public void place(Context context) {
        if (context.logs().isEmpty() || context.random().nextFloat() >= this.chance) {
            return;
        }
        placeOnTrunk(context);
        placeOnRoots(context);
    }

    private static void placeOnTrunk(Context context) {
        int bottom = Integer.MAX_VALUE;
        for (BlockPos log : context.logs()) {
            bottom = Math.min(bottom, log.getY());
        }
        int count = 0;
        for (BlockPos log : context.logs()) {
            if (log.getY() > bottom + 4 || context.random().nextBoolean()) {
                continue;
            }
            for (Direction facing : Direction.Plane.HORIZONTAL.shuffledCopy(context.random())) {
                BlockPos target = log.relative(facing);
                if (context.isAir(target)) {
                    context.setBlock(target, ModBlocks.STARBLIGHT_SHELF_FUNGUS.get()
                            .stateForAttachment(facing, context.random().nextInt(5)));
                    if (++count == 2) {
                        return;
                    }
                    break;
                }
            }
        }
    }

    private static void placeOnRoots(Context context) {
        int count = 0;
        for (int attempt = 0; attempt < Math.min(12, context.roots().size()) && count < 1; attempt++) {
            BlockPos root = context.roots().get(context.random().nextInt(context.roots().size()));
            Direction face = attempt % 3 == 0 ? Direction.UP
                    : Direction.Plane.HORIZONTAL.getRandomDirection(context.random());
            BlockPos target = root.relative(face);
            var state = ModBlocks.STARBLIGHT_SHELF_FUNGUS.get()
                    .stateForAttachment(face, context.random().nextInt(5));
            if (context.isAir(target) && state.canSurvive(context.level(), target)) {
                context.setBlock(target, state);
                count++;
            }
        }
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeDecorators.STARBLIGHT_TRUNK_FUNGUS.get();
    }
}
