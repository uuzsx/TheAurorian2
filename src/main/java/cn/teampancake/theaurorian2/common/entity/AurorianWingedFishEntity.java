package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.registry.ModLegacyItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class AurorianWingedFishEntity extends AurorianFishEntity {
    public AurorianWingedFishEntity(EntityType<? extends AurorianWingedFishEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public ItemStack getBucketItemStack() {
        return ModLegacyItems.AURORIAN_WINGED_FISH_BUCKET.get().getDefaultInstance();
    }
}
