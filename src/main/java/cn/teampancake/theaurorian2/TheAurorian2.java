package cn.teampancake.theaurorian2;

import com.mojang.logging.LogUtils;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModCreativeTabs;
import cn.teampancake.theaurorian2.common.registry.ModFeatures;
import cn.teampancake.theaurorian2.common.registry.ModFluidTypes;
import cn.teampancake.theaurorian2.common.registry.ModFluids;
import cn.teampancake.theaurorian2.common.registry.ModParticles;
import cn.teampancake.theaurorian2.common.registry.ModStructures;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.transfer.fluid.DispenseFluidContainer;
import org.slf4j.Logger;

@Mod(TheAurorian2.MOD_ID)
public final class TheAurorian2 {

    public static final String MOD_ID = "theaurorian2";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheAurorian2(IEventBus modEventBus, ModContainer modContainer) {
        ModFluidTypes.register(modEventBus);
        ModFluids.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModFeatures.register(modEventBus);
        ModStructures.register(modEventBus);
        ModParticles.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        LOGGER.info("Initializing The Aurorian 2");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> DispenserBlock.registerBehavior(
                ModBlocks.MOON_DEW_BUCKET.get(), DispenseFluidContainer.getInstance()));
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
