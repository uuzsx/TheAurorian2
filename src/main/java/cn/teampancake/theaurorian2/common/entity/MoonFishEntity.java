package cn.teampancake.theaurorian2.common.entity;

import cn.teampancake.theaurorian2.common.registry.ModLegacyItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Separate species classes keep vanilla's class-based fish schools from mixing.
public final class MoonFishEntity extends AurorianFishEntity {
    public MoonFishEntity(EntityType<? extends MoonFishEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public ItemStack getBucketItemStack() {
        return ModLegacyItems.MOON_FISH_BUCKET.get().getDefaultInstance();
    }
}
