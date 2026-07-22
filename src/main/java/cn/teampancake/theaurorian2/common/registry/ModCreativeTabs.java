package cn.teampancake.theaurorian2.common.registry;

import cn.teampancake.theaurorian2.TheAurorian2;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheAurorian2.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THE_AURORIAN_2 = TABS.register(
            "the_aurorian_2",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.theaurorian2.the_aurorian_2"))
                    .icon(() -> new ItemStack(ModBlocks.AURORIAN_GRASS_BLOCK.get()))
                    .displayItems((parameters, output) ->
                            ModBlocks.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
