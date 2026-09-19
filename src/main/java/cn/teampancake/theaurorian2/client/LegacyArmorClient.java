package cn.teampancake.theaurorian2.client;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.client.model.armor.*;
import cn.teampancake.theaurorian2.common.item.HolyKnightArmorItem;
import cn.teampancake.theaurorian2.common.item.LegacyArmorItem;
import cn.teampancake.theaurorian2.common.registry.ModLegacyItems;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class LegacyArmorClient {
    private LegacyArmorClient() {
    }

    private static ModelLayerLocation layer(LegacyArmorItem item, boolean baby) {
        return new ModelLayerLocation(TheAurorian2.id("legacy_armor/" + item.modelName() + "/"
                + item.armorType().getSlot().getName()), baby ? "baby" : "main");
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ModLegacyItems.forEachEquipment(item -> {
            if (item instanceof LegacyArmorItem armor && !(item instanceof HolyKnightArmorItem)) {
                event.registerLayerDefinition(layer(armor, false), () -> createLayer(armor, false));
                event.registerLayerDefinition(layer(armor, true), () -> createLayer(armor, true));
            }
        });
    }

    private static LayerDefinition createLayer(LegacyArmorItem item, boolean baby) {
        LayerDefinition definition = switch (item.modelName()) {
            case "cerulean" -> CeruleanArmorModel.createBodyLayer(item.armorType());
            case "crystal_rune" -> CrystalRuneArmorModel.createBodyLayer(item.armorType());
            case "knight" -> KnightArmorModel.createBodyLayer(item.armorType());
            case "moonsilver" -> MoonsilverArmorModel.createBodyLayer(item.armorType());
            case "mysterium_wool" -> MysteriumWoolArmorModel.createBodyLayer(item.armorType());
            case "spectral" -> SpectralArmorModel.createBodyLayer(item.armorType());
            case "spiked_chestplate" -> SpikedChestplateModel.createBodyLayer();
            case "aurorian_slime_boots" -> AurorianSlimeBootsModel.createBodyLayer();
            default -> throw new IllegalArgumentException("Unknown legacy armor: " + item.modelName());
        };
        Set<String> visible = switch (item.armorType()) {
            case HELMET -> Set.of("head");
            case CHESTPLATE -> Set.of("body", "right_arm", "left_arm");
            case LEGGINGS -> Set.of("body", "right_leg", "left_leg");
            case BOOTS -> Set.of("right_leg", "left_leg");
            default -> throw new IllegalArgumentException("Unsupported armor slot: " + item.armorType());
        };
        definition = definition.apply(mesh -> {
            mesh.getRoot().retainPartsAndChildren(visible);
            return mesh;
        });
        return baby ? definition.apply(HumanoidModel.BABY_TRANSFORMER) : definition;
    }

    @SubscribeEvent
    public static void registerExtensions(RegisterClientExtensionsEvent event) {
        ModLegacyItems.forEachEquipment(item -> {
            if (item instanceof LegacyArmorItem armor && !(item instanceof HolyKnightArmorItem)) {
                event.registerItem(new ArmorExtension(armor), item);
            }
        });
    }

    private static final class ArmorExtension implements IClientItemExtensions {
        private final Identifier texture;
        private final ModelLayerLocation adultLayer;
        private final ModelLayerLocation babyLayer;
        private EntityModelSet modelSet;
        private HumanoidModel<HumanoidRenderState> adultModel;
        private HumanoidModel<HumanoidRenderState> babyModel;

        private ArmorExtension(LegacyArmorItem item) {
            String name = switch (item.modelName()) {
                case "mysterium_wool" -> "mysterium_armor";
                case "spiked_chestplate", "aurorian_slime_boots" -> item.modelName();
                default -> item.modelName() + "_armor";
            };
            this.texture = TheAurorian2.id("textures/models/armor/" + name + ".png");
            this.adultLayer = layer(item, false);
            this.babyLayer = layer(item, true);
        }

        @Override
        public Model getHumanoidArmorModel(ItemStack stack, EquipmentClientInfo.LayerType type, Model original) {
            EntityModelSet current = Minecraft.getInstance().getEntityModels();
            if (this.modelSet != current) {
                this.modelSet = current;
                this.adultModel = new HumanoidModel<>(current.bakeLayer(this.adultLayer));
                this.babyModel = new HumanoidModel<>(current.bakeLayer(this.babyLayer));
            }
            return type == EquipmentClientInfo.LayerType.HUMANOID_BABY ? this.babyModel : this.adultModel;
        }

        @Override
        public Identifier getArmorTexture(ItemStack stack, EquipmentClientInfo.LayerType type,
                EquipmentClientInfo.Layer layer, Identifier original) {
            // Reuse each original atlas across armor slots without duplicating its PNG.
            return this.texture;
        }
    }
}
