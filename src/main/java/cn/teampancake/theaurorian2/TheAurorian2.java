package cn.teampancake.theaurorian2;

import com.mojang.logging.LogUtils;
import cn.teampancake.theaurorian2.common.registry.ModBlocks;
import cn.teampancake.theaurorian2.common.registry.ModCreativeTabs;
import cn.teampancake.theaurorian2.common.registry.ModFeatures;
import cn.teampancake.theaurorian2.common.registry.ModParticles;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(TheAurorian2.MOD_ID)
public final class TheAurorian2 {

    public static final String MOD_ID = "theaurorian2";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheAurorian2(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModFeatures.register(modEventBus);
        ModParticles.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        LOGGER.info("Initializing The Aurorian 2");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
