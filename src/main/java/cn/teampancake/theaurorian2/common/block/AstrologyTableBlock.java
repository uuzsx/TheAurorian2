package cn.teampancake.theaurorian2.common.block;

import cn.teampancake.theaurorian2.common.block.entity.AstrologyTableBlockEntity;
import cn.teampancake.theaurorian2.common.network.AstrologyForecastPayload;
import cn.teampancake.theaurorian2.common.world.AurorianBlessingCycle;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class AstrologyTableBlock extends BaseEntityBlock {

    public static final MapCodec<AstrologyTableBlock> CODEC = simpleCodec(AstrologyTableBlock::new);

    public AstrologyTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AstrologyTableBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            List<AurorianBlessingCycle.Blessing> forecast =
                    AurorianBlessingCycle.forecast(serverPlayer.level().getServer(), 3);
            PacketDistributor.sendToPlayer(serverPlayer, new AstrologyForecastPayload(
                    forecast.get(0), forecast.get(1), forecast.get(2)));
        }

        return InteractionResult.SUCCESS_SERVER;
    }
}
