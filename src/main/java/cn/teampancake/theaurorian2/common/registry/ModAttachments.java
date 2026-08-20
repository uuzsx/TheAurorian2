package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.effect.CorruptionData;
import cn.teampancake.theaurorian2.common.item.PhantomBlossomMark;
import cn.teampancake.theaurorian2.common.inventory.AccessoryInventory;
import cn.teampancake.theaurorian2.common.world.MoonShieldData;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TheAurorian2.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CorruptionData>> CORRUPTION_DATA =
            ATTACHMENTS.register("corruption_data", () -> AttachmentType.builder(() -> CorruptionData.EMPTY)
                    .serialize(CorruptionData.CODEC.fieldOf("value"))
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Float>> CRYSTALLIZATION_LOSS =
            ATTACHMENTS.register("crystallization_loss", () -> AttachmentType.builder(() -> 0.0F)
                    .serialize(Codec.FLOAT.fieldOf("value"))
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PhantomBlossomMark>> PHANTOM_BLOSSOM_MARK =
            ATTACHMENTS.register("phantom_blossom_mark", () -> AttachmentType.builder(() -> PhantomBlossomMark.EMPTY)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>>
            PHANTOM_BLOSSOM_SENDOFF_READY_AT = ATTACHMENTS.register(
                    "phantom_blossom_sendoff_ready_at",
                    () -> AttachmentType.builder(() -> 0L).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>>
            PHANTOM_BLOSSOM_DEATH_EFFECT = ATTACHMENTS.register(
                    "phantom_blossom_death_effect",
                    () -> AttachmentType.builder(() -> false)
                            .sync(ByteBufCodecs.BOOL)
                            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AccessoryInventory>> ACCESSORY_INVENTORY =
            ATTACHMENTS.register("accessory_inventory", () -> AttachmentType.serializable(
                            holder -> new AccessoryInventory((net.minecraft.world.entity.player.Player) holder))
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MoonShieldData>> MOON_SHIELD =
            ATTACHMENTS.register("moon_shield", () -> AttachmentType.builder(() -> MoonShieldData.EMPTY)
                    .serialize(MoonShieldData.CODEC.fieldOf("value"))
                    .copyOnDeath()
                    .sync((holder, player) -> holder == player, MoonShieldData.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> MOON_SHIELD_RECOVERY_AT =
            ATTACHMENTS.register("moon_shield_recovery_at", () -> AttachmentType.builder(() -> 0L).build());

    private ModAttachments() {
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }
}
