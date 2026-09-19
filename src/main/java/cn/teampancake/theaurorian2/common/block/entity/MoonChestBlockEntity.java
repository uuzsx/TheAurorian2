package cn.teampancake.theaurorian2.common.block.entity;

import cn.teampancake.theaurorian2.common.block.MoonChestBlock;
import cn.teampancake.theaurorian2.common.registry.ModBlockEntities;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** All three original chests share the same server-authoritative unlock lifecycle. */
public final class MoonChestBlockEntity extends ChestBlockEntity implements GeoBlockEntity {
    private static final RawAnimation UNLOCK = RawAnimation.begin().thenPlayAndHold("unlock");
    private static final RawAnimation CLOSED = RawAnimation.begin().thenLoop("closed");
    private static final RawAnimation UNLOCKED = RawAnimation.begin().thenLoop("unlocked");
    private static final RawAnimation LID_OPEN = RawAnimation.begin().thenLoop("lid_open");
    private static final RawAnimation LID_CLOSED = RawAnimation.begin().thenLoop("lid_closed");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private long unlockEnd;

    public MoonChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOON_CHEST.get(), pos, state);
    }

    public MoonChestBlock.Variant variant() { return ((MoonChestBlock)getBlockState().getBlock()).variant(); }

    @Override
    protected Component getDefaultName() { return getBlockState().getBlock().getName(); }

    @Override
    public boolean canOpen(Player player) {
        return getBlockState().getValue(MoonChestBlock.UNLOCKED)
                && !getBlockState().getValue(MoonChestBlock.UNLOCKING) && super.canOpen(player);
    }

    public boolean beginUnlock() {
        if (!(level instanceof ServerLevel server) || getBlockState().getValue(MoonChestBlock.UNLOCKED)
                || getBlockState().getValue(MoonChestBlock.UNLOCKING)) return false;
        unlockEnd = server.getGameTime() + variant().unlockTicks;
        setChanged();
        server.setBlock(worldPosition, getBlockState().setValue(MoonChestBlock.UNLOCKING, true), Block.UPDATE_ALL);
        server.scheduleTick(worldPosition, getBlockState().getBlock(), variant().unlockTicks);
        server.playSound(null, worldPosition, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, .8F, 1.2F);
        return true;
    }

    public void finishUnlock() {
        if (!(level instanceof ServerLevel server) || !getBlockState().getValue(MoonChestBlock.UNLOCKING)) return;
        long remaining = unlockEnd - server.getGameTime();
        if (remaining > 0) {
            server.scheduleTick(worldPosition, getBlockState().getBlock(), (int)Math.min(remaining, variant().unlockTicks));
            return;
        }
        server.setBlock(worldPosition, getBlockState().setValue(MoonChestBlock.UNLOCKING, false)
                .setValue(MoonChestBlock.UNLOCKED, true).setValue(MoonChestBlock.OPEN, false), Block.UPDATE_ALL);
        unlockEnd = 0;
        setChanged();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // Menus do not survive unloading/restarting; the paid unlock does.
        if (level instanceof ServerLevel server && getBlockState().getValue(MoonChestBlock.OPEN))
            server.setBlock(worldPosition, getBlockState().setValue(MoonChestBlock.OPEN, false), Block.UPDATE_CLIENTS);
        if (level instanceof ServerLevel server && getBlockState().getValue(MoonChestBlock.UNLOCKING)) {
            // Keep the paid key and remaining unlock time across chunk unloads/restarts.
            if (unlockEnd <= 0) { unlockEnd = server.getGameTime() + variant().unlockTicks; setChanged(); }
            server.scheduleTick(worldPosition, getBlockState().getBlock(),
                    (int)Math.max(1, Math.min(variant().unlockTicks, unlockEnd-server.getGameTime())));
        }
    }

    @Override
    protected void signalOpenCount(Level level, BlockPos pos, BlockState state, int previous, int current) {
        super.signalOpenCount(level, pos, state, previous, current);
        BlockState actual = level.getBlockState(pos);
        // Vanilla maintains the viewer count, including disconnects and multiple players.
        if (!level.isClientSide() && actual.getBlock() instanceof MoonChestBlock && (previous > 0) != (current > 0))
            level.setBlock(pos, actual.setValue(MoonChestBlock.OPEN, current > 0), Block.UPDATE_CLIENTS);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        unlockEnd = input.getLongOr("unlock_end", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (unlockEnd > 0) output.putLong("unlock_end", unlockEnd);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("unlock_end", unlockEnd);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<MoonChestBlockEntity>("unlock", 0, state -> {
            BlockState blockState = getBlockState();
            if (blockState.getValue(MoonChestBlock.UNLOCKING)) {
                state.setAnimation(UNLOCK);
                // Align late chunk watchers with the server instead of restarting the animation.
                double elapsed = level == null || unlockEnd <= 0 ? 0 :
                        (level.getGameTime() + state.renderState().getPartialTick() - unlockEnd + variant().unlockTicks) / 20.0;
                state.controller().setAnimationTime(Math.max(0, Math.min(variant().unlockTicks/20.0, elapsed)));
                return com.geckolib.animation.object.PlayState.CONTINUE;
            }
            return state.setAndContinue(blockState.getValue(MoonChestBlock.UNLOCKED) ? UNLOCKED : CLOSED);
        }));
        controllers.add(new AnimationController<MoonChestBlockEntity>("lid", 10, state -> {
            BlockState blockState = getBlockState();
            return state.setAndContinue(blockState.getValue(MoonChestBlock.UNLOCKED)
                    && !blockState.getValue(MoonChestBlock.UNLOCKING) && blockState.getValue(MoonChestBlock.OPEN)
                    ? LID_OPEN : LID_CLOSED);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return animationCache; }
}
