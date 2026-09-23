package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.enchantment.ReturningAxeData;
import cn.teampancake.theaurorian2.common.enchantment.EnchantmentState;
import cn.teampancake.theaurorian2.common.effect.CorruptionData;
import cn.teampancake.theaurorian2.common.item.PhantomBlossomMark;
import cn.teampancake.theaurorian2.common.inventory.AccessoryInventory;
import cn.teampancake.theaurorian2.common.world.AurorianTravelData;
import cn.teampancake.theaurorian2.common.world.MoonShieldData;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TheAurorian2.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ReturningAxeData>> RETURNING_AXE =
            ATTACHMENTS.register("returning_axe", () -> AttachmentType.builder(ReturningAxeData::new)
                    .serialize(new IAttachmentSerializer<ReturningAxeData>() {
                        @Override
                        public ReturningAxeData read(IAttachmentHolder holder, ValueInput input) {
                            return ReturningAxeData.read(input);
                        }
                        @Override
                        public boolean write(ReturningAxeData data, ValueOutput output) {
                            return data.write(output);
                        }
                    }).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> TERRA_CHEST_ID =
            ATTACHMENTS.register("terra_chest_id", () -> AttachmentType.builder(() -> java.util.UUID.randomUUID().toString())
                    .serialize(Codec.STRING.fieldOf("value")).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EnchantmentState>> ENCHANTMENT_STATE =
            ATTACHMENTS.register("enchantment_state", () -> AttachmentType.builder(EnchantmentState::new)
                    .serialize(new IAttachmentSerializer<EnchantmentState>() {
                        @Override
                        public EnchantmentState read(IAttachmentHolder holder, ValueInput input) {
                            return EnchantmentState.read(input);
                        }
                        @Override
                        public boolean write(EnchantmentState state, ValueOutput output) {
                            state.write(output);
                            return true;
                        }
                    }).build());

    // A transient visual entity id; no serialized state or copy-on-death.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> WORLD_SCROLL_EFFECT =
            ATTACHMENTS.register("world_scroll_effect", () -> AttachmentType.builder(() -> -1)
                    .sync(ByteBufCodecs.VAR_INT).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Byte>> AZURE_WARBLER_SHOULDERS =
            ATTACHMENTS.register("azure_warbler_shoulders", () -> AttachmentType.builder(() -> (byte) 0)
                    .sync(ByteBufCodecs.BYTE).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> SILK_GROUNDED_UNTIL =
            ATTACHMENTS.register("silk_grounded_until", () -> AttachmentType.builder(() -> 0L)
                    .serialize(Codec.LONG.fieldOf("value"))
                    .sync(ByteBufCodecs.VAR_LONG)
                    .build());

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
            ATTACHMENTS.register("accessory_inventory", () -> AttachmentType.builder(
                            holder -> new AccessoryInventory((net.minecraft.world.entity.player.Player) holder))
                    .serialize(new IAttachmentSerializer<AccessoryInventory>() {
                        @Override
                        public AccessoryInventory read(IAttachmentHolder holder, ValueInput input) {
                            // InventoryMenu has already bound its slots during player construction.
                            // Restore that instance instead of replacing it behind the menu's back.
                            AccessoryInventory inventory = holder.getData(ACCESSORY_INVENTORY);
                            inventory.deserialize(input);
                            return inventory;
                        }

                        @Override
                        public boolean write(AccessoryInventory inventory, ValueOutput output) {
                            inventory.serialize(output);
                            return true;
                        }
                    })
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AurorianTravelData>> AURORIAN_TRAVEL =
            ATTACHMENTS.register("aurorian_travel", () -> AttachmentType.builder(() -> AurorianTravelData.EMPTY)
                    .serialize(AurorianTravelData.CODEC.fieldOf("value"))
                    .copyOnDeath()
                    .sync((holder, player) -> holder == player, ByteBufCodecs.fromCodecWithRegistries(AurorianTravelData.CODEC))
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MoonShieldData>> MOON_SHIELD =
            ATTACHMENTS.register("moon_shield", () -> AttachmentType.builder(() -> MoonShieldData.EMPTY)
                    .serialize(MoonShieldData.CODEC.fieldOf("value"))
                    .copyOnDeath()
                    .sync((holder, player) -> holder == player, MoonShieldData.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> MOON_SHIELD_RECOVERY_AT =
            ATTACHMENTS.register("moon_shield_recovery_at", () -> AttachmentType.builder(() -> 0L)
                    .sync((holder, player) -> holder == player, ByteBufCodecs.VAR_LONG)
                    .build());

    private ModAttachments() {
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }
}
