package cn.teampancake.theaurorian2.common.network;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.inventory.AccessoryEnhancements;
import cn.teampancake.theaurorian2.common.inventory.AccessoryInventory;
import cn.teampancake.theaurorian2.common.inventory.ArtifactRotation;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record RotateArtifactPayload(int slot) implements CustomPacketPayload {

    public static final Type<RotateArtifactPayload> TYPE =
            new Type<>(TheAurorian2.id("rotate_artifact"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RotateArtifactPayload> STREAM_CODEC =
            CustomPacketPayload.codec(RotateArtifactPayload::write, RotateArtifactPayload::new);

    private RotateArtifactPayload(RegistryFriendlyByteBuf buffer) {
        this(buffer.readVarInt());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(this.slot);
    }

    public static void handle(ServerPlayer player, int slot) {
        if (slot < 0
                || slot >= AccessoryInventory.SLOT_COUNT
                || player.containerMenu != player.inventoryMenu) {
            return;
        }

        AccessoryInventory inventory = player.getData(ModAttachments.ACCESSORY_INVENTORY);
        ItemStack stack = inventory.getItem(slot);
        if (!AccessoryEnhancements.isArtifact(stack)) {
            return;
        }

        ArtifactRotation.rotateClockwise(stack);
        inventory.setItem(slot, stack);
        player.inventoryMenu.broadcastChanges();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
