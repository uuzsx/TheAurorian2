package cn.teampancake.theaurorian2.common.block.entity;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public abstract class ModelledBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final @Nullable RawAnimation idleAnimation;

    protected ModelledBlockEntity(
            BlockEntityType<?> type, BlockPos pos, BlockState state, @Nullable String idleAnimationName) {
        super(type, pos, state);
        this.idleAnimation = idleAnimationName == null
                ? null
                : RawAnimation.begin().thenLoop(idleAnimationName);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        if (this.idleAnimation != null) {
            controllers.add(new AnimationController<ModelledBlockEntity>(
                    "idle", state -> state.setAndContinue(this.idleAnimation)));
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }
}
