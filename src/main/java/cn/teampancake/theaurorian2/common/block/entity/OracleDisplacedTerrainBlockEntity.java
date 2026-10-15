package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** No ticking block entity: a scheduled fallback restores terrain after interruption or reload. */
public final class OracleDisplacedTerrainBlockEntity extends BlockEntity {
    private BlockState original = Blocks.AIR.defaultBlockState();
    public OracleDisplacedTerrainBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ORACLE_DISPLACED_TERRAIN.get(), pos, state); }
    public void initialize(BlockState state) { original = state; setChanged(); }
    public void restore(ServerLevel level) {
        if (!isRemoved() && level.getBlockState(worldPosition).is(ModBlocks.ORACLE_DISPLACED_TERRAIN.get())) {
            level.setBlock(worldPosition, original, Block.UPDATE_ALL);
        }
    }
    @Override public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) server.scheduleTick(worldPosition, getBlockState().getBlock(), 160);
    }
    @Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output); output.store("Original", BlockState.CODEC, original); }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); original = input.read("Original", BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState());
    }
}
