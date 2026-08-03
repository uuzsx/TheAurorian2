package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.block.CrystallineSwordPedestalBlock;
import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class CrystallineSwordPedestalBlockEntity extends ModelledBlockEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation UNSEAL = RawAnimation.begin().thenPlayAndHold("misc.unseal");
    private static final RawAnimation SEAL = RawAnimation.begin().thenPlay("misc.seal").thenLoop("misc.idle");

    public CrystallineSwordPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTALLINE_SWORD_PEDESTAL.get(), pos, state, null);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<CrystallineSwordPedestalBlockEntity>("pedestal", animationState -> {
            CrystallineSwordPedestalBlock.Phase phase =
                    this.getBlockState().getValue(CrystallineSwordPedestalBlock.PHASE);
            return switch (phase) {
                case SEALED -> animationState.setAndContinue(IDLE);
                case UNSEALING -> animationState.setAndContinue(UNSEAL);
                case UNSEALED -> animationState.setAndContinue(UNSEAL);
                case SEALING -> animationState.setAndContinue(SEAL);
                case EMPTY -> PlayState.STOP;
            };
        }));
    }
}
