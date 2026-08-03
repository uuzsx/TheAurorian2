package cn.teampancake.theaurorian2.mixin;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModEntities;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces$MineShaftCorridor")
public abstract class MineshaftCorridorMixin {

    private static final Identifier AURORIAN_DIMENSION = TheAurorian2.id("the_aurorian");

    @Redirect(
            method = "createChest",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;"))
    private Entity theaurorian2$createAurorianChestMinecart(
            EntityType<?> originalType, Level level, EntitySpawnReason reason) {
        if (level.dimension().identifier().equals(AURORIAN_DIMENSION)) {
            return ModEntities.AURORIAN_CHEST_MINECART.get().create(level, reason);
        }
        return originalType.create(level, reason);
    }
}
