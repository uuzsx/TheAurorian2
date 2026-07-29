package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.effect.CorruptionData;
import com.mojang.serialization.Codec;
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

    private ModAttachments() {
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }
}
