package cn.teampancake.theaurorian2.client;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID, value = Dist.CLIENT)
public final class ArcherWeaponsClient {
    private static final TagKey<Item> RANGER_WEAPONS = TagKey.create(
            Registries.ITEM, TheAurorian2.id("ranger_ranged_weapons"));

    private ArcherWeaponsClient() {
    }

    @SubscribeEvent
    public static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                // Vanilla's charged hold pose checks Items.CROSSBOW, so opt these four items in through NeoForge.
                return !entity.swinging && CrossbowItem.isCharged(stack)
                        ? HumanoidModel.ArmPose.CROSSBOW_HOLD : null;
            }
        }, ModItems.STARLIGHT_RANGER_CROSSBOW.get(), ModItems.DAWNLIGHT_RANGER_CROSSBOW.get(),
                ModItems.FORESTSHADE_RANGER_CROSSBOW.get(), ModItems.DUSKFLAME_RANGER_CROSSBOW.get());
    }

    @SubscribeEvent
    public static void renderHand(RenderHandEvent event) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        // Vanilla's hand selection also uses exact vanilla item IDs. Hide the other hand only for our weapons.
        if (player.isUsingItem() && player.getUseItem().is(RANGER_WEAPONS)) {
            if (event.getHand() != player.getUsedItemHand()) {
                event.setCanceled(true);
            }
        } else if (event.getHand() == InteractionHand.OFF_HAND) {
            ItemStack crossbow = player.isUsingItem() ? player.getOffhandItem() : player.getMainHandItem();
            if ((!player.isUsingItem() || player.getUsedItemHand() == InteractionHand.MAIN_HAND)
                    && crossbow.is(RANGER_WEAPONS) && crossbow.getItem() instanceof CrossbowItem
                    && CrossbowItem.isCharged(crossbow)) {
                event.setCanceled(true);
            }
        }
    }
}
