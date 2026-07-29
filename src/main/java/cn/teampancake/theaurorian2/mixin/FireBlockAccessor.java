package cn.teampancake.theaurorian2.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FireBlock.class)
public interface FireBlockAccessor {

    @Invoker("setFlammable")
    void theaurorian2$setFlammable(Block block, int igniteOdds, int burnOdds);
}
