package cn.teampancake.theaurorian2.common.network;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.block.entity.PurificationAltarBlockEntity;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record PurificationRitualConfirmPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<PurificationRitualConfirmPayload> TYPE =
            new Type<>(TheAurorian2.id("purification_ritual_confirm"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PurificationRitualConfirmPayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, PurificationRitualConfirmPayload::pos,
                    PurificationRitualConfirmPayload::new);

    public static void handle(ServerPlayer player, BlockPos pos) {
        if (!player.level().hasChunkAt(pos)
                || !player.level().getBlockState(pos).is(ModBlocks.PURIFICATION_ALTAR.get())
                || player.distanceToSqr(pos.getCenter()) > 20.25D) {
            return;
        }
        if (player.level().getBlockEntity(pos) instanceof PurificationAltarBlockEntity altar) {
            altar.confirmRitual(player);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
