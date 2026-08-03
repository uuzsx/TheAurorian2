package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.registry.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

public final class AurorianChestMinecartEntity extends MinecartChest {

    public AurorianChestMinecartEntity(
            EntityType<? extends AurorianChestMinecartEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected Item getDropItem() {
        return ModItems.AURORIAN_CHEST_MINECART.get();
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.AURORIAN_CHEST_MINECART.get());
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
    }

    @Override
    public void setLootTable(ResourceKey<LootTable> lootTable, long seed) {
        super.setContainerLootTable(null);
        super.setContainerLootTableSeed(0L);
    }

    @Override
    public void setContainerLootTable(@Nullable ResourceKey<LootTable> lootTable) {
        super.setContainerLootTable(null);
    }
}
