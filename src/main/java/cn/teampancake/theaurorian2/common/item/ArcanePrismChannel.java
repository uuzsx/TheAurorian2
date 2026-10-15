package cn.teampancake.theaurorian2.common.item;

import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Transient, owner-synchronized channel anchor; never saved or copied on death. */
public record ArcanePrismChannel(int spellId, Vec3 anchor) {
    public static final ArcanePrismChannel IDLE = new ArcanePrismChannel(-1, Vec3.ZERO);
    public static final StreamCodec<ByteBuf, ArcanePrismChannel> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ArcanePrismChannel::spellId,
            Vec3.STREAM_CODEC, ArcanePrismChannel::anchor, ArcanePrismChannel::new);

    public static boolean isHolding(Entity entity) {
        return entity instanceof Player player && player.isAlive() && !player.isSpectator() && !player.isPassenger()
                && player.getMainHandItem().getItem() instanceof ArcanePrismItem
                && player.hasData(ModAttachments.ARCANE_PRISM_CHANNEL)
                && player.getData(ModAttachments.ARCANE_PRISM_CHANNEL).spellId >= 0;
    }
}
