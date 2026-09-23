package cn.teampancake.theaurorian2.common.enchantment;

import cn.teampancake.theaurorian2.TheAurorian2;
import cn.teampancake.theaurorian2.common.registry.ModAttachments;
import cn.teampancake.theaurorian2.common.registry.ModEnchantments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

@EventBusSubscriber(modid = TheAurorian2.MOD_ID)
public final class ToolEnchantmentEvents {
    private ToolEnchantmentEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void bind(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isShiftKeyDown()
                || !EnchantmentAccess.has(event.getItemStack(), event.getLevel().registryAccess(), ModEnchantments.SOURCE_OF_TERRA)
                || !(event.getLevel().getBlockEntity(event.getPos()) instanceof ChestBlockEntity chest)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!chest.canOpen(player)) return;
        TerraBinding old = TerraBinding.read(event.getItemStack());
        String dimension = player.level().dimension().identifier().toString();
        if (old != null && old.dimension().equals(dimension) && old.pos().equals(event.getPos())) {
            TerraBinding.clear(event.getItemStack());
            message(player, "unbound");
        } else {
            String token = chest.getData(ModAttachments.TERRA_CHEST_ID);
            chest.setChanged();
            new TerraBinding(dimension, event.getPos().immutable(), token).write(event.getItemStack());
            message(player, "bound");
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void blockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player)) return;
        ItemStack tool = event.getTool();
        int experience = Math.min(4, EnchantmentAccess.itemLevel(tool, event.getLevel().registryAccess(), ModEnchantments.EXPERIENCE_ORE));
        if (experience > 0 && event.getState().is(Tags.Blocks.ORES)
                && !EnchantmentAccess.has(tool, event.getLevel().registryAccess(), Enchantments.SILK_TOUCH)
                && event.getLevel().getRandom().nextFloat() < experience * 0.08F)
            event.setDroppedExperience(event.getDroppedExperience() + 1 + event.getLevel().getRandom().nextInt(3));
        if (!EnchantmentAccess.has(tool, event.getLevel().registryAccess(), ModEnchantments.SOURCE_OF_TERRA)) return;
        TerraBinding binding = TerraBinding.read(tool);
        if (binding == null || !binding.dimension().equals(event.getLevel().dimension().identifier().toString())) return;
        // Breaking the destination itself must never insert its own drops back into the departing block entity.
        if (binding.pos().equals(event.getPos())) {
            invalidate(player, tool);
            return;
        }
        ChestBlockEntity chest = destination(player, tool, binding);
        if (chest == null || !chest.canOpen(player)) return;
        var chestState = chest.getBlockState();
        if (chestState.hasProperty(ChestBlock.TYPE) && chestState.getValue(ChestBlock.TYPE) != ChestType.SINGLE
                && !event.getLevel().hasChunkAt(binding.pos().relative(ChestBlock.getConnectedDirection(chestState)))) return;
        var handler = event.getLevel().getCapability(Capabilities.Item.BLOCK, binding.pos(), null);
        if (handler == null && chest.getType().builtInRegistryHolder().key().identifier().getNamespace().equals(TheAurorian2.MOD_ID))
            handler = net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(chest);
        if (handler == null) return;
        for (var iterator = event.getDrops().iterator(); iterator.hasNext();) {
            var drop = iterator.next();
            ItemStack stack = drop.getItem();
            if (stack.isEmpty()) continue;
            int inserted;
            try (Transaction transaction = Transaction.openRoot()) {
                inserted = handler.insert(ItemResource.of(stack), stack.getCount(), transaction);
                transaction.commit();
            }
            stack.shrink(inserted);
            if (stack.isEmpty()) iterator.remove();
        }
    }

    @SubscribeEvent
    public static void validateHeldBinding(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 40 != 0) return;
        checkHeld(player, player.getMainHandItem());
        checkHeld(player, player.getOffhandItem());
    }

    private static void checkHeld(ServerPlayer player, ItemStack stack) {
        if (!EnchantmentAccess.has(stack, player.registryAccess(), ModEnchantments.SOURCE_OF_TERRA)) return;
        TerraBinding binding = TerraBinding.read(stack);
        if (binding != null) destination(player, stack, binding);
    }

    public static ChestBlockEntity destination(ServerPlayer player, ItemStack tool, TerraBinding binding) {
        ServerLevel level = player.level();
        if (!binding.dimension().equals(level.dimension().identifier().toString()) || !level.hasChunkAt(binding.pos())) return null;
        var entity = level.getBlockEntity(binding.pos());
        if (!(entity instanceof ChestBlockEntity chest) || !chest.hasData(ModAttachments.TERRA_CHEST_ID)
                || !chest.getData(ModAttachments.TERRA_CHEST_ID).equals(binding.chestId())) {
            invalidate(player, tool);
            return null;
        }
        return chest;
    }

    private static void invalidate(ServerPlayer player, ItemStack tool) {
        TerraBinding.clear(tool);
        message(player, "destroyed");
    }

    private static void message(ServerPlayer player, String key) {
        player.sendSystemMessage(Component.translatable("message.theaurorian2.terra." + key).withColor(0x9EB8C8), true);
    }
}
