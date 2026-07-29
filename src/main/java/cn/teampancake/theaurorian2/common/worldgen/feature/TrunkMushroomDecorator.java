package cn.teampancake.theaurorian2.common.worldgen.feature;

import cn.teampancake.theaurorian2.common.registry.ModTreeDecorators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public final class TrunkMushroomDecorator extends TreeDecorator {

    public static final MapCodec<TrunkMushroomDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.floatRange(0.0F, 1.0F)
                            .fieldOf("chance")
                            .forGetter(decorator -> decorator.chance),
                    Codec.intRange(1, 3)
                            .fieldOf("min_span")
                            .forGetter(decorator -> decorator.minSpan),
                    Codec.intRange(1, 3)
                            .fieldOf("max_span")
                            .forGetter(decorator -> decorator.maxSpan))
            .apply(instance, TrunkMushroomDecorator::new));

    private final float chance;
    private final int minSpan;
    private final int maxSpan;

    public TrunkMushroomDecorator(float chance, int minSpan, int maxSpan) {
        this.chance = chance;
        this.minSpan = minSpan;
        this.maxSpan = Math.max(minSpan, maxSpan);
    }

    @Override
    public void place(Context context) {
        RandomSource random = context.random();
        if (context.logs().isEmpty() || random.nextFloat() >= this.chance) {
            return;
        }

        BlockPos base = context.logs().stream()
                .min(Comparator.comparingInt(BlockPos::getY))
                .orElseThrow();
        List<BlockPos> trunk = context.logs().stream()
                .filter(pos -> pos.getX() == base.getX() && pos.getZ() == base.getZ())
                .sorted(Comparator.comparingInt(BlockPos::getY))
                .toList();
        if (trunk.isEmpty()) {
            return;
        }

        int requestedSpan = this.minSpan + random.nextInt(this.maxSpan - this.minSpan + 1);
        int span = Math.min(requestedSpan, trunk.size());
        Block mushroom = WallMushroomPlacement.randomMushroom(random);

        for (int layer = 0; layer < span; layer++) {
            BlockPos logPos = trunk.get(layer);
            int targetCount = 2 + random.nextInt(2);
            int placed = 0;
            for (Direction facing : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                BlockPos mushroomPos = logPos.relative(facing);
                if (context.isAir(mushroomPos)) {
                    context.setBlock(
                            mushroomPos,
                            WallMushroomPlacement.stateFor(mushroom, facing, random));
                    if (++placed >= targetCount) {
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeDecorators.TRUNK_MUSHROOMS.get();
    }
}
