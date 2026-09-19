package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.feline.CatSoundVariants;
import net.minecraft.world.level.block.state.BlockState;

/** Event-driven interaction; no block entity ticker or persistent animation state. */
public final class StupidCatBlockEntity extends ModelledBlockEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle_blink");
    private static final RawAnimation HELLO = RawAnimation.begin().thenPlay("wave_hello");
    private long nextHelloTick;

    public StupidCatBlockEntity(BlockPos pos, BlockState state) { super(state.is(cn.teampancake.theaurorian2.common.registry.ModBlocks.NEKO.get())
            ? ModBlockEntities.NEKO.get() : state.is(cn.teampancake.theaurorian2.common.registry.ModBlocks.YOUYOUZI.get())
            ? ModBlockEntities.YOUYOUZI.get() : state.is(cn.teampancake.theaurorian2.common.registry.ModBlocks.BEIDOU_YUHUI.get())
            ? ModBlockEntities.BEIDOU_YUHUI.get() : ModBlockEntities.STUPID_CAT.get(), pos, state, null); }

    public void sayHello() {
        if (!(level instanceof ServerLevel server) || server.getGameTime() < nextHelloTick) return;
        nextHelloTick = server.getGameTime() + 64; // Authored animation: 3.2 seconds.
        triggerAnim("main", "hello");
        server.playSound(null, worldPosition,
                SoundEvents.CAT_SOUNDS.get(CatSoundVariants.SoundSet.CLASSIC).adultSounds().ambientSound().value(),
                SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // One controller avoids an idle blink overriding the hello expression.
        controllers.add(new AnimationController<StupidCatBlockEntity>("main", 0,
                state -> state.setAndContinue(IDLE)).triggerableAnim("hello", HELLO));
    }
}
