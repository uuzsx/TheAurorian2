package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class AurorianCraftingTableBlock extends CraftingTableBlock {

    public static final MapCodec<AurorianCraftingTableBlock> CODEC = simpleCodec(AurorianCraftingTableBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final Component CONTAINER_TITLE = Component.translatable("container.crafting");

    public AurorianCraftingTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<AurorianCraftingTableBlock> codec() {
        return CODEC;
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        ContainerLevelAccess access = ContainerLevelAccess.create(level, pos);
        return new SimpleMenuProvider(
                (containerId, inventory, player) -> new AurorianCraftingMenu(containerId, inventory, access),
                CONTAINER_TITLE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    private static final class AurorianCraftingMenu extends CraftingMenu {

        private final ContainerLevelAccess access;

        private AurorianCraftingMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
            super(containerId, inventory, access);
            this.access = access;
        }

        @Override
        public boolean stillValid(Player player) {
            return stillValid(this.access, player, ModBlocks.AURORIAN_CRAFTING_TABLE.get());
        }
    }
}
