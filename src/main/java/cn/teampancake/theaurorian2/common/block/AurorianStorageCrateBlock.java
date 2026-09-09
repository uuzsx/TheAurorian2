package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.block.entity.DoubleStorageCrateBlockEntity;
import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** Three authored crate arrangements with the vanilla barrel inventory. */
public final class AurorianStorageCrateBlock extends BarrelBlock {
    private static final Component TITLE = Component.translatable("container.theaurorian2.storage_crate");
    private static final MapCodec<BarrelBlock> STORAGE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(1, 3).fieldOf("style").forGetter(block -> ((AurorianStorageCrateBlock) block).style),
            propertiesCodec()).apply(instance, AurorianStorageCrateBlock::new));
    private static final VoxelShape BASE = box(-1, 0, -1, 17, 7.75, 17);
    private static final List<Map<Direction, VoxelShape>> SHAPES = List.of(
            facing(Shapes.or(BASE, angledBox(-1, 7.75, -2, 17, 8.75, 16, 11, 8.5, 8, 22.5, true))),
            facing(Shapes.or(BASE, angledBox(9, 7.75, -1, 27, 8.75, 17, 16, 8.25, 7, -45, false))),
            facing(Shapes.or(BASE, angledBox(0, 7.75, -2, 18, 8.75, 16, 11, 8.5, 8, 22.5, true),
                    angledBox(0, 8.25, -1, 16, 15, 17, 8, 12.75, 8, -22.5, true),
                    angledBox(-0.85195, 15, -1.77164, 17.14805, 16, 16.22836, 8, 12.75, 8, -22.5, true))));
    private final int style;

    public AurorianStorageCrateBlock(int style, Properties properties) {
        super(properties);
        if (style < 1 || style > 3) throw new IllegalArgumentException("Storage crate style must be 1..3");
        this.style = style;
    }

    @Override
    public MapCodec<BarrelBlock> codec() { return STORAGE_CODEC; }

    public boolean isDoubleCrate() { return style == 3; }

    @Override
    public BarrelBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isDoubleCrate() ? new DoubleStorageCrateBlockEntity(pos, state) : new BarrelBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof BarrelBlockEntity barrel)) return null;
        // Delegate lock/loot checks and row count to the underlying container.
        return new SimpleMenuProvider(barrel::createMenu, barrel.hasCustomName() ? barrel.getDisplayName() : TITLE);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel serverLevel) {
            MenuProvider provider = getMenuProvider(state, level, pos);
            if (provider != null) {
                player.openMenu(provider);
                player.awardStat(Stats.OPEN_BARREL);
                PiglinAi.angerNearbyPiglins(serverLevel, player, true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var shapes = SHAPES.get(style - 1);
        return shapes.getOrDefault(state.getValue(FACING), shapes.get(Direction.NORTH));
    }

    private static Map<Direction, VoxelShape> facing(VoxelShape shape) {
        // The original front is south. Match the blockstate model's 180-degree base rotation.
        return Shapes.rotateHorizontal(shape, OctahedralGroup.BLOCK_ROT_Y_180);
    }

    private static VoxelShape angledBox(double x, double y, double z, double X, double Y, double Z,
                                          double ox, double oy, double oz, double degrees, boolean aroundY) {
        // Voxel collision cannot express slanted planes. Subdivide their rotating axes into
        // one-model-pixel cells once at class loading; model vertices and UVs stay exact.
        double c = Math.cos(Math.toRadians(degrees)), s = Math.sin(Math.toRadians(degrees));
        List<VoxelShape> cells = new ArrayList<>();
        double low = aroundY ? z : y, high = aroundY ? Z : Y;
        for (double a = x; a < X; a += 1) for (double b = low; b < high; b += 1) {
            double minA = Double.POSITIVE_INFINITY, minB = minA;
            double maxA = Double.NEGATIVE_INFINITY, maxB = maxA;
            for (double u : new double[]{a, Math.min(a + 1, X)}) for (double v : new double[]{b, Math.min(b + 1, high)}) {
                double du = u - ox, dv = v - (aroundY ? oz : oy);
                double ru = ox + c * du + (aroundY ? s : -s) * dv;
                double rv = (aroundY ? oz : oy) + (aroundY ? -s : s) * du + c * dv;
                minA = Math.min(minA, ru); maxA = Math.max(maxA, ru);
                minB = Math.min(minB, rv); maxB = Math.max(maxB, rv);
            }
            // A bounded half-pixel grid prevents arbitrary rotated coordinates from creating
            // an unnecessarily large discrete shape while merging the cells.
            minA = Math.floor(minA * 2) / 2; minB = Math.floor(minB * 2) / 2;
            maxA = Math.ceil(maxA * 2) / 2; maxB = Math.ceil(maxB * 2) / 2;
            cells.add(aroundY ? box(minA, y, minB, maxA, Y, maxB) : box(minA, minB, z, maxA, maxB, Z));
        }
        return Shapes.or(Shapes.empty(), cells.toArray(VoxelShape[]::new));
    }
}
